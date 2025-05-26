package io.github.vega.core;

import hla.rti1516e.encoding.DataElement;
import hla.rti1516e.encoding.EncoderFactory;

public interface IGenericConverter<T extends DataElement>
{
	public T convert(EncoderFactory encoder);
}
