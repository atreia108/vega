package io.github.vega.spacefom.archetypes;

import com.badlogic.ashley.core.Entity;

import io.github.vega.core.IEntityArchetype;
import io.github.vega.core.World;

public class ModeTransitionRequest implements IEntityArchetype
{

	@Override
	public Entity create()
	{
		Entity modeTransitionRequest = World.createEntity();
		
		return modeTransitionRequest;
	}

}
