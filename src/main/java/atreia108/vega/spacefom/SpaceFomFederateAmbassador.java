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

package atreia108.vega.spacefom;

import atreia108.vega.core.SimulationBase;
import atreia108.vega.hla1516e.FederateAmbassadorBase;
import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.CallbackModel;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;

public class SpaceFomFederateAmbassador extends FederateAmbassadorBase
{
	protected ObjectInstanceHandle exCO;
	
	public SpaceFomFederateAmbassador(SimulationBase simulation)
	{
		super(simulation);
	}
	
	// TODO - Implement SpaceFOM late joiner sequence
	public void initialize()
	{
		connect();
		createExCO();
		createMTR();
		
		while (true) {}
	}
	
	protected void connect()
	{
		try
		{
			rtiAmbassador.connect(this, CallbackModel.HLA_IMMEDIATE, "crcHost=" + hostName + "\n" + "crcPort=" + port);
			rtiAmbassador.joinFederationExecution(federateType, federationName);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	// TODO
	protected void createExCO()
	{
		try
		{
			ObjectClassHandle exCOClass = rtiAmbassador.getObjectClassHandle("ExecutionConfiguration");
			AttributeHandleSet attributeHandleSet = rtiAmbassador.getAttributeHandleSetFactory().create();
			AttributeHandle rootFrameName = rtiAmbassador.getAttributeHandle(exCOClass, "root_frame_name");
			AttributeHandle currentExecutionMode = rtiAmbassador.getAttributeHandle(exCOClass, "current_execution_mode");
			AttributeHandle nextExecutionMode = rtiAmbassador.getAttributeHandle(exCOClass, "next_execution_mode");
			AttributeHandle leastCommonTimeStep = rtiAmbassador.getAttributeHandle(exCOClass, "least_common_time_step");
			
			attributeHandleSet.add(rootFrameName);
			attributeHandleSet.add(currentExecutionMode);
			attributeHandleSet.add(nextExecutionMode);
			attributeHandleSet.add(leastCommonTimeStep);
			
			rtiAmbassador.subscribeObjectClassAttributes(exCOClass, attributeHandleSet);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	// TODO
	protected void createMTR()
	{
		try
		{
			InteractionClassHandle mtrClassHandle = rtiAmbassador.getInteractionClassHandle("ModeTransitionRequest");
			rtiAmbassador.publishInteractionClass(mtrClassHandle);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
