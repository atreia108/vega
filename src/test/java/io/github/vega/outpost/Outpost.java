package io.github.vega.outpost;

import com.badlogic.ashley.core.Entity;

import io.github.vega.configuration.Configuration;
import io.github.vega.core.SimulationBase;
import io.github.vega.core.World;
import io.github.vega.hla.HlaObjectComponent;
import io.github.vega.math.Vector3;
import io.github.vega.outpost.components.PhysicsComponent;
import io.github.vega.outpost.components.PositionComponent;
import io.github.vega.outpost.systems.MovementSystem;

public class Outpost extends SimulationBase
{
	private static String configFilePath = "src/test/resources/Settings.xml";
	
	public Outpost()
	{
		super(configFilePath);
		exec();
	}
	
	@Override
	public void init()
	{
		Entity rover = World.createEntity();
		PositionComponent position = World.createComponent(PositionComponent.class);
		PhysicsComponent physics = World.createComponent(PhysicsComponent.class);
		
		physics.parentReferenceFrame = "SeeLunarSouthPoleBaseLocalFixed";
		physics.centerOfMass = new Vector3(0, 0, 0);
		physics.velocity = new Vector3(0, 0, 0);
		physics.vector = new Vector3(0, 0, 0);
		physics.angularRate = new Vector3(0, 0, 0);
		
		HlaObjectComponent object = World.createComponent(HlaObjectComponent.class);
		object.className = "HLAobjectRoot.PhysicalEntity";
		object.instanceName = "Rover";
		
		World.addComponent(rover, position);
		World.addComponent(rover, physics);
		World.addComponent(rover, object);
		
		World.addEntity(rover);
		World.addSystem(new MovementSystem());
	}

	public static void main(String[] args)
	{
		new Outpost();
	}
}
