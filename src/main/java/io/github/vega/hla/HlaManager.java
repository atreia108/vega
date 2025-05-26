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

import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.CallbackModel;
import hla.rti1516e.LogicalTime;
import hla.rti1516e.LogicalTimeFactoryFactory;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.ResignAction;
import hla.rti1516e.RtiFactory;
import hla.rti1516e.RtiFactoryFactory;
import hla.rti1516e.TimeQueryReturn;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.exceptions.RTIexception;
import hla.rti1516e.exceptions.RTIinternalError;
import hla.rti1516e.time.HLAinteger64Time;
import hla.rti1516e.time.HLAinteger64TimeFactory;
import io.github.vega.configuration.Configuration;
import io.github.vega.core.IAdapter;
import io.github.vega.core.Registry;
import io.github.vega.core.World;
import io.github.vega.spacefom.SpaceFomFederateAmbassador;

public class HlaManager
{
	private static final Logger logger = LoggerFactory.getLogger(HlaManager.class);

	private static RTIambassador rtiAmbassador;
	private static EncoderFactory encoderFactory;
	private static HLAinteger64TimeFactory timeFactory;

	private static HLAinteger64Time currentTime;
	private static HLAinteger64Time lookAheadTime;
	private static final long LOOK_AHEAD_INTERVAL = 1000000;

	private static boolean initialized;
	private static Object reservationSemaphore;
	private static boolean reservationSucceeded;

	static
	{
		try
		{
			RtiFactory rtiFactory = RtiFactoryFactory.getRtiFactory();
			rtiAmbassador = rtiFactory.getRtiAmbassador();
			encoderFactory = rtiFactory.getEncoderFactory();
			timeFactory = LogicalTimeFactoryFactory.getLogicalTimeFactory(HLAinteger64TimeFactory.class);
			initialized = false;
			reservationSemaphore = new Object();
			reservationSucceeded = false;
		}
		catch (RTIinternalError e)
		{
			logger.error("[REASON]\n");
			e.printStackTrace();
		}
	}

	public static void connect()
	{
		String federateName = Configuration.getFederateName();
		String federationName = Configuration.getFederationName();

		try
		{
			rtiAmbassador.connect(new SpaceFomFederateAmbassador(), CallbackModel.HLA_IMMEDIATE);
			rtiAmbassador.joinFederationExecution(federateName, federationName);
			logger.info("Successfully joined the HLA federation <" + federationName + "> as <" + federateName + ">.");
		}
		catch (Exception e)
		{
			logger.error("Failed to join the HLA federation <" + federationName + ">.\n" + "[REASON]\n");
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static void disconnect()
	{
		try
		{
			rtiAmbassador.resignFederationExecution(ResignAction.CANCEL_THEN_DELETE_THEN_DIVEST);
			logger.info("Simulation terminated successfully...");
			System.exit(1);
		}
		catch (RTIexception e)
		{
			logger.error("[REASON]");
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static boolean sendUpdate(Entity entity)
	{
		ComponentMapper<HlaObjectComponent> mapper = ComponentMapper.getFor(HlaObjectComponent.class);
		HlaObjectComponent object = mapper.get(entity);

		if (!checkHlaObjectComponent(entity, object))
			return false;

		String typeName = object.className;
		HlaObjectType type = Registry.getObjectType(typeName);
		Map<String, AttributeHandle> attributeNameHandleMap = type.getAttributeNameHandleMap();
		
		try
		{
			ObjectInstanceHandle instanceHandle = Registry.getObjectInstance(entity);
			AttributeHandleValueMap updatedAttributeHandleMap = rtiAmbassador.getAttributeHandleValueMapFactory().create(type.attributeHandleCount());
			
			attributeNameHandleMap.forEach((attributeName, attributeHandle) -> {
				String adapterName = type.getAdapterName(attributeName);
				IAdapter adapter = Registry.getAdapter(adapterName);
				updatedAttributeHandleMap.put(attributeHandle, adapter.serialize(entity, encoderFactory));
			});
			
			rtiAmbassador.updateAttributeValues(instanceHandle, updatedAttributeHandleMap, null);
		}
		catch (Exception e) {
			logger.warn("Updates could not be sent the entity {}\n[REASON]", entity);
			e.printStackTrace();
		}
		
		return true;
	}

	public static boolean sendInteraction(Entity entity)
	{
		ComponentMapper<HlaInteractionComponent> mapper = ComponentMapper.getFor(HlaInteractionComponent.class);
		HlaInteractionComponent interaction = mapper.get(entity);

		if (!checkHlaInteractionComponent(entity, interaction))
			return false;

		return true;
	}

	private static boolean checkHlaObjectComponent(Entity entity, HlaObjectComponent object)
	{
		String typeName = object.className;

		if (Registry.getObjectType(typeName) == null)
		{
			logger.warn(
					"The class name \"{}\" specified in the HlaObjectComponent of {} is either invalid or was not published/subscribed at runtime.",
					typeName, entity);
			return false;
		}

		if (Registry.getObjectInstance(entity) == null)
		{
			logger.warn(
					"Updates cannot be sent for {} because it has no corresponding object instance at the RTI. The object instance may have been previously deleted.",
					entity);
			return false;
		}

		return true;
	}

	private static boolean checkHlaInteractionComponent(Entity entity, HlaInteractionComponent interaction)
	{
		String typeName = interaction.className;

		if (Registry.getInteractionType(typeName) == null)
		{
			logger.warn(
					"The class name \"{}\" specified in the HlaInteractionComponent of {} is either invalid or was not published/subscribed at runtime.",
					typeName, entity);
			return false;
		}

		return true;
	}

	public static void declareAllObjects()
	{
		Set<HlaObjectType> objectTypes = Registry.getObjectTypes();

		for (HlaObjectType type : objectTypes)
		{
			if (!type.getIntentDeclaredToRti())
			{
				publishObjectAttributes(type);
				subscribeObjectAttributes(type);
				type.intentDeclared();
			}
		}
	}

	/*
	 * TODO - For object classes, evaluate the consequences of attempting to pub/sub
	 * with an empty attribute set
	 */
	public static void publishObjectAttributes(HlaObjectType type)
	{
		String typeName = type.getName();

		try
		{
			ObjectClassHandle classHandle = rtiAmbassador.getObjectClassHandle(typeName);
			AttributeHandleSet attributeSetHandle = rtiAmbassador.getAttributeHandleSetFactory().create();

			Set<String> publisheableAttributes = type.getPublisheableAttributes();

			publisheableAttributes.forEach((attributeName) ->
			{
				try
				{
					AttributeHandle attributeHandle = rtiAmbassador.getAttributeHandle(classHandle, attributeName);
					attributeSetHandle.add(attributeHandle);
					type.registerAttributeHandle(attributeName, attributeHandle);
				}
				catch (Exception e)
				{
					logger.warn("Could not publish HLA object class <" + typeName + ">." + "[REASON]\n");
					e.printStackTrace();
				}
			});

			rtiAmbassador.publishObjectClassAttributes(classHandle, attributeSetHandle);

			type.setRtiClassHandle(classHandle);
			type.setRtiAttributeHandleSet(attributeSetHandle);
			// type.intentDeclared();
		}
		catch (Exception e)
		{
			logger.warn("Could not publish HLA object class <" + typeName + ">." + "\n[REASON]\n");
			e.printStackTrace();
		}
	}

	public static void subscribeObjectAttributes(HlaObjectType type)
	{
		String typeName = type.getName();

		try
		{
			ObjectClassHandle classHandle = rtiAmbassador.getObjectClassHandle(typeName);
			AttributeHandleSet attributeSetHandle = rtiAmbassador.getAttributeHandleSetFactory().create();

			Set<String> subscribeableAttributes = type.getSubscribeableAttributes();

			subscribeableAttributes.forEach((attributeName) ->
			{
				try
				{
					AttributeHandle attributeHandle = rtiAmbassador.getAttributeHandle(classHandle, attributeName);
					attributeSetHandle.add(attributeHandle);
					type.registerAttributeHandle(attributeName, attributeHandle);
				}
				catch (Exception e)
				{
					logger.warn("Could not subscribe HLA object class <" + typeName + ">." + "\n[REASON]");
					e.printStackTrace();
				}
			});

			rtiAmbassador.subscribeObjectClassAttributes(classHandle, attributeSetHandle);

			type.setRtiClassHandle(classHandle);
			type.setRtiAttributeHandleSet(attributeSetHandle);
			// type.intentDeclared();
		}
		catch (Exception e)
		{
			logger.warn("Could not subscribe HLA object class <" + typeName + ">." + "\n[REASON]\n");
			e.printStackTrace();
		}
	}

	public void declareAllInteractions()
	{

	}

	public static void publishInteraction(HlaInteractionType type)
	{

	}

	public static void subscribeInteraction(HlaInteractionType type)
	{

	}

	public static boolean registerObjectInstance(Entity entity, String typeName, String instanceName)
	{
		HlaObjectType objectType = Registry.getObjectType(typeName);

		ObjectClassHandle classHandle = objectType.getRtiClassHandle();
		ObjectInstanceHandle instanceHandle = null;

		try
		{
			synchronized (reservationSemaphore)
			{
				rtiAmbassador.reserveObjectInstanceName(instanceName);
				awaitReservation();
			}

			if (!reservationSucceeded)
			{
				logger.warn(
						"The entity {} could not be registered as an object instance at the RTI with the name \"{}\". Its HlaObjectComponent has been removed to prevent undefined behavior.\n[REASON]",
						entity, instanceName);
				return false;
			}

			instanceHandle = rtiAmbassador.registerObjectInstance(classHandle, instanceName);
			Registry.addEntityForInstance(entity, instanceHandle);

			return true;
		}
		catch (Exception e)
		{
			logger.warn(
					"The entity {} could not be registered as an object instance at the RTI. Its HlaObjectComponent has been removed to prevent undefined behavior.\n[REASON]",
					entity);
			e.printStackTrace();
			return false;
		}
	}
	
	public static boolean deleteObjectInstance(Entity entity)
	{
		ObjectInstanceHandle instanceHandle = Registry.getObjectInstance(entity);
		
		if (instanceHandle == null)
			return false;
		
		if (!deleteObjectInstance(instanceHandle))
			return false;
		
		Registry.deleteObjectInstance(entity);
		
		return true;
	}
	
	private static boolean deleteObjectInstance(ObjectInstanceHandle instanceHandle)
	{
		try
		{
			rtiAmbassador.deleteObjectInstance(instanceHandle, null);
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}
	
	public static void deleteRemoteEntity(ObjectInstanceHandle instanceHandle)
	{
		Entity entity = Registry.getEntity(instanceHandle);
		Registry.deleteObjectInstance(entity);
		PooledEngine engine = World.getEngine();
		engine.removeEntity(entity);
	}

	private static boolean awaitReservation()
	{
		try
		{
			synchronized (reservationSemaphore)
			{
				reservationSemaphore.wait();
				return true;
			}
		}
		catch (InterruptedException e)
		{
			return false;
		}
	}

	public static void notifyReservationOutcome(boolean outcome)
	{
		reservationSucceeded = outcome;
		synchronized (reservationSemaphore)
		{
			reservationSemaphore.notify();
		}
	}

	public static void latestObjectInstanceUpdates(ObjectInstanceHandle handle, HlaObjectType type)
	{
		AttributeHandleSet attributeSet = type.getRtiAttributeHandleSet();
		try
		{
			rtiAmbassador.requestAttributeValueUpdate(handle, attributeSet, null);
		}
		catch (Exception e)
		{
			logger.warn("Unable to request the latest updates for object instance of type <{}>.\n[REASON]",
					type.getName());
			e.printStackTrace();
		}
	}

	public static void enableHlaTimeConstrained()
	{
		try
		{
			rtiAmbassador.enableTimeConstrained();
		}
		catch (Exception e)
		{
			logger.error("The simulation was terminated prematurely\n[REASON]");
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static void enableHlaTimeRegulation(long leastCommonTimeStep)
	{
		lookAheadTime = timeFactory.makeTime(leastCommonTimeStep);
		
		try
		{
			rtiAmbassador.enableTimeRegulation(timeFactory.makeInterval(LOOK_AHEAD_INTERVAL));
		}
		catch (Exception e)
		{
			logger.error("The simulation was terminated prematurely\n[REASON]");
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public static HLAinteger64Time logicalTimeBoundary(long leastCommonTimeStep)
	{
		try
		{
			TimeQueryReturn galtQuery = rtiAmbassador.queryGALT();
			HLAinteger64Time galt = (HLAinteger64Time) galtQuery.time;
			long timeBoundary = (long) ((Math.floor(galt.getValue() / leastCommonTimeStep) + 1) * leastCommonTimeStep);
			HLAinteger64Time hltb = timeFactory.makeTime(timeBoundary);
			
			return hltb;
		}
		catch (Exception e) {
			logger.error("The simulation was terminated prematurely\n[REASON]");
			e.printStackTrace();
			System.exit(1);
		}
		
		return null;
	}
	
	public static void advanceTime(HLAinteger64Time time)
	{
		try
		{
			rtiAmbassador.timeAdvanceRequest(time);
		}
		catch (Exception e) {
			logger.error("The simulation was terminated prematurely\n[REASON]");
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public static HLAinteger64Time nextTimeStep()
	{
		long present = currentTime.getValue();
		long timeStep = lookAheadTime.getValue();
		long future = present + timeStep;
		HLAinteger64Time futureInHlaTime = timeFactory.makeTime(future);
		
		return futureInHlaTime;
	}
	
	@SuppressWarnings("rawtypes")
	public static void updateCurrentTime(LogicalTime newTime)
	{
		currentTime = (HLAinteger64Time) newTime;
	}

	public static RTIambassador getRtiAmbassador()
	{
		return rtiAmbassador;
	}

	public static EncoderFactory getEncoderFactory()
	{
		return encoderFactory;
	}

	public static HLAinteger64TimeFactory getLogicalTimeFactoryFactory()
	{
		return timeFactory;
	}

	public static boolean isInitialized()
	{
		return initialized;
	}

	public static Object getReservationSemaphore()
	{
		return reservationSemaphore;
	}

	public static void setInitialized(boolean status)
	{
		initialized = status;
	}
}
