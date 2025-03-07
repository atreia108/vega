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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import atreia108.vega.types.IRemoteEntityFactory;
import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.encoding.EncoderFactory;

public class HlaTaskProcessor {
	private RTIambassador rtiAmbassador;
	private EncoderFactory encoderFactory;

	private Map<String, IRemoteEntityFactory> remoteEntityCreationPatterns;
	private Set<ObjectClassHandle> objectClassHandles;
	private Set<InteractionClassHandle> interactionClassHandles;

	public HlaTaskProcessor(RTIambassador rtiAmbassador, EncoderFactory encoderFactory) {
		this.rtiAmbassador = rtiAmbassador;
		this.encoderFactory = encoderFactory;
		remoteEntityCreationPatterns = new HashMap<String, IRemoteEntityFactory>();
		objectClassHandles = new HashSet<ObjectClassHandle>();
		interactionClassHandles = new HashSet<InteractionClassHandle>();
	}

	public void createObjectClasses(Set<HlaObjectClass> objectClasses) {
		for (HlaObjectClass objectClass : objectClasses) {
			try {
				String className = objectClass.getName();
				ObjectClassHandle classHandle = rtiAmbassador.getObjectClassHandle(className);
				AttributeHandleSet publishableSetHandle = rtiAmbassador.getAttributeHandleSetFactory().create();
				AttributeHandleSet subscribeableSetHandle = rtiAmbassador.getAttributeHandleSetFactory().create();
				Set<String> publishableAttributes = objectClass.getPublishedAttributes();
				Set<String> subscribeableAttributes = objectClass.getSubscribedAttributes();
				
				publishableAttributes.forEach((String attribute) -> {
					try {
						AttributeHandle attributeHandle = rtiAmbassador.getAttributeHandle(classHandle, attribute);
						publishableSetHandle.add(attributeHandle);
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
				
				subscribeableAttributes.forEach((String attribute) -> {
					try {
						AttributeHandle attributeHandle = rtiAmbassador.getAttributeHandle(classHandle, attribute);
						subscribeableSetHandle.add(attributeHandle);
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
				
				if (!publishableSetHandle.isEmpty()) {
					rtiAmbassador.publishObjectClassAttributes(classHandle, publishableSetHandle);
					objectClassHandles.add(classHandle);
				}
				
				if (!subscribeableSetHandle.isEmpty()) {
					rtiAmbassador.subscribeObjectClassAttributes(classHandle, subscribeableSetHandle);
					objectClassHandles.add(classHandle);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void createInteractionClasses(Set<HlaInteractionClass> interactionClasses) {
		for (HlaInteractionClass interactionClass : interactionClasses) {
			try {
				String className = interactionClass.getName();
				InteractionClassHandle classHandle = rtiAmbassador.getInteractionClassHandle(className);
				
				if (interactionClass.isPublishable()) {
					rtiAmbassador.publishInteractionClass(classHandle);
					interactionClassHandles.add(classHandle);
				}
				
				if (interactionClass.isSubscribeable()) {
					rtiAmbassador.subscribeInteractionClass(classHandle);
					interactionClassHandles.add(classHandle);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void registerRemoteEntity(String entityClassName, IRemoteEntityFactory creationPattern) {
		remoteEntityCreationPatterns.put(entityClassName, creationPattern);
	}
}
