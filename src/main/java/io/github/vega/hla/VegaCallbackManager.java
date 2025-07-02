/*-
 * SPDX-License-Identifier: BSD-3-Clause
 * Copyright (c) 2025 Hridyanshu Aatreya <2200096@brunel.ac.uk>
 * All rights reserved.

 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
 * 1. Redistributions of source code must retain the above copyright notice, 
 *	  this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the 
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its 
 * 	  contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * 
 */

package io.github.vega.hla;

import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.encoding.EncoderFactory;
import io.github.vega.core.IDataConverter;
import io.github.vega.core.IEntityArchetype;
import io.github.vega.core.IMultiDataConverter;
import io.github.vega.core.World;
import io.github.vega.utils.ExecutionLatch;
import io.github.vega.utils.ProjectRegistry;

public class VegaCallbackManager
{
	private static final Logger LOGGER = LogManager.getLogger();
	
	private static final String EXCO_CLASS_NAME = "HLAobjectRoot.ExecutionConfiguration";
	
	private static final Set<String> OBJECTS_PENDING_DISCOVERY = new HashSet<String>(ProjectRegistry.requiredObjects);
	private static ComponentMapper<HLAObjectComponent> objectComponentMapper = ComponentMapper.getFor(HLAObjectComponent.class);
	
	private static final Object NAME_RESERVATION_SEMAPHORE = new Object();
	private static boolean nameReservationStatus;

	public static void discoverObjectInstance(final ObjectInstanceHandle theObject, ObjectClassHandle theObjectClass, String objectName)
	{
		RTIambassador rtiAmbassador = VegaRTIAmbassador.instance();
		HLAObjectComponent objectComponent = World.createComponent(HLAObjectComponent.class);
		
		String className = null;
		String instanceName = null;
		
		try
		{
			className = rtiAmbassador.getObjectClassName(theObjectClass);
			instanceName = rtiAmbassador.getObjectInstanceName(theObject);
		}
		catch (Exception e)
		{
			LOGGER.error("Failed to save a new object instance\n[REASON]", e);
			System.exit(1);
		}
		
		VegaObjectClass objectClass = ProjectRegistry.getObjectClass(className);
		
		if (objectClass == null)
		{	
			LOGGER.warn("Failed to create an internalized entity representation of the object instance \"{}\" of class <{}>.\n[REASON] The corresponding object class for the instance was not created at runtime. The definition for this object class may be missing in the project file", instanceName, className);
			return;
		}
		
		IEntityArchetype archetype = ProjectRegistry.getArchetype(objectClass.archetypeName);
		Entity entity = archetype.createEntity();
		
		if (entity == null)
		{
			LOGGER.warn("Failed to create an internalized entity representation of the object instance \"{}\" of class <{}>.\n[REASON] The archetype <{}> returned NULL instead of an entity. Re-check to ensure it returns an entity with the necessary components", instanceName, className, archetype);
			return;
		}

		objectComponent.className = className;
		objectComponent.instanceName = instanceName;
		
		World.addComponent(entity, objectComponent);
		ProjectRegistry.addEntityObjectInstance(entity, theObject);
		
		if (className.equals(EXCO_CLASS_NAME))
			ExecutionLatch.disable();
		
		if (OBJECTS_PENDING_DISCOVERY.contains(instanceName))
		{
			LOGGER.info("Discovered the object instance \"{}\" of class <{}>", instanceName, className);
			OBJECTS_PENDING_DISCOVERY.remove(instanceName);
			
			if (OBJECTS_PENDING_DISCOVERY.isEmpty())
				ExecutionLatch.disable();
		}
		
		requestLatestAttributeValues(theObject, instanceName, objectClass);
	}

	private static void requestLatestAttributeValues(ObjectInstanceHandle instanceHandle, String instanceName, VegaObjectClass objectClass)
	{
		RTIambassador rtiAmbassador = VegaRTIAmbassador.instance();
		AttributeHandleSet attributeHandles = objectClass.subscribeableAttributeHandles();

		try
		{
			rtiAmbassador.requestAttributeValueUpdate(instanceHandle, attributeHandles, null);
		}
		catch (Exception e)
		{
			LOGGER.error("Failed to get the latest values for the object instance \"{}\"\n[REASON]", instanceName);
			System.exit(1);
		}
	}

	public static void reflectAttributeValues(ObjectInstanceHandle theObject, AttributeHandleValueMap theAttributes)
	{
		Entity entity = ProjectRegistry.getRemoteEntity(theObject);
		HLAObjectComponent objectComponent = objectComponentMapper.get(entity);
		String className = objectComponent.className;
		String instanceName = objectComponent.instanceName;
		
		VegaObjectClass objectClass = ProjectRegistry.getObjectClass(className);
		AttributeHandleSet attributeHandles = objectClass.subscribeableAttributeHandles();
		
		if (!allAttributesPresent(attributeHandles, theAttributes))
			LOGGER.warn("Discarded latest values received for the object instance \"{}\" because one or more required attributes are missing", instanceName);
		else
			updateObjectInstance(entity, objectClass, theAttributes);
		
		if (className.equals(EXCO_CLASS_NAME))
			ExecutionLatch.disable();
	}
	
	private static boolean allAttributesPresent(AttributeHandleSet attributeHandles, AttributeHandleValueMap providedAttributeMap)
	{
		for (AttributeHandle attributeHandle : attributeHandles)
		{
			if (!providedAttributeMap.containsKey(attributeHandle))
				return false;
		}
		return true;
	}
	
	private static void updateObjectInstance(Entity entity, VegaObjectClass objectClass, AttributeHandleValueMap latestValues)
	{
		EncoderFactory encoderFactory = VegaEncoderFactory.instance();
		
		for (String attributeName : objectClass.attributeNames)
		{
			AttributeHandle attributeHandle = objectClass.getAttributeHandle(attributeName);
			byte[] value = latestValues.get(attributeHandle);
			
			if (objectClass.attributeUsesMultiConverter(attributeName))
			{
				String converterName = objectClass.getAttributeMultiConverterName(attributeName);
				IMultiDataConverter converter = ProjectRegistry.getMultiConverter(converterName);
				int trigger = objectClass.getAttributeConverterTrigger(attributeName, converterName);
				converter.decode(entity, encoderFactory, value, trigger);
			}
			else
			{
				String converterName = objectClass.getAttributeConverterName(attributeName);
				IDataConverter converter = ProjectRegistry.getDataConverter(converterName);
				converter.decode(entity, encoderFactory, value);
			}
		}
	}
	
	public static void objectInstanceNameReservationSucceeded(String objectName)
	{
		nameReservationStatus = true;
		synchronized(NAME_RESERVATION_SEMAPHORE)
		{
			NAME_RESERVATION_SEMAPHORE.notify();
		}
	}
	
	public static void objectInstanceNameReservationFailed(String objectName)
	{
		nameReservationStatus = false;
		synchronized(NAME_RESERVATION_SEMAPHORE)
		{
			NAME_RESERVATION_SEMAPHORE.notify();
		}
	}
	
	public static Object getNameReservationSemaphore()
	{
		return NAME_RESERVATION_SEMAPHORE;
	}
	
	public static boolean getNameReservationStatus()
	{
		return nameReservationStatus;
	}
}
