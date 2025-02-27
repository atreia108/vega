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
import java.util.Set;

import atreia108.vega.utils.HlaConfigParser;
import atreia108.vega.utils.HlaObjectClass;
import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.CallbackModel;
import hla.rti1516e.NullFederateAmbassador;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.RtiFactory;
import hla.rti1516e.RtiFactoryFactory;
import hla.rti1516e.exceptions.FederatesCurrentlyJoined;
import hla.rti1516e.exceptions.FederationExecutionAlreadyExists;
import hla.rti1516e.exceptions.FederationExecutionDoesNotExist;

public class Federate extends NullFederateAmbassador {
	private String hostName;
	private String portNumber;
	private RTIambassador rtiAmbassador;
	private String federationName;
	private String federateType;
	private URL[] foms;

	private Set<HlaObjectClass> federateObjectClasses;

	public Federate() {
		try {
			RtiFactory rtiFactory = RtiFactoryFactory.getRtiFactory();
			rtiAmbassador = rtiFactory.getRtiAmbassador();
		} catch (Exception e) {
			e.printStackTrace();
		}

		init();
		joinFederation();
		initObjectClasses();
	}

	private void init() {
		HlaConfigParser configParser = new HlaConfigParser();
		hostName = configParser.getRtiConfig().get("Host");
		portNumber = configParser.getRtiConfig().get("Port");
		federationName = configParser.getRtiConfig().get("Federation");
		federateType = configParser.getRtiConfig().get("FederateType");
		foms = configParser.getFomsUrlPath();
		federateObjectClasses = configParser.getObjectClasses();
	}

	private void joinFederation() {
		try {
			rtiAmbassador.connect(this, CallbackModel.HLA_IMMEDIATE,
					"crcHost=" + hostName + "\n" + "crcPort=" + portNumber);
			try {
				rtiAmbassador.destroyFederationExecution(federationName);
			} catch (FederatesCurrentlyJoined e) {
			} catch (FederationExecutionDoesNotExist e) {
			}

			try {
				rtiAmbassador.createFederationExecution(federationName, foms);
			} catch (FederationExecutionAlreadyExists e) {
			}

			rtiAmbassador.joinFederationExecution(federateType, federationName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initObjectClasses() {
		for (HlaObjectClass objectClass : federateObjectClasses) {
			try {
				String objectClassName = objectClass.getName();
				ObjectClassHandle classHandle = rtiAmbassador.getObjectClassHandle(objectClassName);
				AttributeHandleSet publishableSetHandle = rtiAmbassador.getAttributeHandleSetFactory().create();
				AttributeHandleSet subscribeableSetHandle = rtiAmbassador.getAttributeHandleSetFactory().create();
				Set<String> publishableAttributes = objectClass.getPublishedAttributes();
				Set<String> subscribableAttributes = objectClass.getSubscribedAttributes();

				publishableAttributes.forEach(attribute -> {
					try {
						AttributeHandle attributeHandle = rtiAmbassador.getAttributeHandle(classHandle, attribute);
						publishableSetHandle.add(attributeHandle);
					} catch (Exception e) {
						e.printStackTrace();
					}
				});

				subscribableAttributes.forEach(attribute -> {
					try {
						AttributeHandle attributeHandle = rtiAmbassador.getAttributeHandle(classHandle, attribute);
						subscribeableSetHandle.add(attributeHandle);
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
				
				if (!publishableSetHandle.isEmpty())
					rtiAmbassador.publishObjectClassAttributes(classHandle, publishableSetHandle);
				
				if (!subscribeableSetHandle.isEmpty())
					rtiAmbassador.subscribeObjectClassAttributes(classHandle, subscribeableSetHandle);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
