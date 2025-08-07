package io.github.vega.utils;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.PooledEngine;

import hla.rti1516e.RTIambassador;
import hla.rti1516e.RtiFactory;
import hla.rti1516e.RtiFactoryFactory;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.exceptions.RTIinternalError;
import io.github.vega.components.ExCOComponent;
import io.github.vega.components.HLAInteractionComponent;
import io.github.vega.components.HLAObjectComponent;

public final record FrameworkObjects()
{
	private static RtiFactory rtiFactory;
	private static RTIambassador rtiAmbassador;
	private static EncoderFactory encoderFactory;
	
	private static PooledEngine engine;
	private static final ComponentMapper<HLAObjectComponent> HLA_OBJECT_MAPPER = ComponentMapper.getFor(HLAObjectComponent.class);
	private static final ComponentMapper<HLAInteractionComponent> HLA_INTERACTION_MAPPER = ComponentMapper.getFor(HLAInteractionComponent.class);
	private static final ComponentMapper<ExCOComponent> SPACEFOM_EXCO_MAPPER = ComponentMapper.getFor(ExCOComponent.class);
	
	static
	{
		try
		{
			rtiFactory = RtiFactoryFactory.getRtiFactory();
			rtiAmbassador = rtiFactory.getRtiAmbassador();
			encoderFactory = rtiFactory.getEncoderFactory();
		}
		catch (RTIinternalError e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public static RtiFactory getRtiFactory()
	{
		return rtiFactory;
	}
	
	public static RTIambassador getRtiAmbassador()
	{
		return rtiAmbassador;
	}
	
	public static EncoderFactory getEncoderFactory()
	{
		return encoderFactory;
	}
	
	public static PooledEngine getEngine()
	{
		return engine;
	}
	
	public static ComponentMapper<HLAObjectComponent> getHLAObjectComponentMapper()
	{
		return HLA_OBJECT_MAPPER;
	}
	
	public static ComponentMapper<HLAInteractionComponent> getHLAInteractionComponentMapper()
	{
		return HLA_INTERACTION_MAPPER;
	}
	
	public static ComponentMapper<ExCOComponent> getExCOComponentMapper()
	{
		return SPACEFOM_EXCO_MAPPER;
	}
	
	protected static void setEngineParameters(int minEntities, int maxEntities, int minComponents, int maxComponents)
	{
		if (engine != null)
			return;
		
		engine = new PooledEngine(minEntities, maxEntities, minComponents, maxComponents);
	}
}
