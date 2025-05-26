package io.github.vega.outpost.adapters;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;

import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.encoding.HLAfixedArray;
import hla.rti1516e.encoding.HLAfixedRecord;
import hla.rti1516e.encoding.HLAfloat64LE;
import io.github.vega.core.IAdapter;
import io.github.vega.outpost.components.PhysicsComponent;
import io.github.vega.outpost.components.PositionComponent;

public class SpaceTimeCoordinateStateAdapter implements IAdapter
{
	ComponentMapper<PositionComponent> positionMapper;
	ComponentMapper<PhysicsComponent> physicsMapper;

	public SpaceTimeCoordinateStateAdapter()
	{
		positionMapper = ComponentMapper.getFor(PositionComponent.class);
		physicsMapper = ComponentMapper.getFor(PhysicsComponent.class);
	}

	@Override
	public void deserialize(Entity entity, EncoderFactory encoder, byte[] buffer)
	{
		PositionComponent position = positionMapper.get(entity);
		PhysicsComponent physics = physicsMapper.get(entity);
		
		HLAfixedRecord spaceTimeCoordinateState = encoder.createHLAfixedRecord();
		HLAfixedArray<HLAfloat64LE> positionVector = encoder.createHLAfixedArray(
				encoder.createHLAfloat64LE(),
				encoder.createHLAfloat64LE(),
				encoder.createHLAfloat64LE());
		HLAfixedArray<HLAfloat64LE> velocityVector = encoder.createHLAfixedArray(encoder.createHLAfloat64LE(),
				encoder.createHLAfloat64LE(),
				encoder.createHLAfloat64LE());
		HLAfixedRecord translationalState = encoder.createHLAfixedRecord();
		translationalState.add(positionVector);
		translationalState.add(velocityVector);
		
		HLAfloat64LE scalar = encoder.createHLAfloat64LE();
		HLAfixedArray<HLAfloat64LE> vector = encoder.createHLAfixedArray(encoder.createHLAfloat64LE(),
				encoder.createHLAfloat64LE(),
				encoder.createHLAfloat64LE());
		HLAfixedRecord attitudeQuaternion = encoder.createHLAfixedRecord();
		attitudeQuaternion.add(scalar);
		attitudeQuaternion.add(vector);
		HLAfixedArray<HLAfloat64LE> angularVelocityVector = encoder.createHLAfixedArray(encoder.createHLAfloat64LE(),
				encoder.createHLAfloat64LE(),
				encoder.createHLAfloat64LE());
		HLAfixedRecord rotationalState = encoder.createHLAfixedRecord();
		rotationalState.add(attitudeQuaternion);
		rotationalState.add(angularVelocityVector);
		
		HLAfloat64LE time = encoder.createHLAfloat64LE();
		
		
		spaceTimeCoordinateState.add(translationalState);
		spaceTimeCoordinateState.add(rotationalState);
		spaceTimeCoordinateState.add(time);
		
		try
		{
			spaceTimeCoordinateState.decode(buffer);
			
			position.x = positionVector.get(0).getValue();
			position.y = positionVector.get(1).getValue();
			position.z = positionVector.get(2).getValue();
			
			physics.velocity.x = velocityVector.get(0).getValue();
			physics.velocity.y = velocityVector.get(1).getValue();
			physics.velocity.z = velocityVector.get(2).getValue();
			physics.scalar = scalar.getValue();
			
			physics.vector.x = vector.get(0).getValue();
			physics.vector.y = vector.get(1).getValue();
			physics.vector.z = vector.get(2).getValue();
			
			physics.angularRate.x = angularVelocityVector.get(0).getValue();
			physics.angularRate.y = angularVelocityVector.get(1).getValue();
			physics.angularRate.z = angularVelocityVector.get(2).getValue();
			
			physics.time = time.getValue();
		}
		catch (DecoderException e) {
			e.printStackTrace();
		}
	}

	@Override
	public byte[] serialize(Entity entity, EncoderFactory encoder)
	{
		PositionComponent position = positionMapper.get(entity);
		PhysicsComponent physics = physicsMapper.get(entity);

		HLAfixedArray<HLAfloat64LE> positionVector = encoder.createHLAfixedArray(encoder.createHLAfloat64LE(position.x),
				encoder.createHLAfloat64LE(position.y), encoder.createHLAfloat64LE(position.z));
		HLAfixedArray<HLAfloat64LE> velocityVector = physics.velocity.convert(encoder);
		HLAfixedRecord translationalState = encoder.createHLAfixedRecord();
		translationalState.add(positionVector);
		translationalState.add(velocityVector);
		
		HLAfloat64LE scalar = encoder.createHLAfloat64LE();
		scalar.setValue(physics.scalar);
		HLAfixedArray<HLAfloat64LE> vector = physics.vector.convert(encoder);
		HLAfixedRecord attitudeQuaternion = encoder.createHLAfixedRecord();
		attitudeQuaternion.add(scalar);
		attitudeQuaternion.add(vector);
		HLAfixedArray<HLAfloat64LE> angularVelocityVector = physics.angularRate.convert(encoder);
		HLAfixedRecord rotationalState = encoder.createHLAfixedRecord();
		rotationalState.add(attitudeQuaternion);
		rotationalState.add(angularVelocityVector);
		
		HLAfloat64LE time = encoder.createHLAfloat64LE();
		time.setValue(physics.time);
		
		HLAfixedRecord spaceTimeCoordinateState = encoder.createHLAfixedRecord();
		spaceTimeCoordinateState.add(translationalState);
		spaceTimeCoordinateState.add(rotationalState);
		spaceTimeCoordinateState.add(time);
		
		return spaceTimeCoordinateState.toByteArray();
	}
}
