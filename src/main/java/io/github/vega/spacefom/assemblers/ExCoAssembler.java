package io.github.vega.spacefom.assemblers;

import com.badlogic.ashley.core.Entity;

import io.github.vega.core.IAssembler;
import io.github.vega.core.World;
import io.github.vega.spacefom.components.ExCoComponent;

public class ExCoAssembler implements IAssembler
{
	@Override
	public Entity assembleEntity()
	{
		Entity exCo = World.createEntity();
		
		ExCoComponent exCoComponent = World.createComponent(ExCoComponent.class);
		// HlaObjectComponent objectComponent = World.createComponent(HlaObjectComponent.class);
		
		// objectComponent.className = "HLAobjectRoot.ExecutionConfiguration";
		// objectComponent.instanceName = "ExCO";
		
		World.addComponent(exCo, exCoComponent);
		// World.addComponent(exCo, objectComponent);
		
		return exCo;
	}
}
