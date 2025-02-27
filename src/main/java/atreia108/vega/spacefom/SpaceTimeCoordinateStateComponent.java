package atreia108.vega.spacefom;

import atreia108.vega.core.AComponentData;
import atreia108.vega.types.IEncodeable;
import hla.rti1516e.encoding.HLAfixedRecord;
import hla.rti1516e.encoding.EncoderFactory;

public class SpaceTimeCoordinateStateComponent extends AComponentData implements IEncodeable<HLAfixedRecord> {

	public double time = 0;

	public byte[] encode(EncoderFactory encoder, HLAfixedRecord element) {
		// TODO
		return null;
	}

	public void decode(HLAfixedRecord element, byte[] data) {
		// TODO
	}
}
