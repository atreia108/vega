package io.github.atreia108.vega.utils;

import java.util.HashMap;
import java.util.Map;

import hla.rti1516e.ObjectInstanceHandle;

/**
 * Bi-directional mapping between HLA object instance names and associated RTI handles.
 * 
 * @author Hridyanshu Aatreya
 * @since 1.0.0
 */
public class EntityMapping
{
	private final Map<ObjectInstanceHandle, String> map = new HashMap<ObjectInstanceHandle, String>();
	private final Map<String, ObjectInstanceHandle> inverseMap = new HashMap<String, ObjectInstanceHandle>();
	
	public void put(ObjectInstanceHandle handle, String instanceName)
	{
		map.put(handle, instanceName);
		inverseMap.put(instanceName, handle);
	}
	
	public boolean hasEntity(ObjectInstanceHandle handle)
	{
		return map.containsKey(handle);
	}
	
	public boolean hasEntity(String instanceName)
	{
		return inverseMap.containsKey(instanceName);
	}
	
	public String translate(ObjectInstanceHandle handle)
	{
		return map.get(handle);
	}
	
	public ObjectInstanceHandle translate(String instanceName)
	{
		return inverseMap.get(instanceName);
	}
	
	public void remove(ObjectInstanceHandle handle)
	{
		inverseMap.remove(map.get(handle));
		map.remove(handle);
	}
}
