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

import com.badlogic.ashley.core.Entity;

import io.github.vega.configuration.ConfigurationLoader;
import io.github.vega.hla.HlaInteractionType;
import io.github.vega.hla.HlaManager;
import io.github.vega.hla.HlaObjectType;
import io.github.vega.spacefom.components.ExCoComponent;

public abstract class SimulationBase
{
	private static final Logger logger = LoggerFactory.getLogger(SimulationBase.class);
	
	private static Entity exCo;
	
	public SimulationBase(String configFileDirectory)
	{
		new ConfigurationLoader(configFileDirectory);
	}
	
	protected abstract void init();
	
	protected void exec()
	{
		HlaManager.connect();
		startUp();
		process();
		// HlaManager.disconnect();
	}
	
	protected void startUp()
	{
		final int NUMBER_OF_INITIALIZATION_STEPS = 7;
		
		subscribeExCo(1, NUMBER_OF_INITIALIZATION_STEPS);
		
		// .. MTR interaction-related content pending.
		publishMtr(2, NUMBER_OF_INITIALIZATION_STEPS);
		
		discoverExCo(3, NUMBER_OF_INITIALIZATION_STEPS);
		updateExCo(4, NUMBER_OF_INITIALIZATION_STEPS);
		
		// A temporary measure that prevents the initialized check in discoverObjectInstance and reflectAttributeValues
		//  from setting off the latch. Likely to happen as we start discovering objects after pub/sub to RTI.
		HlaManager.setInitialized(true);
		
		pubSubFederateClasses(5, NUMBER_OF_INITIALIZATION_STEPS);
		
		logger.info("({}/{}) Initializing simulation entities.", 6, NUMBER_OF_INITIALIZATION_STEPS);
		init();
		logger.info("All simulation entities were successfully initialized.");
		
		// Enable again when fixing the object discovery bug.
		//HlaManager.setInitialized(false);
		
		setUpTimeManagement(7, NUMBER_OF_INITIALIZATION_STEPS);
		// ...
	}
	
	protected void subscribeExCo(int stepNumber, int totalSteps)
	{
		logger.info("({}/{}) Subscribing to the Execution Configuration [ExCO] object class.", stepNumber, totalSteps);
		HlaObjectType exCoClassType = Registry.getObjectType("HLAobjectRoot.ExecutionConfiguration");
		HlaManager.subscribeObjectAttributes(exCoClassType);
		
		// Manually update its intent declaration to prevent it from published/subscribed again by HlaManager.
		exCoClassType.intentDeclared();
		logger.info("Subscribed to the ExCO object class.");
	}
	
	protected void publishMtr(int stepNumber, int totalSteps)
	{
		logger.info("({}/{}) Publishing the ModeTransitionRequest [MTR] interaction class.", stepNumber, totalSteps);
		HlaInteractionType mtrClassType = Registry.getInteractionType("HLAinteractionRoot.ModeTransitionRequest");
		HlaManager.publishInteraction(mtrClassType);
		
		// Manually update its intent declaration to prevent it from published/subscribed again by HlaManager.
		mtrClassType.intentDeclared();
		logger.info("Published the MTR interaction class.");
	}
	
	protected void discoverExCo(int stepNumber, int totalSteps)
	{
		logger.info("({}/{}) Waiting to discover the ExCO object instance from the RTI.", stepNumber, totalSteps);
		ThreadLatch.start();
		logger.info("The ExCO object was discovered.");	
	}
	
	protected void updateExCo(int stepNumber, int totalSteps)
	{
		logger.info("({}/{}) Waiting to receive the latest values of the ExCO object instance from the RTI.", stepNumber, totalSteps);
		ThreadLatch.start();
		logger.info("Latest updates for the ExCO object instance have been received from the RTI.");
	}
	
	protected void pubSubFederateClasses(int stepNumber, int totalSteps)
	{
		logger.info("({}/{}) Declaring all objects and interactions to the RTI.", stepNumber, totalSteps);
		HlaManager.declareAllObjects();
		HlaManager.declareAllInteractions();
		logger.info("All objects and interactions have been declared to the RTI.");
	}
	
	protected void setUpTimeManagement(int stepNumber, int totalSteps)
	{
		logger.info("({}/{}) Aligning simulation timeline with the HLA federation.", stepNumber, totalSteps);
		HlaManager.enableHlaTimeConstrained();
		ThreadLatch.start();
		
		ExCoComponent exCoComponent = exCo.getComponent(ExCoComponent.class);
		long leastCommonTimeStep = exCoComponent.leastCommonTimeStep;
		HlaManager.enableHlaTimeRegulation(leastCommonTimeStep);
		ThreadLatch.start();
		
		HlaManager.advanceTime(HlaManager.logicalTimeBoundary(leastCommonTimeStep));
		ThreadLatch.start();
		logger.info("Simulation timeline is now synchronized with the HLA federation.");
		
		// Update our ECS world just once because we've already been given a grant.
		World.update();
	}
	
	protected void process()
	{
		logger.info("The simulation is now running...");
		
		while (true)
		{
			HlaManager.advanceTime(HlaManager.nextTimeStep());
			ThreadLatch.start();
			World.update();
		}
	}
	
	public static void setExCo(Entity entity) { exCo = entity; }
	
	public static Entity getExCo() { return exCo; }
}
