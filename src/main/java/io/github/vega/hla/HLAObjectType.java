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

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.ObjectClassHandle;

public class HLAObjectType
{
	public String name;
	public String archetypeName;
	public ObjectClassHandle classHandle;
	public Set<String> attributeNames;
	public Map<String, String> attributeAdapterNameMap;
	public Map<String, AttributeHandle> attributeHandleMap;
	public Map<String, PubSubIntent> attributePubSubMap;
	public Map<String, Map<String, Integer>> attributeMultiAdapterNameMap;

	// A flag used to determine whether an HLA object/interaction type should be automatically declared to the RTI or not.
	// If set to false, it means we intend to manually handle the declaration ourselves.
	public boolean declareIntent;
	
	public HLAObjectType(String name, String archetypeName)
	{
		this.name = name;
		this.archetypeName = archetypeName;

		attributeNames = new HashSet<String>();
		attributeAdapterNameMap = new HashMap<String, String>();

		attributeHandleMap = new HashMap<String, AttributeHandle>();
		attributePubSubMap = new HashMap<String, PubSubIntent>();

		attributeMultiAdapterNameMap = new HashMap<String, Map<String, Integer>>();
		declareIntent = true;
	}

	public void registerAttribute(String attributeName, PubSubIntent intent)
	{
		attributeNames.add(attributeName);
		attributePubSubMap.put(attributeName, intent);
	}

	public void registerAdapter(String attributeName, String adapterName)
	{
		attributeAdapterNameMap.put(attributeName, adapterName);
	}

	public void registerMultiAdapter(String attributeName, String multiAdapterName, int trigger)
	{
		Map<String, Integer> multiAdapterTriggerMap = new HashMap<String, Integer>();
		multiAdapterTriggerMap.put(multiAdapterName, trigger);

		attributeMultiAdapterNameMap.put(attributeName, multiAdapterTriggerMap);
	}

	public String lookupAdapterName(String attributeName)
	{
		return attributeAdapterNameMap.get(attributeName);
	}

	public String lookupMultiAdapterName(String attributeName)
	{
		Map<String, Integer> multiAdapterParameters = attributeMultiAdapterNameMap.get(attributeName);

		if (multiAdapterParameters != null)
		{
			String multiAdapterName = null;

			for (String name : multiAdapterParameters.keySet())
				multiAdapterName = name;

			return multiAdapterName;
		}
		else
			return null;
	}

	public int lookupMultiAdapterTrigger(String attributeName, String multiAdapterName)
	{
		Map<String, Integer> multiAdapterParameters = attributeMultiAdapterNameMap.get(attributeName);
		return multiAdapterParameters.get(multiAdapterName);
	}

	public boolean adapter(String attributeName)
	{
		String adapterName = attributeAdapterNameMap.get(attributeName);

		if (adapterName != null)
			return true;
		else
			return false;
	}

	public boolean multiAdapter(String attributeName)
	{
		String multiAdapterName = lookupMultiAdapterName(attributeName);

		if (multiAdapterName != null)
		{
			return true;
		}
		else
			return false;
	}

	public PubSubIntent lookupIntent(String attributeName)
	{
		return attributePubSubMap.get(attributeName);
	}
}
