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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.ObjectClassHandle;

public class HlaObjectType
{
	private String name;
	private String assemblerName;
	private ObjectClassHandle classHandle;
	private Map<String, String> attributeAdapterNameMap;
	private Map<String, PubSubModel> attributePubSubMap;
	private Map<String, AttributeHandle> attributeNameHandleMap;
	private AttributeHandleSet attributeHandleSet;

	private boolean intentDeclaredToRti;

	public HlaObjectType(String classType, String assembler)
	{
		name = classType;
		assemblerName = assembler;
		attributeAdapterNameMap = new HashMap<String, String>();
		attributePubSubMap = new HashMap<String, PubSubModel>();
		attributeNameHandleMap = new HashMap<String, AttributeHandle>();
	}

	public void registerAttribute(String attributeName, String adapterName, PubSubModel attributePubSub)
	{
		attributeAdapterNameMap.put(attributeName, adapterName);
		attributePubSubMap.put(attributeName, attributePubSub);
	}

	public String getAdapterName(String attributeName)
	{
		return attributeAdapterNameMap.get(attributeName);
	}

	public Set<String> getAttributeNames()
	{
		return attributeAdapterNameMap.keySet();
	}

	public List<String> getAdapterNames()
	{
		List<String> adapters = new ArrayList<String>(attributeAdapterNameMap.values());
		return adapters;
	}

	public Set<String> getPublisheableAttributes()
	{
		Set<String> result = new HashSet<String>();

		attributePubSubMap.forEach((attribute, pubSub) ->
		{
			if (pubSub == PubSubModel.PUBLISH_ONLY || pubSub == PubSubModel.PUBLISH_SUBSCRIBE)
				result.add(attribute);
		});

		return result;
	}

	public Set<String> getSubscribeableAttributes()
	{
		Set<String> result = new HashSet<String>();

		attributePubSubMap.forEach((attribute, pubSub) ->
		{
			if (pubSub == PubSubModel.SUBSCRIBE_ONLY || pubSub == PubSubModel.PUBLISH_SUBSCRIBE)
				result.add(attribute);
		});

		return result;
	}

	public String getName()
	{
		return name;
	}

	public String getAssemblerName()
	{
		return assemblerName;
	}

	public void setRtiClassHandle(ObjectClassHandle handle)
	{
		classHandle = handle;
	}

	public ObjectClassHandle getRtiClassHandle()
	{
		return classHandle;
	}

	public Map<String, String> getAttributeAdapterNameMap()
	{
		return attributeAdapterNameMap;
	}

	public Map<String, PubSubModel> getAttributePubSubMap()
	{
		return attributePubSubMap;
	}

	public String printPubSub(String attribute)
	{
		PubSubModel pubSub = attributePubSubMap.get(attribute);
		switch (pubSub)
		{
			case PubSubModel.PUBLISH_ONLY:
				return "Pub";
			case PubSubModel.SUBSCRIBE_ONLY:
				return "Sub";
			case PubSubModel.PUBLISH_SUBSCRIBE:
				return "Pub/Sub";
			default:
				return "N/A";
		}
	}

	public AttributeHandleSet getRtiAttributeHandleSet()
	{
		return attributeHandleSet;
	}

	public void setRtiAttributeHandleSet(AttributeHandleSet attributeSet)
	{
		attributeHandleSet = attributeSet;
	}

	public void registerAttributeHandle(String name, AttributeHandle handle)
	{
		attributeNameHandleMap.put(name, handle);
	}
	
	public Map<String, AttributeHandle> getAttributeNameHandleMap()
	{
		return attributeNameHandleMap;
	}

	public AttributeHandle getAttributeHandle(String name)
	{
		return attributeNameHandleMap.get(name);
	}
	
	public int attributeHandleCount()
	{
		return attributeHandleSet.size();
	}

	public boolean getIntentDeclaredToRti()
	{
		return intentDeclaredToRti;
	}

	public void intentDeclared()
	{
		intentDeclaredToRti = true;
	}
}
