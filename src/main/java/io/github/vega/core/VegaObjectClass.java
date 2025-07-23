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

package io.github.vega.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.RTIambassador;
import io.github.vega.utils.HLASharingModel;

public final class VegaObjectClass
{
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Marker HLA_MARKER = MarkerManager.getMarker("HLA");

	public String name;
	public String archetypeName;
	public ObjectClassHandle classHandle;
	public Set<String> attributeNames;

	private Map<String, String> attributeConverterMap;
	private BidiMap<String, AttributeHandle> attributeHandleMap;
	private Map<String, HLASharingModel> attributeSharingMap;
	private Map<String, Map<String, Integer>> attributeMultiConverterMap;
	private AttributeHandleSet publicationHandleSet;
	private AttributeHandleSet subscriptionHandleSet;

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

		attributeHandleMap = new DualHashBidiMap<String, AttributeHandle>();
		attributeSharingMap = new HashMap<String, HLASharingModel>();

		attributeMultiConverterMap = new HashMap<String, Map<String, Integer>>();

		isPublished = false;
		isSubscribed = false;
	}

	public void addAttribute(String attributeName, HLASharingModel sharingModel)
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

	public String getAttributeConverterName(String attributeName)
	{
		return attributeConverterMap.get(attributeName);
	}

	public String getAttributeMultiConverterName(String attributeName)
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

	public int getAttributeConverterTrigger(String attributeName, String multiConverterName)
	{
		Map<String, Integer> multiConverterParameters = attributeMultiConverterMap.get(attributeName);
		return multiConverterParameters.get(multiConverterName);
	}

	public boolean attributeUsesConverter(String attributeName)
	{
		String converterName = attributeConverterMap.get(attributeName);

		if (converterName != null)
			return true;
		else
			return false;
	}

	public AttributeHandle getHandleForAttribute(String attributeName)
	{
		return attributeHandleMap.get(attributeName);
	}
	
	public String getAttributeNameForHandle(AttributeHandle attributeHandle)
	{
		return attributeHandleMap.getKey(attributeHandle);
	}

	public boolean attributeUsesMultiConverter(String attributeName)
	{
		String multiConverterName = getAttributeMultiConverterName(attributeName);

		if (multiConverterName != null)
			return true;
		else
			return false;
	}

	public HLASharingModel getSharingModel(String attributeName)
	{
		return attributeSharingMap.get(attributeName);
	}

	public void addAttributeHandle(String attributeName, AttributeHandle attributeHandle)
	{
		attributeHandleMap.put(attributeName, attributeHandle);
	}

	public Set<String> getPublisheableAttributeNames()
	{
		Set<String> result = new HashSet<String>();

		attributeSharingMap.forEach((attribute, sharing) ->
		{
			if (sharing == HLASharingModel.PUBLISH_ONLY || sharing == HLASharingModel.PUBLISH_SUBSCRIBE)
				result.add(attribute);
		});

		return result;
	}

	public AttributeHandleSet getPublisheableAttributeHandles()
	{
		if (publicationHandleSet == null)
		{
			RTIambassador rtiAmbassador = VegaRTIAmbassador.instance();
			try
			{
				AttributeHandleSet result = rtiAmbassador.getAttributeHandleSetFactory().create();

				attributeHandleMap.forEach((attributeName, handle) ->
				{
					HLASharingModel sharingModel = getSharingModel(attributeName);

					if (sharingModel == HLASharingModel.PUBLISH_ONLY || sharingModel == HLASharingModel.PUBLISH_SUBSCRIBE)
						result.add(handle);
				});

				return result;
			}
			catch (Exception e)
			{
				LOGGER.error(HLA_MARKER, "Could not generate attribute handle set containing published attributes for the HLA object class <{}>\n[REASON]", e, name);
				System.exit(1);
			}
		}
		else
			return publicationHandleSet;

		return null;
	}

	public Set<String> getSubscribeableAttributeNames()
	{
		Set<String> result = new HashSet<String>();

		attributeSharingMap.forEach((attribute, sharing) ->
		{
			if (sharing == HLASharingModel.SUBSCRIBE_ONLY || sharing == HLASharingModel.PUBLISH_SUBSCRIBE)
				result.add(attribute);
		});

		return result;
	}

	public AttributeHandleSet getSubscribeableAttributeHandles()
	{
		if (subscriptionHandleSet == null)
		{
			RTIambassador rtiAmbassador = VegaRTIAmbassador.instance();
			try
			{
				AttributeHandleSet result = rtiAmbassador.getAttributeHandleSetFactory().create();

				attributeHandleMap.forEach((attributeName, handle) ->
				{
					HLASharingModel sharingModel = getSharingModel(attributeName);

					if (sharingModel == HLASharingModel.SUBSCRIBE_ONLY || sharingModel == HLASharingModel.PUBLISH_SUBSCRIBE)
						result.add(handle);
				});

				return result;
			}
			catch (Exception e)
			{
				LOGGER.error(HLA_MARKER, "Could not generate attribute handle set containing subscribed attributes for the HLA object class <{}>\n[REASON]", e, name);
				System.exit(1);
			}
		}
		else
			return subscriptionHandleSet;

		return null;
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
			LOGGER.warn(HLA_MARKER, "The HLA object class <{}> was not published since this intention has already been shared with the RTI", name);
			return;
		}

		RTIambassador rtiAmbassador = VegaRTIAmbassador.instance();

		try
		{
			if (classHandle == null)
				classHandle = rtiAmbassador.getObjectClassHandle(name);

			AttributeHandleSet publicationHandleSet = rtiAmbassador.getAttributeHandleSetFactory().create();

			for (String attributeName : getPublisheableAttributeNames())
			{
				AttributeHandle attributeHandle = attributeHandleMap.get(attributeName);

				if (attributeHandle == null)
				{
					attributeHandle = rtiAmbassador.getAttributeHandle(classHandle, attributeName);
					addAttributeHandle(attributeName, attributeHandle);
				}

				publicationHandleSet.add(attributeHandle);
			}

			rtiAmbassador.publishObjectClassAttributes(classHandle, publicationHandleSet);

			this.publicationHandleSet = publicationHandleSet;
		}
		catch (Exception e)
		{
			LOGGER.error(HLA_MARKER, "Failed to publish the HLA object class <{}>\n[REASON]", name, e);
			System.exit(1);
		}

		isPublished = true;
		LOGGER.info(HLA_MARKER, "The HLA object class <{}> was successfully published", name);
	}

	public void subscribe()
	{
		if (isSubscribed)
		{
			LOGGER.warn(HLA_MARKER, "The HLA object class <{}> was not subscribed to since this intention has already been shared with the RTI", name);
			return;
		}

		RTIambassador rtiAmbassador = VegaRTIAmbassador.instance();

		try
		{
			if (classHandle == null)
				classHandle = rtiAmbassador.getObjectClassHandle(name);

			AttributeHandleSet subscriptionHandleSet = rtiAmbassador.getAttributeHandleSetFactory().create();

			for (String attributeName : getSubscribeableAttributeNames())
			{
				AttributeHandle attributeHandle = attributeHandleMap.get(attributeName);

				if (attributeHandle == null)
				{
					attributeHandle = rtiAmbassador.getAttributeHandle(classHandle, attributeName);
					addAttributeHandle(attributeName, attributeHandle);
				}

				subscriptionHandleSet.add(attributeHandle);
			}

			rtiAmbassador.subscribeObjectClassAttributes(classHandle, subscriptionHandleSet);

			this.subscriptionHandleSet = subscriptionHandleSet;
		}
		catch (Exception e)
		{
			LOGGER.error(HLA_MARKER, "Failed to subscribe to the HLA object class <{}>\n[REASON]", name, e);
			System.exit(1);
		}

		isSubscribed = true;
		LOGGER.info(HLA_MARKER, "The HLA object class <{}> was successfully subscribed to", name);
	}
	
	public int getNumberOfPublisheableAttributes()
	{
		return publicationHandleSet.size();
	}
	
	public int getNumberOfSubscribeableAttributes()
	{
		return subscriptionHandleSet.size();
	}
}
