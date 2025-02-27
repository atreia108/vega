package atreia108.vega.spacefom;

import atreia108.vega.core.AComponentData;
import atreia108.vega.types.IEncodeable;
import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.encoding.HLAunicodeString;

public class ParentReferenceFrameComponent extends AComponentData implements IEncodeable<HLAunicodeString>{
	public String name = "SeeLunarSouthPoleBaseLocalFixed";
	
	public byte[] encode(HLAunicodeString parentReferenceFrameName, EncoderFactory encoder) {
		parentReferenceFrameName.setValue(name);
		return parentReferenceFrameName.toByteArray();
	}

	public void decode(HLAunicodeString parentReferenceFrameName, byte[] data) {
		try {
			parentReferenceFrameName.decode(data);
			name = parentReferenceFrameName.getValue();
		} catch (DecoderException e) {
			e.printStackTrace();
		}
	}
}
