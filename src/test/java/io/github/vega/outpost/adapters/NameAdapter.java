package io.github.vega.outpost.adapters;

import com.badlogic.ashley.core.Entity;

import hla.rti1516e.encoding.EncoderFactory;
import io.github.vega.core.IAdapter;

public class NameAdapter implements IAdapter
{

	@Override
	public void deserialize(Entity entity, EncoderFactory encoder, byte[] buffer)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public byte[] serialize(Entity entity, EncoderFactory encoder)
	{
		// TODO Auto-generated method stub
		return null;
	}

}
