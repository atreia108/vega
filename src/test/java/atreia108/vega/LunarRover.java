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

package atreia108.vega;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.systems.IteratingSystem;

import atreia108.vega.core.SimulationBase;
import atreia108.vega.spacefom.SpaceFomFederateAmbassador;
import atreia108.vega.spacefom.components.CenterOfMassComponent;
import atreia108.vega.spacefom.components.NameComponent;
import atreia108.vega.spacefom.components.ReferenceFrameComponent;
import atreia108.vega.spacefom.components.SpaceTimeCoordinateStateComponent;

public class LunarRover extends SimulationBase
{
	private SpaceFomFederateAmbassador federateAmbassador;
	
	private Entity lunarRover;
	
	public LunarRover()
	{
		federateAmbassador = new SpaceFomFederateAmbassador(this);
		federateAmbassador.initialize();
	}
	
	public void initialize()
	{
		lunarRover = world.createEntity("LunarRover", "HLAobjectRoot.PhysicalEntity");
		world.addComponent(lunarRover, NameComponent.class);
		world.addComponent(lunarRover, ReferenceFrameComponent.class);
		world.addComponent(lunarRover, SpaceTimeCoordinateStateComponent.class);
		world.addComponent(lunarRover, CenterOfMassComponent.class);
		
		NameComponent entityName = world.getComponent(lunarRover, NameComponent.class);
		entityName.name = "LunarRover";
		
		ReferenceFrameComponent parentReferenceFrame = world.getComponent(lunarRover, ReferenceFrameComponent.class);
		parentReferenceFrame.frameName = "SeeLunarSouthPoleBaseLocalFixed";
		
		IteratingSystem movementSystem = new MovementSystem(world);
		world.addSystem(movementSystem);
	}
	
	public static void main(String[] args)
	{
		new LunarRover();
	}
}
