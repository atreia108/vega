/*-
 * SPDX-License-Identifier: BSD-3-Clause
 * Copyright (c) 2025 Hridyanshu Aatreya <Hridyanshu.Aatreya2@brunel.ac.uk>
 * All rights reserved.

 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
 * 1. Redistributions of source code must retain the above copyright notice, 
 *	  this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the 
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its 
 * 	  contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * 
 */

package io.github.atreia108.vega.core;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.badlogic.ashley.core.Entity;

import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.encoding.EncoderFactory;
import io.github.atreia108.vega.components.HLAInteractionComponent;
import io.github.atreia108.vega.utils.VegaUtilities;

/**
 * The <code>HLAInteractionManager</code> class enables sending interactions to the RTI.
 * 
 * @author Hridyanshu Aatreya
 * @since 1.0.0
 */
public final class HLAInteractionManager
{
	private static final Logger LOGGER = LogManager.getLogger();
	
	/**
	 * Creates an interaction from a valid entity to be sent to the RTI.
	 * A valid entity in this case:
	 * <ul>
	 * 	<li> Contains an HLAInteractionComponent.
	 * 	<li> The className field in the component is set to an interaction class defined in the simulation's project file.
	 *  <li> The interaction class in question was previously declared as publish/subscribe or both to the RTI.
	 * </ul>
	 * Invalid entities are rejected and the interaction is not sent.
	 * @param entity entity representing the interaction.
	 * @return outcome of the operation as a true or false value.
	 */
	public static boolean sendInteraction(Entity entity)
	{
		HLAInteractionComponent interactionComponent = VegaUtilities.interactionComponentMapper().get(entity);
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
			RTIambassador rtiAmbassador = VegaUtilities.rtiAmbassador();
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
		EncoderFactory encoderFactory = VegaUtilities.encoderFactory();

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
	 * @param interaction entity representing the HLA interaction that is to be freed.
	 */
	private static void clearInteraction(Entity interaction)
	{
		interaction.removeAll();
	}
}
