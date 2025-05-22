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

package io.github.vega.settings;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.vega.core.IEntityAssembler;
import io.github.vega.core.IGenericAdapter;
import io.github.vega.core.World;
import io.github.vega.hla.HlaInteraction;
import io.github.vega.hla.HlaObject;
import io.github.vega.hla.HlaShareType;

public class Parser
{
	private static Document configFile;
	private static Element root;
	private static Element rtiProperties;
	private static Element fomModules;
	private static Element objectClasses;
	private static Element interactionClasses;
	private static Element simulationProperties;

	private static Logger logger = LoggerFactory.getLogger(Parser.class);

	private static URL[] convertToUrlArray(Set<File> fomSet)
	{
		URL[] fomUrls = new URL[fomSet.size()];

		int index = 0;

		for (File fom : fomSet)
		{
			try
			{
				URL fomUrl = fom.toURI().toURL();
				fomUrls[index++] = fomUrl;
			} catch (MalformedURLException e)
			{
				logger.error("Project initialization was terminated prematurely\n[CAUSE] {}", e.getMessage());
				System.exit(1);
			}
		}

		return fomUrls;
	}

	private static IGenericAdapter createAdapter(String adapterName)
	{
		IGenericAdapter adapter = null;

		try
		{
			Class<?> adapterClass = Class.forName(adapterName);
			adapter = (IGenericAdapter) adapterClass.getDeclaredConstructor().newInstance();
		} catch (Exception e)
		{
			// TODO: handle exception
			logger.error("Project initialization was terminated prematurely\n[CAUSE]");
			e.printStackTrace();
		}

		return adapter;
	}

	private static IEntityAssembler createAssembler(String assemblerName)
	{
		IEntityAssembler assembler = null;

		try
		{
			Class<?> assemblerClass = Class.forName(assemblerName);
			assembler = (IEntityAssembler) assemblerClass.getDeclaredConstructor().newInstance();
		} catch (Exception e)
		{
			// TODO: handle exception
			logger.error("Project initialization was terminated prematurely\n[CAUSE]");
			e.printStackTrace();
		}

		return assembler;
	}

	private static HlaShareType getHlaShareType(String publishFlag, String subscribeFlag)
	{
		boolean publish = publishFlag.equals("True") ? true : false;
		boolean subscribe = subscribeFlag.equals("True") ? true : false;

		if (publish && subscribe)
			return HlaShareType.PUBLISH_SUBSCRIBE;
		if (publish && !subscribe)
			return HlaShareType.PUBLISH_ONLY;
		if (!publish && subscribe)
			return HlaShareType.SUBSCRIBE_ONLY;

		return HlaShareType.NONE;
	}

	private static void initialize(String configFilePath)
	{
		SAXReader reader = new SAXReader();

		try
		{
			configFile = reader.read(configFilePath);
			logger.info("Found project configuration in \"{}\"", configFilePath);

			root = configFile.getRootElement();
			rtiProperties = root.element("RtiProperties");
			fomModules = rtiProperties.element("FomModules");
			simulationProperties = root.element("Simulation");
			objectClasses = rtiProperties.element("ObjectClasses");
			interactionClasses = rtiProperties.element("InteractionClasses");

			if (root == null)
			{
				logger.error(
						"Project initialization was terminated prematurely\n[CAUSE] The root \"Project\" element is missing");
				System.exit(1);
			} else if (rtiProperties == null)
			{
				logger.error(
						"Project initialization was terminated prematurely\n[CAUSE] The \"RtiProperties\" element is missing");
				System.exit(1);
			} else if (fomModules == null)
			{
				logger.error(
						"Project initialization was terminated prematurely\n[CAUSE] The \"FomModules\" element is missing");
				System.exit(1);
			} else if (simulationProperties == null)
			{
				logger.error(
						"Project initialization was terminated prematurely\n[CAUSE] The \"Simulation\" element is missing");
				System.exit(1);
			}
		} catch (DocumentException e)
		{
			logger.error("Project initialization was terminated prematurely\n[CAUSE] {}", e.getMessage());
			System.exit(1);
		}
	}

	private static void loadConnectionParameters()
	{
		String hostName = rtiProperties.attributeValue("HostName");
		String portNumber = rtiProperties.attributeValue("PortNumber");
		String federateName = rtiProperties.attributeValue("Federate");
		String federationName = rtiProperties.attributeValue("Federation");

		if (hostName == null || hostName.isEmpty())
		{
			logger.error(
					"Project initialization was terminated prematurely\n[CAUSE] The \"RtiProperties\" element is missing the \"HostName\" attribute");
			System.exit(1);
		}

		if (portNumber == null || portNumber.isEmpty())
		{
			logger.error(
					"Project initialization was terminated prematurely\n[CAUSE] The \"RtiProperties\" element is missing the \"PortNumber\" attribute.");
			System.exit(1);
		}

		if (federateName == null || federateName.isEmpty())
		{
			logger.error(
					"Project initialization was terminated prematurely\n[CAUSE] The \"RtiProperties\" element is missing the \"Federate\" attribute");
			System.exit(1);
		}

		if (federationName == null || federationName.isEmpty())
		{
			logger.error(
					"Project initialization was terminated prematurely\n[CAUSE] The \"RtiProperties\" element is missing the \"Federation\" attribute");
			System.exit(1);
		}

		Settings.HOST_NAME = hostName;
		Settings.PORT_NUMBER = Integer.parseInt(portNumber);
		Settings.FEDERATE = federateName;
		Settings.FEDERATION = federationName;
	}

	private static void loadFomModules()
	{
		Iterator<Element> fomIterator = fomModules.elementIterator();
		if (!fomIterator.hasNext())
		{
			logger.error(
					"Project initialization was terminated prematurely\n[CAUSE] No FOM module(s) have been specified. Cannot connect to the HLA federation without prerequisite FOM modules.");
			System.exit(1);
		}

		Set<File> fomSet = new HashSet<File>();

		while (fomIterator.hasNext())
		{
			Element module = fomIterator.next();
			String filePath = module.attributeValue("FilePath");

			if (filePath == null || filePath.isEmpty())
			{
				logger.error(
						"Project initialization was terminated prematurely\n[CAUSE] Missing filepath for one or more FOM modules");
				System.exit(1);
			}

			File fomFile = new File(filePath);
			if (!fomFile.exists())
			{
				logger.error(
						"Project initialization was terminated prematurely\n[CAUSE] The FOM module at \"{}\" does not exist",
						filePath);
				System.exit(1);
			}

			fomSet.add(fomFile);

			URL[] fomUrls = convertToUrlArray(fomSet);

			Settings.FOM_MODULES = fomUrls;
		}
		printFomsInUse(fomSet);
	}

	private static void loadObjectClasses()
	{
		Iterator<Element> objectIterator = objectClasses.elementIterator();

		if (objectClasses == null || !objectIterator.hasNext())
		{
			logger.warn("No object classes were found. HLA object classes will not be published/subscribed");
			return;
		}

		while (objectIterator.hasNext())
		{
			Element objectClass = objectIterator.next();
			String objectClassName = objectClass.attributeValue("Name");

			if (objectClassName == null || objectClassName.isEmpty())
			{
				logger.error(
						"Project initialization was terminated prematurely\n[CAUSE] Object Class with missing \"Name\" attribute in configuration file");
				System.exit(1);
			}

			String assemblerName = objectClass.attributeValue("Assembler");
			verifyClassExistence("Assembler", assemblerName);

			HlaObject object = new HlaObject(objectClassName, assemblerName);

			Iterator<Element> objectAttributeIterator = objectClass.elementIterator();

			while (objectAttributeIterator.hasNext())
			{
				Element objectAttribute = objectAttributeIterator.next();
				String attributeName = objectAttribute.attributeValue("Name");

				if (attributeName == null || attributeName.isEmpty())
				{
					logger.error(
							"Project initialization was terminated prematurely\n[CAUSE] HLA Object Class attribute \"{}\" is missing its \"Name\" attribute in configuration file",
							objectClassName);
					System.exit(1);
				}

				String adapterName = objectAttribute.attributeValue("Adapter");

				if (adapterName == null || adapterName.isEmpty())
				{
					logger.error(
							"Project initialization was terminated prematurely\n[CAUSE] HLA Object Class attribute \"{}\" is missing its \"Adapter\" attribute in configuration file",
							adapterName);
					System.exit(1);
				}

				verifyClassExistence("Adapter", adapterName);

				IGenericAdapter adapter = createAdapter(adapterName);
				Settings.ADAPTERS.put(adapterName, adapter);

				String publishFlag = objectAttribute.attributeValue("Publish");
				String subscribeFlag = objectAttribute.attributeValue("Subscribe");
				verifyPublishSubscribeFlags("HLA Object Class <" + objectClassName + ">", publishFlag, subscribeFlag);
				HlaShareType attributeShareType = getHlaShareType(publishFlag, subscribeFlag);

				object.registerAttribute(attributeName, adapterName, attributeShareType);
			}

			Settings.OBJECT_CLASSES.add(object);
		}
	}

	// TODO
	private static void loadInteractionClasses()
	{
		Iterator<Element> interactionIterator = interactionClasses.elementIterator();

		if (interactionClasses == null || !interactionIterator.hasNext())
		{
			logger.warn("No interaction classes were found. HLA interaction classes will not be published/subscribed");
			return;
		}

		while (interactionIterator.hasNext())
		{
			Element interaction = interactionIterator.next();

			String interactionName = interaction.attributeValue("Name");
			if (interactionName == null || interactionName.isEmpty())
			{
				logger.error(
						"Project initialization was terminated prematurely\n[CAUSE] HLA interaction class with missing \"Name\" attribute in configuration file");
				System.exit(1);
			}

			String assemblerName = interaction.attributeValue("Assembler");
			if (assemblerName == null || assemblerName.isEmpty())
			{
				logger.error(
						"Project initialization was terminated prematurely\n[CAUSE] HLA Interaction Class with missing \"Assembler\" attribute in configuration file");
				System.exit(1);
			}
			verifyClassExistence("Assembler", assemblerName);

			IEntityAssembler assembler = createAssembler(assemblerName);
			Settings.ASSEMBLERS.put(assemblerName, assembler);

			String publishFlag = interaction.attributeValue("Publish");
			String subscribeFlag = interaction.attributeValue("Subscribe");
			verifyPublishSubscribeFlags("HLA Interaction Class <" + interactionName + ">", publishFlag, subscribeFlag);
			HlaShareType shareType = getHlaShareType(publishFlag, subscribeFlag);

			HlaInteraction interactionClass = new HlaInteraction(interactionName, assemblerName, shareType);

			Iterator<Element> parameterIterator = interaction.elementIterator();
			while (parameterIterator.hasNext())
			{
				Element parameter = parameterIterator.next();
				String parameterName = parameter.attributeValue("Name");

				if (parameterName == null || parameterName.isEmpty())
				{
					logger.error(
							"Project initialization was terminated prematurely\n[CAUSE] HLA Interaction class \"{}\" is missing \"Name\" attribute for parameter \"{}\" in configuration file",
							interactionName);
					System.exit(1);
				}

				String adapterName = parameter.attributeValue("Adapter");

				if (adapterName == null || adapterName.isEmpty())
				{
					logger.error(
							"Project initialization was terminated prematurely\n[CAUSE] HLA Interaction Class \"{}\" has a parameter \"{}\" with missing \"Adapter\" attribute in configuration file",
							parameterName, adapterName);
					System.exit(1);
				}

				verifyClassExistence("Adapter", adapterName);

				IGenericAdapter adapter = createAdapter(adapterName);
				Settings.ADAPTERS.put(adapterName, adapter);

				interactionClass.registerParameter(parameterName, adapterName);
			}

			Settings.INTERACTION_CLASSES.add(interactionClass);
		}
	}

	private static void loadSimulationParameters()
	{
		String frameRate = simulationProperties.attributeValue("FrameRate");
		
		if (frameRate == null || frameRate.isEmpty())
		{
			logger.error(
					"Project initialization was terminated prematurely\n[CAUSE] The \"Simulation\" element is missing the \"FrameRate\" attribute.");
			System.exit(1);
		}

		Settings.FRAME_RATE = Long.parseLong(frameRate);
		
		setEngineParameters();
	}

	public static void read(String configFilePath)
	{
		initialize(configFilePath);
		loadConnectionParameters();
		loadFomModules();
		loadObjectClasses();
		loadInteractionClasses();
		loadSimulationParameters();

		logger.info("Successfully finished loading project configuration");
	}

	private static void printFomsInUse(Set<File> fomSet)
	{
		String logOutput = "";
		int counter = 0;

		for (File fom : fomSet)
		{
			if ((counter + 1) == fomSet.size())
			{
				logOutput += "\"" + fom.getName() + "\"";
				counter++;
			} else
			{
				logOutput += "\"" + fom.getName() + "\", ";
				counter++;
			}
		}

		logger.info("Using FOM file(s): " + logOutput);
	}

	private static void printLoadedSettings()
	{
		for (HlaObject object : Settings.OBJECT_CLASSES)
		{
			System.out.println(object.getClassName());

			for (String attributeName : object.getAttributeAdapters().keySet())
				System.out.println(attributeName);
		}

		for (HlaInteraction interaction : Settings.INTERACTION_CLASSES)
		{
			System.out.println(interaction.getClassName());
			for (String parameterName : interaction.getParameterAdapters().keySet())
				System.out.println(parameterName);
		}

		for (String adapter : Settings.ADAPTERS.keySet())
		{
			System.out.println(adapter);
		}

		for (String assembler : Settings.ASSEMBLERS.keySet())
		{
			System.out.println(assembler);
		}
	}
	
	private static void setEngineParameters()
	{
		String minEntities = simulationProperties.attributeValue("MinimumEntities");
		String maxEntities = simulationProperties.attributeValue("MaximumEntities");
		String minComponents = simulationProperties.attributeValue("MinimumComponents");
		String maxComponents = simulationProperties.attributeValue("MaximumComponents");
		
		if (minEntities != null && !minEntities.isEmpty())
			Settings.MIN_ENTITIES = Integer.parseInt(minEntities);
		else
			Settings.MIN_ENTITIES = 100;
		
		if (maxEntities != null && !maxEntities.isEmpty())
			Settings.MAX_ENTITIES = Integer.parseInt(maxEntities);
		else
			Settings.MAX_ENTITIES = 10000;
		
		if (minComponents != null && !minComponents.isEmpty())
			Settings.MIN_COMPONENTS = Integer.parseInt(minComponents);
		else
			Settings.MIN_COMPONENTS = 75;
		
		if (maxComponents != null && !maxComponents.isEmpty())
			Settings.MAX_COMPONENTS = Integer.parseInt(maxComponents);
		else
			Settings.MAX_COMPONENTS = 5000;
		
		World.enableEngine(Settings.MIN_ENTITIES, Settings.MAX_ENTITIES, Settings.MIN_COMPONENTS, Settings.MAX_COMPONENTS);
	}

	private static void verifyClassExistence(String elementAttribute, String className)
	{
		try
		{
			Class.forName(className);
		} catch (ClassNotFoundException e)
		{
			logger.error(
					"Project initialization was terminated prematurely\n[CAUSE] Class with name \"{}\" for the \"{}\" attribute could not be found",
					className, elementAttribute);
			System.exit(1);
		}
	}

	private static void verifyPublishSubscribeFlags(String elementName, String publishFlag, String subscribeFlag)
	{
		if (publishFlag == null || publishFlag.isEmpty())
		{
			logger.error(
					"Project initialization was terminated prematurely\n[CAUSE] \"{}\" is missing its \"Publish\" attribute in configuration file",
					elementName);
			System.exit(1);
		}

		if (!(publishFlag.equals("True") || publishFlag.equals("False")))
		{
			logger.error(
					"Project initialization was terminated prematurely\n[CAUSE] \"{}\" has an invalid value \"{}\" for its \"Publish\" attribute in configuration file",
					elementName, publishFlag);
			System.exit(1);
		}

		if (subscribeFlag == null || subscribeFlag.isEmpty())
		{
			logger.error(
					"Project initialization was terminated prematurely\n[CAUSE] \"{}\" is missing its \"Subscribe\" attribute in configuration file",
					elementName);
			System.exit(1);
		}

		if (!(subscribeFlag.equals("True") || subscribeFlag.equals("False")))
		{
			logger.error(
					"Project initialization was terminated prematurely\n[CAUSE] \"{}\" has an invalid value \"{}\" for its \"Subscribe\" attribute in configuration file",
					elementName, subscribeFlag);
			System.exit(1);
		}
	}
}

