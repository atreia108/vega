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

package vega.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import vega.hla1516e.HlaMessagePattern;

import java.util.Map.Entry;

public class EntityClass
{
	private String name;
	private Map<String, HlaMessagePattern> attributeMap;
	private BidiMap<String, String> attributeComponentMap;
	
	public EntityClass(String name)
	{
		attributeMap = new HashMap<String, HlaMessagePattern>();
		attributeComponentMap = new DualHashBidiMap<String, String>();
		this.name = name;
	}
	
	public String getAttributeEquivalent(String componentName)
	{
		return attributeComponentMap.getKey(componentName);
	}
	
	public int getComponentCount()
	{
		return attributeComponentMap.size();
	}
	
	public Set<String> getComponentTypes()
	{
		Set<String> componentTypeSet = new HashSet<String>();
		
		for (Iterator<Entry<String, String>> iterator = attributeComponentMap.entrySet().iterator(); iterator.hasNext();)
		{
			String componentType = iterator.next().getValue();
			componentTypeSet.add(componentType);
		}
		
		return componentTypeSet;
	}
	
	public String getName() { return name; }
	
	public Set<String> getPublishedAttributes()
	{
		Set<String> publicationSet = new HashSet<String>();
		attributeMap.forEach((String attribute, HlaMessagePattern pattern) ->
		{
			if (pattern == HlaMessagePattern.PUBLISH_ONLY || pattern == HlaMessagePattern.PUBLISH_SUBSCRIBE)
				publicationSet.add(attribute);
		});
		
		return publicationSet;
	}
	
	public Set<String> getSubscribedAttributes()
	{
		Set<String> subscriptionSet = new HashSet<String>();
		attributeMap.forEach((String attribute, HlaMessagePattern pattern) -> { 
			if (pattern == HlaMessagePattern.SUBSCRIBE_ONLY || pattern == HlaMessagePattern.PUBLISH_SUBSCRIBE)
				subscriptionSet.add(attribute); 
			});
		
		return subscriptionSet;
	}
	
	public void registerAttribute(String attributeName, HlaMessagePattern messageModel, String component)
	{
		attributeMap.put(attributeName, messageModel);
		attributeComponentMap.put(attributeName, component);
	}
}
