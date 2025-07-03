package io.github.vega.data;

import com.badlogic.ashley.core.Entity;

import io.github.vega.archetypes.ExecutionConfiguration;
import io.github.vega.components.ExCOComponent;
import io.github.vega.core.World;

public class ExCO
{
	private static Entity exCO;
	private static ExCOComponent exCoComponent;
	
	private ExCO()
	{
		exCO = new ExecutionConfiguration().createEntity();
		exCoComponent = World.getComponent(exCO, ExCOComponent.class);
	}
	
	public static String getRootFrameName()
	{
		return exCoComponent.rootFrameName;
	}
	
	public static ExecutionMode getCurrentExecutionMode()
	{
		return exCoComponent.currentExecutionMode;
	}
	
	public static ExecutionMode getNextExecutionMode()
	{
		return exCoComponent.nextExecutionMode;
	}
	
	public static long getLeastCommonTimeStep()
	{
		return exCoComponent.leastCommonTimeStep;
	}
	
	public static Entity getEntity()
	{
		if (exCO == null)
			new ExCO();
		
		return exCO;
	}
}
