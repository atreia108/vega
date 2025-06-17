package io.github.vega.spacefom.archetypes;

import com.badlogic.ashley.core.Entity;

import io.github.vega.core.IEntityArchetype;
import io.github.vega.core.World;

public class ExecutionConfiguration implements IEntityArchetype
{

	@Override
	public Entity create()
	{
		Entity exCO = World.createEntity();
		
		return exCO;
	}

}
