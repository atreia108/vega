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

import java.lang.annotation.ElementType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.ashley.core.Entity;

import io.github.vega.configuration.ConfigurationLoader;
import io.github.vega.hla.HlaManager;
import io.github.vega.hla.HlaObjectType;

public abstract class SimulationBase
{
	private static final Logger logger = LoggerFactory.getLogger(SimulationBase.class);
	
	private static Entity exCo;
	
	public SimulationBase(String configFileDirectory)
	{
		new ConfigurationLoader(configFileDirectory);
		exec();
	}
	
	protected void exec()
	{
		HlaManager.connect();
		startUp();
		
		while (true) {}
		// HlaManager.disconnect();
	}
	
	protected void startUp()
	{
		// ...
		HlaObjectType exCoClassType = EntityDatabase.getObjectType("HLAobjectRoot.ExecutionConfiguration");
		HlaManager.subscribeObject(exCoClassType);
		// .. MTR interaction-related content pending.
		
		logger.info("(1/X) Waiting to discover the ExCO object instance from the RTI.");
		ThreadLatch.start();
		logger.info("The ExCO object was discovered.");
		
		/*
		HlaObjectType object = EntityDatabase.getObjectType("HLAobjectRoot.ExecutionConfiguration");
		object.getAttributeNames().forEach(a -> { System.out.println(object.getAttributeHandle(a)); });
		*/
		
		logger.info("(2/X) Waiting to receive the latest values of the ExCO object instance from the RTI.");
		ThreadLatch.start();
		logger.info("Latest updates for the ExCO object instance have been received from the RTI.");
		
		logger.info("(3/X) Initializing simulation entities.");
		init();
		logger.info("All simulation entities were successfully initialized.");
		
		// ...
		// HlaManager.initialized();
	}
	
	protected abstract void init();
	
	public static void setExCo(Entity entity) { exCo = entity; }
}
