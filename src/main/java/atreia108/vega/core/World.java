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

package atreia108.vega.core;

import java.util.Set;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.RTIambassador;

public class World
{
	protected String name;
	protected PooledEngine worldEngine;
	
	protected Object entityReserverSemaphore;
	protected boolean entityReservationStatus;
	
	private RTIambassador rtiAmbassador;
	private BidiMap<String, ObjectClassHandle> entityClassHandles;
	private BidiMap<Entity, ObjectInstanceHandle> entityObjectInstances;
	private BidiMap<String, Entity> entityNames;
	private Set<EntityClass> entityClasses;
	
	public World(SimulationBase simulation)
	{
		entityReserverSemaphore = new Object();
		worldEngine = new PooledEngine();
		entityClassHandles = new DualHashBidiMap<String, ObjectClassHandle>();
		entityObjectInstances = new DualHashBidiMap<Entity, ObjectInstanceHandle>();
		entityNames = new DualHashBidiMap<String, Entity>();
		
		ConfigurationParser parser = simulation.getParser();
		rtiAmbassador = simulation.getRtiAmbassador();
		name = parser.getRtiParameters().get("FederateType");
		entityClasses = parser.getObjectClasses();
	}
	
	public <T extends IComponent> boolean addComponent(Entity entity, Class<T> componentType)
	{
		IComponent component = worldEngine.createComponent(componentType);
		entity.add(component);
		if (getComponent(entity, componentType) != null)
			return true;
		
		return false;
	}
	
	public void addObjectClassHandle(String objectClassName, ObjectClassHandle objectClassHandle)
	{
		entityClassHandles.put(objectClassName, objectClassHandle);
	}
	
	public Entity createEntity(String entityName, String entityClassName)
	{
		ObjectInstanceHandle entityHandle = registerObjectInstance(entityName, entityClassName);
		Entity entity = null;
		if (entityHandle != null)
		{
			entity = worldEngine.createEntity();
			entityObjectInstances.put(entity, entityHandle);
			entityNames.put(entityName, entity);
			worldEngine.addEntity(entity);
		}
		
		return entity;
	}
	
	public void createEntityClasses()
	{
		for (EntityClass entityClass : entityClasses)
		{
			try
			{
				String className = entityClass.getName();
				ObjectClassHandle classHandle = rtiAmbassador.getObjectClassHandle(className);
				AttributeHandleSet publishableSetHandle = rtiAmbassador.getAttributeHandleSetFactory().create();
				AttributeHandleSet subscribeableSetHandle = rtiAmbassador.getAttributeHandleSetFactory().create();
				Set<String> publishableAttributes = entityClass.getPublishedAttributes();
				Set<String> subscribeableAttributes = entityClass.getSubscribedAttributes();
				
				publishableAttributes.forEach((String attribute) -> {
					try
					{
						AttributeHandle attributeHandle = rtiAmbassador.getAttributeHandle(classHandle, attribute);
						publishableSetHandle.add(attributeHandle);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				});
				
				subscribeableAttributes.forEach((String attribute) ->
				{
					try
					{
						AttributeHandle attributeHandle = rtiAmbassador.getAttributeHandle(classHandle, attribute);
						subscribeableSetHandle.add(attributeHandle);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				});
				
				if (!publishableSetHandle.isEmpty())
					rtiAmbassador.publishObjectClassAttributes(classHandle, publishableSetHandle);
				
				if (!subscribeableSetHandle.isEmpty())
					rtiAmbassador.subscribeObjectClassAttributes(classHandle, subscribeableSetHandle);
				
				addObjectClassHandle(className, classHandle);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public Entity createRemoteEntity(String entityName, ObjectInstanceHandle entityHandle)
	{
		Entity remoteEntity = worldEngine.createEntity();
		entityObjectInstances.put(remoteEntity, entityHandle);
		entityNames.put(entityName, remoteEntity);
		return remoteEntity;
	}
	
	public void destroyEntity(Entity entity)
	{
		worldEngine.removeEntity(entity);
	}
	
	public <T extends IComponent> IComponent getComponent(Entity entity, Class<T> componentType)
	{
		IComponent component = entity.getComponent(componentType);
		return component;
	}
	
	public BidiMap<String, Entity> getEntityNames() { return entityNames; }
	
	public Set<EntityClass> getEntityClasses() { return entityClasses; }
	
	public BidiMap<String, ObjectClassHandle> getEntityClassHandles() { return entityClassHandles; }
	
	public String getName(Entity entity)
	{
		return entityNames.getKey(entity);
	}
	
	// TODO
	public void update()
	{
		worldEngine.update(1.0f);
	}
	
	public void notifyEntityNameReservationStatus(boolean status)
	{
		entityReservationStatus = status;
		synchronized (entityReserverSemaphore)
		{
			entityReserverSemaphore.notify();
		}
	}
	
	private ObjectInstanceHandle registerObjectInstance(String entityName, String entityClassName)
	{
		ObjectClassHandle classHandle = entityClassHandles.get(entityClassName);
		ObjectInstanceHandle instanceHandle = null;
		
		try
		{
			synchronized (entityReserverSemaphore)
			{
				rtiAmbassador.reserveObjectInstanceName(entityName);
				awaitEntityReservation();
			}
			
			if (!entityReservationStatus)
			{
				System.out.println("[INFO] Failed to register entity with name <" + entityName + ">");
			}
			
			instanceHandle = rtiAmbassador.registerObjectInstance(classHandle, entityName);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return instanceHandle;
	}
	
	private void awaitEntityReservation()
	{
		try
		{
			synchronized (entityReserverSemaphore) 
			{
				entityReserverSemaphore.wait();
			}
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
	public void setEntityClasses(Set<EntityClass> entityClasses)
	{
		this.entityClasses = entityClasses;
	}
}
