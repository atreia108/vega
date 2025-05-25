package io.github.vega.spacefom;

import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.NullFederateAmbassador;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.OrderType;
import hla.rti1516e.TransportationTypeHandle;
import hla.rti1516e.exceptions.FederateInternalError;
import io.github.vega.hla.HlaCallbackManager;
import io.github.vega.hla.HlaManager;

public class SpaceFomFederateAmbassador extends NullFederateAmbassador
{
	@Override
	public void reflectAttributeValues(ObjectInstanceHandle theObject, AttributeHandleValueMap theAttributes,
			byte[] userSuppliedTag, OrderType sentOrdering, TransportationTypeHandle theTransport,
			SupplementalReflectInfo reflectInfo) throws FederateInternalError
	{
		HlaCallbackManager.reflectAttributeValues(theObject, theAttributes);
	}
	
	@Override
	public void discoverObjectInstance(final ObjectInstanceHandle theObject, ObjectClassHandle theObjectClass,
			String objectName)
	{
		HlaCallbackManager.discoverObjectInstance(theObject, theObjectClass, objectName);
	}
	
	@Override
	public void objectInstanceNameReservationFailed(String objectName)
	{
		HlaCallbackManager.objectInstanceNameReservationFailed(objectName);
	}
	
	@Override
	public void objectInstanceNameReservationSucceeded(String objectName)
	{
		HlaCallbackManager.objectInstanceNameReservationSucceeded(objectName);
	}
}
