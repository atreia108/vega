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

package io.github.vega.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.vega.core.IAdapter;
import io.github.vega.core.IEntityArchetype;
import io.github.vega.core.IMultiAdapter;
import io.github.vega.hla.HLAInteractionType;
import io.github.vega.hla.HLAObjectType;
import io.github.vega.hla.PubSubModel;

public record ProjectRegistry()
{
	private static final Logger LOGGER = LogManager.getLogger();
	
	public static Set<HLAObjectType> objectTypes = new HashSet<HLAObjectType>();
	public static Set<HLAInteractionType> interactionTypes = new HashSet<HLAInteractionType>();
	public static Map<String, IEntityArchetype> archetypes = new HashMap<String, IEntityArchetype>();
	public static Map<String, IAdapter> adapters = new HashMap<String, IAdapter>();
	public static Map<String, IMultiAdapter> multiAdapters = new HashMap<String, IMultiAdapter>();
	
	public static Set<String> requiredObjects;
	
	private static final String separatorStyle1 = "========================================";
	private static final String separatorStyle2 = "****************************************";
	
	public static void addArchetype(String archetypeName, IEntityArchetype archetype)
	{
		archetypes.put(archetypeName, archetype);
	}
	
	public static void addObjectType(HLAObjectType type)
	{
		objectTypes.add(type);
	}
	
	public static void addInteractionType(HLAInteractionType type)
	{
		interactionTypes.add(type);
	}
	
	public static void addAdapter(String adapterName, IAdapter adapter)
	{
		adapters.put(adapterName, adapter);
	}
	
	public static void addMultiAdapter(String multiAdapterName, IMultiAdapter multiAdapter)
	{
		multiAdapters.put(multiAdapterName, multiAdapter);
	}
	
	public static HLAObjectType lookupObjectType(String typeName)
	{
		Optional<HLAObjectType> query = objectTypes.stream().filter(o -> o.name.equals(typeName)).findAny();
		
		if (query.isEmpty())
			return null;
		else
			return query.get();
	}
	
	public static HLAInteractionType lookupInteractionType(String typeName)
	{
		Optional<HLAInteractionType> query = interactionTypes.stream().filter(i -> i.name.equals(typeName)).findAny();
		
		if (query.isEmpty())
			return null;
		else
			return query.get();
	}
	
	public static IEntityArchetype lookupArchetype(String archetypeName)
	{
		return archetypes.get(archetypeName);
	}
	
	public static IAdapter lookupAdapter(String adapterName)
	{
		return adapters.get(adapterName);
	}
	
	public static IMultiAdapter lookupMultiAdapter(String multiAdapterName)
	{
		return multiAdapters.get(multiAdapterName);
	}
	
	public static void print()
	{
		System.out.println(separatorStyle1);
		System.out.println("Registry for <" + ProjectSettings.FEDERATE_NAME + ">");
		System.out.println(separatorStyle1 + "\n");
		printRequiredObjects();
		printObjectTypes();
		printInteractionTypes();
		printArchetypes();
		printAdapters();
		printMultiAdapters();
	}
	
	public static void printRequiredObjects()
	{
		System.out.println("Required Objects");
		System.out.println(separatorStyle1);
		
		if (requiredObjects == null || requiredObjects.isEmpty())
			System.out.println("None");
		else
		{
			int counter = 0;
			int length = requiredObjects.size();
			
			for (String object : requiredObjects)
			{
				if (counter == length - 1)
					System.out.print(object + "\n\n");
				else
					System.out.print(object + ", ");
				
				counter++;
			}
		}
	}
	
	public static void printObjectTypes()
	{
		System.out.println("HLA Object Classes");
		System.out.println(separatorStyle1);
		
		if (objectTypes.isEmpty())
			System.out.println("None");
		
		for (HLAObjectType objectType : objectTypes)
		{
			System.out.println("<" + objectType.name + ">");
			System.out.println("Archetype: " + trimClassName(objectType.archetypeName));
			System.out.println(separatorStyle2);
			
			for (String attributeName : objectType.attributeNames)
			{
				String pubSub = PubSubModel.toString(objectType.lookupPubSub(attributeName));
				
				if (objectType.multiAdapter(attributeName))
				{
					String multiAdapterName = objectType.lookupMultiAdapterName(attributeName);
					System.out.println(attributeName + " -> " + trimClassName(multiAdapterName) + " (Trigger: " + objectType.lookupMultiAdapterTrigger(attributeName, multiAdapterName) + ")" + " [" + pubSub + "]");
				}
				else if (objectType.adapter(attributeName))
				{
					String adapterName = objectType.lookupAdapterName(attributeName);
					System.out.println(attributeName + " -> " + trimClassName(adapterName) + " [" + pubSub + "]");
				}
			}
			System.out.println();
		}
	}
	
	public static void printInteractionTypes()
	{
		System.out.println("HLA Interaction Classes");
		System.out.println(separatorStyle1);
		
		if (interactionTypes.isEmpty())
			System.out.println("None");
		
		for (HLAInteractionType interactionType : interactionTypes)
		{
			String pubSub = PubSubModel.toString(interactionType.pubSub);
			System.out.println("<" + interactionType.name + ">" + " [" + pubSub + "]");
			System.out.println("Archetype: " + trimClassName(interactionType.archetypeName));
			System.out.println(separatorStyle2);
			
			for (String parameterName : interactionType.parameterNames)
			{
				if (interactionType.multiAdapter(parameterName))
				{
					String multiAdapterName = interactionType.lookupMultiAdapterName(parameterName);
					System.out.println(parameterName + " -> " + trimClassName(multiAdapterName) + " (Trigger: " + interactionType.lookupMultiAdapterTrigger(parameterName, multiAdapterName) + ")");
				}
				else if (interactionType.adapter(parameterName))
				{
					String adapterName = interactionType.lookupAdapterName(parameterName);
					System.out.println(parameterName + " -> " + trimClassName(adapterName));
				}
			}
			System.out.println();
		}
	}
	
	public static void printArchetypes()
	{
		System.out.println("Entity Archetypes");
		System.out.println(separatorStyle1);
		
		if (archetypes.isEmpty())
			System.out.println("None");
		
		for (String archetypeName : archetypes.keySet())
			System.out.println(trimClassName(archetypeName));
		
		System.out.println();
	}
	
	public static void printAdapters()
	{
		System.out.println("Adapters in-use");
		System.out.println(separatorStyle1);
		
		for (String adapterName : adapters.keySet())
			System.out.println(trimClassName(adapterName));
		
		System.out.println();
	}
	
	public static void printMultiAdapters()
	{
		System.out.println("Multi-Adapters in-use");
		System.out.println(separatorStyle1);
		
		for (String multiAdapterName : multiAdapters.keySet())
			System.out.println(trimClassName(multiAdapterName));
		
		System.out.println();
	}
	
	private static String trimClassName(String fullClassName)
	{
		if (fullClassName.isEmpty())
		{
			LOGGER.warn("Encountered blank canonical name for a class during an attempt to trim it to its simple name. Using NULL as fallback value instead");
			return "NULL";
		}
		
		String[] partition = fullClassName.split("\\.");
		int len = partition.length;
		
		return partition[len - 1];
	}
}
