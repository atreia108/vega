package atreia108.vega.spacefom;

import atreia108.vega.core.AComponentData;
import atreia108.vega.types.IEncodeable;
import atreia108.vega.types.Vector3;
import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.encoding.HLAfixedArray;
import hla.rti1516e.encoding.HLAfloat64LE;

public class CenterOfMassComponent extends AComponentData implements IEncodeable<HLAfixedArray<HLAfloat64LE>> {
	public Vector3 com = new Vector3(0, 0, 0);
	
	public byte[] encode(HLAfixedArray<HLAfloat64LE> centerOfMass, EncoderFactory encoder) {
		centerOfMass.get(0).setValue(com.getX());
		centerOfMass.get(1).setValue(com.getY());
		centerOfMass.get(2).setValue(com.getZ());
		return centerOfMass.toByteArray();
	}
	
	public void decode(HLAfixedArray<HLAfloat64LE> centerOfMass, byte[] dataStream) {
		try {
			centerOfMass.decode(dataStream);
			double newX = centerOfMass.get(0).getValue();
			double newY = centerOfMass.get(1).getValue();
			double newZ = centerOfMass.get(2).getValue();
			com.set(newX, newY, newZ);
		} catch (DecoderException e) {
			e.printStackTrace();
		}
	}
}
