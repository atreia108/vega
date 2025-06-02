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

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import io.github.vega.core.IAdapter;
import io.github.vega.core.IAssembler;
import io.github.vega.core.Registry;
import io.github.vega.core.SimulationBase;
import io.github.vega.core.ThreadLatch;
import io.github.vega.core.World;
import io.github.vega.spacefom.components.ExCoComponent;

public class HlaCallbackManager
{
	private static final Logger logger = LoggerFactory.getLogger(HlaCallbackManager.class);

	public static void discoverObjectInstance(final ObjectInstanceHandle theObject, ObjectClassHandle theObjectClass,
			String objectName)
	{
		RTIambassador rtiAmbassador = HlaManager.getRtiAmbassador();

		try
		{
			String objectClassName = rtiAmbassador.getObjectClassName(theObjectClass);
			String objectInstanceName = rtiAmbassador.getObjectInstanceName(theObject);

			HlaObjectType type = Registry.getObjectType(objectClassName);
			IAssembler assembler = Registry.getAssembler(type.getAssemblerName());

			Entity entity = assembler.assembleEntity();

			HlaObjectComponent objectComponent = World.createComponent(HlaObjectComponent.class);
			objectComponent.className = objectClassName;
			objectComponent.instanceName = objectInstanceName;

			World.addComponent(entity, objectComponent);

			Registry.addEntityForInstance(entity, theObject);

			if (!HlaManager.isInitialized() && World.getComponent(entity, ExCoComponent.class) != null)
			{
				SimulationBase.setExCo(entity);
			}

			HlaManager.latestObjectInstanceUpdates(theObject, type);
		}
		catch (Exception e)
		{
			logger.warn("Could not create entity representation for instance {} of object class {}\n[REASON]\n",
					theObject, theObjectClass);
			e.printStackTrace();
		}

		if (!HlaManager.isInitialized())
			ThreadLatch.stop();
	}

	public static void reflectAttributeValues(ObjectInstanceHandle theObject, AttributeHandleValueMap theAttributes)
	{
		Entity entity = Registry.getEntity(theObject);
		RTIambassador rtiAmbassador = HlaManager.getRtiAmbassador();

		try
		{
			ObjectClassHandle classHandle = rtiAmbassador.getKnownObjectClassHandle(theObject);
			String className = rtiAmbassador.getObjectClassName(classHandle);

			HlaObjectType type = Registry.getObjectType(className);
			AttributeHandleSet attributeHandleSet = type.getRtiAttributeHandleSet();

			if (!containsAllAttributes(attributeHandleSet, theAttributes))
			{
				logger.warn(
						"Updated set of values received for [Entity: {}, Instance: {}, Object Class: {}] was missing one or more required attributes. No changes were made to the simulation.",
						entity, theObject, classHandle);
				return;
			}

			updateEntity(entity, type, theAttributes);
		}
		catch (Exception e)
		{
			logger.warn("Could not set the updated values for {}\n[REASON]\n", entity);
			e.printStackTrace();
		}

		if (!HlaManager.isInitialized())
			ThreadLatch.stop();
	}

	private static boolean containsAllAttributes(AttributeHandleSet required, AttributeHandleValueMap candidate)
	{
		for (AttributeHandle attribute : required)
		{
			if (!candidate.containsKey(attribute))
				return false;
		}

		return true;
	}

	private static void updateEntity(Entity entity, HlaObjectType objectType, AttributeHandleValueMap newValues)
	{
		EncoderFactory encoder = HlaManager.getEncoderFactory();

		Set<String> attributeNames = objectType.getAttributeNames();

		for (String attributeName : attributeNames)
		{
			AttributeHandle attributeHandle = objectType.getAttributeHandle(attributeName);
			byte[] attributeValue = newValues.get(attributeHandle);

			String adapterName = objectType.getAdapterName(attributeName);
			IAdapter adapter = Registry.getAdapter(adapterName);
			adapter.deserialize(entity, encoder, attributeValue);
		}
	}

	public static void objectInstanceNameReservationFailed(String objectName)
	{
		HlaManager.notifyReservationOutcome(false);
	}

	public static void objectInstanceNameReservationSucceeded(String objectName)
	{
		HlaManager.notifyReservationOutcome(true);
	}

	@SuppressWarnings("rawtypes")
	public static void timeConstrainedEnabled(LogicalTime time) throws FederateInternalError
	{
		ThreadLatch.stop();
	}

	@SuppressWarnings("rawtypes")
	public static void timeRegulationEnabled(LogicalTime time) throws FederateInternalError
	{
		ThreadLatch.stop();
	}

	@SuppressWarnings("rawtypes")
	public static void timeAdvanceGrant(LogicalTime theTime) throws FederateInternalError
	{
		HlaManager.updateCurrentTime(theTime);
		ThreadLatch.stop();
	}

	public static void removeObjectInstance(ObjectInstanceHandle theObject)
	{
		HlaManager.deleteRemoteEntity(theObject);
	}

	public static void receiveInteraction(InteractionClassHandle interactionClass,
			ParameterHandleValueMap theParameters, byte[] userSuppliedTag)
	{
		RTIambassador rtiAmbassador = HlaManager.getRtiAmbassador();
		try
		{
			String className = rtiAmbassador.getInteractionClassName(interactionClass);
			HlaInteractionType interactionType = Registry.getInteractionType(className);
			Set<ParameterHandle> parameterHandleSet = interactionType.getParameterHandles();

			if (!containsAllParameters(parameterHandleSet, theParameters))
			{
				logger.warn("Ignored a received interaction of type {} which was missing one or more parameters.",
						interactionClass);
				return;
			}

			createInteraction(interactionType, theParameters);
		}
		catch (Exception e)
		{
			logger.warn("Could not receive interaction from the RTI\n[REASON]");
			e.printStackTrace();
		}
	}

	private static boolean containsAllParameters(Set<ParameterHandle> required, ParameterHandleValueMap candidate)
	{
		for (ParameterHandle parameterHandle : required)
		{
			if (!candidate.containsKey(parameterHandle))
				return false;
		}
		return true;
	}

	private static void createInteraction(HlaInteractionType type, ParameterHandleValueMap valueMap)
	{
		EncoderFactory encoder = HlaManager.getEncoderFactory();
		
		String assemblerName = type.getAssemblerName();
		IAssembler assembler = Registry.getAssembler(assemblerName);

		Entity interactionEntity = assembler.assembleEntity();
		HlaInteractionComponent interactionComponent = World.createComponent(HlaInteractionComponent.class);
		interactionComponent.className = type.getName();
		World.addComponent(interactionEntity, interactionComponent);

		Set<String> parameterNames = type.getParameterNames();
		parameterNames.forEach((parameterName) ->
		{
			ParameterHandle parameterHandle = type.getParameterHandle(parameterName);
			byte[] parameterValue = valueMap.get(parameterHandle);
			String adapterName = type.getAdapterName(parameterName);
			IAdapter adapter = Registry.getAdapter(adapterName);
			adapter.deserialize(interactionEntity, encoder, parameterValue);
		});

		HlaManager.addInteraction(interactionEntity);
	}
}
