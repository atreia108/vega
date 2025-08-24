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

package io.github.atreia108.vega.core;

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
import io.github.atreia108.vega.components.HLAObjectComponent;
import io.github.atreia108.vega.utils.EntityMapping;
import io.github.atreia108.vega.utils.VegaUtilities;

/**
 * The <code>HLAObjectManager</code> enables the creation, update, and
 * destruction of instances of an HLA object class. In Vega, an ECS entity
 * corresponds to an HLA object instance. The
 * {@link io.github.atreia108.vega.core.IDataConverter IDataConverter} and
 * {@link io.github.atreia108.vega.core.IMultiDataConverter IMultiDataConverter}
 * {@link io.github.atreia108.vega.core.IEntityArchetype IEntityArchetype} interfaces form part
 * of a conversion layer to translate between the HLA's object-oriented design
 * and data-oriented design.
 * 
 * @author Hridyanshu Aatreya
 * @since 1.0.0
 */
public final class HLAObjectManager
{
	private static final Logger LOGGER = LogManager.getLogger();

	private static final ComponentMapper<HLAObjectComponent> objectMapper = VegaUtilities.objectComponentMapper();
	
	private static final Set<Entity> remoteEntities = new HashSet<Entity>();
	
	public static final EntityMapping entityMapping = new EntityMapping();
	
	public static int registeredInstancesCount = 0;

	/*
	public static boolean registerInstance(Entity entity)
	{
		HLAObjectComponent objectComponent = objectMapper.get(entity);

		if (ProjectRegistry.isRemoteEntity(entity))
		{
			LOGGER.warn("Omitted registration for the entity <{}>\n[REASON] An object instance for this entity already exists as a REMOTE entity", objectComponent.instanceName);
			return false;
		}

		if (objectComponent.instanceHandle != null)
		{
			LOGGER.warn("Omitted registration for the entity <{}>\n[REASON] It has already been registered as an object instance", objectComponent.instanceName);
			return false;
		}

		if (objectComponent.instanceName == null)
		{
			LOGGER.warn("Omitted registration for the entity <{}>\n[REASON] Got NULL instead of a valid instance name for this entity");
			return false;
		}

		if (objectComponent.className == null)
		{
			LOGGER.warn("Omitted registration for the entity <{}>\n[REASON] Got NULL instead of a valid HLA object class name for this entity");
		}

		Object nameReservationSemaphore = HLACallbackManager.getNameReservationSemaphore();
		RTIambassador rtiAmbassador = VegaUtilities.rtiAmbassador();

		try
		{
			synchronized (nameReservationSemaphore)
			{
				rtiAmbassador.reserveObjectInstanceName(objectComponent.instanceName);
				awaitReservation();
			}

			boolean nameReservationStatus = HLACallbackManager.getNameReservationStatus();
			if (!nameReservationStatus)
			{
				LOGGER.warn("Failed to reserve the name <{}>. The corresponding HLA object instance for this entity was not created", objectComponent.instanceName);
				return false;
			}

			ObjectClassProfile objectClass = ProjectRegistry.getObjectClass(objectComponent.className);
			ObjectClassHandle classHandle = objectClass.classHandle;

			ObjectInstanceHandle instanceHandle = rtiAmbassador.registerObjectInstance(classHandle, objectComponent.instanceName);
			objectComponent.instanceHandle = instanceHandle;

			LOGGER.info("Created the HLA object instance \"{}\" of the class <{}>", objectComponent.instanceName, objectComponent.className);
			registeredInstancesCount += 1;
		}
		catch (Exception e)
		{
			LOGGER.warn("Failed to reserve the name <{}>. The corresponding HLA object instance for this entity was not created\n[REASON]", objectComponent.instanceName, e);
			return false;
		}

		return false;
	}
	*/
	
	/**
	 * Registers an HLA object instance for a valid entity. A valid entity in this
	 * case:
	 * <ul>
	 * <li>Contains an HLAObjectComponent.
	 * <li>The className field in the component is set to an object class defined in
	 * the simulation's project file.
	 * <li>The instanceName field in the component contains a name (non-conflicting
	 * with other objects) for the instance.
	 * </ul>
	 * Invalid entities are rejected and the object is not registered.
	 * 
	 * @param entity entity representing the object instance.
	 * @return outcome of the operation as a true or false value.
	 */
	public static boolean registerInstance(Entity entity)
	{
		HLAObjectComponent objectComponent = objectMapper.get(entity);
		
		if (objectComponent == null || objectComponent.className == null || objectComponent.instanceName == null)
		{
			LOGGER.warn("Object instance registration aborted: <NullPointerException> The entity ({}) is potentially missing an HLAObjectComponent or one (or more) fields in the component is NULL.", entity);
			return false;
		}
		
		if (isRemoteEntity(entity))
		{
			LOGGER.warn("Registration of the object instance \"{}\" aborted: The supplied entity ({}) is already a registered entity that is owned by another federate.", objectComponent.instanceName, entity);
			return false;
		}
		
		Object nameReservationSemaphore = HLACallbackManager.getNameReservationSemaphore();
		RTIambassador rtiAmbassador = VegaUtilities.rtiAmbassador();
		
		synchronized (nameReservationSemaphore)
		{
			try
			{
				rtiAmbassador.reserveObjectInstanceName(objectComponent.instanceName);
				awaitReservation();
				
				boolean nameReservationStatus = HLACallbackManager.getNameReservationStatus();
				if (!nameReservationStatus)
				{
					LOGGER.error("Registration of an object instance with the name \"{}\" failed.", objectComponent.instanceName);
					return false;
				}
				
				ObjectClassProfile objectClass = ProjectRegistry.getObjectClass(objectComponent.className);
				
				if (objectClass == null)
				{
					LOGGER.warn("Registration of the object instance \"{}\" was aborted: The HLA object class \"{}\" does not match anything that was published/subscribed at runtime.", objectComponent.instanceName, objectComponent.className);
					return false;
				}
				
				if (objectClass.getNumberOfPublisheableAttributes() < 1)
				{
					LOGGER.warn("Registration of the object instance \"{}\" was aborted: The associated object class has no publishable attributes.");
					return false;
				}
				
				ObjectClassHandle classHandle = objectClass.classHandle;
				
				ObjectInstanceHandle instanceHandle = rtiAmbassador.registerObjectInstance(classHandle, objectComponent.instanceName);
				entityMapping.put(instanceHandle, objectComponent.instanceName);
			}
			catch (Exception e)
			{
				LOGGER.error("Object instance registration failed: {}", e);
				return false;
			}
		}
		
		LOGGER.info("A new object instance \"{}\" of the class \"{}\" was successfully registered.", objectComponent.instanceName, objectComponent.className, entity);
		registeredInstancesCount++;
		return true;
	}
	
	/**
	 * Updates the entity's corresponding object instance (if it exists) at the RTI
	 * with the latest values.
	 * 
	 * @param entity the entity to be updated.
	 * @return outcome of the operation as a true or false value.
	 */
	public static boolean updateInstance(Entity entity)
	{
		HLAObjectComponent objectComponent = objectMapper.get(entity);
		ObjectClassProfile objectClass = ProjectRegistry.getObjectClass(objectComponent.className);
		
		if (objectComponent == null || objectComponent.className == null || objectComponent.instanceName == null)
		{
			LOGGER.warn("Object instance registration aborted: <NullPointerException> The supplied entity ({}) is potentially missing an HLAObjectComponent or one (or more) fields in the component is NULL.", entity);
			return false;
		}
		
		if (isRemoteEntity(entity))
		{
			LOGGER.warn("Update attempt for the object instance \"{}\" aborted: The supplied entity ({}) is read-only as it is owned by another federate.");
			return false;
		}
		
		if (!entityMapping.hasEntity(objectComponent.instanceName))
		{
			LOGGER.warn("Update attempt for the object instance \"{}\" aborted: No handle exists for this object instance. It may have been deleted previously or never registered in the first place.", objectComponent.instanceName);
			return false;
		}
		
		if (objectClass == null)
		{
			LOGGER.warn("Update attempt for the object instance \"{}\" aborted: The object class \"{}\" associated with this instance was not found.", objectComponent.instanceName, objectComponent.className);
			return false;
		}
				
		RTIambassador rtiAmbassador = VegaUtilities.rtiAmbassador();
		AttributeHandleValueMap instanceAttributeValues = getPublishableInstanceAttributes(entity, objectClass, rtiAmbassador);
		
		if ((instanceAttributeValues == null) || (instanceAttributeValues.size() == 0))
		{
			LOGGER.warn("Update attempt for the object instance \"{}\" aborted: No publishable object instance attributes were found.", objectComponent.instanceName);
			return false;
		}
		
		ObjectInstanceHandle instanceHandle = entityMapping.translate(objectComponent.instanceName);
		try
		{
			rtiAmbassador.updateAttributeValues(instanceHandle, instanceAttributeValues, null);
		}
		catch (Exception e)
		{
			LOGGER.error("Update attempt for the object instance \"{}\" failed: ", e);
			return false;
		}
		
		return true;
	}
	
	/**
	 * Destroys the entity's corresponding object instance (if it exists) at the
	 * RTI.
	 * 
	 * @param entity entity representing the object instance.
	 * @return outcome of the operation as a true or false value.
	 */
	public static boolean destroyInstance(Entity entity)
	{
		HLAObjectComponent objectComponent = objectMapper.get(entity);
		
		if (objectComponent == null || objectComponent.className == null || objectComponent == null)
		{
			LOGGER.warn("Object instance deletion aborted: <NullPointerException> The supplied entity ({}) is potentially missing an HLAObjectComponent or one (or more) fields in the component is NULL.", entity);
			return false;
		}
		
		if (isRemoteEntity(entity))
		{
			LOGGER.warn("Deletion of object instance \"{}\" aborted: The supplied entity ({}) is read-only as it is owned by another federate.");
			return false;
		}
		
		if (!entityMapping.hasEntity(objectComponent.instanceName))
		{
			LOGGER.warn("Deletion of object instance \"{}\" aborted: No handle exists for this object instance. It may have been deleted previously or never registered in the first place.", objectComponent.instanceName);
			return false;
		}
		
		RTIambassador rtiAmbassador = VegaUtilities.rtiAmbassador();
		try
		{
			ObjectInstanceHandle instanceHandle = entityMapping.translate(objectComponent.instanceName);
			rtiAmbassador.deleteObjectInstance(instanceHandle, null);
			
			// Cleanup entity data remains in the simulation.
			entityMapping.remove(instanceHandle);
		}
		catch (Exception e)
		{
			LOGGER.error("Deletion of the object instance \"{}\" failed: ", e);
			return false;
		}
		
		return true;
	}
	
	public static boolean isRemoteEntity(Entity entity)
	{
		for (Entity e : remoteEntities)
			if (e == entity)
				return true;
		
		return false;
	}

	private static void awaitReservation()
	{
		Object nameReservationSemaphore = HLACallbackManager.getNameReservationSemaphore();

		try
		{
			synchronized (nameReservationSemaphore)
			{
				nameReservationSemaphore.wait();
			}
		}
		catch (InterruptedException e)
		{
			LOGGER.error("Unexpected interruption while waiting for the reservation of an object instance's name\n[REASON]", e);
			System.exit(1);
		}
	}

	/*
	public static boolean destroyInstance(Entity entity)
	{
		HLAObjectComponent objectComponent = null;

		if (ProjectRegistry.isRemoteEntity(entity))
		{
			LOGGER.warn("Failed to destroy object instance for the entity <{}>\n[REASON] This is a remote entity and cannot be destroyed since the federate does not have the required privileges to do so", entity);
			return false;
		}
		else if ((objectComponent = objectMapper.get(entity)) == null)
		{
			LOGGER.warn("Failed to destroy object instance for the entity <{}>\n[REASON] Its lacks an HLAObjectComponent and may not be a registered HLA object instance", entity);
			return false;
		}
		else if (objectComponent.instanceHandle == null)
		{
			LOGGER.warn("Failed to destroy object instance for the entity <{}>\n[REASON] This entity is missing its associated object instance");
			return false;
		}

		RTIambassador rtiAmbassador = VegaUtilities.rtiAmbassador();
		try
		{
			rtiAmbassador.deleteObjectInstance(objectComponent.instanceHandle, null);
		}
		catch (Exception e)
		{
			LOGGER.warn("Failed to destroy object instance for the entity <{}>\n[REASON]", e);
			return false;
		}

		--registeredInstancesCount;
		return true;
	}
	*/
	
	/*
	public static boolean updateInstance(Entity entity)
	{
		HLAObjectComponent objectComponent = null;
		ObjectClassProfile objectClass = null;

		if (ProjectRegistry.isRemoteEntity(entity))
		{
			LOGGER.warn("Failed to send update for the entity <{}>\n[REASON] This is a remote entity and cannot be updated by the federate as it does not have the required privileges to do so", entity);
			return false;
		}
		else if ((objectComponent = objectMapper.get(entity)) == null)
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

		try
		{
			RTIambassador rtiAmbassador = VegaUtilities.rtiAmbassador();
			AttributeHandleValueMap instanceAttributes = getObjectInstanceAttributes(entity, objectClass, rtiAmbassador);

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
	*/
	
	private static AttributeHandleValueMap getPublishableInstanceAttributes(Entity entity, ObjectClassProfile objectClass, RTIambassador rtiAmbassador)
	{
		AttributeHandleValueMap attributeValues = null;
		int numberOfAttributes = objectClass.getNumberOfPublisheableAttributes();

		if (numberOfAttributes < 1)
			return attributeValues;

		EncoderFactory encoderFactory = VegaUtilities.encoderFactory();
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
	
	protected static void addRemoteEntity(Entity remoteEntity)
	{
		remoteEntities.add(remoteEntity);
	}
	
	public static Entity getRemoteEntity(String name)
	{
		for (Entity e : remoteEntities)
		{
			var objectComponent = objectMapper.get(e);
			if (objectComponent.instanceName.equals(name))
				return e;
		}
		
		return null;
	}
	
	protected static void removeRemoteEntity(String name)
	{
		var remoteEntity = getRemoteEntity(name);
		remoteEntities.remove(remoteEntity);
		
		ObjectInstanceHandle entityHandle = entityMapping.translate(name);
		entityMapping.remove(entityHandle);
	}
}
