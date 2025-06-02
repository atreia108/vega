package io.github.vega.spacefom.assemblers;

import com.badlogic.ashley.core.Entity;

import io.github.vega.core.IAssembler;
import io.github.vega.core.World;
import io.github.vega.spacefom.components.MTRComponent;

public class MTRAssembler implements IAssembler
{

	@Override
	public Entity assembleEntity()
	{
		Entity modeTransitionRequest = World.createEntity();
		MTRComponent mtrComponent = World.createComponent(MTRComponent.class);
		World.addComponent(modeTransitionRequest, mtrComponent);
		
		return modeTransitionRequest;
	}

}
