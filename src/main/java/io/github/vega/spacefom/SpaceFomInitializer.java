package io.github.vega.spacefom;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.vega.hla.HLAManager;

public class SpaceFomInitializer
{
	private static final Logger LOGGER = LogManager.getLogger();
	
	public SpaceFomInitializer(SpaceFomSimulation simulation)
	{
		initialize(simulation);
	}
	
	private void initialize(SpaceFomSimulation simulation)
	{
		HLAManager.connect(new SpaceFomFederateAmbassador());
		
		final int TOTAL_STEPS = 0;
		int stepsCompleted = 0;
		
		LOGGER.info("({}/{})", ++stepsCompleted, TOTAL_STEPS);
		subscribeExCO();
	}
	
	/*
	 * The Mode Transition Request (MTR) and Execution Configuration (ExCO) are "special". We manually create the type representations
	 * for both in here because they are to be declared to the RTI *before* all other objects/interactions (see SISO SpaceFOM Standard p. 79-80).
	 * So, we refrain from going full auto-pilot on the pub/sub declarations for our simulation.
	 */
	
	private void subscribeExCO()
	{
		
	}
	
	private void publishMTR()
	{
		
	}
	
	private void discoverExCO()
	{
		
	}
	
	private void updateExCO()
	{
		
	}
	
	private void declareFederateClasses()
	{
		
	}
	
	private void discoverRequiredObjects()
	{
		
	}
	
	private void initTimeManagement()
	{
		
	}
}
