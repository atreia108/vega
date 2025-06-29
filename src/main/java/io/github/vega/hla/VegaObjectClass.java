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

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.RTIambassador;

public class VegaObjectClass
{
	private static final Logger LOGGER = LogManager.getLogger();

	public String name;
	public String archetypeName;
	public ObjectClassHandle classHandle;
	public Set<String> attributeNames;
	public Map<String, String> attributeConverterMap;
	public Map<String, AttributeHandle> attributeHandleMap;
	public Map<String, SharingModel> attributeSharingMap;
	public Map<String, Map<String, Integer>> attributeMultiConverterMap;

	// A flag used to determine whether an HLA object/interaction type should be
	// automatically declared to the RTI or not.
	// If set to false, it means we intend to manually handle the declaration
	// ourselves.
	public boolean declareAutomatically;

	public boolean isPublished;
	public boolean isSubscribed;

	public VegaObjectClass(String name, String archetypeName, boolean declareAutomatically)
	{
		this.name = name;
		this.archetypeName = archetypeName;
		this.declareAutomatically = declareAutomatically;
		
		attributeNames = new HashSet<String>();
		attributeConverterMap = new HashMap<String, String>();

		attributeHandleMap = new HashMap<String, AttributeHandle>();
		attributeSharingMap = new HashMap<String, SharingModel>();

		attributeMultiConverterMap = new HashMap<String, Map<String, Integer>>();
		
		isPublished = false;
		isSubscribed = false;
	}

	public void addAttribute(String attributeName, SharingModel sharingModel)
	{
		attributeNames.add(attributeName);
		attributeSharingMap.put(attributeName, sharingModel);
	}

	public void addConverter(String attributeName, String converterName)
	{
		attributeConverterMap.put(attributeName, converterName);
	}

	public void addMultiConverter(String attributeName, String converterName, int trigger)
	{
		Map<String, Integer> multiConverterTriggerMap = new HashMap<String, Integer>();
		multiConverterTriggerMap.put(converterName, trigger);

		attributeMultiConverterMap.put(attributeName, multiConverterTriggerMap);
	}

	public String getConverterName(String attributeName)
	{
		return attributeConverterMap.get(attributeName);
	}

	public String getMultiConverterName(String attributeName)
	{
		Map<String, Integer> multiConverterParameters = attributeMultiConverterMap.get(attributeName);

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

	public int getConverterTrigger(String attributeName, String multiConverterName)
	{
		Map<String, Integer> multiConverterParameters = attributeMultiConverterMap.get(attributeName);
		return multiConverterParameters.get(multiConverterName);
	}

	public boolean getConverter(String attributeName)
	{
		String converterName = attributeConverterMap.get(attributeName);

		if (converterName != null)
			return true;
		else
			return false;
	}

	public AttributeHandle getAttributeHandle(String attributeName)
	{
		return attributeHandleMap.get(attributeName);
	}

	public boolean isMultiConverter(String attributeName)
	{
		String multiConverterName = getMultiConverterName(attributeName);

		if (multiConverterName != null)
		{
			return true;
		}
		else
			return false;
	}

	public SharingModel getSharingModel(String attributeName)
	{
		return attributeSharingMap.get(attributeName);
	}

	public void addAttributeHandle(String attributeName, AttributeHandle attributeHandle)
	{
		attributeHandleMap.put(attributeName, attributeHandle);
	}

	public Set<String> publisheableAttributeNames()
	{
		Set<String> result = new HashSet<String>();

		attributeSharingMap.forEach((attribute, sharing) ->
		{
			if (sharing == SharingModel.PUBLISH_ONLY || sharing == SharingModel.PUBLISH_SUBSCRIBE)
				result.add(attribute);
		});

		return result;
	}

	public Set<AttributeHandle> publisheableAttributeHandles()
	{
		Set<AttributeHandle> result = new HashSet<AttributeHandle>();

		attributeHandleMap.forEach((attributeName, handle) ->
		{
			SharingModel sharingModel = getSharingModel(attributeName);

			if (sharingModel == SharingModel.PUBLISH_ONLY || sharingModel == SharingModel.PUBLISH_SUBSCRIBE)
				result.add(handle);
		});

		return result;
	}

	public Set<String> subscribeableAttributeNames()
	{
		Set<String> result = new HashSet<String>();

		attributeSharingMap.forEach((attribute, sharing) ->
		{
			if (sharing == SharingModel.SUBSCRIBE_ONLY || sharing == SharingModel.PUBLISH_SUBSCRIBE)
				result.add(attribute);
		});

		return result;
	}

	public Set<AttributeHandle> subscribeableAttributeHandles()
	{
		Set<AttributeHandle> result = new HashSet<AttributeHandle>();

		attributeHandleMap.forEach((attributeName, handle) ->
		{
			SharingModel sharingModel = getSharingModel(attributeName);

			if (sharingModel == SharingModel.SUBSCRIBE_ONLY || sharingModel == SharingModel.PUBLISH_SUBSCRIBE)
				result.add(handle);
		});

		return result;
	}
	
	public void declare()
	{
		publish();
		subscribe();
	}

	public void publish()
	{
		if (isPublished)
		{
			LOGGER.warn("The HLA object class <{}> was not published since this intention has already been shared with the RTI", name);
			return;
		}

		RTIambassador rtiAmbassador = VegaRTIAmbassador.instance();

		try
		{
			if (classHandle == null)
				classHandle = rtiAmbassador.getObjectClassHandle(name);

			AttributeHandleSet publicationSetHandle = rtiAmbassador.getAttributeHandleSetFactory().create();

			for (String attributeName : publisheableAttributeNames())
			{
				AttributeHandle attributeHandle = attributeHandleMap.get(attributeName);

				if (attributeHandle == null)
				{
					attributeHandle = rtiAmbassador.getAttributeHandle(classHandle, attributeName);
					addAttributeHandle(attributeName, attributeHandle);
				}

				publicationSetHandle.add(attributeHandle);
			}

			rtiAmbassador.publishObjectClassAttributes(classHandle, publicationSetHandle);
		}
		catch (Exception e)
		{
			LOGGER.error("Failed to publish the HLA object class <{}>\n[REASON]", name, e);
			System.exit(1);
		}

		isPublished = true;
	}

	public void subscribe()
	{
		if (isSubscribed)
		{
			LOGGER.warn("The HLA object class <{}> was not subscribed to since this intention has already been shared with the RTI", name);
			return;
		}

		RTIambassador rtiAmbassador = VegaRTIAmbassador.instance();

		try
		{
			if (classHandle == null)
				classHandle = rtiAmbassador.getObjectClassHandle(name);

			AttributeHandleSet subscriptionSetHandle = rtiAmbassador.getAttributeHandleSetFactory().create();

			for (String attributeName : subscribeableAttributeNames())
			{
				AttributeHandle attributeHandle = attributeHandleMap.get(attributeName);

				if (attributeHandle == null)
				{
					attributeHandle = rtiAmbassador.getAttributeHandle(classHandle, attributeName);
					addAttributeHandle(attributeName, attributeHandle);
				}

				subscriptionSetHandle.add(attributeHandle);
			}

			rtiAmbassador.subscribeObjectClassAttributes(classHandle, subscriptionSetHandle);
		}
		catch (Exception e)
		{
			LOGGER.error("Failed to subscribe to the HLA object class <{}>\n[REASON]", name, e);
			System.exit(1);
		}

		isSubscribed = true;
	}
}
