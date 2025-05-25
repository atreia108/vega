package io.github.vega.hla;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.ashley.core.Entity;

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.encoding.EncoderFactory;
import io.github.vega.core.EntityDatabase;
import io.github.vega.core.IAdapter;
import io.github.vega.core.IAssembler;
import io.github.vega.core.SimulationBase;
import io.github.vega.core.ThreadLatch;

public class HlaCallbackManager
{
	private static final Logger logger = LoggerFactory.getLogger(HlaCallbackManager.class);

	public static void discoverObjectInstance(final ObjectInstanceHandle theObject, ObjectClassHandle theObjectClass,
			String objectName)
	{
		RTIambassador rtiAmbassador = HlaManager.getRtiAmbassador();

		try
		{
			String objectClassName = rtiAmbassador.getObjectClassName(theObjectClass);
			HlaObjectType type = EntityDatabase.getObjectType(objectClassName);
			IAssembler assembler = EntityDatabase.getAssembler(type.getAssemblerName());
			Entity entity = assembler.assembleEntity();

			EntityDatabase.addEntityForInstance(entity, theObject);
			SimulationBase.setExCo(entity);
			
			HlaManager.latestObjectInstanceUpdates(theObject, type);
		}
		catch (Exception e)
		{
			logger.warn("Could not create entity representation for instance {} of object class {}\n[REASON]\n",
					theObject, theObjectClass);
			e.printStackTrace();
		}

		if (!HlaManager.isInitialized()) ThreadLatch.stop();
	}

	public static void reflectAttributeValues(ObjectInstanceHandle theObject, AttributeHandleValueMap theAttributes)
	{
		Entity entity = EntityDatabase.getEntity(theObject);
		RTIambassador rtiAmbassador = HlaManager.getRtiAmbassador();
		
		try
		{
			ObjectClassHandle classHandle = rtiAmbassador.getKnownObjectClassHandle(theObject);
			String className = rtiAmbassador.getObjectClassName(classHandle);
			
			HlaObjectType type = EntityDatabase.getObjectType(className);
			AttributeHandleSet attributeHandleSet = type.getRtiAttributeHandleSet();
			
			if (!containsAllAttributes(attributeHandleSet, theAttributes))
			{
				logger.warn("Updated set of values received for [Entity: {}, Instance: {}, Object Class: {}] was missing one or more required attributes. No changes were made to the simulation.");
				return;
			}
			
			updateEntity(entity, type, theAttributes);
		}
		catch (Exception e) {
			logger.warn("Could not set the updated values for {}\n[REASON]\n", entity);
			e.printStackTrace();
		}
		
		if (!HlaManager.isInitialized()) ThreadLatch.stop();
	}
	
	private static boolean containsAllAttributes(AttributeHandleSet required, AttributeHandleValueMap candidate)
	{
		for (AttributeHandle attribute : required)
		{
			if (!candidate.containsKey(attribute))
				return false;
		}
		
		return true;
	}
	
	private static void updateEntity(Entity entity, HlaObjectType objectType, AttributeHandleValueMap newValues)
	{
		EncoderFactory encoder = HlaManager.getEncoderFactory();
		
		Set<String> attributeNames = objectType.getAttributeNames();
		
		for (String name : attributeNames)
		{
			AttributeHandle attributeHandle = objectType.getAttributeHandle(name);
			byte[] attributeValue = newValues.get(attributeHandle);
			
			String adapterName = objectType.getAdapterName(name);
			IAdapter adapter = EntityDatabase.getAdapter(adapterName);
			adapter.deserialize(entity, encoder, attributeValue);
		}
	}
	
	public static void objectInstanceNameReservationFailed(String objectName)
	{
		
	}
	
	public static void objectInstanceNameReservationSucceeded(String objectName)
	{
		
	}
}
