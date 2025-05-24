package io.github.vega.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.vega.configuration.Configuration;
import io.github.vega.configuration.ConfigurationLoader;

class ParserTest
{
	private static final String CONFIG_DIRECTORY = "src/test/resources/Settings.xml";
	
	/*
	private static final String SPACEFOM_DATATYPES_PATH = "src/test/resources/SISO_SpaceFOM_datatypes.xml";
	private static final String SPACEFOM_ENTITY_PATH = "src/test/resources/SISO_SpaceFOM_entity.xml";
	private static final String SPACEFOM_ENVIRONMENT_PATH = "src/test/resources/SISO_SpaceFOM_environment.xml";
	private static final String SPACEFOM_MANAGEMENT_PATH = "src/test/resources/SISO_SpaceFOM_management.xml";
	private static final String SPACEFOM_SWITCHES_PATH = "src/test/resources/SISO_SpaceFOM_switches.xml";
	
	private static Set<String> SPACEFOM_MODULES_PATHS = new HashSet<String>();
	*/
	
	@BeforeAll
	static void setUpBeforeClass() throws Exception
	{
		new ConfigurationLoader(CONFIG_DIRECTORY);
		/*
		SPACEFOM_MODULES_PATHS.add(SPACEFOM_DATATYPES_PATH);
		SPACEFOM_MODULES_PATHS.add(SPACEFOM_ENTITY_PATH);
		SPACEFOM_MODULES_PATHS.add(SPACEFOM_ENVIRONMENT_PATH);
		SPACEFOM_MODULES_PATHS.add(SPACEFOM_MANAGEMENT_PATH);
		SPACEFOM_MODULES_PATHS.add(SPACEFOM_SWITCHES_PATH);
		*/
	}

	@Test
	public void testRtiProperties()
	{
		assertEquals("localhost", Configuration.getHostName());
		assertEquals(8989, Configuration.getPort());
		assertEquals("LunarOutpost", Configuration.getFederateName());
		assertEquals("SEE 2026", Configuration.getFederationName());
	}
	
	@Test
	public void testSimulationProperties()
	{
		assertEquals(1000, Configuration.getMinSimulatedEntities());
		assertEquals(10000, Configuration.getMaxSimulatedEntities());
		assertEquals(500, Configuration.getMinComponents());
		assertEquals(10000, Configuration.getMaxComponents());
	}
}
