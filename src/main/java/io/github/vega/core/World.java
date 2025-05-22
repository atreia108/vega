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

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.PooledEngine;

public class World
{
	private static PooledEngine engine;
	
	public static <T extends Component> boolean addComponent(Entity entity, T component)
	{
		entity.add(component);
		
		// TODO - add condition for HLAObjectComponent
		
		if (getComponent(entity, component.getClass()) == null)
			return false;
		
		return true;
	}
	
	public static void addEntity(Entity entity)
	{
		engine.addEntity(entity);
	}
	
	public static void addSystem(EntitySystem system)
	{
		engine.addSystem(system);
	}
	
	public static <T extends Component> T createComponent(Class<T> componentType)
	{
		return engine.createComponent(componentType);
	}
	
	public static Entity createEntity()
	{
		return engine.createEntity();
	}
	
	public static void destroyEntity(Entity entity)
	{
		// TODO - add condition for HLAObjectComponent
		engine.removeEntity(entity);
	}
	
	public static void destroySystem(EntitySystem system)
	{
		engine.removeSystem(system);
	}
	
	public static void enableEngine(int minEntities, int maxEntities, int minComponents, int maxComponents)
	{
		engine = new PooledEngine(minEntities, maxEntities, minComponents, maxComponents);
	}
	
	public static <T extends Component> T getComponent(Entity entity, Class<T> componentType)
	{
		ComponentMapper<T> mapper = ComponentMapper.getFor(componentType);
		return mapper.get(entity);
	}
	
	public static PooledEngine getEngine()
	{
		return engine;
	}
	
	// TODO - Implement the method and don't forget about the HLAObjectComponent condition!
	public static <T extends Component> void removeComponent(Class<T> componentType)
	{
		
	}
}

