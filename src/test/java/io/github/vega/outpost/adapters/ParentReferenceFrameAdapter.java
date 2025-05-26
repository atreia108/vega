package io.github.vega.outpost.adapters;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;

import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.encoding.HLAunicodeString;
import io.github.vega.core.IAdapter;
import io.github.vega.outpost.components.PhysicsComponent;

public class ParentReferenceFrameAdapter implements IAdapter
{
	ComponentMapper<PhysicsComponent> mapper;

	public ParentReferenceFrameAdapter()
	{
		mapper = ComponentMapper.getFor(PhysicsComponent.class);
	}

	@Override
	public void deserialize(Entity entity, EncoderFactory encoder, byte[] buffer)
	{
		PhysicsComponent physics = mapper.get(entity);
		HLAunicodeString encodedFrameName = encoder.createHLAunicodeString();

		try
		{
			encodedFrameName.decode(buffer);
			physics.parentReferenceFrame = encodedFrameName.getValue();
		}
		catch (DecoderException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public byte[] serialize(Entity entity, EncoderFactory encoder)
	{
		PhysicsComponent physics = mapper.get(entity);
		HLAunicodeString encodedFrameName = encoder.createHLAunicodeString();
		encodedFrameName.setValue(physics.parentReferenceFrame);
		
		return encodedFrameName.toByteArray();
	}

}
