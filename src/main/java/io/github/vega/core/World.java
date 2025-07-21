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

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.PooledEngine;

import io.github.vega.components.HLAObjectComponent;
import io.github.vega.utils.ProjectSettings;

public class World
{
	protected static final Logger LOGGER = LogManager.getLogger();
	protected static final Marker SIMUL_MARKER = MarkerManager.getMarker("SIMUL");

	private static PooledEngine engine;
	private static final ComponentMapper<HLAObjectComponent> OBJECT_COMPONENT_MAPPER = ComponentMapper.getFor(HLAObjectComponent.class);

	public static Entity createEntity()
	{
		return engine.createEntity();
	}

	public static void addEntity(Entity entity)
	{
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

	public static <T extends Component> boolean removeComponent(Entity entity, Class<T> componentType)
	{
		LOGGER.warn(SIMUL_MARKER, "Failed to remove the component type <{}> from the entity <{}>\n[REASON] The supplied component type is absent from the entity", componentType, entity);
		return false;
	}

	public static <T extends Component> T getComponent(Entity entity, Class<T> componentType)
	{
		ComponentMapper<T> mapper = ComponentMapper.getFor(componentType);
		return mapper.get(entity);
	}

	public static void addSystem(EntitySystem system)
	{
		engine.addSystem(system);
	}

	public static void removeSystem(EntitySystem system)
	{
		engine.removeSystem(system);
	}

	public static void update()
	{
		engine.update(1.0f);
	}

	protected static void setupEngine()
	{
		if (engine != null)
			return;

		engine = new PooledEngine(ProjectSettings.MIN_ENTITIES, ProjectSettings.MAX_ENTITIES, ProjectSettings.MIN_COMPONENTS, ProjectSettings.MAX_COMPONENTS);
	}
}
