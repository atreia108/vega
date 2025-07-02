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

	private static boolean initializationComplete = false;
	private static ComponentMapper<HLAObjectComponent> objectComponentMapper = ComponentMapper.getFor(HLAObjectComponent.class);
	
	private static final Object nameReservationSemaphore = new Object();
	private static boolean nameReservationStatus;

	public static void discoverObjectInstance(final ObjectInstanceHandle theObject, ObjectClassHandle theObjectClass, String objectName)
	{
		RTIambassador rtiAmbassador = VegaRTIAmbassador.instance();
		HLAObjectComponent objectComponent = World.createComponent(HLAObjectComponent.class);

		try
		{
			String className = rtiAmbassador.getObjectClassName(theObjectClass);
			String instanceName = rtiAmbassador.getObjectInstanceName(theObject);
			VegaObjectClass objectClass = ProjectRegistry.getObjectClass(className);
			
			IEntityArchetype archetype = ProjectRegistry.getArchetype(objectClass.archetypeName);

			Entity entity = archetype.createEntity();

			objectComponent.className = className;
			objectComponent.instanceName = instanceName;
			World.addComponent(entity, objectComponent);
			
			ProjectRegistry.addEntityObjectInstance(entity, theObject);

			if (!initializationComplete && className.equals("HLAobjectRoot.ExecutionConfiguration"))
				ExecutionLatch.disable();

			requestLatestAttributeValues(theObject, instanceName, objectClass);
		}
		catch (Exception e)
		{
			LOGGER.error("Failed during the discovery of a new object instance\n[REASON]", e);
			System.exit(1);
		}
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
		
		try
		{
			VegaObjectClass objectClass = ProjectRegistry.getObjectClass(className);
			AttributeHandleSet attributeHandles = objectClass.subscribeableAttributeHandles();
			
			if (!allAttributesPresent(attributeHandles, theAttributes))
				LOGGER.warn("Discarded latest values received for the object instance \"{}\" because one or more required attributes are missing", instanceName);
			else
				updateObjectInstance(entity, objectClass, theAttributes);
				
		}
		catch (Exception e)
		{
			LOGGER.error("Failed to set the latest values for the object instance \"{}\"\n[REASON]", instanceName, e);
			System.exit(1);
		}
		
		if (!initializationComplete && className.equals("HLAobjectRoot.ExecutionConfiguration"))
		{
			initializationComplete = true;
			ExecutionLatch.disable();
		}
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
		synchronized(nameReservationSemaphore)
		{
			nameReservationSemaphore.notify();
		}
	}
	
	public static void objectInstanceNameReservationFailed(String objectName)
	{
		nameReservationStatus = false;
		synchronized(nameReservationSemaphore)
		{
			nameReservationSemaphore.notify();
		}
	}
	
	public static Object getNameReservationSemaphore()
	{
		return nameReservationSemaphore;
	}
	
	public static boolean getNameReservationStatus()
	{
		return nameReservationStatus;
	}
}
