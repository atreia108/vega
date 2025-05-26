package io.github.vega.spacefom.adapters;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;

import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.encoding.HLAinteger64BE;
import io.github.vega.core.IAdapter;
import io.github.vega.hla.HlaManager;
import io.github.vega.spacefom.components.ExCoComponent;

public class LctsAdapter implements IAdapter
{
	@Override
	public void deserialize(Entity entity, EncoderFactory encoder, byte[] buffer)
	{
		HLAinteger64BE int64 = encoder.createHLAinteger64BE();
		
		try
		{
			int64.decode(buffer);
			ComponentMapper<ExCoComponent> mapper = ComponentMapper.getFor(ExCoComponent.class);
			ExCoComponent component = mapper.get(entity);
			component.leastCommonTimeStep = int64.getValue();
		}
		catch (DecoderException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public byte[] serialize(Entity entity, EncoderFactory encoder)
	{
		ComponentMapper<ExCoComponent> mapper = ComponentMapper.getFor(ExCoComponent.class);
		ExCoComponent component = mapper.get(entity);
		
		HLAinteger64BE encodedInteger = encoder.createHLAinteger64BE();
		encodedInteger.setValue(component.leastCommonTimeStep);
		
		return encodedInteger.toByteArray();
	}

}
