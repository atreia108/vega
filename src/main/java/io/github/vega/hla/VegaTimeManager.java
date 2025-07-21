package io.github.vega.hla;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import hla.rti1516e.LogicalTimeFactoryFactory;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.TimeQueryReturn;
import hla.rti1516e.time.HLAinteger64Interval;
import hla.rti1516e.time.HLAinteger64Time;
import hla.rti1516e.time.HLAinteger64TimeFactory;
import io.github.vega.data.ExCO;

public class VegaTimeManager
{
	private static final Logger LOGGER = LogManager.getLogger();

	private static final RTIambassador RTI_AMBASSADOR = VegaRTIAmbassador.instance();
	private static final HLAinteger64TimeFactory TIME_FACTORY = LogicalTimeFactoryFactory.getLogicalTimeFactory(HLAinteger64TimeFactory.class);
	private static HLAinteger64Time presentTime;
	private static HLAinteger64Time lookAheadTime;

	public static void enableTimeConstrained()
	{
		try
		{
			RTI_AMBASSADOR.enableTimeConstrained();
		}
		catch (Exception e)
		{
			LOGGER.error("Failed to enable HLA time constrained\n[REASON]", e);
			System.exit(1);
		}
	}

	public static void enableTimeRegulation()
	{
		long leastCommonTimeStep = ExCO.getLeastCommonTimeStep();

		if (lookAheadTime == null)
			lookAheadTime = getLookAheadTime(leastCommonTimeStep);

		HLAinteger64Interval lookAheadInterval = getLookAheadInterval(leastCommonTimeStep);

		try
		{
			RTI_AMBASSADOR.enableTimeRegulation(lookAheadInterval);
		}
		catch (Exception e)
		{
			LOGGER.error("Failed to enable HLA time regulation\n[REASON]", e);
			System.exit(1);
		}
	}

	private static HLAinteger64Time getLookAheadTime(long leastCommonTimeStep)
	{
		return TIME_FACTORY.makeTime(leastCommonTimeStep);
	}

	private static HLAinteger64Interval getLookAheadInterval(long leastCommonTimeStep)
	{
		return TIME_FACTORY.makeInterval(leastCommonTimeStep);
	}

	public static HLAinteger64Time getLogicalTimeBoundary()
	{
		long leastCommonTimeStep = ExCO.getLeastCommonTimeStep();

		try
		{
			TimeQueryReturn galtQuery = RTI_AMBASSADOR.queryGALT();
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

	public static void advanceTime()
	{
		HLAinteger64Time nextTimeStep = getNextTimeStep();

		try
		{
			RTI_AMBASSADOR.timeAdvanceRequest(nextTimeStep);
		}
		catch (Exception e)
		{
			LOGGER.error("Failed to request time advance to the next time step\n[REASON]", e);
			System.exit(1);
		}
	}

	public static void advanceToLogicalTimeBoundary()
	{
		try
		{
			presentTime = getLogicalTimeBoundary();
			RTI_AMBASSADOR.timeAdvanceRequest(presentTime);
		}
		catch (Exception e)
		{
			LOGGER.error("Failed to advance to HLA logical time boundary\n[REASON]", e);
			System.exit(1);
		}
	}

	private static HLAinteger64Time getNextTimeStep()
	{
		long present = presentTime.getValue();
		long stepIncrement = lookAheadTime.getValue();
		long future = present + stepIncrement;

		HLAinteger64Time nextTimeStep = TIME_FACTORY.makeTime(future);
		System.out.println("Next Time Step: " + nextTimeStep.getValue());
		return nextTimeStep;
	}

	public static void setPresentTime(HLAinteger64Time newTime)
	{
		presentTime = newTime;
	}
}
