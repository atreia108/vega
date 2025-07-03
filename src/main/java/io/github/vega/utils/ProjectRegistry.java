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

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;

import hla.rti1516e.ObjectInstanceHandle;
import io.github.vega.core.IDataConverter;
import io.github.vega.core.IEntityArchetype;
import io.github.vega.core.IMultiDataConverter;
import io.github.vega.hla.VegaInteractionClass;
import io.github.vega.hla.VegaObjectClass;
import io.github.vega.hla.HLAObjectComponent;
import io.github.vega.hla.HLASharingModel;

public record ProjectRegistry()
{
	private static final Logger LOGGER = LogManager.getLogger();

	public static Set<String> requiredObjects;
	public static Set<VegaObjectClass> objectClasses = new HashSet<VegaObjectClass>();
	public static Set<VegaInteractionClass> interactionClasses = new HashSet<VegaInteractionClass>();
	public static Map<String, IEntityArchetype> archetypes = new HashMap<String, IEntityArchetype>();
	public static Map<String, IDataConverter> dataConverters = new HashMap<String, IDataConverter>();
	public static Map<String, IMultiDataConverter> multiDataConverters = new HashMap<String, IMultiDataConverter>();
	
	private static BidiMap<Entity, ObjectInstanceHandle> entityInstances = new DualHashBidiMap<Entity, ObjectInstanceHandle>();
	
	private static ComponentMapper<HLAObjectComponent> objectComponentMapper = ComponentMapper.getFor(HLAObjectComponent.class);

	private static final String separatorStyle1 = "========================================";
	private static final String separatorStyle2 = "****************************************";

	public static void addArchetype(String archetypeName, IEntityArchetype archetype)
	{
		archetypes.put(archetypeName, archetype);
	}

	public static void addObjectClass(VegaObjectClass objectClass)
	{
		objectClasses.add(objectClass);
	}

	public static void addInteractionClass(VegaInteractionClass interactionClass)
	{
		interactionClasses.add(interactionClass);
	}

	public static void addDataConverter(String converterName, IDataConverter converter)
	{
		dataConverters.put(converterName, converter);
	}

	public static void addMultiConverter(String converterName, IMultiDataConverter multiConverter)
	{
		multiDataConverters.put(converterName, multiConverter);
	}

	public static VegaObjectClass getObjectClass(String name)
	{
		Optional<VegaObjectClass> query = objectClasses.stream().filter(o -> o.name.equals(name)).findAny();

		if (query.isEmpty())
			return null;
		else
			return query.get();
	}

	public static VegaInteractionClass getInteractionClass(String name)
	{
		Optional<VegaInteractionClass> query = interactionClasses.stream().filter(i -> i.name.equals(name)).findAny();

		if (query.isEmpty())
			return null;
		else
			return query.get();
	}

	public static IEntityArchetype getArchetype(String archetypeName)
	{
		return archetypes.get(archetypeName);
	}

	public static IDataConverter getDataConverter(String converterName)
	{
		return dataConverters.get(converterName);
	}

	public static IMultiDataConverter getMultiConverter(String converterName)
	{
		return multiDataConverters.get(converterName);
	}
	
	public static void addEntityObjectInstance(Entity entity, ObjectInstanceHandle instanceHandle)
	{
		entityInstances.put(entity, instanceHandle);
	}
	
	public static Entity getRemoteEntity(String objectInstanceName)
	{
		for (Entity entity : entityInstances.keySet())
		{
			HLAObjectComponent objectComponent = objectComponentMapper.get(entity);
			
			if (objectComponent != null && objectComponent.instanceName.equals(objectInstanceName))
				return entity;
		}
		
		return null;
	}
	
	public static Entity getRemoteEntity(ObjectInstanceHandle instanceHandle)
	{
		return entityInstances.getKey(instanceHandle);
	}
	
	
	public static ObjectInstanceHandle getRemoteEntityHandle(Entity entity)
	{
		return entityInstances.get(entity);
	}

	public static void print()
	{
		System.out.println(separatorStyle1);
		System.out.println("Registry for <" + ProjectSettings.FEDERATE_NAME + ">");
		System.out.println(separatorStyle1 + "\n");
		printRequiredObjects();
		printObjectClasses();
		printInteractionClasses();
		printArchetypes();
		printConverters();
		printMultiConverters();
	}

	public static void printRequiredObjects()
	{
		System.out.println("Required Objects");
		System.out.println(separatorStyle1);

		if (requiredObjects == null || requiredObjects.isEmpty())
			System.out.println("None\n");
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

	public static void printObjectClasses()
	{
		System.out.println("HLA Object Classes");
		System.out.println(separatorStyle1);

		if (objectClasses.isEmpty())
			System.out.println("None");
		else
		{
			for (VegaObjectClass objectClass : objectClasses)
			{
				System.out.println("<" + objectClass.name + ">");
				System.out.println("Archetype: " + trimClassName(objectClass.archetypeName));

				if (!objectClass.declareAutomatically)
					System.out.println("AUTO-DECLARATION DISABLED");

				System.out.println(separatorStyle2);

				for (String attributeName : objectClass.attributeNames)
				{
					String pubSub = HLASharingModel.toString(objectClass.getSharingModel(attributeName));

					if (objectClass.attributeUsesMultiConverter(attributeName))
					{
						String multiConverterName = objectClass.getAttributeMultiConverterName(attributeName);
						System.out.println(attributeName + " -> " + trimClassName(multiConverterName) + " (Trigger: " + objectClass.getAttributeConverterTrigger(attributeName, multiConverterName) + ")" + " [" + pubSub + "]");
					}
					else if (objectClass.attributeUsesConverter(attributeName))
					{
						String adapterName = objectClass.getAttributeConverterName(attributeName);
						System.out.println(attributeName + " -> " + trimClassName(adapterName) + " [" + pubSub + "]");
					}
				}
				System.out.println();
			}
		}
	}

	public static void printInteractionClasses()
	{
		System.out.println("HLA Interaction Classes");
		System.out.println(separatorStyle1);

		if (interactionClasses.isEmpty())
			System.out.println("None");
		else
		{
			for (VegaInteractionClass interactionClass : interactionClasses)
			{
				String pubSub = HLASharingModel.toString(interactionClass.sharingModel);
				System.out.println("<" + interactionClass.name + ">" + " [" + pubSub + "]");
				System.out.println("Archetype: " + trimClassName(interactionClass.archetypeName));
				if (!interactionClass.declareAutomatically)
					System.out.println("AUTO-DECLARATION DISABLED");

				System.out.println(separatorStyle2);

				for (String parameterName : interactionClass.parameterNames)
				{
					if (interactionClass.parameterUsesMultiConverter(parameterName))
					{
						String multiAdapterName = interactionClass.getParameterMultiConverterName(parameterName);
						System.out.println(parameterName + " -> " + trimClassName(multiAdapterName) + " (Trigger: " + interactionClass.getParameterMultiConverterTrigger(parameterName, multiAdapterName) + ")");
					}
					else if (interactionClass.parameterUsesConverter(parameterName))
					{
						String adapterName = interactionClass.getParameterConverterName(parameterName);
						System.out.println(parameterName + " -> " + trimClassName(adapterName));
					}
				}
				System.out.println();
			}
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

	public static void printConverters()
	{
		System.out.println("Data Converters in-use");
		System.out.println(separatorStyle1);

		if (dataConverters.isEmpty())
			System.out.println("None");
		else
		{
			for (String adapterName : dataConverters.keySet())
				System.out.println(trimClassName(adapterName));
		}
		System.out.println();
	}

	public static void printMultiConverters()
	{
		System.out.println("Multi Data Converters in-use");
		System.out.println(separatorStyle1);

		for (String multiAdapterName : multiDataConverters.keySet())
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
