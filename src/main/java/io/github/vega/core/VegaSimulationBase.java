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

import hla.rti1516e.CallbackModel;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.ResignAction;
import io.github.vega.archetypes.ExecutionConfiguration;
import io.github.vega.archetypes.ModeTransitionRequest;
import io.github.vega.components.ExCOComponent;
import io.github.vega.converters.ExCOConverter;
import io.github.vega.converters.MTRConverter;
import io.github.vega.data.ExCO;
import io.github.vega.data.ExecutionMode;
import io.github.vega.hla.HLASharingModel;
import io.github.vega.hla.VegaDataManager;
import io.github.vega.hla.VegaFederateAmbassador;
import io.github.vega.hla.VegaInteractionClass;
import io.github.vega.hla.VegaObjectClass;
import io.github.vega.hla.VegaRTIAmbassador;
import io.github.vega.hla.VegaTimeManager;
import io.github.vega.utils.ExecutionLatch;
import io.github.vega.utils.ProjectLoader;
import io.github.vega.utils.ProjectRegistry;
import io.github.vega.utils.ProjectSettings;

public abstract class VegaSimulationBase
{
	protected static final Logger LOGGER = LogManager.getLogger();

	public VegaSimulationBase(String projectFilePath, String[] args)
	{
		unpackArgs(args);
		new ProjectLoader(projectFilePath);
		World.setupEngine();
	}

	public VegaSimulationBase(String projectFilePath)
	{
		new ProjectLoader(projectFilePath);
		World.setupEngine();
	}

	private void unpackArgs(String[] args)
	{
		if (args == null || args.length == 0)
			return;

		if (args[0].equals("reduced_logging"))
			ProjectSettings.REDUCED_LOGGING = true;
	}

	// This method is called during the "Register Federate Object Instances" step of
	// the SpaceFOM late joiner initialization p.80. It is anticipated that users
	// will initialize the entities and systems they plan to use in here.
	protected abstract void onInit();

	protected abstract void onRun();

	protected abstract void onShutdown();

	protected abstract void onFreeze();

	protected void init()
	{
		final int TOTAL_STEPS = 9;
		int currentStep = 0;

		connect();

		if (!ProjectSettings.REDUCED_LOGGING)
			LOGGER.info("({}/{}) Subscribing to the ExecutionConfiguration (ExCO) object class", ++currentStep, TOTAL_STEPS);
		subscribeExCO();
		if (!ProjectSettings.REDUCED_LOGGING)
			LOGGER.info("Subscribed to the ExCO object class");

		if (!ProjectSettings.REDUCED_LOGGING)
			LOGGER.info("({}/{}) Declaring the ModeTransitionRequest (MTR) interaction class", ++currentStep, TOTAL_STEPS);
		publishMTR();
		if (!ProjectSettings.REDUCED_LOGGING)
			LOGGER.info("MTR interaction class has been declared");

		if (!ProjectSettings.REDUCED_LOGGING)
			LOGGER.info("({}/{}) Waiting to discover the ExCO object instance", ++currentStep, TOTAL_STEPS);
		ExecutionLatch.enable();
		if (!ProjectSettings.REDUCED_LOGGING)
			LOGGER.info("Discovered ExCO object instance");

		if (!ProjectSettings.REDUCED_LOGGING)
			LOGGER.info("({}/{}) Waiting to receive the latest values of the ExCO object instance", ++currentStep, TOTAL_STEPS);
		ExecutionLatch.enable();
		System.out.println("Root Frame Name: " + ExCO.getRootFrameName());
		System.out.println("Current Execution Mode: " + ExCO.getCurrentExecutionMode());
		System.out.println("Next Execution Mode: " + ExCO.getNextExecutionMode());
		System.out.println("Least Common Time Step: " + ExCO.getLeastCommonTimeStep());
		if (!ProjectSettings.REDUCED_LOGGING)
			LOGGER.info("Latest values for ExCO have been received");
		
		if (!ProjectSettings.REDUCED_LOGGING)
			LOGGER.info("({}/{}) Publishing all object and interaction classes used by this federate", ++currentStep, TOTAL_STEPS);
		publishAllObjectClasses();
		publishAllInteractionClasses();
		if (!ProjectSettings.REDUCED_LOGGING)
			LOGGER.info("All object and interaction classes used by this federate have been published");

		if (!ProjectSettings.REDUCED_LOGGING)
			LOGGER.info("({}/{}) Registering federate object instances", ++currentStep, TOTAL_STEPS);
		onInit();

		int registeredInstancesCount = VegaDataManager.getRegisteredInstancesCount();
		String verb = registeredInstancesCount == 1 ? " was" : "s were";
		LOGGER.info("{} object instance{} successfully registered", registeredInstancesCount, verb);

		if (!ProjectSettings.REDUCED_LOGGING)
			LOGGER.info("({}/{}) Subscribing to all object and interaction classes used by this federate", ++currentStep, TOTAL_STEPS);
		subscribeAllObjectClasses();
		subscribeAllInteractionClasses();
		if (!ProjectSettings.REDUCED_LOGGING)
			LOGGER.info("All object and interaction classes used by this federate have been subscribed to");

		if (ProjectRegistry.requiredObjects != null)
		{	
			if (!ProjectSettings.REDUCED_LOGGING)
				LOGGER.info("({}/{}) Waiting for all required object instances to be discovered", ++currentStep, TOTAL_STEPS);
			ExecutionLatch.enable();
			if (!ProjectSettings.REDUCED_LOGGING)
				LOGGER.info("All required object instances were discovered", ++currentStep, TOTAL_STEPS);
		}
		if (!ProjectSettings.REDUCED_LOGGING)
			LOGGER.info("({}/{}) Aligning simulation timeline with the HLA federation");
		setupTimeManagement();
		if (!ProjectSettings.REDUCED_LOGGING)
			LOGGER.info("Simulation timeline is now in sync with the federation");
		
		LOGGER.info("Starting execution of the simulation");
		execLoop();
	}

	private void subscribeExCO()
	{
		final String className = "HLAobjectRoot.ExecutionConfiguration";
		final String archetypeName = "io.github.vega.archetypes.ExecutionConfiguration";
		final String converterName = "io.github.vega.converters.ExCOConverter";

		VegaObjectClass exCoClass = new VegaObjectClass(className, archetypeName, false);
		final IMultiDataConverter exCoConverter = new ExCOConverter();
		final IEntityArchetype exCoArchetype = new ExecutionConfiguration();

		exCoClass.addAttribute("root_frame_name", HLASharingModel.SUBSCRIBE_ONLY);
		exCoClass.addMultiConverter("root_frame_name", converterName, 0);

		exCoClass.addAttribute("current_execution_mode", HLASharingModel.SUBSCRIBE_ONLY);
		exCoClass.addMultiConverter("current_execution_mode", converterName, 1);

		exCoClass.addAttribute("next_execution_mode", HLASharingModel.SUBSCRIBE_ONLY);
		exCoClass.addMultiConverter("next_execution_mode", converterName, 2);

		exCoClass.addAttribute("least_common_time_step", HLASharingModel.SUBSCRIBE_ONLY);
		exCoClass.addMultiConverter("least_common_time_step", converterName, 3);

		ProjectRegistry.addObjectClass(exCoClass);
		ProjectRegistry.addArchetype(archetypeName, exCoArchetype);
		ProjectRegistry.addMultiConverter(converterName, exCoConverter);

		exCoClass.declare();
	}

	private void publishMTR()
	{
		final String className = "HLAinteractionRoot.ModeTransitionRequest";
		final String archetypeName = "io.github.vega.archetypes.ModeTransitionRequest";
		final String converterName = "io.github.vega.converters.MTRConverter";

		VegaInteractionClass mtrClass = new VegaInteractionClass(className, archetypeName, HLASharingModel.PUBLISH_SUBSCRIBE, false);
		final IMultiDataConverter mtrConverter = new MTRConverter();
		final ModeTransitionRequest mtrArchetype = new ModeTransitionRequest();

		mtrClass.addParameter("execution_mode");

		ProjectRegistry.addInteractionClass(mtrClass);
		ProjectRegistry.addArchetype(archetypeName, mtrArchetype);
		ProjectRegistry.addMultiConverter(converterName, mtrConverter);

		mtrClass.declare();
	}

	private static void publishAllObjectClasses()
	{
		for (VegaObjectClass objectClass : ProjectRegistry.objectClasses)
		{
			if (!objectClass.isPublished)
				objectClass.publish();
		}
	}

	private static void publishAllInteractionClasses()
	{
		for (VegaInteractionClass interactionClass : ProjectRegistry.interactionClasses)
		{
			if (!interactionClass.isPublished)
				interactionClass.publish();
		}
	}

	public static void subscribeAllObjectClasses()
	{
		for (VegaObjectClass objectClass : ProjectRegistry.objectClasses)
		{
			if (!objectClass.isSubscribed)
				objectClass.subscribe();
		}
	}

	private static void subscribeAllInteractionClasses()
	{
		for (VegaInteractionClass interactionClass : ProjectRegistry.interactionClasses)
		{
			if (!interactionClass.isSubscribed)
				interactionClass.subscribe();
		}
	}

	private static void setupTimeManagement()
	{
		VegaTimeManager.enableTimeConstrained();
		ExecutionLatch.enable();
		VegaTimeManager.enableTimeRegulation();
		ExecutionLatch.enable();
		VegaTimeManager.advanceTime(VegaTimeManager.getLogicalTimeBoundary());
		ExecutionLatch.enable();
	}

	private void execLoop()
	{
		while (true)
		{
			
			
		}
	}

	public static void connect()
	{
		RTIambassador rtiAmbassador = VegaRTIAmbassador.instance();
		
		try
		{
			rtiAmbassador.connect(new VegaFederateAmbassador(), CallbackModel.HLA_IMMEDIATE);
			if (ProjectSettings.FOM_MODULES != null)
				rtiAmbassador.joinFederationExecution(ProjectSettings.FEDERATE_NAME, ProjectSettings.FEDERATION_NAME, ProjectSettings.FOM_MODULES);
			else
				rtiAmbassador.joinFederationExecution(ProjectSettings.FEDERATE_NAME, ProjectSettings.FEDERATION_NAME);

			LOGGER.info("Joined the HLA federation <" + ProjectSettings.FEDERATION_NAME + "> with the name \"" + ProjectSettings.FEDERATE_NAME + "\"");
		}
		catch (Exception e)
		{
			LOGGER.error("Failed to join the HLA federation <" + ProjectSettings.FEDERATION_NAME + ">\n[REASON]", e);
			System.exit(1);
		}
	}

	public static void disconnect()
	{
		RTIambassador rtiAmbassador = VegaRTIAmbassador.instance();
		
		try
		{
			rtiAmbassador.resignFederationExecution(ResignAction.CANCEL_THEN_DELETE_THEN_DIVEST);
			LOGGER.info("Simulation terminated successfully...");
			System.exit(1);
		}
		catch (Exception e)
		{
			LOGGER.error("Simulation termination attempt failed unexpectedly\n[REASON]", e);
			// Leave the option to manually terminate the program to the user (for debugging
			// purposes)
			// System.exit(1);
		}
	}
}
