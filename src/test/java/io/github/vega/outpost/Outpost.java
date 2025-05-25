package io.github.vega.outpost;

import com.badlogic.ashley.core.Entity;

import io.github.vega.configuration.Configuration;
import io.github.vega.core.SimulationBase;
import io.github.vega.core.World;

public class Outpost extends SimulationBase
{
	private static String configFilePath = "src/test/resources/Settings.xml";
	
	public Outpost()
	{
		super(configFilePath);
		Configuration.get();
	}
	
	@Override
	public void init()
	{
		Entity rover = World.createEntity();
		
		World.addEntity(rover);
	}

	public static void main(String[] args)
	{
		new Outpost();
	}
}
