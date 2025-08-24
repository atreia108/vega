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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;

import hla.rti1516e.LogicalTimeFactoryFactory;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.TimeQueryReturn;
import hla.rti1516e.time.HLAinteger64Interval;
import hla.rti1516e.time.HLAinteger64Time;
import hla.rti1516e.time.HLAinteger64TimeFactory;
import io.github.atreia108.vega.components.ExCOComponent;
import io.github.atreia108.vega.utils.ExecutionLatch;
import io.github.atreia108.vega.utils.VegaUtilities;

/**
 * Handles HLA time management for the simulation.
 * 
 * @author Hridyanshu Aatreya
 * @since 1.0.0
 */
public final class HLATimeManager
{
	private static final Logger LOGGER = LogManager.getLogger();
	
	private static final HLAinteger64TimeFactory TIME_FACTORY = LogicalTimeFactoryFactory.getLogicalTimeFactory(HLAinteger64TimeFactory.class);
	private static HLAinteger64Time presentTime;
	private static HLAinteger64Time lookAheadTime;

	protected static void enableTimeConstrained()
	{
		try
		{
			RTIambassador rtiAmbassador = VegaUtilities.rtiAmbassador();
			rtiAmbassador.enableTimeConstrained();
			ExecutionLatch.enable();
		}
		catch (Exception e)
		{
			LOGGER.error("Failed to enable HLA time constrained\n[REASON]", e);
			System.exit(1);
		}
	}

	protected static void enableTimeRegulation()
	{
		long leastCommonTimeStep = getLeastCommonTimeStep();

		if (lookAheadTime == null)
			lookAheadTime = getLookAheadTime(leastCommonTimeStep);

		HLAinteger64Interval lookAheadInterval = getLookAheadInterval(leastCommonTimeStep);

		try
		{
			RTIambassador rtiAmbassador = VegaUtilities.rtiAmbassador();
			rtiAmbassador.enableTimeRegulation(lookAheadInterval);
			ExecutionLatch.enable();
		}
		catch (Exception e)
		{
			LOGGER.error("Failed to enable HLA time regulation\n[REASON]", e);
			System.exit(1);
		}
	}
	
	protected static long getLeastCommonTimeStep()
	{
		// Entity exCO = ProjectRegistry.getRemoteEntityByName("ExCO");
		Entity exCO = HLAObjectManager.getRemoteEntity("ExCO");
		ComponentMapper<ExCOComponent> exCOMapper = ComponentMapper.getFor(ExCOComponent.class);
		ExCOComponent exCOComponent = exCOMapper.get(exCO);
		
		return exCOComponent.leastCommonTimeStep;
	}

	protected static HLAinteger64Time getLookAheadTime(long leastCommonTimeStep)
	{
		return TIME_FACTORY.makeTime(leastCommonTimeStep);
	}

	protected static HLAinteger64Interval getLookAheadInterval(long leastCommonTimeStep)
	{
		return TIME_FACTORY.makeInterval(leastCommonTimeStep);
	}

	protected static HLAinteger64Time getLogicalTimeBoundary()
	{
		long leastCommonTimeStep = getLeastCommonTimeStep();
		
		try
		{
			RTIambassador rtiAmbassador = VegaUtilities.rtiAmbassador();
			TimeQueryReturn galtQuery = rtiAmbassador.queryGALT();
			HLAinteger64Time galt = (HLAinteger64Time) galtQuery.time;
			long hltb = (long) ((Math.floor(galt.getValue() / leastCommonTimeStep) + 1) * leastCommonTimeStep);

			HLAinteger64Time logicalTimeBoundary = TIME_FACTORY.makeTime(hltb);
			return logicalTimeBoundary;
		}
		catch (Exception e)
		{
			LOGGER.error("Failed to compute the HLA logical time boundary (HLTB)\n[REASON]", e);
			System.exit(1);
		}

		return null;
	}

	protected static void advanceTime()
	{
		HLAinteger64Time nextTimeStep = null;
		
		if (presentTime == null)
			nextTimeStep = getLogicalTimeBoundary();
		else
			nextTimeStep = getNextTimeStep();
		
		try
		{
			RTIambassador rtiAmbassador = VegaUtilities.rtiAmbassador();
			rtiAmbassador.timeAdvanceRequest(nextTimeStep);
			ExecutionLatch.enable();
		}
		catch (Exception e)
		{
			LOGGER.error("Failed to request time advance to the next time step\n[REASON]", e);
			System.exit(1);
		}
	}

	protected static HLAinteger64Time getNextTimeStep()
	{
		long present = presentTime.getValue();
		long stepIncrement = lookAheadTime.getValue();
		long future = present + stepIncrement;

		HLAinteger64Time nextTimeStep = TIME_FACTORY.makeTime(future);
		return nextTimeStep;
	}

	protected static void setPresentTime(HLAinteger64Time newTime)
	{
		presentTime = newTime;
	}
}
