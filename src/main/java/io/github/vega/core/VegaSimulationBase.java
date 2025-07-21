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

import hla.rti1516e.CallbackModel;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.ResignAction;
import io.github.vega.archetypes.ExecutionConfiguration;
import io.github.vega.archetypes.ModeTransitionRequest;
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
	protected static final Marker SPACEFOM_INIT_MARKER = MarkerManager.getMarker("SPACEFOM_INIT");
	protected static final Marker HLA_MARKER = MarkerManager.getMarker("HLA");

	public VegaSimulationBase(String projectFilePath)
	{
		new ProjectLoader(projectFilePath);
		World.setupEngine();
	}

	// This method is called during the "Register Federate Object Instances" step of
	// the SpaceFOM late joiner initialization p.80. It is anticipated that users
	// will initialize the entities and systems they plan to use in here.
	protected abstract void onInit();

	protected abstract void onRun();

	protected abstract void onShutdown();

	// protected abstract void onFreeze();

	protected void init()
	{
		final int TOTAL_STEPS = 9;
		int currentStep = 0;

		connect();

		LOGGER.info(SPACEFOM_INIT_MARKER, "({}/{}) Subscribing to the ExecutionConfiguration (ExCO) object class", ++currentStep, TOTAL_STEPS);
		subscribeExCO();
		LOGGER.info(SPACEFOM_INIT_MARKER, "Subscribed to the ExCO object class");

		LOGGER.info(SPACEFOM_INIT_MARKER, "({}/{}) Declaring the ModeTransitionRequest (MTR) interaction class", ++currentStep, TOTAL_STEPS);
		publishMTR();
		LOGGER.info(SPACEFOM_INIT_MARKER, "MTR interaction class has been declared");

		LOGGER.info(SPACEFOM_INIT_MARKER, "({}/{}) Waiting to discover the ExCO object instance", ++currentStep, TOTAL_STEPS);
		ExecutionLatch.enable();
		LOGGER.info(SPACEFOM_INIT_MARKER, "Discovered ExCO object instance");

		
		LOGGER.info(SPACEFOM_INIT_MARKER, "({}/{}) Waiting to receive the latest values of the ExCO object instance", ++currentStep, TOTAL_STEPS);
		ExecutionLatch.enable();
		LOGGER.info(SPACEFOM_INIT_MARKER, "Latest values for ExCO have been received");

		/* Temporarily disabled
		LOGGER.info(SPACEFOM_INIT_MARKER, "({}/{}) Publishing all object and interaction classes used by this federate", ++currentStep, TOTAL_STEPS);
		publishAllObjectClasses();
		publishAllInteractionClasses();
		LOGGER.info(SPACEFOM_INIT_MARKER, "All object and interaction classes used by this federate have been published");

		LOGGER.info(SPACEFOM_INIT_MARKER, "({}/{}) Registering federate object instances", ++currentStep, TOTAL_STEPS);
		onInit();

		int registeredInstancesCount = VegaDataManager.getRegisteredInstancesCount();
		String verb = registeredInstancesCount == 1 ? " was" : "s were";
		LOGGER.info(SPACEFOM_INIT_MARKER, "{} object instance{} successfully registered", registeredInstancesCount, verb);

		LOGGER.info(SPACEFOM_INIT_MARKER, "({}/{}) Subscribing to all object and interaction classes used by this federate", ++currentStep, TOTAL_STEPS);
		subscribeAllObjectClasses();
		subscribeAllInteractionClasses();
		LOGGER.info(SPACEFOM_INIT_MARKER, "All object and interaction classes used by this federate have been subscribed to");

		if (ProjectRegistry.requiredObjects != null)
		{
			LOGGER.info(SPACEFOM_INIT_MARKER, "({}/{}) Waiting for all required object instances to be discovered", ++currentStep, TOTAL_STEPS);
			ExecutionLatch.enable();
			LOGGER.info(SPACEFOM_INIT_MARKER, "All required object instances were discovered");
		}
		
		
		LOGGER.info(SPACEFOM_INIT_MARKER, "({}/{}) Aligning simulation timeline with the HLA federation", ++currentStep, TOTAL_STEPS);
		setupTimeManagement();
		LOGGER.info(SPACEFOM_INIT_MARKER, "Simulation timeline is now in sync with the federation");

		LOGGER.info("Starting execution of the simulation");
		tick();
		*/
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
		VegaTimeManager.advanceToLogicalTimeBoundary();
		ExecutionLatch.enable();
	}

	private void tick()
	{
		int runCounter = 0;

		while (true)
		{
			ExecutionMode currentMode = ExCO.getCurrentExecutionMode();
			ExecutionMode nextMode = ExCO.getNextExecutionMode();

			System.out.println("[TAR] current mode: " + currentMode);
			System.out.println("[TAR] next mode: " + nextMode);

			if (runCounter > 30)
				disconnect();
			else
			{
				onRun();
				VegaTimeManager.advanceTime();
				ExecutionLatch.enable();
			}

			runCounter++;
			/*
			 * if (currentMode == ExecutionMode.EXEC_MODE_RUNNING && nextMode ==
			 * ExecutionMode.EXEC_MODE_RUNNING) { onRun(); VegaTimeManager.advanceTime();
			 * System.out.println("Attempting to enable"); ExecutionLatch.enable();
			 * System.out.println("Post-enablement"); } else if ((currentMode ==
			 * ExecutionMode.EXEC_MODE_RUNNING || currentMode ==
			 * ExecutionMode.EXEC_MODE_FREEZE) && nextMode ==
			 * ExecutionMode.EXEC_MODE_SHUTDOWN) { LOGGER.
			 * info("Federate has been advised to shut down. Terminating simulation now");
			 * onShutdown(); disconnect(); } else if (currentMode ==
			 * ExecutionMode.EXEC_MODE_RUNNING && nextMode ==
			 * ExecutionMode.EXEC_MODE_FREEZE) onFreeze(); else if (currentMode ==
			 * ExecutionMode.EXEC_MODE_FREEZE && nextMode ==
			 * ExecutionMode.EXEC_MODE_RUNNING) { onRun(); VegaTimeManager.advanceTime();
			 * ExecutionLatch.enable(); } else LOGGER.
			 * warn("Unknown execution mode encountered. Federate will not engage in any potential mode transition"
			 * );
			 */
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

			LOGGER.info(HLA_MARKER, "Joined the HLA federation <" + ProjectSettings.FEDERATION_NAME + "> with the name \"" + ProjectSettings.FEDERATE_NAME + "\"");
		}
		catch (Exception e)
		{
			LOGGER.error(HLA_MARKER, "Failed to join the HLA federation <" + ProjectSettings.FEDERATION_NAME + ">\n[REASON]", e);
			System.exit(1);
		}
	}

	public static void disconnect()
	{
		RTIambassador rtiAmbassador = VegaRTIAmbassador.instance();

		try
		{
			rtiAmbassador.resignFederationExecution(ResignAction.CANCEL_THEN_DELETE_THEN_DIVEST);
			LOGGER.info(HLA_MARKER, "Federate was successfully terminated");
		}
		catch (Exception e)
		{
			LOGGER.error(HLA_MARKER, "Federate termination attempt failed unexpectedly\n[REASON]", e);
		}
		
		System.exit(1);
	}
}
