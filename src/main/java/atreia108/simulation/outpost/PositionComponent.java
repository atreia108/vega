package atreia108.simulation.outpost;

import atreia108.vega.types.IComponent;
import atreia108.vega.types.IEncodeable;
import atreia108.vega.types.Vector3;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.encoding.HLAinteger32BE;

public class PositionComponent implements IComponent, IEncodeable<HLAinteger32BE> {
	public Vector3 vec3 = new Vector3(0, 0, 0);

	public byte[] encode(HLAinteger32BE target, EncoderFactory encoder) {
		
		return null;
	}

	public void decode(HLAinteger32BE source, byte[] data) {
		
	}
	
	
}
