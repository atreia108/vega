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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
import io.github.atreia108.vega.utils.VegaUtilities;

/**
 * The <code>HLAObjectManager</code> enables the creation, update, and
 * destruction of instances of an HLA object class. In Vega, an ECS entity
 * corresponds to an HLA object instance. The
 * {@link io.github.atreia108.vega.core.IDataConverter IDataConverter},
 * {@link io.github.atreia108.vega.core.IMultiDataConverter IMultiDataConverter}
 * and {@link io.github.atreia108.vega.core.IEntityArchetype IEntityArchetype}
 * interfaces form part of a conversion layer that translates between the HLA's
 * object-oriented design and data-oriented design.
 * 
 * @author Hridyanshu Aatreya
 * @since 1.0.0
 */
public final class HLAObjectManager
{
	private static final Logger LOGGER = LogManager.getLogger();

	private static final ComponentMapper<HLAObjectComponent> objectMapper = VegaUtilities.objectComponentMapper();

	private static final Set<Entity> remoteEntitySet = new HashSet<Entity>();
	private static final Set<Entity> localEntitySet = new HashSet<Entity>();

	private static final Map<ObjectInstanceHandle, String> entityMap = new HashMap<ObjectInstanceHandle, String>();
	private static final Map<String, ObjectInstanceHandle> inverseEntityMap = new HashMap<String, ObjectInstanceHandle>();

	public static int registeredInstancesCount = 0;

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
				put(instanceHandle, objectComponent.instanceName);

				localEntitySet.add(entity);
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
	public static boolean sendInstanceUpdate(Entity entity)
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
			LOGGER.warn("Update attempt for the object instance \"{}\" aborted: The supplied entity ({}) is read-only as it is owned by another federate.", objectComponent.instanceName, entity);
			return false;
		}

		if (!has(objectComponent.instanceName))
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

		ObjectInstanceHandle instanceHandle = translate(objectComponent.instanceName);
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

		if (!has(objectComponent.instanceName))
		{
			LOGGER.warn("Deletion of object instance \"{}\" aborted: No handle exists for this object instance. It may have been deleted previously or never registered in the first place.", objectComponent.instanceName);
			return false;
		}

		RTIambassador rtiAmbassador = VegaUtilities.rtiAmbassador();
		try
		{
			ObjectInstanceHandle instanceHandle = translate(objectComponent.instanceName);
			rtiAmbassador.deleteObjectInstance(instanceHandle, null);

			// Cleanup entity data remains in the simulation.
			remove(instanceHandle);
		}
		catch (Exception e)
		{
			LOGGER.error("Deletion of the object instance \"{}\" failed: ", e);
			return false;
		}

		return true;
	}

	/**
	 * Verifies whether an entity is remote or not.
	 * 
	 * @param entity The entity
	 */
	public static boolean isRemoteEntity(Entity entity)
	{
		for (Entity e : remoteEntitySet)
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

	private static AttributeHandleValueMap getPublishableInstanceAttributes(Entity entity, ObjectClassProfile objectClass, RTIambassador rtiAmbassador)
	{
		HLAObjectComponent objectComponent = VegaUtilities.objectComponentMapper().get(entity);

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

				byte[] encodedValue = null;

				if (objectClass.attributeUsesMultiConverter(attributeName))
				{
					String multiDataConverterName = objectClass.getAttributeMultiConverterName(attributeName);
					IMultiDataConverter multiDataConverter = ProjectRegistry.getMultiConverter(multiDataConverterName);
					int trigger = objectClass.getAttributeConverterTrigger(attributeName, multiDataConverterName);
					encodedValue = multiDataConverter.encode(entity, encoderFactory, trigger);
				}
				else
				{
					String dataConverterName = objectClass.getAttributeConverterName(attributeName);
					IDataConverter dataConverter = ProjectRegistry.getDataConverter(dataConverterName);
					encodedValue = dataConverter.encode(entity, encoderFactory);
				}

				// Cause for concern - it would very impolite of us to send NULL data for a
				// field to the RTI!
				if (encodedValue == null)
				{
					LOGGER.warn("Aborted attempt to send updated values for the object instance \"{}\"  of class \"{}\": NULL encoded data detected for one of its fields", objectComponent.instanceName, objectComponent.className);
					throw new Exception();
				}

				attributeValues.put(attributeHandle, encodedValue);
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Failed while trying to pack object instance attribute values: ", e);
		}

		return attributeValues;
	}

	protected static void addRemoteEntity(Entity entity)
	{
		remoteEntitySet.add(entity);
	}

	/**
	 * Find the corresponding entity of a remote object instance (owned by another
	 * federate).
	 * 
	 * @param instanceName Name of the object instance.
	 */
	public static Entity getRemoteEntity(String instanceName)
	{
		for (Entity e : remoteEntitySet)
		{
			HLAObjectComponent objectComponent = objectMapper.get(e);
			if (objectComponent.instanceName.equals(instanceName))
				return e;
		}

		return null;
	}

	/**
	 * Returns a set containing copies of all remote entities available to the
	 * federate.
	 */
	public static Set<Entity> getAllRemoteEntities()
	{
		Set<Entity> result = new HashSet<Entity>();
		remoteEntitySet.forEach((entity) -> result.add(entity));

		return result;
	}

	/**
	 * Finds and returns a local entity (an object instance originating from this
	 * federate) if it exists.
	 * 
	 * @param instanceName The name of the object instance represented by the entity
	 */
	public static Entity getLocalEntity(String instanceName)
	{
		for (Entity e : localEntitySet)
		{
			HLAObjectComponent objectComponent = objectMapper.get(e);
			if (objectComponent.instanceName.equals(instanceName))
				return e;
		}

		return null;
	}

	protected static void destroyRemoteEntity(String instanceName)
	{
		Entity entity = getRemoteEntity(instanceName);
		entity.removeAll();
		remoteEntitySet.remove(entity);

		ObjectInstanceHandle entityHandle = translate(instanceName);
		remove(entityHandle);
	}

	protected static void put(ObjectInstanceHandle handle, String instanceName)
	{
		entityMap.put(handle, instanceName);
		inverseEntityMap.put(instanceName, handle);
	}

	protected static boolean has(ObjectInstanceHandle handle)
	{
		return entityMap.containsKey(handle);
	}

	protected static boolean has(String instanceName)
	{
		return inverseEntityMap.containsKey(instanceName);
	}

	protected static String translate(ObjectInstanceHandle handle)
	{
		return entityMap.get(handle);
	}

	protected static ObjectInstanceHandle translate(String instanceName)
	{
		return inverseEntityMap.get(instanceName);
	}

	protected static void remove(ObjectInstanceHandle handle)
	{
		inverseEntityMap.remove(entityMap.get(handle));
		entityMap.remove(handle);
	}
}
