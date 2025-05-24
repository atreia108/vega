package io.github.vega.outpost;

import io.github.vega.configuration.Configuration;
import io.github.vega.configuration.ConfigurationLoader;
import io.github.vega.core.SimulationBase;

public class Outpost extends SimulationBase
{

	@Override
	public void init()
	{

	}

	public static void main(String[] args)
	{
		new ConfigurationLoader("src/test/resources/Settings.xml");
		Configuration.get();
	}
}
