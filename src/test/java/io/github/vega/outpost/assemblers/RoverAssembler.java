package io.github.vega.outpost.assemblers;

import com.badlogic.ashley.core.Entity;

import io.github.vega.core.IAssembler;
import io.github.vega.core.World;
import io.github.vega.math.Vector3;
import io.github.vega.outpost.components.PhysicsComponent;
import io.github.vega.outpost.components.PositionComponent;

public class RoverAssembler implements IAssembler
{
	@Override
	public Entity assembleEntity()
	{
		Entity rover = World.createEntity();
		PositionComponent position = World.createComponent(PositionComponent.class);
		PhysicsComponent physics = World.createComponent(PhysicsComponent.class);
		physics.parentReferenceFrame = "";
		physics.centerOfMass = new Vector3(0, 0, 0);
		physics.velocity = new Vector3(0, 0, 0);
		physics.vector = new Vector3(0, 0, 0);
		physics.angularRate = new Vector3(0, 0, 0);
		
		World.addComponent(rover, position);
		World.addComponent(rover, physics);
		
		return rover;
	}
}
