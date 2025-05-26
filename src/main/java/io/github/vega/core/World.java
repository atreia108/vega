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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.PooledEngine;

import io.github.vega.configuration.Configuration;
import io.github.vega.hla.HlaManager;
import io.github.vega.hla.HlaObjectComponent;

public class World
{
	private static final Logger logger = LoggerFactory.getLogger(World.class);

	private static PooledEngine engine;

	public static Entity createEntity()
	{
		return engine.createEntity();
	}

	public static void addEntity(Entity entity)
	{
		ComponentMapper<HlaObjectComponent> mapper = ComponentMapper.getFor(HlaObjectComponent.class);
		HlaObjectComponent objectComponent = mapper.get(entity);
		
		if (isHlaObject(entity, objectComponent))
			registerAsHlaObject(entity, objectComponent);
		else
			engine.addEntity(entity);
	}
	
	private static boolean isHlaObject(Entity entity, HlaObjectComponent objectComponent)
	{
		if (objectComponent == null)
		{
			return false;
		}
		
		String typeName = objectComponent.className;
		
		if (Registry.getObjectType(typeName) == null)
		{
			logger.warn(
					"An HLA object instance for the entity {} was not registered because its HlaObjectComponent specifies a class name that is either invalid or was not published/subscribed at runtime. Its associated HlaObjectComponent has been removed to prevent undefined behavior.",
					entity);
			removeComponent(entity, HlaObjectComponent.class);
			return false;
		}
		return true;
	}
	
	private static void registerAsHlaObject(Entity entity, HlaObjectComponent component)
	{
		String typeName = component.className;
		String instanceName = component.instanceName;
		
		// TODO - Add reservation methods into HlaManager and connect them here.
		boolean registration = HlaManager.registerObjectInstance(entity, typeName, instanceName);
		
		if (!registration)
		{
			removeComponent(entity, HlaObjectComponent.class);
		}
		
		engine.addEntity(entity);
	}

	public static <T extends Component> T createComponent(Class<T> componentType)
	{
		return engine.createComponent(componentType);
	}

	public static <T extends Component> boolean addComponent(Entity entity, T component)
	{
		entity.add(component);

		if (getComponent(entity, component.getClass()) == null)
			return false;
		else
			return true;
	}

	public static <T extends Component> T getComponent(Entity entity, Class<T> componentType)
	{
		ComponentMapper<T> mapper = ComponentMapper.getFor(componentType);
		return mapper.get(entity);
	}
	
	public static <T extends Component> boolean removeComponent(Entity entity, Class<T> componentType)
	{
		entity.remove(componentType);
		
		if (getComponent(entity, componentType) == null)
			return true;
		else
			return false;
	}
	
	public static void addSystem(EntitySystem system)
	{
		engine.addSystem(system);
	}
	
	public static void destroySystem(EntitySystem system)
	{
		engine.removeSystem(system);
	}

	public static void init()
	{
		if (engine != null)
			return;
		engine = new PooledEngine(Configuration.getMinSimulatedEntities(), Configuration.getMaxSimulatedEntities(),
				Configuration.getMinComponents(), Configuration.getMaxComponents());
	}

	public static void update()
	{
		engine.update(1.0f);
	}
}
