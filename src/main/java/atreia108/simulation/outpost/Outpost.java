package atreia108.simulation.outpost;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;

import atreia108.vega.core.ASimulation;
import atreia108.vega.core.HlaFederateAmbassador;
import atreia108.vega.core.World;
import atreia108.vega.types.IRemoteEntityFactory;

public class Outpost extends ASimulation {
	private HlaFederateAmbassador federateAmbassador;
	private World world;
	
	private Entity lunarRover;
	
	public Outpost() {
		federateAmbassador = new HlaFederateAmbassador(this);
		world = new World(federateAmbassador);
		
		lunarRover = world.createEntity();
		HlaComponent hlaComponent = world.createComponent(HlaComponent.class);
		PositionComponent positionComponent = world.createComponent(PositionComponent.class);
		
		world.addComponent(lunarRover, HlaComponent.class);
		world.addComponent(lunarRover, PositionComponent.class);
		
		for (Component component : lunarRover.getComponents()) {
			System.out.println(component);
		}
		
		System.out.println("**************");
		world.removeComponent(lunarRover, PositionComponent.class);
		
		for (Component component : lunarRover.getComponents()) {
			System.out.println(component);
		}
		
		IRemoteEntityFactory remoteEntityDefinition = () -> {
			Entity entity = new Entity();
			PositionComponent pos = new PositionComponent();
			// pos.vec3.x = 0.0;
			// pos.vec3.y = 0.1;
			// pos.vec3.z = -200.0;
			
			return entity;
		};
		
		federateAmbassador.beginExecution();
		update();
	}
	
	public static void main(String[] args) {
		new Outpost();
	}
}
