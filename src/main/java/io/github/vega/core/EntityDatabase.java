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
import java.util.Optional;
import java.util.Set;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import com.badlogic.ashley.core.Entity;

import hla.rti1516e.ObjectInstanceHandle;
import io.github.vega.hla.HlaInteractionType;
import io.github.vega.hla.HlaObjectType;

public record EntityDatabase()
{
	private static Set<HlaObjectType> objectTypes = new HashSet<HlaObjectType>();
	private static Set<HlaInteractionType> interactionTypes = new HashSet<HlaInteractionType>();
	private static Map<String, IAdapter> adapters = new HashMap<String, IAdapter>();
	private static Map<String, IAssembler> assemblers = new HashMap<String, IAssembler>();

	private static BidiMap<Entity, ObjectInstanceHandle> entityInstances = new DualHashBidiMap<Entity, ObjectInstanceHandle>();

	private static final String separator = "*************************************";

	public static Set<HlaObjectType> getObjectTypes()
	{
		return objectTypes;
	}

	public static HlaObjectType getObjectType(String typeName)
	{
		Optional<HlaObjectType> object = objectTypes.stream().filter(o -> o.getName().equals(typeName)).findAny();

		return object.get();
	}

	public static void addObjectType(HlaObjectType type)
	{
		objectTypes.add(type);
	}

	public static void printObjectTypes()
	{
		System.out.println("HLA object classes in-use");
		System.out.println(separator);

		if (objectTypes.isEmpty())
		{
			System.out.println("None");
		}
		else
		{
			for (HlaObjectType object : objectTypes)
			{
				System.out.println("<" + object.getName() + ">");
				printEqualCharLine(object.getName().length());

				Set<String> attributes = object.getAttributeNames();

				for (String attribute : attributes)
				{
					String fullAdapterName = object.getAdapterName(attribute);
					String adapterNameOnly = getNameFromClassPath(fullAdapterName);
					System.out
							.println(attribute + " -> " + adapterNameOnly + " [" + object.printPubSub(attribute) + "]");
				}
			}
		}

	}

	public static Set<HlaInteractionType> getInteractionTypes()
	{
		return interactionTypes;
	}

	public static HlaInteractionType getInteractionType(String typeName)
	{
		Optional<HlaInteractionType> interaction = interactionTypes.stream()
				.filter(i -> i.getClassName().equals(typeName)).findAny();

		return interaction.get();
	}

	public static void addInteractionType(HlaInteractionType type)
	{
		interactionTypes.add(type);
	}

	public static void printInteractionTypes()
	{
		System.out.println("HLA interaction classes in-use");
		System.out.println(separator);

		if (interactionTypes.isEmpty())
		{
			System.out.println("None");
		}
		else
		{
			for (HlaInteractionType interaction : interactionTypes)
			{
				System.out.println("<" + interaction.getClassName() + ">" + " [" + interaction.printPubSub() + "]");
				printEqualCharLine(interaction.getClassName().length());

				Set<String> parameters = interaction.getParameters();

				for (String parameter : parameters)
				{
					String fullAdapterName = interaction.getAdapterNameFor(parameter);
					String adapterNameOnly = getNameFromClassPath(fullAdapterName);
					System.out.print(parameter + " -> " + adapterNameOnly);
				}
			}
		}

	}

	public static Map<String, IAdapter> getAdapters()
	{
		return adapters;
	}
	
	public static IAdapter getAdapter(String name)
	{
		return adapters.get(name);
	}

	public static void addAdapter(String name, IAdapter adapterInstance)
	{
		adapters.put(name, adapterInstance);
	}

	public static void printAdapters()
	{
		System.out.println("Adapters in-use");
		System.out.println(separator);

		Set<String> adapterSet = adapters.keySet();
		if (adapterSet.isEmpty())
		{
			System.out.println("None");
		}
		else
		{
			for (String adapter : adapters.keySet())
			{
				IAdapter adapterObject = adapters.get(adapter);
				String adapterNameOnly = getNameFromClassPath(adapter);

				System.out.println(adapterNameOnly + " -> " + adapterObject);
			}
		}
	}

	public static Map<String, IAssembler> getAssemblers()
	{
		return assemblers;
	}
	
	public static IAssembler getAssembler(String name) { return assemblers.get(name); }

	public static void addAssembler(String name, IAssembler assemblerInstance)
	{
		assemblers.put(name, assemblerInstance);
	}

	public static void printAssemblers()
	{
		System.out.println("Assemblers in-use");
		System.out.println(separator);

		Set<String> assemblerSet = assemblers.keySet();

		if (assemblerSet.isEmpty())
		{
			System.out.println("None");
		}
		else
		{
			for (String assembler : assemblers.keySet())
			{
				IAssembler assemblerObject = assemblers.get(assembler);
				String assemblerNameOnly = getNameFromClassPath(assembler);

				System.out.println(assemblerNameOnly + " -> " + assemblerObject);
			}
		}

	}

	public static ObjectInstanceHandle getObjectInstance(Entity entity)
	{
		return entityInstances.get(entity);
	}
	
	public static Entity getEntity(ObjectInstanceHandle instanceHandle)
	{
		return entityInstances.getKey(instanceHandle);
	}

	private static void printEqualCharLine(int stringLength)
	{
		for (int i = 0; i < stringLength; ++i)
		{
			System.out.print("=");
		}
		System.out.print("\n");
	}

	private static String getNameFromClassPath(String classPath)
	{
		String[] classPathParts = classPath.split("\\.");
		return classPathParts[classPathParts.length - 1];
	}
	
	public static void addEntityForInstance(Entity entity, ObjectInstanceHandle instanceHandle)
	{
		entityInstances.put(entity, instanceHandle);
	}
}
