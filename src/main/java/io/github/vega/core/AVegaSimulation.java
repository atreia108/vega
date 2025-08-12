/*-
 * SPDX-License-Identifier: BSD-3-Clause
 * Copyright (c) 2025 Hridyanshu Aatreya <Hridyanshu.Aatreya2@brunel.ac.uk>
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

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;

import hla.rti1516e.CallbackModel;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.ResignAction;
import io.github.vega.archetypes.ExecutionConfiguration;
import io.github.vega.archetypes.ModeTransitionRequest;
import io.github.vega.components.ExCOComponent;
import io.github.vega.converters.ExCOConverter;
import io.github.vega.converters.MTRConverter;
import io.github.vega.data.ExecutionMode;
import io.github.vega.utils.ExecutionLatch;
import io.github.vega.utils.FrameworkObjects;
import io.github.vega.utils.ProjectLoader;
import io.github.vega.utils.ProjectSettings;

/**
 * The base class for simulations using the Vega framework. It implements the
 * SpaceFOM late joiner initialization and execution sequences.
 * 
 * @author Hridyanshu Aatreya
 * @since 1.0.0
 */
public abstract class AVegaSimulation
{
	protected static final Logger LOGGER = LogManager.getLogger();

	protected ExCOComponent exCOComponent;

	public AVegaSimulation(String projectFilePath)
	{
		new ProjectLoader(projectFilePath);
	}

	/**
	 * Called during the "Register Federate Object Instances" step of the SpaceFOM
	 * late joiner initialization. It is anticipated that users will initialize the
	 * entities and systems they plan to use in here.
	 */
	protected abstract void onInit();

	/**
	 * Called whenever the federation execution's run mode is
	 * <code>EXEC_MODE_RUNNING</code>.
	 */
	protected abstract void onRun();

	/**
	 * Called as soon as the switch to <code>EXEC_MODE_SHUTDOWN</code> happens. Any
	 * final internal tasks to be performed right before shutdown should happen
	 * here. Note, however, that attempts to communicate with other federates at
	 * this stage will likely fail because they would be in the process of leaving
	 * the federation themselves.
	 */
	protected abstract void onShutdown();

	/**
	 * Late joiner initialization steps for the simulation. This method must be
	 * called to start the simulation. It can be overridden to implement custom
	 * initialization sequences.
	 */
	protected void init()
	{
		final int TOTAL_STEPS = 9;
		int currentStep = 0;

		connect();

		LOGGER.debug("({}/{}) Subscribing to the ExecutionConfiguration (ExCO) object class", ++currentStep, TOTAL_STEPS);
		subscribeExCO();
		LOGGER.debug("Subscribed to the ExCO object class");

		LOGGER.debug("({}/{}) Declaring the ModeTransitionRequest (MTR) interaction class", ++currentStep, TOTAL_STEPS);
		publishMTR();
		LOGGER.debug("MTR interaction class has been declared");

		LOGGER.info("({}/{}) Waiting to discover the ExCO object instance", ++currentStep, TOTAL_STEPS);
		ExecutionLatch.enable();
		LOGGER.info("Discovered ExCO object instance");

		LOGGER.info("({}/{}) Waiting to receive the latest values of the ExCO object instance", ++currentStep, TOTAL_STEPS);
		ExecutionLatch.enable();
		getExCOData();
		LOGGER.info("Latest values for ExCO have been received");

		LOGGER.info("({}/{}) Publishing all object and interaction classes used by this federate", ++currentStep, TOTAL_STEPS);
		publishAllObjectClasses();
		publishAllInteractionClasses();
		LOGGER.info("All object and interaction classes used by this federate have been published");

		LOGGER.info("({}/{}) Registering federate object instances", ++currentStep, TOTAL_STEPS);
		onInit();

		int registeredInstancesCount = HLAObjectManager.getRegisteredInstancesCount();
		String verb = registeredInstancesCount == 1 ? " was" : "s were";
		LOGGER.info("{} object instance{} successfully registered", registeredInstancesCount, verb);

		LOGGER.info("({}/{}) Subscribing to all object and interaction classes used by this federate", ++currentStep, TOTAL_STEPS);
		subscribeAllObjectClasses();
		subscribeAllInteractionClasses();
		LOGGER.info("All object and interaction classes used by this federate have been subscribed to");

		if (ProjectRegistry.requiredObjects != null)
		{
			LOGGER.info("({}/{}) Waiting for all required object instances to be discovered", ++currentStep, TOTAL_STEPS);
			ExecutionLatch.enable();
			LOGGER.info("All required object instances were discovered");
		}

		LOGGER.info("({}/{}) Aligning simulation timeline with the HLA federation", ++currentStep, TOTAL_STEPS);
		setupTimeManagement();
		LOGGER.info("Simulation timeline is now in sync with the federation");

		LOGGER.info("Starting execution of the simulation");
		tick();
	}

	private void subscribeExCO()
	{
		final String className = "HLAobjectRoot.ExecutionConfiguration";
		final String archetypeName = "io.github.vega.archetypes.ExecutionConfiguration";
		final String converterName = "io.github.vega.converters.ExCOConverter";

		ObjectClassProfile exCoClass = new ObjectClassProfile(className, archetypeName, false);
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

		InteractionClassProfile mtrClass = new InteractionClassProfile(className, archetypeName, HLASharingModel.PUBLISH_SUBSCRIBE, false);
		final IMultiDataConverter mtrConverter = new MTRConverter();
		final ModeTransitionRequest mtrArchetype = new ModeTransitionRequest();

		mtrClass.addParameter("execution_mode");

		ProjectRegistry.addInteractionClass(mtrClass);
		ProjectRegistry.addArchetype(archetypeName, mtrArchetype);
		ProjectRegistry.addMultiConverter(converterName, mtrConverter);

		mtrClass.declare();
	}

	private void getExCOData()
	{
		final Entity exCO = ProjectRegistry.getRemoteEntityByName("ExCO");
		final ComponentMapper<ExCOComponent> exCOMapper = ComponentMapper.getFor(ExCOComponent.class);
		exCOComponent = exCOMapper.get(exCO);
	}

	private void publishAllObjectClasses()
	{
		for (ObjectClassProfile objectClass : ProjectRegistry.objectClassProfiles)
		{
			if (!objectClass.isPublished)
				objectClass.publish();
		}
	}

	private static void publishAllInteractionClasses()
	{
		for (InteractionClassProfile interactionClass : ProjectRegistry.interactionClassProfiles)
		{
			if (!interactionClass.isPublished)
				interactionClass.publish();
		}
	}

	public static void subscribeAllObjectClasses()
	{
		for (ObjectClassProfile objectClass : ProjectRegistry.objectClassProfiles)
		{
			if (!objectClass.isSubscribed)
				objectClass.subscribe();
		}
	}

	private void subscribeAllInteractionClasses()
	{
		for (InteractionClassProfile interactionClass : ProjectRegistry.interactionClassProfiles)
		{
			if (!interactionClass.isSubscribed)
				interactionClass.subscribe();
		}
	}

	private void setupTimeManagement()
	{
		HLATimeManager.enableTimeConstrained();
		HLATimeManager.enableTimeRegulation();
		HLATimeManager.advanceTime();
	}

	private void tick()
	{

		while (true)
		{
			ExecutionMode currentMode = exCOComponent.currentExecutionMode;
			ExecutionMode nextMode = exCOComponent.nextExecutionMode;

			if (currentMode == ExecutionMode.EXEC_MODE_RUNNING && nextMode == ExecutionMode.EXEC_MODE_RUNNING)
			{
				onRun();
				HLATimeManager.advanceTime();
			}
			else if (currentMode == ExecutionMode.EXEC_MODE_RUNNING && nextMode == ExecutionMode.EXEC_MODE_SHUTDOWN)
			{
				onShutdown();
				disconnect();
			}
		}
	}

	/**
	 * Connect to the RTI and join the SpaceFOM federation execution.
	 */
	public void connect()
	{
		RTIambassador rtiAmbassador = FrameworkObjects.getRtiAmbassador();

		try
		{
			rtiAmbassador.connect(new SpaceFomFederateAmbassador(), CallbackModel.HLA_IMMEDIATE);
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

	/**
	 * Resign from the SpaceFOM federation execution.
	 */
	public void disconnect()
	{
		RTIambassador rtiAmbassador = FrameworkObjects.getRtiAmbassador();

		try
		{
			rtiAmbassador.resignFederationExecution(ResignAction.CANCEL_THEN_DELETE_THEN_DIVEST);
			LOGGER.info("Federate was successfully terminated");
		}
		catch (Exception e)
		{
			LOGGER.error("Federate termination attempt failed unexpectedly\n[REASON]", e);
		}

		System.exit(1);
	}
}
