package io.github.vega.core;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;

import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.encoding.EncoderFactory;
import io.github.vega.components.HLAInteractionComponent;
import io.github.vega.utils.FrameworkObjects;

public final class HLAInteractionManager
{
	private static final Logger LOGGER = LogManager.getLogger();
	private static final ComponentMapper<HLAInteractionComponent> INTERACTION_MAPPER = ComponentMapper.getFor(HLAInteractionComponent.class);
	
	public static boolean sendInteraction(Entity entity)
	{
		HLAInteractionComponent interactionComponent = INTERACTION_MAPPER.get(entity);
		InteractionClassProfile interactionClass = null ;

		if (interactionComponent == null || (interactionClass = ProjectRegistry.getInteractionClass(interactionComponent.className)) == null)
		{
			LOGGER.warn("Failed to send the interaction <{}> because either its HLAInteractionComponent is NULL or the interaction class associated with it is invalid", entity);
			return false;
		}

		InteractionClassHandle classHandle = interactionClass.classHandle;
		ParameterHandleValueMap parameterHandleValueMap = null;
		try
		{
			RTIambassador rtiAmbassador = FrameworkObjects.getRtiAmbassador();
			parameterHandleValueMap = getInteractionParameters(entity, interactionClass, rtiAmbassador);

			if ((parameterHandleValueMap == null) || (parameterHandleValueMap.size() == 0))
			{
				LOGGER.warn("Failed to send the interaction <{}> because no parameters could be found for this entity", entity);
				return false;
			}
			else
			{
				rtiAmbassador.sendInteraction(classHandle, parameterHandleValueMap, null);
				LOGGER.info("The interaction <{}> was successfully sent", entity);
				
				clearInteraction(entity);
				return true;
			}
		}
		catch (Exception e)
		{
			LOGGER.error("RTI ambassador failed to create a ParameterHandleValueMap for packing data", e);
			return false;
		}
		
	}

	private static ParameterHandleValueMap getInteractionParameters(Entity entity, InteractionClassProfile interactionClass, RTIambassador rtiAmbassador)
	{
		ParameterHandleValueMap parameterHandleValueMap = null;

		int numberOfParameters = interactionClass.getNumberOfParameters();

		if (numberOfParameters < 1)
			return parameterHandleValueMap;

		Map<String, ParameterHandle> parameterHandleMap = interactionClass.getParameterHandleMap();
		EncoderFactory encoderFactory = FrameworkObjects.getEncoderFactory();

		try
		{
			parameterHandleValueMap = rtiAmbassador.getParameterHandleValueMapFactory().create(parameterHandleMap.size());

			for (String parameterName : parameterHandleMap.keySet())
			{
				ParameterHandle parameterHandle = interactionClass.getParameterHandle(parameterName);
				String dataConverterName = null;
				byte[] encodedValue = null;

				if (interactionClass.parameterUsesMultiConverter(parameterName))
				{
					dataConverterName = interactionClass.getParameterMultiConverterName(parameterName);
					IMultiDataConverter multiDataConverter = ProjectRegistry.getMultiConverter(dataConverterName);
					int trigger = interactionClass.getParameterMultiConverterTrigger(parameterName, dataConverterName);
					encodedValue = multiDataConverter.encode(entity, encoderFactory, trigger);
				}
				else
				{
					dataConverterName = interactionClass.getParameterConverterName(parameterName);
					IDataConverter dataConverter = ProjectRegistry.getDataConverter(dataConverterName);
					encodedValue = dataConverter.encode(entity, encoderFactory);
				}

				parameterHandleValueMap.put(parameterHandle, encodedValue);
			}
		}
		catch (Exception e)
		{
			LOGGER.error("RTI ambassador failed to create a ParameterHandleValueMap for packing data", e);
		}

		return parameterHandleValueMap;
	}
	
	/**
	 * Deletes all components from the entity that represents the HLA interaction to free them up for
	 * use by other entities.
	 * @param interaction The entity representing the HLA interaction that is to be freed
	 * @since 1.0
	 */
	private static void clearInteraction(Entity interaction)
	{
		interaction.removeAll();
	}
}
