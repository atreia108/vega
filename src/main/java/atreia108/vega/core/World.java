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

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;

import atreia108.vega.types.IComponent;
import atreia108.vega.types.IRemoteEntityFactory;
import hla.rti1516e.ObjectInstanceHandle;

public class World {
	private PooledEngine engine;
	
	private HlaTaskProcessor processor;
	
	public World(HlaFederateAmbassador federateAmbassador) {
		processor = federateAmbassador.getHlaProcessor();
		engine = new PooledEngine();
	}
	
	public Entity createEntity() {
		return engine.createEntity();
	}
	
	public void destroyEntity(Entity entity) {
		engine.removeEntity(entity);
	}
	
	// TODO
	public boolean registerEntityAsManaged(Entity entity) {
		return false;
	}
	
	// TODO
	public boolean deregisterManagedEntity(Entity entity) {
		return false;
	}
	
	public void registerRemoteEntity(String objectClassName, IRemoteEntityFactory creationPattern) {
		processor.registerRemoteEntity(objectClassName, creationPattern);
	}
	
	public <T extends IComponent> T createComponent(Class<T> componentType) {
		T component = engine.createComponent(componentType);
		return component;
	}
	
	public boolean removeComponent(Entity entity, Class<? extends IComponent> componentType) {
		if (getComponent(entity, componentType) != null) {
			entity.remove(componentType);
			return true;
		} else {
			return false;
		}
	}
	
	public IComponent getComponent(Entity entity, Class <? extends IComponent> componentType) {
		IComponent component = entity.getComponent(componentType);
		return component;
	}
	
	public boolean addComponent(Entity entity, Class <? extends IComponent> componentType) {
		IComponent component = engine.createComponent(componentType);
		entity.add(component);
		if (getComponent(entity, componentType) != null)
			return true;
		else 
			return false;
	}
	
	public PooledEngine getEngine() { return engine; }
}
