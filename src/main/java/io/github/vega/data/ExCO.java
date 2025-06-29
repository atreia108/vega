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

package io.github.vega.data;

import com.badlogic.ashley.core.Entity;

import io.github.vega.archetypes.ExecutionConfiguration;
import io.github.vega.components.ExCOComponent;
import io.github.vega.core.World;

public class ExCO
{
	private static Entity exCO;
	private static ExCOComponent component;
	
	private ExCO()
	{
		ExecutionConfiguration exCOArchetype = new ExecutionConfiguration();
		exCO = exCOArchetype.assemble();
		component = World.getComponent(exCO, ExCOComponent.class);
	}
	
	public static Entity instance()
	{
		if (exCO == null)
			new ExCO();
		
		return exCO;
	}
	
	public static String getRootFrameName()
	{
		return component.rootFrameName;
	}
	
	public static ExecutionMode getCurrentExecutionMode()
	{
		return component.currentExecutionMode;
	}
	
	public static ExecutionMode getNextExecutionMode()
	{
		return component.nextExecutionMode;
	}
	
	public static long getLeastCommonTimeStep()
	{
		return component.leastCommonTimeStep;
	}
}
