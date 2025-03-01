package atreia108.vega.types;

import hla.rti1516e.encoding.DataElement;
import hla.rti1516e.encoding.EncoderFactory;

public interface IConvertable<T extends DataElement> {
	public T convert(EncoderFactory e);
}
