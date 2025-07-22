package io.github.vega.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;

import hla.rti1516e.LogicalTimeFactoryFactory;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.TimeQueryReturn;
import hla.rti1516e.time.HLAinteger64Interval;
import hla.rti1516e.time.HLAinteger64Time;
import hla.rti1516e.time.HLAinteger64TimeFactory;
import io.github.vega.components.ExCOComponent;
import io.github.vega.utils.ExecutionLatch;
import io.github.vega.utils.ProjectRegistry;

public class VegaTimeManager
{
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Marker HLA_MARKER = MarkerManager.getMarker("HLA");
	
	private static final HLAinteger64TimeFactory TIME_FACTORY = LogicalTimeFactoryFactory.getLogicalTimeFactory(HLAinteger64TimeFactory.class);
	private static HLAinteger64Time presentTime;
	private static HLAinteger64Time lookAheadTime;

	protected static void enableTimeConstrained()
	{
		try
		{
			RTIambassador rtiAmbassador = VegaRTIAmbassador.instance();
			rtiAmbassador.enableTimeConstrained();
			ExecutionLatch.enable();
		}
		catch (Exception e)
		{
			LOGGER.error(HLA_MARKER, "Failed to enable HLA time constrained\n[REASON]", e);
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
			RTIambassador rtiAmbassador = VegaRTIAmbassador.instance();
			rtiAmbassador.enableTimeRegulation(lookAheadInterval);
			ExecutionLatch.enable();
		}
		catch (Exception e)
		{
			LOGGER.error(HLA_MARKER, "Failed to enable HLA time regulation\n[REASON]", e);
			System.exit(1);
		}
	}
	
	protected static long getLeastCommonTimeStep()
	{
		Entity exCO = ProjectRegistry.getRemoteEntityByName("ExCO");
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
			RTIambassador rtiAmbassador = VegaRTIAmbassador.instance();
			TimeQueryReturn galtQuery = rtiAmbassador.queryGALT();
			HLAinteger64Time galt = (HLAinteger64Time) galtQuery.time;
			long hltb = (long) ((Math.floor(galt.getValue() / leastCommonTimeStep) + 1) * leastCommonTimeStep);

			HLAinteger64Time logicalTimeBoundary = TIME_FACTORY.makeTime(hltb);
			return logicalTimeBoundary;
		}
		catch (Exception e)
		{
			LOGGER.error(HLA_MARKER, "Failed to compute the HLA logical time boundary (HLTB)\n[REASON]", e);
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
			RTIambassador rtiAmbassador = VegaRTIAmbassador.instance();
			rtiAmbassador.timeAdvanceRequest(nextTimeStep);
			ExecutionLatch.enable();
		}
		catch (Exception e)
		{
			LOGGER.error(HLA_MARKER, "Failed to request time advance to the next time step\n[REASON]", e);
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
