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

package io.github.atreia108.vega.core;

import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.FederateHandle;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.LogicalTime;
import hla.rti1516e.MessageRetractionHandle;
import hla.rti1516e.NullFederateAmbassador;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.OrderType;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.TransportationTypeHandle;
import hla.rti1516e.exceptions.FederateInternalError;

/**
 * The federate ambassador used by Vega simulations. It simply redirects all
 * operations to the {@link io.github.atreia108.vega.core.HLACallbackManager
 * HLACallbackManager}.
 * 
 * @author Hridyanshu Aatreya
 * @since 1.0.0
 */
public class SpaceFomFederateAmbassador extends NullFederateAmbassador
{
	@Override
	public void discoverObjectInstance(final ObjectInstanceHandle theObject, ObjectClassHandle theObjectClass, String objectName)
	{
		HLACallbackManager.discoverObjectInstance(theObject, theObjectClass, objectName);
	}

	@Override
	public void discoverObjectInstance(ObjectInstanceHandle theObject, ObjectClassHandle theObjectClass, String objectName, FederateHandle producingFederate) throws FederateInternalError
	{
		HLACallbackManager.discoverObjectInstance(theObject, theObjectClass, objectName);
	}

	@Override
	public void removeObjectInstance(ObjectInstanceHandle theObject, byte[] userSuppliedTag, OrderType sentOrdering, SupplementalRemoveInfo removeInfo) throws FederateInternalError
	{
		HLACallbackManager.removeObjectInstance(theObject);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void removeObjectInstance(ObjectInstanceHandle theObject, byte[] userSuppliedTag, OrderType sentOrdering, LogicalTime theTime, OrderType receivedOrdering, MessageRetractionHandle retractionHandle, SupplementalRemoveInfo removeInfo) throws FederateInternalError
	{
		HLACallbackManager.removeObjectInstance(theObject);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void removeObjectInstance(ObjectInstanceHandle theObject, byte[] userSuppliedTag, OrderType sentOrdering, LogicalTime theTime, OrderType receivedOrdering, SupplementalRemoveInfo removeInfo) throws FederateInternalError
	{
		HLACallbackManager.removeObjectInstance(theObject);
	}

	@Override
	public void reflectAttributeValues(ObjectInstanceHandle theObject, AttributeHandleValueMap theAttributes, byte[] userSuppliedTag, OrderType sentOrdering, TransportationTypeHandle theTransport, SupplementalReflectInfo reflectInfo) throws FederateInternalError
	{
		HLACallbackManager.reflectAttributeValues(theObject, theAttributes);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void reflectAttributeValues(ObjectInstanceHandle theObject, AttributeHandleValueMap theAttributes, byte[] userSuppliedTag, OrderType sentOrdering, TransportationTypeHandle theTransport, LogicalTime theTime, OrderType receivedOrdering, MessageRetractionHandle retractionHandle, SupplementalReflectInfo reflectInfo) throws FederateInternalError
	{
		HLACallbackManager.reflectAttributeValues(theObject, theAttributes);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void reflectAttributeValues(ObjectInstanceHandle theObject, AttributeHandleValueMap theAttributes, byte[] userSuppliedTag, OrderType sentOrdering, TransportationTypeHandle theTransport, LogicalTime theTime, OrderType receivedOrdering, SupplementalReflectInfo reflectInfo) throws FederateInternalError
	{
		HLACallbackManager.reflectAttributeValues(theObject, theAttributes);
	}

	@Override
	public void objectInstanceNameReservationSucceeded(String objectName)
	{
		HLACallbackManager.objectInstanceNameReservationSucceeded(objectName);
	}

	@Override
	public void objectInstanceNameReservationFailed(String objectName)
	{
		HLACallbackManager.objectInstanceNameReservationFailed(objectName);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void timeConstrainedEnabled(LogicalTime time) throws FederateInternalError
	{
		HLACallbackManager.timeConstrainedEnabled(time);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void timeRegulationEnabled(LogicalTime time) throws FederateInternalError
	{
		HLACallbackManager.timeRegulationEnabled(time);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void timeAdvanceGrant(LogicalTime theTime) throws FederateInternalError
	{
		HLACallbackManager.timeAdvanceGrant(theTime);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void receiveInteraction(InteractionClassHandle interactionClass, ParameterHandleValueMap theParameters, byte[] userSuppliedTag, OrderType sentOrdering, TransportationTypeHandle theTransport, LogicalTime theTime, OrderType receivedOrdering, SupplementalReceiveInfo receiveInfo) throws FederateInternalError
	{
		HLACallbackManager.receiveInteraction(interactionClass, theParameters);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void receiveInteraction(InteractionClassHandle interactionClass, ParameterHandleValueMap theParameters, byte[] userSuppliedTag, OrderType sentOrdering, TransportationTypeHandle theTransport, LogicalTime theTime, OrderType receivedOrdering, MessageRetractionHandle retractionHandle, SupplementalReceiveInfo receiveInfo) throws FederateInternalError
	{
		HLACallbackManager.receiveInteraction(interactionClass, theParameters);
	}

	@Override
	public void receiveInteraction(InteractionClassHandle interactionClass, ParameterHandleValueMap theParameters, byte[] userSuppliedTag, OrderType sentOrdering, TransportationTypeHandle theTransport, SupplementalReceiveInfo receiveInfo) throws FederateInternalError
	{
		HLACallbackManager.receiveInteraction(interactionClass, theParameters);
	}

	@Override
	public void provideAttributeValueUpdate(ObjectInstanceHandle theObject, AttributeHandleSet theAttributes, byte[] userSuppliedTag) throws FederateInternalError
	{
		HLACallbackManager.provideAttributeValueUpdate(theObject, theAttributes);
	}
}
