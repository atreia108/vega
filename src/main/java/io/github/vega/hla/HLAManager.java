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

package io.github.vega.hla;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import hla.rti1516e.CallbackModel;
import hla.rti1516e.NullFederateAmbassador;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.ResignAction;
import hla.rti1516e.RtiFactory;
import hla.rti1516e.RtiFactoryFactory;
import io.github.vega.utils.ProjectSettings;

public class HLAManager
{
	private static final Logger LOGGER = LogManager.getLogger();

	protected static RtiFactory rtiFactory;
	protected static RTIambassador rtiAmbassador;

	static
	{
		try
		{
			rtiFactory = RtiFactoryFactory.getRtiFactory();
			rtiAmbassador = rtiFactory.getRtiAmbassador();
		}
		catch (Exception e)
		{
			LOGGER.error("Project initialization failed\n[REASON]");
			System.exit(1);
		}
	}

	public static void connect(NullFederateAmbassador federateAmbassador)
	{
		try
		{
			rtiAmbassador.connect(federateAmbassador, CallbackModel.HLA_IMMEDIATE);
			rtiAmbassador.joinFederationExecution(ProjectSettings.FEDERATE_NAME, ProjectSettings.FEDERATION_NAME, ProjectSettings.FOM_MODULES);
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
		try
		{
			rtiAmbassador.resignFederationExecution(ResignAction.CANCEL_THEN_DELETE_THEN_DIVEST);
			LOGGER.info("Simulation terminated successfully...");
			System.exit(1);
		}
		catch (Exception e)
		{
			LOGGER.error("Attempt to terminate simulation failed unexpectedly\n[REASON]", e);
			// Leave the option to manually terminate the program to the user (for debugging purposes)
			// System.exit(1);
		}
	}

	public static void declareAllObjects()
	{
		
	}

	public static void declareAllInteractions()
	{

	}
}
