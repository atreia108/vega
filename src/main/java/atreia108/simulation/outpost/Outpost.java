package atreia108.simulation.outpost;

import atreia108.vega.core.ASimulation;
import atreia108.vega.core.HlaFederateAmbassador;
import atreia108.vega.core.World;

public class Outpost extends ASimulation {
	private HlaFederateAmbassador federateAmbassador;
	private World world;
	
	public Outpost() {
		federateAmbassador = new HlaFederateAmbassador(this);
		world = new World(this, federateAmbassador);
		
		federateAmbassador.beginExecution();
		update();
	}
	
	public static void main(String[] args) {
		new Outpost();
	}
}
