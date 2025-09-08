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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import io.github.atreia108.vega.utils.ProjectSettings;

/**
 * The registry is effectively a database for storing associations of object and
 * interaction class profiles, data converters, and remote HLA entities.
 * 
 * @author Hridyanshu Aatreya
 * @since 1.0.0
 */
public final record ProjectRegistry()
{
	// private static final Logger LOGGER = LogManager.getLogger();

	public static Set<String> requiredObjects;
	public static Set<ObjectClassProfile> objectClassProfiles = new HashSet<ObjectClassProfile>();
	public static Set<InteractionClassProfile> interactionClassProfiles = new HashSet<InteractionClassProfile>();
	public static Map<String, IEntityArchetype> archetypes = new HashMap<String, IEntityArchetype>();
	public static Map<String, IDataConverter> dataConverters = new HashMap<String, IDataConverter>();
	public static Map<String, IMultiDataConverter> multiDataConverters = new HashMap<String, IMultiDataConverter>();
	
	/*
	private static Map<String, ObjectInstanceHandle> remoteEntities = new HashMap<String, ObjectInstanceHandle>();
	private static Map<ObjectInstanceHandle, String> remoteEntitiesInverted = new HashMap<ObjectInstanceHandle, String>();
	
	private static final Map<String, ObjectInstanceHandle> localEntities = new HashMap<String, ObjectInstanceHandle>();
	private static final Map<ObjectInstanceHandle, String> localEntitiesInverted = new HashMap<ObjectInstanceHandle, String>();
	
	private static final ComponentMapper<HLAObjectComponent> OBJECT_COMPONENT_MAPPER = ComponentMapper.getFor(HLAObjectComponent.class);
	*/
	
	private static final String SEPARATOR_STYLE_1 = "========================================";
	private static final String SEPARATOR_STYLE_2 = "****************************************";

	public static void addArchetype(String archetypeName, IEntityArchetype archetype)
	{
		archetypes.put(archetypeName, archetype);
	}

	public static void addObjectClass(ObjectClassProfile objectClass)
	{
		objectClassProfiles.add(objectClass);
	}

	public static void addInteractionClass(InteractionClassProfile interactionClass)
	{
		interactionClassProfiles.add(interactionClass);
	}

	public static void addDataConverter(String converterName, IDataConverter converter)
	{
		dataConverters.put(converterName, converter);
	}

	public static void addMultiConverter(String converterName, IMultiDataConverter multiConverter)
	{
		multiDataConverters.put(converterName, multiConverter);
	}

	public static ObjectClassProfile getObjectClass(String name)
	{
		Optional<ObjectClassProfile> query = objectClassProfiles.stream().filter(o -> o.name.equals(name)).findAny();

		if (query.isEmpty())
			return null;
		else
			return query.get();
	}

	public static InteractionClassProfile getInteractionClass(String name)
	{
		Optional<InteractionClassProfile> query = interactionClassProfiles.stream().filter(i -> i.name.equals(name)).findAny();

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

	/*
	public static void addRemoteEntity(String name, ObjectInstanceHandle handle)
	{
		remoteEntities.put(name, handle);
		remoteEntitiesInverted.put(handle, name);
	}

	public static void removeRemoteEntity(String name)
	{
		remoteEntitiesInverted.remove(remoteEntities.get(name));
		remoteEntities.remove(name);
	}

	public static Entity getRemoteEntityByName(String objectInstanceName)
	{
		for (Entity entity : remoteEntities)
		{
			HLAObjectComponent objectComponent = OBJECT_COMPONENT_MAPPER.get(entity);

			if (objectComponent != null && objectComponent.instanceName.equals(objectInstanceName))
				return entity;
		}

		return null;
	}

	public static Entity getRemoteEntityByHandle(ObjectInstanceHandle instanceHandle)
	{
		for (Entity entity : remoteEntities)
		{
			HLAObjectComponent objectComponent = OBJECT_COMPONENT_MAPPER.get(entity);

			if (objectComponent != null && (objectComponent.instanceHandle == instanceHandle))
				return entity;
		}

		return null;
	}

	public static boolean isRemoteEntity(Entity entity)
	{
		if (remoteEntities.contains(entity))
			return true;
		else
			return false;
	}
	
	public static void addLocalEntity(String name, ObjectInstanceHandle handle)
	{
		localEntities.put(name, handle);
		localEntitiesInverted.put(handle, name);
	}
	
	public static String getLocalEntityName(ObjectInstanceHandle handle)
	{
		return localEntitiesInverted.get(handle);
	}
	
	public static ObjectInstanceHandle getLocalEntityHandle(String name)
	{
		return localEntities.get(name);
	}
	
	public static void removeLocalEntityEntry(ObjectInstanceHandle handle)
	{
		localEntities.remove(localEntitiesInverted.get(handle));
		localEntitiesInverted.remove(handle);
	}
	*/
	
	/**
	 * Print a summary of the registry's data for this project to standard output.
	 */
	public static void print()
	{
		System.out.println(SEPARATOR_STYLE_1);
		System.out.println("Registry for <" + ProjectSettings.FEDERATE_NAME + ">");
		System.out.println(SEPARATOR_STYLE_1 + "\n");
		printRequiredObjects();
		printObjectClasses();
		printInteractionClasses();
		printArchetypes();
		printConverters();
		printMultiConverters();
	}

	private static void printRequiredObjects()
	{
		System.out.println("Required Objects");
		System.out.println(SEPARATOR_STYLE_1);

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

	private static void printObjectClasses()
	{
		System.out.println("HLA Object Classes");
		System.out.println(SEPARATOR_STYLE_1);

		if (objectClassProfiles.isEmpty())
			System.out.println("None");
		else
		{
			for (ObjectClassProfile objectClass : objectClassProfiles)
			{
				System.out.println("<" + objectClass.name + ">");
				System.out.println("Archetype: " + trimClassName(objectClass.archetypeName));

				if (!objectClass.declareAutomatically)
					System.out.println("AUTO-DECLARATION DISABLED");

				System.out.println(SEPARATOR_STYLE_2);

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

	private static void printInteractionClasses()
	{
		System.out.println("HLA Interaction Classes");
		System.out.println(SEPARATOR_STYLE_1);

		if (interactionClassProfiles.isEmpty())
			System.out.println("None");
		else
		{
			for (InteractionClassProfile interactionClass : interactionClassProfiles)
			{
				String pubSub = HLASharingModel.toString(interactionClass.sharingModel);
				System.out.println("<" + interactionClass.name + ">" + " [" + pubSub + "]");
				System.out.println("Archetype: " + trimClassName(interactionClass.archetypeName));
				if (!interactionClass.declareAutomatically)
					System.out.println("AUTO-DECLARATION DISABLED");

				System.out.println(SEPARATOR_STYLE_2);

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

	private static void printArchetypes()
	{
		System.out.println("Entity Archetypes");
		System.out.println(SEPARATOR_STYLE_1);

		if (archetypes.isEmpty())
			System.out.println("None");

		for (String archetypeName : archetypes.keySet())
			System.out.println(trimClassName(archetypeName));

		System.out.println();
	}

	private static void printConverters()
	{
		System.out.println("Data Converters in-use");
		System.out.println(SEPARATOR_STYLE_1);

		if (dataConverters.isEmpty())
			System.out.println("None");
		else
		{
			for (String converterName : dataConverters.keySet())
				System.out.println(trimClassName(converterName));
		}
		System.out.println();
	}

	private static void printMultiConverters()
	{
		System.out.println("Multi Data Converters in-use");
		System.out.println(SEPARATOR_STYLE_1);

		for (String multiConverterName : multiDataConverters.keySet())
			System.out.println(trimClassName(multiConverterName));

		System.out.println();
	}

	private static String trimClassName(String fullClassName)
	{
		if (fullClassName == null || fullClassName.isEmpty())
			return "NONE";

		String[] partition = fullClassName.split("\\.");
		int len = partition.length;

		return partition[len - 1];
	}
}
