package io.github.vega.outpost.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

import io.github.vega.core.Registry;
import io.github.vega.hla.HlaManager;
import io.github.vega.hla.HlaObjectComponent;
import io.github.vega.outpost.components.PositionComponent;

public class MovementSystem extends IteratingSystem
{
	ComponentMapper<PositionComponent> pm;
	ComponentMapper<HlaObjectComponent> hm;

	public MovementSystem()
	{
		super(Family.all(PositionComponent.class).get());
		pm = ComponentMapper.getFor(PositionComponent.class);
		hm = ComponentMapper.getFor(HlaObjectComponent.class);
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime)
	{
		showRemoteEntity();

		PositionComponent position = pm.get(entity);
		position.x += 1;
		position.y += 1;
		position.z += 1;

		HlaObjectComponent object = hm.get(entity);
		System.out.println(
				object.instanceName + "\'s position: (" + position.x + ", " + position.y + ", " + position.z + ")");
		HlaManager.sendUpdate(entity);
	}

	private void showRemoteEntity()
	{
		Entity remoteRover = Registry.findEntity("LunarRover");
		if (remoteRover != null)
		{
			PositionComponent position = pm.get(remoteRover);
			HlaObjectComponent object = hm.get(remoteRover);

			System.out.println(
					object.instanceName + "\'s position: (" + position.x + ", " + position.y + ", " + position.z + ")");
		}
	}
}
