package atreia108.vega.spacefom;

import atreia108.vega.core.AComponentData;
import atreia108.vega.types.IEncodeable;
import atreia108.vega.types.Vector3;
import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.encoding.HLAfixedArray;
import hla.rti1516e.encoding.HLAfixedRecord;
import hla.rti1516e.encoding.HLAfloat64LE;

public class SpaceTimeCoordinateStateComponent extends AComponentData implements IEncodeable<HLAfixedRecord> {

	public double time = 0;
	
	// ReferenceFrameTranslation
	public Vector3 positionVector = new Vector3(0, 0, 0);
	public Vector3 velocityVector = new Vector3(0, 0, 0);
	
	// ReferenceFrameRotation
	double attitudeQuaternionScalar = 0;
	public Vector3 attitudeQuaternionVector = new Vector3(0, 0, 0);
	public Vector3 angularVelocityVector = new Vector3(0, 0, 0);

	public byte[] encode(HLAfixedRecord state, EncoderFactory encoder) {
		HLAfloat64LE timeField = encoder.createHLAfloat64LE();
		timeField.setValue(time);
		
		HLAfixedRecord translationalState = encoder.createHLAfixedRecord();
		HLAfixedArray<HLAfloat64LE> positionField = positionVector.getHlaRepresentation(encoder);
		HLAfixedArray<HLAfloat64LE> velocityField = velocityVector.getHlaRepresentation(encoder);
		translationalState.add(positionField);
		translationalState.add(velocityField);
		
		HLAfixedRecord rotationalState = encoder.createHLAfixedRecord();
		HLAfixedRecord attitudeQuaternion = encoder.createHLAfixedRecord();
		HLAfloat64LE scalarField = encoder.createHLAfloat64LE(attitudeQuaternionScalar);
		HLAfixedArray<HLAfloat64LE> vectorField = attitudeQuaternionVector.getHlaRepresentation(encoder);
		attitudeQuaternion.add(scalarField);
		attitudeQuaternion.add(vectorField);
		HLAfixedArray<HLAfloat64LE> angularVelocityField = angularVelocityVector.getHlaRepresentation(encoder);
		rotationalState.add(attitudeQuaternion);
		rotationalState.add(angularVelocityField);
		
		return state.toByteArray();
	}

	public void decode(HLAfixedRecord state, byte[] dataStream) {
		try {
			state.decode(dataStream);
		} catch (DecoderException e) {
			e.printStackTrace();
		}
	}
}
