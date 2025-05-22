/*-
 * SPDX-License-Identifier: BSD-3-Clause
 * Copyright (c) 2025 Hridyanshu Aatreya <2200096@brunel.ac.uk>
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

package io.github.vega.hla;

import java.util.Map;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.ashley.core.Entity;

import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.RtiFactory;
import hla.rti1516e.RtiFactoryFactory;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.exceptions.RTIinternalError;
import io.github.vega.core.IEntityAssembler;
import io.github.vega.core.IGenericAdapter;
import io.github.vega.settings.Settings;

public class HlaCallbackManager
{
	private static final Logger logger = LoggerFactory.getLogger(HlaCallbackManager.class);

	private static RTIambassador rtiAmbassador;
	private static EncoderFactory encoder;

	static
	{
		try
		{
			RtiFactory rtiFactory = RtiFactoryFactory.getRtiFactory();
			rtiAmbassador = rtiFactory.getRtiAmbassador();
			encoder = rtiFactory.getEncoderFactory();
		}
		catch (RTIinternalError e)
		{
			logger.error("{}", e);
		}
	}
	
	private static Entity assembleEntity(String assemblerName)
	{
		IEntityAssembler assembler = Settings.ASSEMBLERS.get(assemblerName);
		Entity entity = assembler.assemble();
		
		return entity;
	}
	
	private static void checkAllParametersPresent(HlaInteraction interaction, ParameterHandleValueMap theParameters)
	{
		String className = interaction.getClassName();
		Map<String, ParameterHandle> parameterHandles = interaction.getParameterHandles();
		
		for (String parameterName : parameterHandles.keySet())
		{
			ParameterHandle handle = parameterHandles.get(parameterName);
			if (!theParameters.containsKey(handle))
			{
				logger.warn("An HLA interaction of type \"{}\" was received from RTI. Skipping as values for some expected parameters is missing", className);
				return;
			}
		}
	}
	
	public RTIambassador getRtiAmbassador() { return rtiAmbassador; }
	
	public EncoderFactory getEncoderFactory() { return encoder; }

	public void receiveInteraction(InteractionClassHandle interactionClass, ParameterHandleValueMap theParameters,
			byte[] userSuppliedTag)
	{
		try
		{
			String className = rtiAmbassador.getInteractionClassName(interactionClass);
			HlaInteraction interaction = Settings.getHlaInteraction(className);
			
			if (interaction == null)
				logger.warn("An HLA interaction of type \"{}\" was received from RTI. Skipping it as no internal representation for it was generated during project initialization", className);
			
			checkAllParametersPresent(interaction, theParameters);
			
			Entity interactionEntity = assembleEntity(interaction.getAssemblerName());
			updateInteractionValues(interaction, theParameters, interactionEntity);
			HlaInteractionManager.addInteraction(interactionEntity);
		}
		catch (Exception e)
		{
			logger.error("{}", e);
		}
	}
	
	private static void updateInteractionValues(HlaInteraction interaction, ParameterHandleValueMap theParameters, Entity interactionEntity)
	{
		Map<String, ParameterHandle> parameterHandles = interaction.getParameterHandles();
		
		for (String parameterName : parameterHandles.keySet())
		{
			ParameterHandle handle = parameterHandles.get(parameterName);
			byte[] parameterData = theParameters.get(handle);
			
			IGenericAdapter adapter = Settings.ADAPTERS.get(parameterName);
			adapter.deserialize(interactionEntity, encoder, parameterData);
		}
	}
}
