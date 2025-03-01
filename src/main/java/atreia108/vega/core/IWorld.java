package atreia108.vega.core;

import com.badlogic.ashley.core.Entity;

public interface IWorld {
	public Entity createEntity();
	public void destroyEntity();
}
