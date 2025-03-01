package atreia108.vega.types;

import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.encoding.HLAfixedArray;
import hla.rti1516e.encoding.HLAfixedRecord;
import hla.rti1516e.encoding.HLAfloat64LE;

public class AttitudeQuaternion implements IConvertable<HLAfixedRecord> {
	
	public double scalar;
	public Vector3 vector;
	
	public HLAfixedRecord convert(EncoderFactory encoder) {
		HLAfixedRecord convertedAttitudeQuaternion = encoder.createHLAfixedRecord();
		HLAfloat64LE convertedScalar = encoder.createHLAfloat64LE();
		HLAfixedArray<HLAfloat64LE> convertedVector = vector.convert(encoder);
		
		convertedAttitudeQuaternion.add(convertedScalar);
		convertedAttitudeQuaternion.add(convertedVector);
		
		return convertedAttitudeQuaternion;
	}

}
