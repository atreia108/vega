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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.RTIambassador;

public class VegaInteractionClass
{
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Marker HLA_MARKER = MarkerManager.getMarker("HLA");

	public String name;
	public String archetypeName;
	public HLASharingModel sharingModel;
	public InteractionClassHandle classHandle;
	public Set<String> parameterNames;
	public Map<String, String> parameterConverterNameMap;
	public Map<String, ParameterHandle> parameterHandleMap;
	public Map<String, Map<String, Integer>> parameterMultiConverterNameMap;

	// A flag used to determine whether an HLA object/interaction type should be
	// automatically declared to the RTI or not.
	// If set to false, it means we intend to manually handle the declaration
	// ourselves.
	public boolean declareAutomatically;

	public boolean isPublished;
	public boolean isSubscribed;

	public VegaInteractionClass(String name, String archetypeName, HLASharingModel sharingModel, boolean declareAutomatically)
	{
		this.name = name;
		this.archetypeName = archetypeName;
		this.sharingModel = sharingModel;
		this.declareAutomatically = declareAutomatically;

		parameterNames = new HashSet<String>();
		parameterConverterNameMap = new HashMap<String, String>();
		parameterHandleMap = new HashMap<String, ParameterHandle>();

		parameterMultiConverterNameMap = new HashMap<String, Map<String, Integer>>();

		isPublished = false;
		isSubscribed = false;
	}

	public void addParameter(String parameterName)
	{
		parameterNames.add(parameterName);
	}

	public void addConverter(String parameterName, String converterName)
	{
		parameterConverterNameMap.put(parameterName, converterName);
	}

	public void addMultiConverter(String parameterName, String converterName, int trigger)
	{
		Map<String, Integer> multiConverterTriggerMap = new HashMap<String, Integer>();
		multiConverterTriggerMap.put(converterName, trigger);

		parameterMultiConverterNameMap.put(parameterName, multiConverterTriggerMap);
	}

	public String getParameterConverterName(String parameterName)
	{
		return parameterConverterNameMap.get(parameterName);
	}

	public String getParameterMultiConverterName(String parameterName)
	{
		Map<String, Integer> multiConverterParameters = parameterMultiConverterNameMap.get(parameterName);

		if (multiConverterParameters != null)
		{
			String multiConverterName = null;

			for (String name : multiConverterParameters.keySet())
				multiConverterName = name;

			return multiConverterName;
		}
		else
			return null;
	}

	public int getParameterMultiConverterTrigger(String parameterName, String converterName)
	{
		Map<String, Integer> multiConverterParameters = parameterMultiConverterNameMap.get(parameterName);
		return multiConverterParameters.get(converterName);
	}

	public ParameterHandle getParameterHandle(String parameterName)
	{
		return parameterHandleMap.get(parameterName);
	}

	public boolean parameterUsesConverter(String parameterName)
	{
		String converterName = parameterConverterNameMap.get(parameterName);

		if (converterName != null)
			return true;
		else
			return false;
	}

	public boolean parameterUsesMultiConverter(String parameterName)
	{
		String multiConverterName = getParameterMultiConverterName(parameterName);

		if (multiConverterName != null)
		{
			return true;
		}
		else
			return false;
	}

	public void addParameterHandle(String parameterName, ParameterHandle handle)
	{
		parameterHandleMap.put(parameterName, handle);
	}

	public boolean publisheable()
	{
		if (sharingModel == HLASharingModel.PUBLISH_ONLY || sharingModel == HLASharingModel.PUBLISH_SUBSCRIBE)
			return true;
		else
			return false;
	}

	public boolean subscribeable()
	{
		if (sharingModel == HLASharingModel.SUBSCRIBE_ONLY || sharingModel == HLASharingModel.PUBLISH_SUBSCRIBE)
			return true;
		else
			return false;
	}
	
	public void declare()
	{
		if (sharingModel == HLASharingModel.PUBLISH_ONLY)
			publish();
		else if (sharingModel == HLASharingModel.SUBSCRIBE_ONLY)
			subscribe();
		else
		{
			publish();
			subscribe();
		}
	}

	public void publish()
	{
		if (isPublished)
		{
			LOGGER.warn(HLA_MARKER, "The HLA interaction class <{}> was not published since this intention has already been shared with the RTI", name);
			return;
		}

		RTIambassador rtiAmbassador = VegaRTIAmbassador.instance();

		try
		{
			if (classHandle == null)
				classHandle = rtiAmbassador.getInteractionClassHandle(name);
			
			if (parameterHandleMap.isEmpty())
			{
				for (String parameterName : parameterNames)
				{
					ParameterHandle parameterHandle = rtiAmbassador.getParameterHandle(classHandle, parameterName);
					addParameterHandle(parameterName, parameterHandle);
				}
			}
			
			rtiAmbassador.publishInteractionClass(classHandle);
		}
		catch (Exception e)
		{
			LOGGER.error(HLA_MARKER, "Failed to publish the HLA interaction class <{}>\n[REASON]", name, e);
			System.exit(1);
		}
		
		isPublished = true;
		LOGGER.info(HLA_MARKER, "The HLA interaction class <{}> was successfully published", name);
	}

	public void subscribe()
	{
		if (isSubscribed)
		{
			LOGGER.warn(HLA_MARKER, "The HLA interaction class <{}> was not subscribed to since this intention has already been shared with the RTI", name);
			return;
		}

		RTIambassador rtiAmbassador = VegaRTIAmbassador.instance();

		try
		{
			if (classHandle == null)
				classHandle = rtiAmbassador.getInteractionClassHandle(name);
			
			if (parameterHandleMap.isEmpty())
			{
				for (String parameterName : parameterNames)
				{
					ParameterHandle parameterHandle = rtiAmbassador.getParameterHandle(classHandle, parameterName);
					addParameterHandle(parameterName, parameterHandle);
				}
			}
			
			rtiAmbassador.subscribeInteractionClass(classHandle);
		}
		catch (Exception e)
		{
			LOGGER.error(HLA_MARKER, "Failed to subscribe to the HLA interaction class <{}>\n[REASON]", name, e);
			System.exit(1);
		}
		
		isSubscribed = true;
		LOGGER.info(HLA_MARKER, "The HLA interaction class <{}> was successfully subscribed to", name);
	}
}
