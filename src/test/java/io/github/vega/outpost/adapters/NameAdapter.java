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
		// No need to deserialize anything. The unique name of this entity is the instance name
		// in the HlaObjectComponent after all.
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
