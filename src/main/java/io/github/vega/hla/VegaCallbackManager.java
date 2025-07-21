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
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.LogicalTime;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.exceptions.FederateInternalError;
import hla.rti1516e.time.HLAinteger64Time;
import io.github.vega.components.ExCOComponent;
import io.github.vega.components.HLAObjectComponent;
import io.github.vega.core.IDataConverter;
import io.github.vega.core.IEntityArchetype;
import io.github.vega.core.IMultiDataConverter;
import io.github.vega.core.World;
import io.github.vega.data.ExCO;
import io.github.vega.data.ExecutionMode;
import io.github.vega.utils.ExecutionLatch;
import io.github.vega.utils.ProjectRegistry;

public class VegaCallbackManager
{
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Marker HLA_MARKER = MarkerManager.getMarker("HLA");
	private static final Marker SIMUL_MARKER = MarkerManager.getMarker("SIMUL");
	
	private static final RTIambassador RTI_AMBASSADOR = VegaRTIAmbassador.instance();
	private static final String EXCO_CLASS_NAME = "HLAobjectRoot.ExecutionConfiguration";
	private static boolean exCoInitialized = false;

	private static Set<String> objectsPendingDiscovery;

	private static final ComponentMapper<HLAObjectComponent> OBJECT_COMPONENT_MAPPER = ComponentMapper.getFor(HLAObjectComponent.class);

	private static final Object NAME_RESERVATION_SEMAPHORE = new Object();
	private static boolean nameReservationStatus;
	
	static
	{
		Set<String> requiredObjects = ProjectRegistry.requiredObjects;
		if (requiredObjects != null)
			objectsPendingDiscovery = new HashSet<String>(requiredObjects);
	}

	public static void discoverObjectInstance2(final ObjectInstanceHandle theObject, ObjectClassHandle theObjectClass, String objectName)
	{
		String className = null;
		VegaObjectClass objectClass = null;
		IEntityArchetype archetype = null;
		Entity entity = null;
		
		try
		{
			className = RTI_AMBASSADOR.getObjectClassName(theObjectClass);
		}
		catch (Exception e)
		{
			LOGGER.warn(HLA_MARKER, "The newly discovered object instance \"{}\" was discarded\n[REASON] Failed to acquire name of its associated object class", objectName);
			return;
		}
		
		if ((objectClass = ProjectRegistry.getObjectClass(className)) == null)
		{
			LOGGER.warn(SIMUL_MARKER, "The newly discovered object instance \"{}\" was discarded\n[REASON] Failed to acquire name of its associated object class", objectName);
			return;
		}
		
		if ((archetype = ProjectRegistry.getArchetype(objectClass.archetypeName)) == null)
		{
			LOGGER.warn(SIMUL_MARKER, "The newly discovered object instance \"{}\" was discarded\n[REASON]The archetype <{}> defined for the HLA object class <{}> is not defined", objectName, objectClass.name, objectClass.archetypeName);
			return;
		}
		
		if ((entity = archetype.createEntity()) == null)
		{
			LOGGER.error(SIMUL_MARKER, "The newly discovered object instance \"{}\" was discarded\n[REASON] The archetype produced NULL instead of a valid entity", objectName);
			return;
		}
		
		ProjectRegistry.addRemoteEntity(entity);
		
		if (objectsPendingDiscovery != null && objectsPendingDiscovery.contains(objectName))
		{
			LOGGER.info(HLA_MARKER, "Discovered a new object instance \"{}\" of the class <{}>", objectName, className);
			objectsPendingDiscovery.remove(objectName);
			
			if (objectsPendingDiscovery.isEmpty() && ExecutionLatch.isActive())
				ExecutionLatch.disable();
		}
		
		createRemoteEntity(className, objectName, theObject, entity);
		requestLatestAttributeValues(theObject, objectName, objectClass);
		
		if (className.equals(EXCO_CLASS_NAME) && ExecutionLatch.isActive())
			ExecutionLatch.disable();
	}
	
	private static void createRemoteEntity(String className, String objectName, ObjectInstanceHandle instanceHandle, Entity entity)
	{
		HLAObjectComponent objectComponent = World.createComponent(HLAObjectComponent.class);
		objectComponent.className = className;
		objectComponent.instanceName = objectName;
		objectComponent.instanceHandle = instanceHandle;
		World.addComponent(entity, objectComponent);
	}

	/*
	public static void discoverObjectInstance(final ObjectInstanceHandle theObject, ObjectClassHandle theObjectClass, String objectName)
	{
		// TODO - Why are you getting the instance name again? It's provided already!
		RTIambassador rtiAmbassador = VegaRTIAmbassador.instance();

		String className = null;
		String instanceName = null;
		Entity entity = null;

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

		if (className.equals(EXCO_CLASS_NAME) && !exCoInitialized)
		{
			entity = ExCO.getEntity();
			ExecutionLatch.disable();
		}
		else
		{
			IEntityArchetype archetype = ProjectRegistry.getArchetype(objectClass.archetypeName);
			entity = archetype.createEntity();

			if (entity == null)
			{
				LOGGER.warn("Failed to create an internalized entity representation of the object instance \"{}\" of class <{}>.\n[REASON] The archetype <{}> returned NULL instead of an entity. Re-check to ensure it returns an entity with the necessary components", instanceName, className, archetype);
				return;
			}

			ProjectRegistry.addRemoteEntity(entity);

			if (objectsPendingDiscovery != null && objectsPendingDiscovery.contains(instanceName))
			{
				LOGGER.info("Discovered the object instance \"{}\" of class <{}>", instanceName, className);
				objectsPendingDiscovery.remove(instanceName);

				if (objectsPendingDiscovery.isEmpty() && ExecutionLatch.isActive())
					ExecutionLatch.disable();
			}
		}

		HLAObjectComponent objectComponent = World.createComponent(HLAObjectComponent.class);
		objectComponent.className = className;
		objectComponent.instanceName = instanceName;
		World.addComponent(entity, objectComponent);

		requestLatestAttributeValues(theObject, instanceName, objectClass);
	}
	*/
	
	private static void requestLatestAttributeValues(ObjectInstanceHandle instanceHandle, String instanceName, VegaObjectClass objectClass)
	{
		AttributeHandleSet attributeHandles = objectClass.subscribeableAttributeHandles();

		try
		{
			RTI_AMBASSADOR.requestAttributeValueUpdate(instanceHandle, attributeHandles, null);
		}
		catch (Exception e)
		{
			LOGGER.error("Failed to request the latest values for the object instance \"{}\"\n[REASON]", instanceName, e);
			System.exit(1);
		}
	}

	public static void reflectAttributeValues2(ObjectInstanceHandle theObject, AttributeHandleValueMap theAttributes)
	{
		String className = null;
		ObjectClassHandle classHandle = null;
		String instanceName = null;
		
		try
		{
			instanceName = RTI_AMBASSADOR.getObjectInstanceName(theObject);
			classHandle = RTI_AMBASSADOR.getKnownObjectClassHandle(theObject);
			className = RTI_AMBASSADOR.getObjectClassName(classHandle);
		}
		catch (Exception e) 
		{
			if (instanceName != null)
				LOGGER.warn(HLA_MARKER, "New values for the object instance \"{}\" were discarded\n[REASON]", e);
			else
				LOGGER.warn(HLA_MARKER, "New values for the object instance <{}> were discarded\nREASON]", e);
			
			return;
		}
		
		VegaObjectClass objectClass = null;
		if ((objectClass = ProjectRegistry.getObjectClass(className)) == null)
		{
			LOGGER.warn("New values for the object instance \"{}\" were discarded\n[REASON] Associated object class for this instance was not found", instanceName);
			return;
		}
		
		Entity entity = null;
		if ((entity = ProjectRegistry.getRemoteEntityByHandle(theObject)) == null)
		{
			LOGGER.warn("New values for the object instance \"{}\" were discarded\n[REASON] The entity associated with this object instance was not found", instanceName);
			return;
		}
		
		updateObjectInstance(entity, instanceName, objectClass, theAttributes);
		
		if (instanceName.equals("ExCO") && ExecutionLatch.isActive())
		{
			Entity exCO = ProjectRegistry.getRemoteEntityByName("ExCO");
			ComponentMapper<ExCOComponent> exComponentMapper = ComponentMapper.getFor(ExCOComponent.class);
			ExCOComponent exCOComponent = exComponentMapper.get(exCO);
			
			System.out.println("Frame: " + exCOComponent.rootFrameName);
			System.out.println("LCTS: " + exCOComponent.leastCommonTimeStep);
			System.out.println("Current Mode: " + exCOComponent.currentExecutionMode);
			System.out.println("Next Mode: " + exCOComponent.nextExecutionMode);
			ExecutionLatch.disable();
		}
	}

	public static void reflectAttributeValues(ObjectInstanceHandle theObject, AttributeHandleValueMap theAttributes)
	{
		RTIambassador rtiAmbassador = VegaRTIAmbassador.instance();

		ObjectClassHandle classHandle = null;
		String className = null;
		String instanceName = null;

		try
		{
			classHandle = rtiAmbassador.getKnownObjectClassHandle(theObject);
			className = rtiAmbassador.getObjectClassName(classHandle);
			instanceName = rtiAmbassador.getObjectInstanceName(theObject);
		}
		catch (Exception e)
		{
			if (instanceName != null)
				LOGGER.info("Failed to set the latest incoming values for the object instance\"{}\"\n[REASON]", instanceName, e);
			else
				LOGGER.info("Failed to set the latest incoming values for an unknown object instance\n[REASON]", e);
			System.exit(1);
		}

		System.out.println("Updates for " + instanceName);

		Entity entity = null;
		if (instanceName.equals("ExCO"))
			entity = ExCO.getEntity();
		else
			entity = ProjectRegistry.getRemoteEntityByHandle(theObject);

		if (entity == null)
		{
			LOGGER.warn("Discarding the latest incoming values for the object instance\"{}\"\n[REASON] No corresponding entity was created for the object instance", instanceName);
			return;
		}

		VegaObjectClass objectClass = ProjectRegistry.getObjectClass(className);

		if (objectClass == null)
		{
			LOGGER.warn("Discarding the latest incoming values for the object instance\"{}\"\n[REASON] The object instance is of an unknown class <{}>", instanceName, className);
			return;
		}

		updateObjectInstance(entity, instanceName, objectClass, theAttributes);

		if (instanceName.equals("ExCO") && !exCoInitialized)
		{
			exCoInitialized = true;
			ExecutionLatch.disable();
		}
	}

	private static void updateObjectInstance(Entity entity, String instanceName, VegaObjectClass objectClass, AttributeHandleValueMap latestValues)
	{
		EncoderFactory encoderFactory = VegaEncoderFactory.instance();

		for (String attributeName : objectClass.attributeNames)
		{
			AttributeHandle attributeHandle = objectClass.getAttributeHandle(attributeName);

			if (!latestValues.containsKey(attributeHandle))
				return;

			byte[] newValue = latestValues.get(attributeHandle);

			if (objectClass.attributeUsesMultiConverter(attributeName))
			{
				String converterName = objectClass.getAttributeMultiConverterName(attributeName);
				IMultiDataConverter converter = ProjectRegistry.getMultiConverter(converterName);
				int trigger = objectClass.getAttributeConverterTrigger(attributeName, converterName);
				converter.decode(entity, encoderFactory, newValue, trigger);
			}
			else
			{
				String converterName = objectClass.getAttributeConverterName(attributeName);
				IDataConverter converter = ProjectRegistry.getDataConverter(converterName);
				converter.decode(entity, encoderFactory, newValue);
			}
		}
	}

	public static void objectInstanceNameReservationSucceeded(String objectName)
	{
		nameReservationStatus = true;
		synchronized (NAME_RESERVATION_SEMAPHORE)
		{
			NAME_RESERVATION_SEMAPHORE.notify();
		}
	}

	public static void objectInstanceNameReservationFailed(String objectName)
	{
		nameReservationStatus = false;
		synchronized (NAME_RESERVATION_SEMAPHORE)
		{
			NAME_RESERVATION_SEMAPHORE.notify();
		}
	}

	@SuppressWarnings("rawtypes")
	public static void timeConstrainedEnabled(LogicalTime time) throws FederateInternalError
	{
		LOGGER.info("The federate is now HLA time constrained");
		ExecutionLatch.disable();
	}

	@SuppressWarnings("rawtypes")
	public static void timeRegulationEnabled(LogicalTime time) throws FederateInternalError
	{
		LOGGER.info("HLA time regulation has been enabled");
		ExecutionLatch.disable();
	}

	@SuppressWarnings("rawtypes")
	public static void timeAdvanceGrant(LogicalTime theTime) throws FederateInternalError
	{
		ExecutionMode currentMode = ExCO.getCurrentExecutionMode();
		ExecutionMode nextMode = ExCO.getNextExecutionMode();

		System.out.println("[TAG] current mode: " + ExCO.getCurrentExecutionMode());
		System.out.println("[TAG] next mode: " + ExCO.getNextExecutionMode());

		VegaTimeManager.setPresentTime((HLAinteger64Time) theTime);
		System.out.println("Disabling latch");
		ExecutionLatch.disable();
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
