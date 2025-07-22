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

package io.github.vega.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.encoding.EncoderFactory;
import io.github.vega.components.HLAInteractionComponent;
import io.github.vega.components.HLAObjectComponent;
import io.github.vega.utils.ProjectRegistry;

public class VegaDataManager
{
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Marker SIMUL_MARKER = MarkerManager.getMarker("SIMUL");
	private static final Marker HLA_MARKER = MarkerManager.getMarker("HLA");

	private static final ComponentMapper<HLAObjectComponent> OBJECT_COMPONENT_MAPPER = ComponentMapper.getFor(HLAObjectComponent.class);
	private static final ComponentMapper<HLAInteractionComponent> INTERACTION_COMPONENT_MAPPER = ComponentMapper.getFor(HLAInteractionComponent.class);

	private static int registeredInstancesCount = 0;

	public static boolean registerObjectInstance(Entity entity)
	{
		HLAObjectComponent objectComponent = OBJECT_COMPONENT_MAPPER.get(entity);

		if (ProjectRegistry.isRemoteEntity(entity))
		{
			LOGGER.warn(SIMUL_MARKER, "Omitted registration for the entity <{}>\n[REASON] An object instance for this entity already exists as a REMOTE entity", objectComponent.instanceName);
			return false;
		}

		if (objectComponent.instanceHandle != null)
		{
			LOGGER.warn("Omitted registration for the entity <{}>\n[REASON] It has already been registered as an object instance", objectComponent.instanceName);
			return false;
		}

		Object nameReservationSemaphore = VegaCallbackManager.getNameReservationSemaphore();
		RTIambassador rtiAmbassador = VegaRTIAmbassador.instance();

		try
		{
			synchronized (nameReservationSemaphore)
			{
				rtiAmbassador.reserveObjectInstanceName(objectComponent.instanceName);
				awaitReservation();
			}

			boolean nameReservationStatus = VegaCallbackManager.getNameReservationStatus();
			if (!nameReservationStatus)
			{
				LOGGER.warn("Failed to reserve the name <{}>. The corresponding HLA object instance for this entity was not created", objectComponent.instanceName);
				return false;
			}

			VegaObjectClass objectClass = ProjectRegistry.getObjectClass(objectComponent.className);
			ObjectClassHandle classHandle = objectClass.classHandle;

			ObjectInstanceHandle instanceHandle = rtiAmbassador.registerObjectInstance(classHandle, objectComponent.instanceName);
			objectComponent.instanceHandle = instanceHandle;

			LOGGER.info(HLA_MARKER, "Created the HLA object instance \"{}\" of the class <{}>", objectComponent.instanceName, objectComponent.className);
			registeredInstancesCount += 1;
		}
		catch (Exception e)
		{
			LOGGER.warn(HLA_MARKER, "Failed to reserve the name <{}>. The corresponding HLA object instance for this entity was not created\n[REASON]", objectComponent.instanceName, e);
			return false;
		}

		return false;
	}

	private static void awaitReservation()
	{
		Object nameReservationSemaphore = VegaCallbackManager.getNameReservationSemaphore();

		try
		{
			synchronized (nameReservationSemaphore)
			{
				nameReservationSemaphore.wait();
			}
		}
		catch (InterruptedException e)
		{
			LOGGER.error(SIMUL_MARKER, "Unexpected interruption while waiting for the reservation of an object instance's name\n[REASON]", e);
			System.exit(1);
		}
	}

	public static boolean destroyObjectInstance(Entity entity)
	{
		HLAObjectComponent objectComponent = null;

		if (ProjectRegistry.isRemoteEntity(entity))
		{
			LOGGER.warn(SIMUL_MARKER, "Failed to destroy object instance for the entity <{}>\n[REASON] This is a remote entity and cannot be destroyed since the federate does not have the required privileges to do so", entity);
			return false;
		}
		else if ((objectComponent = OBJECT_COMPONENT_MAPPER.get(entity)) == null)
		{
			LOGGER.warn(SIMUL_MARKER, "Failed to destroy object instance for the entity <{}>\n[REASON] Its lacks an HLAObjectComponent and may not be a registered HLA object instance", entity);
			return false;
		}
		else if (objectComponent.instanceHandle == null)
		{
			LOGGER.warn(SIMUL_MARKER, "Failed to destroy object instance for the entity <{}>\n[REASON] This entity is missing its associated object instance");
			return false;
		}

		RTIambassador rtiAmbassador = VegaRTIAmbassador.instance();
		try
		{
			rtiAmbassador.deleteObjectInstance(objectComponent.instanceHandle, null);
		}
		catch (Exception e)
		{
			LOGGER.warn(HLA_MARKER, "Failed to destroy object instance for the entity <{}>\n[REASON]", e);
			return false;
		}
		
		--registeredInstancesCount;
		return true;
	}

	public static boolean sendObjectInstanceUpdate(Entity entity)
	{
		HLAObjectComponent objectComponent = null;
		VegaObjectClass objectClass = null;
		
		if (ProjectRegistry.isRemoteEntity(entity)) 
		{
			LOGGER.warn("Failed to send update for the entity <{}>\n[REASON] This is a remote entity and cannot be updated by the federate as it does not have the required privileges to do so", entity);
			return false;
		}
		else if ((objectComponent = OBJECT_COMPONENT_MAPPER.get(entity)) == null)
		{
			LOGGER.warn("Failed to send update for the entity <{}>\n[REASON] It lacks an HLAObjectComponent and may not be a registered HLA object instance", entity);
			return false;
		}
		else if ((objectClass = ProjectRegistry.getObjectClass(objectComponent.className)) == null)
		{
			LOGGER.warn("Failed to send update for the entity <{}>\n[REASON] The HLA object class of this entity \"{}\" is unrecognized", objectComponent.className);
			return false;
		}
		else if (objectComponent.instanceHandle == null)
		{
			LOGGER.warn("Failed to send update for the entity <{}>\n[REASON] The object instance handle for this entity is NULL and therefore unknown");
			return false;
		}
		
		RTIambassador rtiAmbassador = VegaRTIAmbassador.instance();
		try
		{
			AttributeHandleValueMap instanceAttributes = getObjectInstanceAttributes(entity, objectClass);
			
			if ((instanceAttributes == null) || (instanceAttributes.size() == 0))
			{
				LOGGER.warn("Failed to send update for the entity <{}>\n[REASON] No object instance attributes were found for this entity", entity);
				return false;
			}
			else
			{
				rtiAmbassador.updateAttributeValues(objectComponent.instanceHandle, instanceAttributes, null);
				LOGGER.info("An update was sent for the entity <{}>", entity);
				return true;
			}
		}
		catch (Exception e) 
		{
			LOGGER.warn("Failed to send update for the entity <{}>\n[REASON]", e);
			return false;
		}
	}
	
	private static AttributeHandleValueMap getObjectInstanceAttributes(Entity entity, VegaObjectClass objectClass)
	{
		int numberOfAttributes = objectClass.getNumberOfPublisheableAttributes();
		
		if (numberOfAttributes == 0)
			return null;
		
		RTIambassador rtiAmbassador = VegaRTIAmbassador.instance();
		EncoderFactory encoderFactory = VegaEncoderFactory.instance();
		AttributeHandleValueMap attributeValues = null;
		try
		{
			attributeValues = rtiAmbassador.getAttributeHandleValueMapFactory().create(numberOfAttributes);
			AttributeHandleSet attributeHandles = objectClass.getPublisheableAttributeHandles();
			
			for (AttributeHandle attributeHandle : attributeHandles)
			{
				String attributeName = objectClass.getAttributeNameForHandle(attributeHandle);
				String dataConverterName = objectClass.getAttributeConverterName(attributeName);
				byte[] encodedValue = null;
				
				if (objectClass.attributeUsesMultiConverter(attributeName))
				{
					IMultiDataConverter multiDataConverter = ProjectRegistry.getMultiConverter(dataConverterName);
					int trigger = objectClass.getAttributeConverterTrigger(attributeName, dataConverterName);
					encodedValue = multiDataConverter.encode(entity, encoderFactory, trigger);
				}
				else
				{
					IDataConverter dataConverter = ProjectRegistry.getDataConverter(dataConverterName);
					encodedValue = dataConverter.encode(entity, encoderFactory);
				}
				
				attributeValues.put(attributeHandle, encodedValue);
			}
		}
		catch (Exception e)
		{
			LOGGER.error("RTI ambassador failed to provide a AttributeHandleValueMap for packing object instance data\n[REASON]", e);
		}
		
		return attributeValues;
	}

	public static boolean sendInteraction(Entity entity)
	{
		return false;
	}

	public static int getRegisteredInstancesCount()
	{
		return registeredInstancesCount;
	}
}
