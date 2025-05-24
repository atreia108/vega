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

package io.github.vega.configuration;

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

import io.github.vega.core.DataRepository;
import io.github.vega.core.IAdapter;
import io.github.vega.core.IAssembler;
import io.github.vega.core.World;
import io.github.vega.hla.HlaInteractionType;
import io.github.vega.hla.HlaObjectType;
import io.github.vega.hla.PubSubModel;

public class ConfigurationLoader
{
	private static final Logger logger = LoggerFactory.getLogger(ConfigurationLoader.class);

	private Document configFile;
	private Element root;
	private Element rtiProperties;
	private Element fomModules;
	private Element objectClasses;
	private Element interactionClasses;
	private Element simulationProperties;

	public ConfigurationLoader(String configFileDirectory)
	{
		readFile(configFileDirectory);
		readElements();
		logger.info("Successfully loaded project configuration!");
	}

	private void readFile(String filePath)
	{
		SAXReader reader = new SAXReader();

		try
		{
			configFile = reader.read(filePath);
			logger.info("Loading project configuration from {}", filePath);
		} catch (DocumentException e)
		{
			logger.error("Initialization was terminated prematurely\n[REASON]", e);
		}
	}

	private void readElements()
	{
		getTopLevelElements();
		checkTopLevelElements();
		setRtiProperties();
		setFomModules();
		
		if (objectClasses != null)
			setObjectClasses();
		
		if (interactionClasses != null)
			setInteractionClasses();
		
		setSimulationProperties();
	}

	private void getTopLevelElements()
	{
		root = configFile.getRootElement();
		rtiProperties = root.element("RtiProperties");
		fomModules = rtiProperties.element("FomModules");
		objectClasses = rtiProperties.element("ObjectClasses");
		interactionClasses = rtiProperties.element("InteractionClasses");
		simulationProperties = root.element("SimulationProperties");
	}

	private void checkTopLevelElements()
	{
		if (root == null)
		{
			logger.error(
					"Initialization was terminated prematurely\n[REASON] Root element of configuration file is missing.");
			System.exit(1);
		}

		if (rtiProperties == null)
		{
			logger.error(
					"Initialization was terminated prematurely\n[REASON] No properties for RTI connection were found. Cannot join an HLA federation without this information.");
			System.exit(1);
		}

		if (fomModules == null)
		{
			logger.error(
					"Initialization was terminated prematurely\n[REASON] No HLA FOM modules were found. Cannot join an HLA federation without this information.");
			System.exit(1);
		}

		if (objectClasses == null)
		{
			logger.warn("No HLA object classes were found. Objects will not be published or subscribed to.");
		}

		if (interactionClasses == null)
		{
			logger.warn("No HLA interaction classes were found. Interactions will not be published or subscribed to.");
		}

		if (simulationProperties == null)
		{
			logger.error(
					"Initialization was terminated prematurely\n[REASON] No properties for the simulation were found. Cannot initialize the simulation without this information.");
			System.exit(1);
		}
	}

	private void setRtiProperties()
	{
		String hostName = rtiProperties.attributeValue("Host");
		String portNumber = rtiProperties.attributeValue("Port");
		String federateName = rtiProperties.attributeValue("Federate");
		String federationName = rtiProperties.attributeValue("Federation");

		exitOnNullOrEmptyAttribute("RtiProperties", "Host", hostName);
		exitOnNullOrEmptyAttribute("RtiProperties", "Port", portNumber);
		exitOnNullOrEmptyAttribute("RtiProperties", "Federate", federateName);
		exitOnNullOrEmptyAttribute("RtiProperties", "Federation", federationName);
		
		Configuration.setHostName(hostName);
		Configuration.setPort(Integer.parseInt(portNumber));
		Configuration.setFederateName(federateName);
		Configuration.setFederationName(federationName);
	}

	private void exitOnNullOrEmptyAttribute(String elementName, String attributeName, String attributeValue)
	{
		if (attributeValue == null || attributeValue.isEmpty())
		{
			logger.error(
					"Initialization was terminated prematurely\n[REASON] The element \"{}\" is missing its mandatory \"{}\" property",
					elementName, attributeName);
			System.exit(1);
		}
	}

	private void setFomModules()
	{
		Iterator<Element> iterator = fomModules.elementIterator();
		
		String exitReason = "No HLA modules were found. Cannot join HLA federation without this information.";
		exitOnEmptyIterator(iterator, exitReason);

		Set<File> fomFiles = new HashSet<File>();

		while (iterator.hasNext())
		{
			Element fomModule = iterator.next();
			String filePath = fomModule.attributeValue("FilePath");
			exitOnNullOrEmptyAttribute("FomModule", "FilePath", filePath);

			File fomFile = checkFileExists(filePath);
			fomFiles.add(fomFile);
		}

		URL[] fomModuleUrls = createUrlArray(fomFiles);
		Configuration.setFomModules(fomModuleUrls);
	}
	
	private void exitOnEmptyIterator(Iterator<Element> iterator, String reason)
	{
		if (!iterator.hasNext())
		{
				logger.error("Initialization was terminated prematurely\n[REASON] {} ", reason);
				System.exit(1);
		}
	}

	public File checkFileExists(String filePath)
	{
		File fom = new File(filePath);

		if (!fom.exists())
		{
			logger.error(
					"Initialization was terminated prematurely\n[REASON] The FOM module at \"{}\" could not be found.",
					filePath);
			System.exit(1);
		}

		return fom;
	}

	private URL[] createUrlArray(Set<File> files)
	{
		URL[] fomUrls = new URL[files.size()];
		int index = 0;

		for (File file : files)
		{
			try
			{
				URL url = file.toURI().toURL();
				fomUrls[index++] = url;
			}
			catch (MalformedURLException e)
			{
				logger.error("Initialization was terminated prematurely\n[REASON] {}", e.getMessage());
				System.exit(1);
			}
		}

		return fomUrls;
	}

	private void setObjectClasses()
	{
		Iterator<Element> iterator = objectClasses.elementIterator();
		String warnReason = "No HLA object classes were found. Objects will not be published or subscribed to.";
		
		if (warnEmptyIterator(iterator, warnReason))
			return;
		
		while (iterator.hasNext())
		{
			Element object = iterator.next();
			String typeName = object.attributeValue("Type");
			String assemblerName = object.attributeValue("Assembler");
			
			exitOnNullOrEmptyAttribute("ObjectClass", "Type", typeName);
			exitOnNullOrEmptyAttribute("ObjectClass", "Assembler", assemblerName);
			
			checkClassExists(assemblerName);
			createAssembler(assemblerName);
			
			HlaObjectType objectType = new HlaObjectType(typeName, assemblerName);
			setObjectAttributes(object, objectType);
			DataRepository.addObjectType(objectType);
		}
	}
	
	private boolean warnEmptyIterator(Iterator<Element> iterator, String reason)
	{
		if (!iterator.hasNext())
		{
			logger.warn(reason);
			return true;
		}
		
		return false;
	}
	
	private void createAssembler(String assemblerName)
	{
		try
		{
			Class<?> assemblerClass = Class.forName(assemblerName);
			IAssembler assembler = (IAssembler) assemblerClass.getDeclaredConstructor().newInstance();
			DataRepository.addAssembler(assemblerName, assembler);
		}
		catch (Exception e)
		{
			logger.error("Initialization was terminated prematurely\n[REASON]");
			e.printStackTrace();
		}
	}
	
	private void createAdapter(String adapterName)
	{
		try
		{
			Class<?> adapterClass = Class.forName(adapterName);
			IAdapter adapter = (IAdapter) adapterClass.getDeclaredConstructor().newInstance();
			DataRepository.addAdapter(adapterName, adapter);
		}
		catch (Exception e) {
			logger.error("Initialization was terminated prematurely\n[REASON]");
			e.printStackTrace();
		}
	}
	
	private void checkPubSubFlag(String flagType, String value)
	{
		if (!(value.equals("True") || value.equals("False")))
		{
			logger.error("Initialization was terminated prematurely\n[REASON] Invalid value \"{}\" for \"{}\". Only \"True\" or \"False\" is considered valid.", value, flagType);
			System.exit(1);
		}
	}
	
	private void setObjectAttributes(Element object, HlaObjectType objectType)
	{
		Iterator<Element> iterator = object.elementIterator();
		String warnReason = "No attributes were found for the HLA object type <" + objectType.getClassName() + ">.";
		
		if (warnEmptyIterator(iterator, warnReason))
			return;
		
		while (iterator.hasNext())
		{
			Element objectAttribute = iterator.next();
			String attributeName = objectAttribute.attributeValue("Name");
			String adapterName = objectAttribute.attributeValue("Adapter");
			String publishValue = objectAttribute.attributeValue("Publish");
			String subscribeValue = objectAttribute.attributeValue("Subscribe");
			
			exitOnNullOrEmptyAttribute("HLA object class Attribute", "Name", attributeName);
			
			exitOnNullOrEmptyAttribute("HLA object class Attribute", "Adapter", adapterName);
			checkClassExists(adapterName);
			createAdapter(adapterName);
			
			exitOnNullOrEmptyAttribute("HLA object class Attribute", "Publish", publishValue);
			exitOnNullOrEmptyAttribute("HLA object class Attribute", "Subscribe", subscribeValue);
			checkPubSubFlag("Publish", publishValue);
			checkPubSubFlag("Subscribe", subscribeValue);
			PubSubModel pubSub = getPubSubValue(publishValue, subscribeValue);
			
			objectType.registerAttribute(attributeName, adapterName, pubSub);
		}
	}

	private void checkClassExists(String className)
	{
		try
		{
			Class.forName(className);
		}
		catch (ClassNotFoundException e)
		{
			logger.error("Initialization was terminated prematurely\n[REASON] Class with name \"{}\" could not be found.", className);
			System.exit(1);
		}
	}
	
	private PubSubModel getPubSubValue(String publishValue, String subscribeValue)
	{
		boolean publishFlag = publishValue.equals("True") ? true : false;
		boolean subscribeFlag = subscribeValue.equals("True") ? true : false;
		
		if (publishFlag && subscribeFlag)
			return PubSubModel.PUBLISH_SUBSCRIBE;
		else if (publishFlag && !subscribeFlag)
			return PubSubModel.PUBLISH_ONLY;
		else if (!publishFlag && subscribeFlag)
			return PubSubModel.SUBSCRIBE_ONLY;
		else
			return PubSubModel.PUBLISH_SUBSCRIBE;
	}

	private void setInteractionClasses()
	{
		Iterator<Element> iterator = interactionClasses.elementIterator();
		String warnReason = "No HLA interaction classes were found. Interactions will not be published or subscribed to.";
		
		if (warnEmptyIterator(iterator, warnReason))
			return;
		
		while (iterator.hasNext())
		{
			Element interaction = iterator.next();
			String typeName = interaction.attributeValue("Type");
			String assemblerName = interaction.attributeValue("Assembler");
			String publishValue = interaction.attributeValue("Publish");
			String subscribeValue = interaction.attributeValue("Subscribe");
			
			exitOnNullOrEmptyAttribute("InteractionClass", "Type", typeName);
			exitOnNullOrEmptyAttribute("InteractionClass", "Assembler", assemblerName);
			checkClassExists(assemblerName);
			createAssembler(assemblerName);
			
			exitOnNullOrEmptyAttribute("InteractionClass", "Publish", publishValue);
			exitOnNullOrEmptyAttribute("InteractionClass", "Subscribe", subscribeValue);
			checkPubSubFlag("Publish", publishValue);
			checkPubSubFlag("Subscribe", subscribeValue);
			PubSubModel pubSub = getPubSubValue(publishValue, subscribeValue);
			
			HlaInteractionType interactionType = new HlaInteractionType(typeName, pubSub);
			setInteractionParameters(interaction, interactionType);
			DataRepository.addInteractionType(interactionType);
		}
	}
	
	private void setInteractionParameters(Element interaction, HlaInteractionType interactionType)
	{
		Iterator<Element> iterator = interaction.elementIterator();
		String warnReason = "No parameters were found for the HLA interaction type <" + interactionType.getClassName() + ">.";
		
		if (warnEmptyIterator(iterator, warnReason))
			return;
		
		while (iterator.hasNext())
		{
			Element interactionParameter = iterator.next();
			String parameterName = interactionParameter.attributeValue("Name");
			String adapterName = interactionParameter.attributeValue("Adapter");
			
			exitOnNullOrEmptyAttribute("HLA interaction class Parameter", "Name", parameterName);
			
			exitOnNullOrEmptyAttribute("HLA interaction class Parameter", "Adapter", adapterName);
			checkClassExists(adapterName);
			createAdapter(adapterName);
			
			interactionType.registerParameter(parameterName, adapterName);
		}
	}

	private void setSimulationProperties()
	{
		String minEntities = simulationProperties.attributeValue("MinEntities");
		String maxEntities = simulationProperties.attributeValue("MaxEntities");
		String minComponents = simulationProperties.attributeValue("MinComponents");
		String maxComponents = simulationProperties.attributeValue("MaxComponents");
		
		exitOnNullOrEmptyAttribute("SimulationProperties", "MinEntities", minEntities);
		exitOnNullOrEmptyAttribute("SimulationProperties", "MaxEntities", maxEntities);
		exitOnNullOrEmptyAttribute("SimulationProperties", "MinComponents", minComponents);
		exitOnNullOrEmptyAttribute("SimulationProperties", "MaxComponents", maxComponents);
		
		Configuration.setMinSimulatedEntities(Integer.parseInt(minEntities));
		Configuration.setMaxSimulatedEntities(Integer.parseInt(maxEntities));
		Configuration.setMinComponents(Integer.parseInt(minComponents));
		Configuration.setMaxComponents(Integer.parseInt(maxComponents));
		World.init();
	}
}
