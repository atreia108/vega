package io.github.vega.spacefom.adapters;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;

import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.encoding.HLAunicodeString;
import io.github.vega.core.IAdapter;
import io.github.vega.spacefom.components.ExCoComponent;

public class RfnAdapter implements IAdapter
{

	@Override
	public void deserialize(Entity entity, EncoderFactory encoder, byte[] buffer)
	{
		HLAunicodeString encodedString = encoder.createHLAunicodeString();
		
		try
		{
			encodedString.decode(buffer);
			
			ComponentMapper<ExCoComponent> mapper = ComponentMapper.getFor(ExCoComponent.class);
			ExCoComponent component = mapper.get(entity);
			component.rootFrameName = encodedString.getValue();
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
		
		HLAunicodeString encodedString = encoder.createHLAunicodeString();
		encodedString.setValue(component.rootFrameName);
		
		return encodedString.toByteArray();
	}

}
