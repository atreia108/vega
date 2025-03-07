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

import java.net.URL;
import java.util.Map;
import java.util.Set;

import atreia108.vega.utils.ProjectConfigParser;
import hla.rti1516e.CallbackModel;
import hla.rti1516e.NullFederateAmbassador;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.RtiFactory;
import hla.rti1516e.RtiFactoryFactory;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.exceptions.FederatesCurrentlyJoined;
import hla.rti1516e.exceptions.FederationExecutionAlreadyExists;
import hla.rti1516e.exceptions.FederationExecutionDoesNotExist;

/**
 * A simple federate ambassador that sets up and connects to an HLA federation execution. This is a good
 * starting point for building more complex federate ambassadors like the 
 * {@link atreia108.spacefom#SpaceFomLateJoinerFederateAmbassador SpaceFomLateJoinerFederateAmbassador} 
 * that follow a specific execution pattern
 * and possibly override existing functionality offered by this implementation.
 * @author Hridyanshu Aatreya
 * @since 0.1
 */

public class HlaFederateAmbassador extends NullFederateAmbassador {
	private ProjectConfigParser configParser;
	
	private String hostName;
	private String portNumber;
	private String federationName;
	private String federateType;
	private URL[] fomModules;
	
	protected RTIambassador rtiAmbassador;
	protected EncoderFactory encoderFactory;

	protected Set<HlaObjectClass> objectClasses;
	protected Set<HlaInteractionClass> interactionClasses;
	
	protected HlaTaskProcessor processor;

	public HlaFederateAmbassador(ASimulation simulation) {
		try {
			RtiFactory rtiFactory = RtiFactoryFactory.getRtiFactory();
			rtiAmbassador = rtiFactory.getRtiAmbassador();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		this.configParser = simulation.getConfigParser();
		
		initFederate();
	}

	private void initFederate() {
		Map<String, String> rtiConfiguration = configParser.getRtiConfig();
		hostName = rtiConfiguration.get("Host");
		portNumber = rtiConfiguration.get("Port");
		federationName = rtiConfiguration.get("Federation");
		federateType = rtiConfiguration.get("FederateType");
		fomModules = configParser.getFomsUrlPath();
		objectClasses = configParser.getObjectClasses();
		interactionClasses = configParser.getInteractionClasses();
		
		processor = new HlaTaskProcessor(rtiAmbassador, encoderFactory);
	}
	
	public void beginExecution() {
		try {
			rtiAmbassador.connect(this, CallbackModel.HLA_IMMEDIATE,
					"crcHost=" + hostName + "\n" + "crcPort=" + portNumber);
			try {
				rtiAmbassador.destroyFederationExecution(federationName);
			} catch (FederatesCurrentlyJoined e) {
			} catch (FederationExecutionDoesNotExist e) {}

			try {
				rtiAmbassador.createFederationExecution(federationName, fomModules);
			} catch (FederationExecutionAlreadyExists e) {}

			rtiAmbassador.joinFederationExecution(federateType, federationName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		processor.createObjectClasses(objectClasses);
		processor.createInteractionClasses(interactionClasses);
		// rtiAmbassador.requestAttributeValueUpdate(null, null, null);
		// FEDERATENAME::entityId
		// Outpost::50
		
	}
	
	public void objectInstanceNameReservationFailed(String objectName) {
		
	}
	
	public void objectInstanceNameReservationSucceeded(String objectName) {
		
	}
	
	public RTIambassador getRtiAmbassador() { return rtiAmbassador; }
	
	public EncoderFactory getEncoderFactory() { return encoderFactory; }
	
	public HlaTaskProcessor getHlaProcessor() { return processor; }
}
