package atreia108.vega.types;

import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.encoding.HLAfixedArray;
import hla.rti1516e.encoding.HLAfloat64LE;

public class Vector3 implements IConvertable<HLAfixedArray<HLAfloat64LE>> {
	public double x;
	public double y;
	public double z;
	
	public static final Vector3 BACK = new Vector3(0, 0, -1);
	public static final Vector3 DOWN = new Vector3(0, -1, 0);
	public static final Vector3 FORWARD = new Vector3(0, 0, 1);
	public static final Vector3 LEFT = new Vector3(-1, 0, 0);
	public static final Vector3 NEGATIVE_INFINITY = new Vector3(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
	public static final Vector3 ONE = new Vector3(1, 1, 1);
	public static final Vector3 POSITIVE_INFINITY = new Vector3(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
	public static final Vector3 RIGHT = new Vector3(1, 0, 0);
	public static final Vector3 UP = new Vector3(0, 1, 0);
	public static final Vector3 ZERO = new Vector3(0, 0, 0);
	
	public Vector3(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public HLAfixedArray<HLAfloat64LE> convert(EncoderFactory encoder) {
		HLAfixedArray<HLAfloat64LE> convertedVector = encoder.createHLAfixedArray(
				encoder.createHLAfloat64LE(x),
				encoder.createHLAfloat64LE(y),
				encoder.createHLAfloat64LE(z)
		);
		
		return convertedVector;
	}
}
