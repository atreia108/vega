package io.github.vega.hla;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.CallbackModel;
import hla.rti1516e.LogicalTimeFactoryFactory;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.ResignAction;
import hla.rti1516e.RtiFactory;
import hla.rti1516e.RtiFactoryFactory;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.exceptions.RTIexception;
import hla.rti1516e.exceptions.RTIinternalError;
import hla.rti1516e.time.HLAinteger64TimeFactory;
import io.github.vega.configuration.Configuration;
import io.github.vega.core.EntityDatabase;
import io.github.vega.spacefom.SpaceFomFederateAmbassador;

public class HlaManager
{
	private static final Logger logger = LoggerFactory.getLogger(HlaManager.class);

	private static RTIambassador rtiAmbassador;
	private static EncoderFactory encoderFactory;
	private static HLAinteger64TimeFactory timeFactory;

	private static boolean initialized;
	private static Object reservationSemaphore;
	private static boolean reservationStatus;

	static
	{
		try
		{
			RtiFactory rtiFactory = RtiFactoryFactory.getRtiFactory();
			rtiAmbassador = rtiFactory.getRtiAmbassador();
			encoderFactory = rtiFactory.getEncoderFactory();
			timeFactory = LogicalTimeFactoryFactory.getLogicalTimeFactory(HLAinteger64TimeFactory.class);
			initialized = false;
			reservationSemaphore = new Object();
			reservationStatus = false;
		}
		catch (RTIinternalError e)
		{
			logger.error("[REASON]\n");
			e.printStackTrace();
		}
	}

	public static void connect()
	{
		String federateName = Configuration.getFederateName();
		String federationName = Configuration.getFederationName();

		try
		{
			rtiAmbassador.connect(new SpaceFomFederateAmbassador(), CallbackModel.HLA_IMMEDIATE);
			rtiAmbassador.joinFederationExecution(federateName, federationName);
			logger.info("Successfully joined the HLA federation <" + federationName + "> as <" + federateName + ">.");
		}
		catch (Exception e)
		{
			logger.error("Failed to join the HLA federation <" + federationName + ">.\n" + "[REASON]\n");
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static void disconnect()
	{
		try
		{
			rtiAmbassador.resignFederationExecution(ResignAction.CANCEL_THEN_DELETE_THEN_DIVEST);
			logger.info("Simulation terminated successfully...");
			System.exit(1);
		}
		catch (RTIexception e)
		{
			logger.error("[REASON]\n");
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static boolean sendUpdate(Entity entity)
	{
		ComponentMapper<HlaObjectComponent> mapper = ComponentMapper.getFor(HlaObjectComponent.class);
		HlaObjectComponent object = mapper.get(entity);

		if (!checkHlaObjectComponent(entity, object))
			return false;

		return true;
	}

	public static boolean sendInteraction(Entity entity)
	{
		ComponentMapper<HlaInteractionComponent> mapper = ComponentMapper.getFor(HlaInteractionComponent.class);
		HlaInteractionComponent interaction = mapper.get(entity);

		if (!checkHlaInteractionComponent(entity, interaction))
			return false;

		return true;
	}

	private static boolean checkHlaObjectComponent(Entity entity, HlaObjectComponent object)
	{
		String typeName = object.className;

		if (EntityDatabase.getObjectType(typeName) == null)
		{
			logger.warn(
					"The class name \"{}\" specified in the HlaObjectComponent of {} is either invalid or was not published/subscribed at runtime.",
					typeName, entity);
			return false;
		}

		if (EntityDatabase.getObjectInstance(entity) == null)
		{
			logger.warn(
					"Updates cannot be sent for {} because it has no corresponding object instance at the RTI. The object instance may have been previously deleted.",
					entity);
			return false;
		}

		return true;
	}

	private static boolean checkHlaInteractionComponent(Entity entity, HlaInteractionComponent interaction)
	{
		String typeName = interaction.className;

		if (EntityDatabase.getInteractionType(typeName) == null)
		{
			logger.warn(
					"The class name \"{}\" specified in the HlaInteractionComponent of {} is either invalid or was not published/subscribed at runtime.",
					typeName, entity);
			return false;
		}

		return true;
	}

	/*
	 * TODO - For object classes, evaluate the consequences of attempting to pub/sub
	 * with an empty attribute set
	 */
	public static void publishObject(HlaObjectType type)
	{
		String name = type.getName();

		try
		{
			ObjectClassHandle classHandle = rtiAmbassador.getObjectClassHandle(name);
			AttributeHandleSet attributeSetHandle = rtiAmbassador.getAttributeHandleSetFactory().create();

			Set<String> publisheableAttributes = type.getPublisheableAttributes();

			publisheableAttributes.forEach((attribute) ->
			{
				try
				{
					AttributeHandle attributeHandle = rtiAmbassador.getAttributeHandle(classHandle, attribute);
					attributeSetHandle.add(attributeHandle);
					type.registerAttributeHandle(name, attributeHandle);
				}
				catch (Exception e)
				{
					logger.warn("Could not publish HLA object class <" + name + ">." + "\n[REASON]\n");
					e.printStackTrace();
				}
			});

			rtiAmbassador.publishObjectClassAttributes(classHandle, attributeSetHandle);

			type.setRtiAttributeHandleSet(attributeSetHandle);
			type.intentDeclared();
		}
		catch (Exception e)
		{
			logger.warn("Could not publish HLA object class <" + name + ">." + "\n[REASON]\n");
			e.printStackTrace();
		}
	}

	public static void publishInteraction(HlaInteractionType type)
	{

	}

	public static void subscribeObject(HlaObjectType type)
	{
		String typeName = type.getName();

		try
		{
			ObjectClassHandle classHandle = rtiAmbassador.getObjectClassHandle(typeName);
			AttributeHandleSet attributeSetHandle = rtiAmbassador.getAttributeHandleSetFactory().create();

			Set<String> subscribeableAttributes = type.getSubscribeableAttributes();

			subscribeableAttributes.forEach((attributeName) ->
			{
				try
				{
					AttributeHandle attributeHandle = rtiAmbassador.getAttributeHandle(classHandle, attributeName);
					attributeSetHandle.add(attributeHandle);
					type.registerAttributeHandle(attributeName, attributeHandle);
				}
				catch (Exception e)
				{
					logger.warn("Could not subscribe HLA object class <" + typeName + ">." + "\n[REASON]");
					e.printStackTrace();
				}
			});

			rtiAmbassador.subscribeObjectClassAttributes(classHandle, attributeSetHandle);

			type.setRtiAttributeHandleSet(attributeSetHandle);
			type.intentDeclared();
		}
		catch (Exception e)
		{
			logger.warn("Could not subscribe HLA object class <" + typeName + ">." + "\n[REASON]\n");
			e.printStackTrace();
		}
	}

	public static void subscribeInteraction(HlaInteractionType type)
	{

	}
	
	public static void registerObjectInstance(String instanceName, String className)
	{
		
	}

	public static void latestObjectInstanceUpdates(ObjectInstanceHandle handle, HlaObjectType type)
	{
		AttributeHandleSet attributeSet = type.getRtiAttributeHandleSet();
		try
		{
			rtiAmbassador.requestAttributeValueUpdate(handle, attributeSet, null);
		}
		catch (Exception e)
		{
			logger.warn("Unable to request the latest updates for object instance of type <{}>.\n[REASON]\n",
					type.getName());
			e.printStackTrace();
		}
	}

	public static RTIambassador getRtiAmbassador()
	{
		return rtiAmbassador;
	}

	public static EncoderFactory getEncoderFactory()
	{
		return encoderFactory;
	}
	
	public static HLAinteger64TimeFactory getLogicalTimeFactoryFactory()
	{
		return timeFactory;
	}

	public static boolean isInitialized()
	{
		return initialized;
	}
	
	public static Object getReservationSemaphore()
	{
		return reservationSemaphore;
	}
	
	public static void setReservationStatus(boolean status)
	{
		reservationStatus = status;
	}

	public static void initialized()
	{
		initialized = true;
	}
}
