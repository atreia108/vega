package io.github.vega.hla;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;

import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.RTIambassador;
import io.github.vega.utils.ProjectRegistry;

public class VegaDataManager
{
	private static final Logger LOGGER = LogManager.getLogger();
	
	private static final ComponentMapper<HLAObjectComponent> objectComponentMapper = ComponentMapper.getFor(HLAObjectComponent.class);
	private static final ComponentMapper<HLAInteractionComponent> interactionComponentMapper = ComponentMapper.getFor(HLAInteractionComponent.class);
	
	private static int registeredInstancesCount = 0;
	
	public static boolean registerObjectInstance(Entity entity)
	{
		HLAObjectComponent objectComponent = objectComponentMapper.get(entity);
		
		if (isInstanceAlive(objectComponent))
		{
			LOGGER.warn("Omitted registration for the object instance <{}>\n[REASON]\nIt has already been registered with the RTI", objectComponent.instanceName);
			return false;
		}
		
		Object nameReservationSemaphore = VegaCallbackManager.getNameReservationSemaphore();
		RTIambassador rtiAmbassador = VegaRTIAmbassador.instance();
		
		try
		{
			synchronized (nameReservationSemaphore)
			{
				rtiAmbassador.reserveObjectInstanceName(objectComponent.instanceName);
				awaitReservation();
			}
			
			boolean nameReservationStatus = VegaCallbackManager.getNameReservationStatus();
			if (!nameReservationStatus)
			{
				LOGGER.warn("Failed to reserve the name <{}>. The corresponding HLA object instance for this entity was not created", objectComponent.instanceName);
				return false;
			}
			
			VegaObjectClass objectClass = ProjectRegistry.getObjectClass(objectComponent.className);
			ObjectClassHandle classHandle = objectClass.classHandle;
			
			ObjectInstanceHandle instanceHandle = rtiAmbassador.registerObjectInstance(classHandle, objectComponent.instanceName);
			objectComponent.handle = instanceHandle;
			
			LOGGER.info("Created the HLA object instance \"{}\" of the class <{}>", objectComponent.instanceName, objectComponent.className);
			registeredInstancesCount += 1;
		}
		catch (Exception e)
		{
			LOGGER.warn("Failed to reserve the name <{}>. The corresponding HLA object instance for this entity was not created\n[REASON]", objectComponent.instanceName, e);
			return false;
		}
		
		return false;
	}
	
	private static void awaitReservation()
	{
		Object nameReservationSemaphore = VegaCallbackManager.getNameReservationSemaphore();

		try
		{
			synchronized (nameReservationSemaphore)
			{
				nameReservationSemaphore.wait();
			}
		}
		catch (InterruptedException e)
		{
			LOGGER.error("Unexpected interrupt while waiting for the reservation of an object instance's name\n[REASON]", e);
			System.exit(1);
		}
	}
	
	private static boolean isInstanceAlive(HLAObjectComponent objectComponent)
	{
		if (objectComponent.handle != null)
			return true;
		
		return false;
	}
	
	public static boolean destroyObjectInstance(Entity entity)
	{
		return false;
	}
	
	public static boolean updateObjectInstance(Entity entity)
	{
		return false;
	}
	
	public static boolean sendInteraction(Entity entity)
	{
		return false;
	}
	
	public static int getRegisteredInstancesCount()
	{
		return registeredInstancesCount;
	}
}
