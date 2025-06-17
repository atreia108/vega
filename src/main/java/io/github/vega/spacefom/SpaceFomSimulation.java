package io.github.vega.spacefom;

import com.badlogic.ashley.core.Entity;

import io.github.vega.core.SimulationBase;

public abstract class SpaceFomSimulation extends SimulationBase
{
	protected Entity exCO;
	
	public SpaceFomSimulation(String projectFilePath)
	{
		super(projectFilePath);
		new SpaceFomInitializer(this);
	}
	
	// An extra state we've included for defining ECS-related operations as well as other things that users may have implemented.
	// The "Launch" State is called during the "Register Federate Object Instances" step of the SpaceFOM late joiner initialization p.80.
	// It is anticipated that users will initialize whatever entities they plan to use in the simulation within this method.
	protected abstract void launchState();
	
	protected abstract void runState();
	
	protected abstract void shutdownState();
	
	protected abstract void freezeState();
}
