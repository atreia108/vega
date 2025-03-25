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

package vega.spacefom;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.CallbackModel;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.LogicalTime;
import hla.rti1516e.LogicalTimeFactoryFactory;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.OrderType;
import hla.rti1516e.TimeQueryReturn;
import hla.rti1516e.TransportationTypeHandle;
import hla.rti1516e.exceptions.FederateInternalError;
import hla.rti1516e.time.HLAinteger64Time;
import hla.rti1516e.time.HLAinteger64TimeFactory;
import vega.core.SimulationBase;
import vega.hla1516e.FederateAmbassadorBase;

public class SpaceFomFederateAmbassador extends FederateAmbassadorBase
{
	protected AtomicInteger advanceState;
	protected final int NOT_ADVANCING;
	protected final int ADVANCING;
	
	protected CountDownLatch threadLatch;

	protected ExecutionConfiguration exCO;
	protected AttributeHandle leastCommonTimeStep;
	protected AttributeHandle rootFrameName;
	
	protected HLAinteger64Time currentTime;
	protected HLAinteger64Time lookAheadTime;
	
	protected HLAinteger64TimeFactory timeFactory;

	public SpaceFomFederateAmbassador(SimulationBase simulation)
	{
		super(simulation);
		NOT_ADVANCING = 0;
		ADVANCING = 1;
		
		advanceState = new AtomicInteger();
		advanceState.set(NOT_ADVANCING);
	}
	
	@Override
	public void initialize()
	{
		try
		{
			timeFactory = LogicalTimeFactoryFactory.getLogicalTimeFactory(HLAinteger64TimeFactory.class);
			
			connect();
			createExCO();
			createMTR();

			System.out.println("[INFO] Waiting to discover ExecutionConfiguration");
			threadLatch = new CountDownLatch(1);
			threadLatch.await();
			System.out.println("[INFO] Discovered ExecutionConfiguration. Requesting attribute value update");

			threadLatch = new CountDownLatch(1);
			threadLatch.await();
			System.out.println("[INFO] Received ExCO attribute update");
			
			lookAheadTime = timeFactory.makeTime(exCO.getLeastCommonTimeStep());
			System.out.println("[INFO] Least Common Time Step: " + lookAheadTime.getValue());
			System.out.println("[INFO] Root Frame Name: " + exCO.getRootFrameName());
			
			System.out.println("[INFO] Initializing internal simulation objects");
			world.createEntityClasses();
			simulation.initialize();
			System.out.println("[INFO] Simulation objects initialized without incident");
			
			System.out.println("[INFO] Setting up HLA time management");
			rtiAmbassador.enableTimeConstrained();
			threadLatch = new CountDownLatch(1);
			threadLatch.await();
			
			rtiAmbassador.enableTimeRegulation(timeFactory.makeInterval(1000000));
			threadLatch = new CountDownLatch(1);
			threadLatch.await();
			
			System.out.println("[INFO] HLA time management successfully enabled");
			
			System.out.println("[INFO] Computing HLA Logical Time Boundary to advance time");
			TimeQueryReturn queriedGALT = rtiAmbassador.queryGALT();
			HLAinteger64Time galt = (HLAinteger64Time) queriedGALT.time;
			long lcts = exCO.getLeastCommonTimeStep();
			long computedTime = (long) ((Math.floor((galt.getValue()/lcts)) + 1) * lcts);
			
			HLAinteger64Time hltb = timeFactory.makeTime(computedTime);
			rtiAmbassador.timeAdvanceRequest(hltb);
			advanceState.set(ADVANCING);
			threadLatch = new CountDownLatch(1);
			threadLatch.await();
			
			simulation.play();
			
			while (true)
			{
				// Wait for time advance grant
				if (advanceState.get() == NOT_ADVANCING)
				{
					rtiAmbassador.timeAdvanceRequest(calculateNextTimeStep());
					advanceState.set(ADVANCING);
					threadLatch = new CountDownLatch(1);
					threadLatch.await();
				}
			}
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	protected HLAinteger64Time calculateNextTimeStep()
	{
		long presentTime = currentTime.getValue();
		long timeStep = lookAheadTime.getValue();
		long predictedNext = presentTime + timeStep;
		HLAinteger64Time nextTimeStep = timeFactory.makeTime(predictedNext);
		return nextTimeStep;
	}
	
	protected void connect()
	{
		try
		{
			rtiAmbassador.connect(this, CallbackModel.HLA_IMMEDIATE, "crcHost=" + hostName + "\n" + "crcPort=" + port);
			rtiAmbassador.joinFederationExecution(federateType, federationName);
		}
		catch (Exception e) { e.printStackTrace(); }
	}

	protected void createExCO()
	{
		try
		{
			ObjectClassHandle exCOClass = rtiAmbassador.getObjectClassHandle("ExecutionConfiguration");
			AttributeHandleSet exCOAttributeHandleSet = rtiAmbassador.getAttributeHandleSetFactory().create();
			rootFrameName = rtiAmbassador.getAttributeHandle(exCOClass, "root_frame_name");
			leastCommonTimeStep = rtiAmbassador.getAttributeHandle(exCOClass, "least_common_time_step");

			exCOAttributeHandleSet.add(rootFrameName);
			exCOAttributeHandleSet.add(leastCommonTimeStep);

			rtiAmbassador.subscribeObjectClassAttributes(exCOClass, exCOAttributeHandleSet);
		}
		catch (Exception e) { e.printStackTrace(); }
	}

	protected void createMTR()
	{
		try
		{
			InteractionClassHandle mtrClassHandle = rtiAmbassador.getInteractionClassHandle("ModeTransitionRequest");
			rtiAmbassador.publishInteractionClass(mtrClassHandle);
		}
		catch (Exception e) { e.printStackTrace(); }
	}

	@Override
	public void discoverObjectInstance(final ObjectInstanceHandle theObject, ObjectClassHandle theObjectClass,
			String objectName)
	{
		try
		{
			String className = rtiAmbassador.getObjectClassName(theObjectClass);
			System.out.println("[INFO] Class discovered <" + className + ">");
			
			if (className.equals("HLAobjectRoot.ExecutionConfiguration"))
			{
				exCO = new ExecutionConfiguration(theObject, encoder);
				AttributeHandleSet exCOAttributeHandleSet = rtiAmbassador.getAttributeHandleSetFactory().create();
				exCOAttributeHandleSet.add(rootFrameName);
				exCOAttributeHandleSet.add(leastCommonTimeStep);
				rtiAmbassador.requestAttributeValueUpdate(theObject, exCOAttributeHandleSet, null);
				
				threadLatch.countDown();
			}
		}
		catch (Exception e) { e.printStackTrace(); }
	}

	@Override
	public void reflectAttributeValues(ObjectInstanceHandle theObject, AttributeHandleValueMap theAttributes,
			byte[] userSuppliedTag, OrderType sentOrdering, TransportationTypeHandle theTransport,
			SupplementalReflectInfo reflectInfo) throws FederateInternalError
	{
		try
		{
			ObjectClassHandle objectType = rtiAmbassador.getKnownObjectClassHandle(theObject);
			String objectClassName = rtiAmbassador.getObjectClassName(objectType);

			if (objectClassName.equals("HLAobjectRoot.ExecutionConfiguration"))
			{
				if (theAttributes.containsKey(rootFrameName) && theAttributes.containsKey(leastCommonTimeStep))
				{
					exCO.setRootFrameName(theAttributes.get(rootFrameName));
					exCO.setLeastCommonTimeStep(theAttributes.get(leastCommonTimeStep));
				}
			}

			threadLatch.countDown();
		}
		catch (Exception e) { e.printStackTrace(); }
	}
	
	@Override
	@SuppressWarnings("rawtypes")
	public void timeAdvanceGrant(LogicalTime theTime) throws FederateInternalError
	{
		System.out.println("[INFO] Federate Ambassador was granted request to advance time");
		advanceState.set(NOT_ADVANCING);
		currentTime = (HLAinteger64Time) theTime;
		threadLatch.countDown();
	}
	
	@Override
	@SuppressWarnings("rawtypes")
	public void timeConstrainedEnabled(LogicalTime time) throws FederateInternalError
	{
		System.out.println("[INFO] Time constraining enabled for Federate Ambassador");
		threadLatch.countDown();
	}
	
	@Override
	@SuppressWarnings("rawtypes")
	public void timeRegulationEnabled(LogicalTime time) throws FederateInternalError
	{
		System.out.println("[INFO] Time regulation enabled for Federate Ambassador");
		threadLatch.countDown();
	}
}
