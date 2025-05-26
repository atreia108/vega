package io.github.vega.outpost.adapters;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;

import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.encoding.HLAunicodeString;
import io.github.vega.core.IAdapter;
import io.github.vega.hla.HlaObjectComponent;

public class NameAdapter implements IAdapter
{
	ComponentMapper<HlaObjectComponent> mapper;

	public NameAdapter()
	{
		mapper = ComponentMapper.getFor(HlaObjectComponent.class);
	}

	@Override
	public void deserialize(Entity entity, EncoderFactory encoder, byte[] buffer)
	{
		/*
		HLAunicodeString encodedName = encoder.createHLAunicodeString();

		try
		{
			encodedName.decode(buffer);
			HlaObjectComponent objectComponent = mapper.get(entity);
			objectComponent.instanceName = encodedName.getValue();
		}
		catch (DecoderException e)
		{
			e.printStackTrace();
		}
		*/
	}

	@Override
	public byte[] serialize(Entity entity, EncoderFactory encoder)
	{
		HlaObjectComponent objectComponent = mapper.get(entity);
		HLAunicodeString encodedName = encoder.createHLAunicodeString();
		encodedName.setValue(objectComponent.instanceName);

		return encodedName.toByteArray();
	}

}
