/*-
 * SPDX-License-Identifier: BSD-3-Clause
 * Copyright (c) 2025 Hridyanshu Aatreya <Hridyanshu.Aatreya2@brunel.ac.uk>
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

package io.github.vega.core;

import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.LogicalTime;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.exceptions.FederateInternalError;
import hla.rti1516e.time.HLAinteger64Time;
import io.github.vega.components.ExCOComponent;
import io.github.vega.components.HLAInteractionComponent;
import io.github.vega.components.HLAObjectComponent;
import io.github.vega.data.ExecutionMode;
import io.github.vega.utils.ExecutionLatch;
import io.github.vega.utils.FrameworkObjects;

/**
 * Handles callbacks passed on from the RTI federate service thread.
 * 
 * @author Hridyanshu Aatreya
 * @since 1.0.0
 */
public final class HLACallbackManager
{
	private static final Logger LOGGER = LogManager.getLogger();
	
	private static final String EXCO_CLASS_NAME = "HLAobjectRoot.ExecutionConfiguration";
	private static boolean exCOInitialized = false;

	private static Set<String> objectsPendingDiscovery;

	private static final Object NAME_RESERVATION_SEMAPHORE = new Object();
	private static boolean nameReservationStatus;
	
	static
	{
		Set<String> requiredObjects = ProjectRegistry.requiredObjects;
		if (requiredObjects != null)
			objectsPendingDiscovery = new HashSet<String>(requiredObjects);
	}

	protected static void discoverObjectInstance(final ObjectInstanceHandle theObject, ObjectClassHandle theObjectClass, String objectName)
	{
		String className = null;
		ObjectClassProfile objectClass = null;
		IEntityArchetype archetype = null;
		Entity entity = null;
		
		try
		{
			RTIambassador rtiAmbassador = FrameworkObjects.getRtiAmbassador();
			className = rtiAmbassador.getObjectClassName(theObjectClass);
		}
		catch (Exception e)
		{
			LOGGER.warn("The newly discovered object instance \"{}\" was discarded\n[REASON] Failed to acquire name of its associated object class", objectName);
			return;
		}
		
		if ((objectClass = ProjectRegistry.getObjectClass(className)) == null)
		{
			LOGGER.warn("The newly discovered object instance \"{}\" was discarded\n[REASON] Failed to acquire name of its associated object class", objectName);
			return;
		}
		
		if ((archetype = ProjectRegistry.getArchetype(objectClass.archetypeName)) == null)
		{
			LOGGER.warn("The newly discovered object instance \"{}\" was discarded\n[REASON]The archetype <{}> defined for the HLA object class <{}> is not defined", objectName, objectClass.name, objectClass.archetypeName);
			return;
		}
		
		if ((entity = archetype.createEntity()) == null)
		{
			LOGGER.error("The newly discovered object instance \"{}\" was discarded\n[REASON] The archetype produced NULL instead of a valid entity", objectName);
			return;
		}
		
		ProjectRegistry.addRemoteEntity(entity);
		
		if (objectsPendingDiscovery != null && objectsPendingDiscovery.contains(objectName))
		{
			LOGGER.info("Discovered a new object instance \"{}\" of the class <{}>", objectName, className);
			objectsPendingDiscovery.remove(objectName);
			
			if (objectsPendingDiscovery.isEmpty() && ExecutionLatch.isActive())
				ExecutionLatch.disable();
		}
		
		createRemoteEntity(className, objectName, theObject, entity);
		requestLatestAttributeValues(theObject, objectName, objectClass);
		
		if (className.equals(EXCO_CLASS_NAME) && !exCOInitialized)
			ExecutionLatch.disable();
	}
	
	private static void createRemoteEntity(String className, String objectName, ObjectInstanceHandle instanceHandle, Entity entity)
	{
		Engine engine = FrameworkObjects.getEngine();
		
		HLAObjectComponent objectComponent = engine.createComponent(HLAObjectComponent.class);
		objectComponent.className = className;
		objectComponent.instanceName = objectName;
		objectComponent.instanceHandle = instanceHandle;
		entity.add(objectComponent);
	}
	
	private static void requestLatestAttributeValues(ObjectInstanceHandle instanceHandle, String instanceName, ObjectClassProfile objectClass)
	{
		AttributeHandleSet attributeHandles = objectClass.getSubscribeableAttributeHandles();

		try
		{
			RTIambassador rtiAmbassador = FrameworkObjects.getRtiAmbassador();
			rtiAmbassador.requestAttributeValueUpdate(instanceHandle, attributeHandles, null);
		}
		catch (Exception e)
		{
			LOGGER.error("Failed to request the latest values for the object instance \"{}\"\n[REASON]", instanceName, e);
			System.exit(1);
		}
	}
	
	protected static void removeObjectInstance(ObjectInstanceHandle theObject)
	{
		if (!exCOExists())
		{
			final Entity exCO = ProjectRegistry.getRemoteEntityByName("ExCO");
			ExCOComponent exCOComponent = FrameworkObjects.getExCOComponentMapper().get(exCO);
			
			exCOComponent.nextExecutionMode = ExecutionMode.EXEC_MODE_SHUTDOWN;
		}
			
		if (!ProjectRegistry.removeRemoteEntityByHandle(theObject))
		{
			LOGGER.warn("Failed to remove the requested object instance <{}>\n[REASON] It is absent from the registry and may have been deleted previously", theObject);
			return;
		}
	}
	
	// The change in ExCO execution mode to SHUTDOWN is faster than we can detect.
	// Therefore, this method helps us determine if ExCO is still present or not
	// i.e., it will throw an exception because the ExCO object will have disappeared
	// when SpaceMaster leaves the federation. We manually set the [next] execution
	// mode to SHUTDOWN ourselves and have the simulation loop terminate normally.
	private static boolean exCOExists()
	{
		try
		{
			RTIambassador rtiAmbassador = FrameworkObjects.getRtiAmbassador();
			rtiAmbassador.getObjectInstanceHandle("ExCO");
			return true;
		}
		catch (Exception e) 
		{
			return false;
		}
	}

	protected static void reflectAttributeValues(ObjectInstanceHandle theObject, AttributeHandleValueMap theAttributes)
	{
		String className = null;
		ObjectClassHandle classHandle = null;
		String instanceName = null;
		
		try
		{
			RTIambassador rtiAmbassador = FrameworkObjects.getRtiAmbassador();
			instanceName = rtiAmbassador.getObjectInstanceName(theObject);
			classHandle = rtiAmbassador.getKnownObjectClassHandle(theObject);
			className = rtiAmbassador.getObjectClassName(classHandle);
		}
		catch (Exception e) 
		{
			if (instanceName != null)
				LOGGER.warn("New values for the object instance \"{}\" were discarded\n[REASON]", e);
			else
				LOGGER.warn("New values for the object instance <{}> were discarded\nREASON]", e);
			
			return;
		}
		
		ObjectClassProfile objectClass = null;
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
		
		if (instanceName.equals("ExCO") && !exCOInitialized)
		{
			exCOInitialized = true;
			ExecutionLatch.disable();
		}
	}
	
	private static void updateObjectInstance(Entity entity, String instanceName, ObjectClassProfile objectClass, AttributeHandleValueMap latestValues)
	{
		EncoderFactory encoderFactory = FrameworkObjects.getEncoderFactory();

		for (String attributeName : objectClass.attributeNames)
		{
			AttributeHandle attributeHandle = objectClass.getHandleForAttribute(attributeName);

			if (!latestValues.containsKey(attributeHandle))
			{
				LOGGER.warn("New values for the object instance from the RTI \"{}\" is missing ", instanceName);
				return;
			}

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

	protected static void objectInstanceNameReservationSucceeded(String objectName)
	{
		nameReservationStatus = true;
		synchronized (NAME_RESERVATION_SEMAPHORE)
		{
			NAME_RESERVATION_SEMAPHORE.notify();
		}
	}

	protected static void objectInstanceNameReservationFailed(String objectName)
	{
		nameReservationStatus = false;
		synchronized (NAME_RESERVATION_SEMAPHORE)
		{
			NAME_RESERVATION_SEMAPHORE.notify();
		}
	}

	@SuppressWarnings("rawtypes")
	protected static void timeConstrainedEnabled(LogicalTime time) throws FederateInternalError
	{
		LOGGER.info("The federate is now HLA time constrained");
		ExecutionLatch.disable();
	}

	@SuppressWarnings("rawtypes")
	protected static void timeRegulationEnabled(LogicalTime time) throws FederateInternalError
	{
		LOGGER.info("HLA time regulation has been enabled");
		ExecutionLatch.disable();
	}

	@SuppressWarnings("rawtypes")
	protected static void timeAdvanceGrant(LogicalTime theTime) throws FederateInternalError
	{
		HLATimeManager.setPresentTime((HLAinteger64Time) theTime);
		ExecutionLatch.disable();
	}
	
	protected static void receiveInteraction(InteractionClassHandle interactionClass, ParameterHandleValueMap theParameters)
	{
		try
		{
			RTIambassador rtiAmbassador = FrameworkObjects.getRtiAmbassador();
			Engine engine = FrameworkObjects.getEngine();
			
			String className = rtiAmbassador.getInteractionClassName(interactionClass);
			
			InteractionClassProfile interactionClassType = null;
			if ((interactionClassType = ProjectRegistry.getInteractionClass(className)) == null)
			{
				LOGGER.error("Incoming interaction was discarded because the class <{}> is unrecognized", className);
				return;
			}
			
			IEntityArchetype archetype = ProjectRegistry.getArchetype(interactionClassType.archetypeName);
			Entity interaction = null;
			
			if ((interaction = archetype.createEntity()) == null)
			{
				LOGGER.error("An incoming interaction could not be processed the archetype <{}> returned NULL instead of an entity", archetype);
				return;
			}
			
			unpackInteractionData(interaction, interactionClassType, theParameters);
			
			HLAInteractionComponent interactionComponent = engine.createComponent(HLAInteractionComponent.class);
			interactionComponent.className = className;
			interaction.add(interactionComponent);
			
			HLAInteractionQueue.addInteraction(interaction);
		}
		catch (Exception e)
		{
			LOGGER.warn("Incoming interaction was discarded\n[REASON]", e);
		}
	}
	
	private static void unpackInteractionData(Entity entity, InteractionClassProfile interactionClass, ParameterHandleValueMap parameterHandleValueMap)
	{
		for (String parameterName : interactionClass.parameterNames)
		{
			EncoderFactory encoderFactory = FrameworkObjects.getEncoderFactory();
			
			ParameterHandle parameterHandle = interactionClass.getParameterHandle(parameterName);
			byte[] encodedValue = parameterHandleValueMap.get(parameterHandle);
			String converterName = null;
			
			if (interactionClass.parameterUsesMultiConverter(parameterName))
			{
				converterName = interactionClass.getParameterMultiConverterName(parameterName);
				IMultiDataConverter multiDataConverter = ProjectRegistry.getMultiConverter(converterName);
				int trigger = interactionClass.getParameterMultiConverterTrigger(parameterName, converterName);
				multiDataConverter.decode(entity, encoderFactory, encodedValue, trigger);
			}
			else
			{
				converterName = interactionClass.getParameterConverterName(parameterName);
				IDataConverter dataConverter = ProjectRegistry.getDataConverter(converterName);
				dataConverter.decode(entity, encoderFactory, encodedValue);
			}
		}
	}

	protected static Object getNameReservationSemaphore()
	{
		return NAME_RESERVATION_SEMAPHORE;
	}

	protected static boolean getNameReservationStatus()
	{
		return nameReservationStatus;
	}
}
