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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import io.github.vega.core.IAdapter;
import io.github.vega.core.IEntityArchetype;
import io.github.vega.core.IMultiAdapter;
import io.github.vega.core.World;
import io.github.vega.hla.HLAInteractionType;
import io.github.vega.hla.HLAObjectType;
import io.github.vega.hla.PubSubModel;

public class ProjectLoader
{
	private static final Logger LOGGER = LogManager.getLogger();

	private Document projectFile;

	private Element projectElement;
	private Element rtiElement;
	private Element fomModulesElement;
	private Element requiredObjectsElement;
	private Element objectClassesElement;
	private Element interactionClassesElement;

	private Element simulationElement;

	private static final int DEFAULT_MIN_ENTITIES = 1000;
	private static final int DEFAULT_MAX_ENTITIES = 5000;
	private static final int DEFAULT_MIN_COMPONENTS = 75;
	private static final int DEFAULT_MAX_COMPONENTS = 500;

	public ProjectLoader(String projectFilePath)
	{
		long startTime = System.currentTimeMillis();
		readFile(projectFilePath);
		loadElements();
		LOGGER.info("Successfully loaded the project \"{}\" in {}s", ProjectSettings.FEDERATE_NAME, duration(startTime));
	}

	private void readFile(String filePath)
	{
		SAXReader reader = new SAXReader();

		try
		{
			projectFile = reader.read(filePath);
			LOGGER.info("Loading project information from \"{}\"", filePath);
		}
		catch (DocumentException e)
		{
			LOGGER.error("Project initialization failed\n[REASON]", e);
			System.exit(1);
		}
	}

	private void loadElements()
	{
		loadProjectElement();
		loadRtiElement();
		loadFomModulesElement();
		loadRequiredObjectsElement();
		loadObjectClassesElement();
		loadInteractionClassesElement();
		loadSimulationElement();

		World.init();
	}

	private void loadProjectElement()
	{
		projectElement = projectFile.getRootElement();
		String projectName = projectElement.attributeValue("Name");

		if (projectElement == null)
		{
			LOGGER.error("Project initialization failed\n[REASON] The project file does not contain a root <Project> element");
			System.exit(1);
		}

		nullOrEmptyAttribute("Project", "Name", projectName);
		ProjectSettings.FEDERATE_NAME = projectName;
	}

	private void loadRtiElement()
	{
		rtiElement = projectElement.element("Rti");

		if (rtiElement == null)
		{
			LOGGER.error("Project initialization failed\n[REASON] The project file does not contain an <Rti> element");
			System.exit(1);
		}

		String hostName = rtiElement.attributeValue("Host");
		nullOrEmptyAttribute("Rti", "Host", hostName);

		String portNumber = rtiElement.attributeValue("Port");
		nullOrEmptyAttribute("Rti", "Port", portNumber);

		String federationName = rtiElement.attributeValue("Federation");
		nullOrEmptyAttribute("Rti", "Federation", federationName);

		ProjectSettings.HOST_NAME = hostName;
		ProjectSettings.PORT_NUMBER = portNumber;
		ProjectSettings.FEDERATION_NAME = federationName;
	}

	private void nullOrEmptyAttribute(String elementName, String attributeName, String attributeValue)
	{
		if (attributeValue == null || attributeValue.isEmpty())
		{
			LOGGER.error("Project initialization failed\n[REASON] The <{}> element is missing the \"{}\" attribute in the project file", elementName, attributeName);
			System.exit(1);
		}
	}

	private void loadFomModulesElement()
	{
		fomModulesElement = projectElement.element("FomModules");

		if (fomModulesElement == null)
		{
			LOGGER.warn("No FOM modules are specified. Assuming no extensions to the SpaceFOM are used in this simulation");
			return;
		}

		Iterator<Element> iterator = fomModulesElement.elementIterator();

		if (!iterator.hasNext())
		{
			LOGGER.warn("No FOM modules are specified. Assuming no extensions to the SpaceFOM are used in this simulation");
			return;
		}

		Set<URL> fomFiles = new HashSet<URL>();

		while (iterator.hasNext())
		{
			Element fomElement = iterator.next();
			String filePath = fomElement.attributeValue("FilePath");
			nullOrEmptyAttribute("Fom", "FilePath", filePath);

			URL fomUrl = toUrl(filePath);
			fomFiles.add(fomUrl);
		}

		URL[] fomModules = toUrlArray(fomFiles);
		ProjectSettings.FOM_MODULES = fomModules;
	}

	private URL toUrl(String filePath)
	{
		File file = new File(filePath);

		if (!file.exists())
		{
			LOGGER.error("Project initialization failed\n[REASON] The file \"{}\" was not found", filePath);
			System.exit(1);
		}

		URL fileUrl = null;

		try
		{
			fileUrl = file.toURI().toURL();
		}
		catch (MalformedURLException e)
		{
			LOGGER.error("Project initialization failed\n[REASON]", e);
			System.exit(1);
		}
		return fileUrl;
	}

	private URL[] toUrlArray(Set<URL> urlSet)
	{
		URL[] fomUrls = new URL[urlSet.size()];
		int index = 0;

		for (URL url : urlSet)
		{
			fomUrls[index++] = url;
		}

		return fomUrls;
	}

	private void loadRequiredObjectsElement()
	{
		requiredObjectsElement = projectElement.element("RequiredObjects");

		if (requiredObjectsElement == null)
		{
			LOGGER.warn("No required objects are specified. Assuming there are no objects that must be discovered before starting the simulation.");
			return;
		}

		Iterator<Element> iterator = requiredObjectsElement.elementIterator();

		if (!iterator.hasNext())
		{
			LOGGER.warn("No required objects are specified. Assuming there are no objects that must be discovered before starting the simulation.");
			return;
		}

		Set<String> requiredObjects = new HashSet<String>();

		while (iterator.hasNext())
		{
			Element requiredObjectElement = iterator.next();
			elementNameCheck(requiredObjectElement, "RequiredObject");

			String objectName = requiredObjectElement.attributeValue("Name");
			nullOrEmptyAttribute("RequiredObject", "Name", objectName);
			requiredObjects.add(objectName);
		}

		ProjectRegistry.requiredObjects = requiredObjects;
	}

	private void elementNameCheck(Element element, String comparison)
	{
		String elementName = element.getName();

		if (!elementName.equals(comparison))
		{
			LOGGER.error("Project initialization failed\n[REASON] Unknown element \"{}\" encountered instead of expected \"{}\"", elementName, comparison);
			System.exit(1);
		}
	}

	private void loadObjectClassesElement()
	{
		objectClassesElement = projectElement.element("ObjectClasses");

		if (objectClassesElement == null)
		{
			LOGGER.warn("No HLA object classes are specified. Automatic publish/subscribe will be skipped and no updates will be received for any object from the RTI");
			return;
		}

		Iterator<Element> iterator = objectClassesElement.elementIterator();

		if (!iterator.hasNext())
		{
			LOGGER.warn("No HLA object classes are specified. Automatic publish/subscribe will be skipped and no updates will be received for any object from the RTI");
			return;
		}

		while (iterator.hasNext())
			objectType(iterator.next());
	}

	private void objectType(Element objectClassElement)
	{
		elementNameCheck(objectClassElement, "ObjectClass");

		String typeName = objectClassElement.attributeValue("Type");
		nullOrEmptyAttribute("ObjectClass", "Type", typeName);

		if (duplicateObjectType(typeName))
		{
			LOGGER.warn("Skipping duplicate definition for the HLA object class <{}>", typeName);
			return;
		}

		String archetypeName = objectClassElement.attributeValue("Archetype");
		nullOrEmptyAttribute("ObjectClass", "Archetype", archetypeName);

		if (!classExists(archetypeName))
		{
			LOGGER.error("Project initialization failed\n[REASON] The Java class definition for the archetype \"{}\" was not found", archetypeName);
			System.exit(1);
		}

		newArchetype(archetypeName);

		HLAObjectType newObjectType = new HLAObjectType(typeName, archetypeName);
		loadObjectAttributes(objectClassElement, newObjectType, typeName);

		ProjectRegistry.addObjectType(newObjectType);
	}

	private boolean duplicateObjectType(String typeName)
	{
		if (ProjectRegistry.lookupObjectType(typeName) != null)
			return true;
		else
			return false;
	}

	private boolean classExists(String className)
	{
		try
		{
			Class.forName(className);
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}

	private void newArchetype(String archetypeName)
	{
		try
		{
			Class<?> archetypeClass = Class.forName(archetypeName);
			IEntityArchetype archetype = (IEntityArchetype) archetypeClass.getDeclaredConstructor().newInstance();
			ProjectRegistry.addArchetype(archetypeName, archetype);
		}
		catch (Exception e)
		{
			LOGGER.error("Project initialization failed\n[REASON]", e);
			System.exit(1);
		}
	}

	private void loadObjectAttributes(Element objectClassElement, HLAObjectType objectType, String objectTypeName)
	{
		Iterator<Element> iterator = objectClassElement.elementIterator();

		if (!iterator.hasNext())
		{
			LOGGER.warn("No attributes specified for the HLA object class <" + objectTypeName + ">. Updates for instances of this type will likely not be received from the RTI");
			return;
		}

		while (iterator.hasNext())
			objectAttribute(iterator.next(), objectType);
	}

	private void objectAttribute(Element attributeElement, HLAObjectType objectType)
	{
		elementNameCheck(attributeElement, "Attribute");

		String objectAttributeName = attributeElement.attributeValue("Name");
		nullOrEmptyAttribute("Attribute", "Name", objectAttributeName);

		if (duplicateObjectAttribute(objectType, objectAttributeName))
		{
			LOGGER.warn("Skipping duplicate attribute definition \"{}\" in the HLA object class <{}>", objectAttributeName, objectType.name);
			return;
		}

		String publishFlag = attributeElement.attributeValue("Publish");
		nullOrEmptyAttribute("Attribute", "Publish", publishFlag);
		pubSubFlagCheck("Publish", publishFlag);

		String subscribeFlag = attributeElement.attributeValue("Subscribe");
		nullOrEmptyAttribute("Attribute", "Subscribe", subscribeFlag);
		pubSubFlagCheck("Subscribe", subscribeFlag);

		PubSubModel pubSub = pubSubValue(publishFlag, subscribeFlag);
		objectType.registerAttribute(objectAttributeName, pubSub);

		attributeAdapter(attributeElement, objectAttributeName, objectType);
	}

	private boolean duplicateObjectAttribute(HLAObjectType objectType, String objectAttributeName)
	{
		boolean exists = objectType.attributeNames.contains(objectAttributeName);
		if (exists)
			return true;
		else
			return false;
	}

	private void pubSubFlagCheck(String flagType, String flagValue)
	{
		if (!(flagValue.equals("True") || flagValue.equals("False")))
		{
			LOGGER.error("Project initialization failed\n[REASON] Unrecognized value \"{}\" for the \"{}\" attribute. Only \"True\" or \"False\" is considered valid", flagValue, flagType);
			System.exit(1);
		}
	}

	private PubSubModel pubSubValue(String publishValue, String subscribeValue)
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

	private void attributeAdapter(Element attributeElement, String objectAttributeName, HLAObjectType objectType)
	{
		Element adapterElement = attributeElement.element("Adapter");
		Element multiAdapterElement = attributeElement.element("MultiAdapter");

		if (adapterElement == null && multiAdapterElement == null)
		{
			LOGGER.error("Project initialization failed\n[REASON] Adapter/MultiAdapter element missing for the object class attribute \"{}\"", objectAttributeName);
			System.exit(1);
		}

		if (adapterElement != null && multiAdapterElement != null)
		{
			LOGGER.error("Project initialization failed\n[REASON] The object class attribute \"{}\" is not allowed to have both an adapter *and* multi-adapter", objectAttributeName);
			System.exit(1);
		}

		if (adapterElement != null)
		{
			String adapterClassName = adapterElement.attributeValue("Class");
			nullOrEmptyAttribute("Adapter", "Class", adapterClassName);

			if (!adapterCreated(adapterClassName))
			{
				classExists(adapterClassName);
				newAdapter(adapterClassName);
			}
			objectType.registerAdapter(objectAttributeName, adapterClassName);
		}
		else
		{
			String multiAdapterClassName = multiAdapterElement.attributeValue("Class");
			nullOrEmptyAttribute("MultiAdapter", "Class", multiAdapterClassName);

			String multiAdapterTrigger = multiAdapterElement.attributeValue("Trigger");
			nullOrEmptyAttribute("MultiAdapter", "Trigger", multiAdapterTrigger);
			int triggerValue = toInteger("Trigger", multiAdapterTrigger);

			if (!multiAdapterCreated(multiAdapterClassName))
			{
				classExists(multiAdapterClassName);
				newMultiAdapter(multiAdapterClassName);
			}
			objectType.registerMultiAdapter(objectAttributeName, multiAdapterClassName, triggerValue);
		}
	}

	private void newAdapter(String adapterClassName)
	{
		try
		{
			Class<?> adapterClass = Class.forName(adapterClassName);
			IAdapter adapter = (IAdapter) adapterClass.getDeclaredConstructor().newInstance();
			ProjectRegistry.addAdapter(adapterClassName, adapter);
		}
		catch (Exception e)
		{
			LOGGER.error("Project initialization failed\n[REASON]", e);
			System.exit(1);
		}
	}

	private void newMultiAdapter(String multiAdapterClassName)
	{
		try
		{
			Class<?> multiAdapterClass = Class.forName(multiAdapterClassName);
			IMultiAdapter multiAdapter = (IMultiAdapter) multiAdapterClass.getDeclaredConstructor().newInstance();
			ProjectRegistry.addMultiAdapter(multiAdapterClassName, multiAdapter);
		}
		catch (Exception e)
		{
			LOGGER.error("Project initialization failed\n[REASON]", e);
			System.exit(1);
		}
	}

	private void loadInteractionClassesElement()
	{
		interactionClassesElement = projectElement.element("InteractionClasses");

		if (interactionClassesElement == null)
		{
			LOGGER.warn("No HLA interaction classes are specified. Automatic publish/subscribe will be skipped and no interactions will be received from the RTI");
			return;
		}

		Iterator<Element> iterator = interactionClassesElement.elementIterator();

		if (!iterator.hasNext())
		{
			LOGGER.warn("No HLA interaction classes are specified. Automatic publish/subscribe will be skipped and no interactions will be received from the RTI");
			return;
		}

		while (iterator.hasNext())
			interactionType(iterator.next());
	}

	private void interactionType(Element interactionClassElement)
	{
		elementNameCheck(interactionClassElement, "InteractionClass");

		String typeName = interactionClassElement.attributeValue("Type");
		nullOrEmptyAttribute("InteractionClass", "Type", typeName);

		if (duplicateInteractionType(typeName))
		{
			LOGGER.warn("Skipping duplicate definition for the HLA interaction class <{}>", typeName);
			return;
		}

		String archetypeName = interactionClassElement.attributeValue("Archetype");
		nullOrEmptyAttribute("InteractionClass", "Archetype", archetypeName);

		if (!classExists(archetypeName))
		{
			LOGGER.error("Project initialization failed\n[REASON] The Java class definition for the archetype \"{}\" was not found", archetypeName);
			System.exit(1);
		}

		if (!archetypeCreated(archetypeName))
			newArchetype(archetypeName);

		String publishFlag = interactionClassElement.attributeValue("Publish");
		nullOrEmptyAttribute("InteractionClass", "Publish", publishFlag);
		pubSubFlagCheck("Publish", publishFlag);

		String subscribeFlag = interactionClassElement.attributeValue("Subscribe");
		nullOrEmptyAttribute("InteractionClass", "Subscribe", subscribeFlag);
		pubSubFlagCheck("Subscribe", subscribeFlag);

		PubSubModel pubSub = pubSubValue(publishFlag, subscribeFlag);

		HLAInteractionType newInteractionType = new HLAInteractionType(typeName, archetypeName, pubSub);
		loadInteractionParameters(interactionClassElement, newInteractionType, typeName);

		ProjectRegistry.addInteractionType(newInteractionType);
	}

	private boolean duplicateInteractionType(String typeName)
	{
		if (ProjectRegistry.lookupInteractionType(typeName) != null)
			return true;
		else
			return false;
	}

	private boolean archetypeCreated(String archetypeName)
	{
		if (ProjectRegistry.lookupArchetype(archetypeName) != null)
			return true;
		else
			return false;
	}

	private void loadInteractionParameters(Element interactionClassElement, HLAInteractionType interactionType, String typeName)
	{
		Iterator<Element> iterator = interactionClassElement.elementIterator();

		if (!iterator.hasNext())
		{
			LOGGER.warn("No parameters specified for the HLA interaction class <" + typeName + ">. Interactions of this type will likely not be received from the RTI");
			return;
		}

		while (iterator.hasNext())
			interactionParameter(iterator.next(), interactionType);
	}

	private void interactionParameter(Element parameterElement, HLAInteractionType interactionType)
	{
		elementNameCheck(parameterElement, "Parameter");

		String interactionParameterName = parameterElement.attributeValue("Name");
		nullOrEmptyAttribute("Parameter", "Name", interactionParameterName);

		if (duplicateInteractionParameter(interactionType, interactionParameterName))
		{
			LOGGER.warn("Skipping duplicate parameter definition \"{}\" in the HLA interaction class <{}>", interactionParameterName, interactionType.name);
			return;
		}

		interactionType.registerParameter(interactionParameterName);

		parameterAdapter(parameterElement, interactionParameterName, interactionType);
	}

	private boolean duplicateInteractionParameter(HLAInteractionType interactionType, String parameterName)
	{
		boolean exists = interactionType.parameterNames.contains(parameterName);
		if (exists)
			return true;
		else
			return false;
	}

	private void parameterAdapter(Element parameterElement, String parameterName, HLAInteractionType interactionType)
	{
		Element adapterElement = parameterElement.element("Adapter");
		Element multiAdapterElement = parameterElement.element("MultiAdapter");

		if (adapterElement == null && multiAdapterElement == null)
		{
			LOGGER.error("Project initialization failed\n[REASON] Adapter/MultiAdapter element missing for the interaction class parameter \"{}\"", parameterName);
			System.exit(1);
		}

		if (adapterElement != null && multiAdapterElement != null)
		{
			LOGGER.error("Project initialization failed\n[REASON] The interaction class parameter \"{}\" is not allowed to have both an adapter *and* multi-adapter", parameterName);
			System.exit(1);
		}

		if (adapterElement != null)
		{
			String adapterClassName = adapterElement.attributeValue("Class");
			nullOrEmptyAttribute("Adapter", "Class", adapterClassName);

			if (!adapterCreated(adapterClassName))
			{
				classExists(adapterClassName);
				newAdapter(adapterClassName);
			}

			interactionType.registerAdapter(parameterName, adapterClassName);
		}
		else
		{
			String multiAdapterClassName = multiAdapterElement.attributeValue("Class");
			nullOrEmptyAttribute("MultiAdapter", "Class", multiAdapterClassName);

			String multiAdapterTrigger = multiAdapterElement.attributeValue("Trigger");
			nullOrEmptyAttribute("MultiAdapter", "Trigger", multiAdapterTrigger);
			int triggerValue = toInteger("Trigger", multiAdapterTrigger);

			if (!multiAdapterCreated(multiAdapterClassName))
			{
				classExists(multiAdapterClassName);
				newMultiAdapter(multiAdapterClassName);
			}
			interactionType.registerMultiAdapter(parameterName, multiAdapterClassName, triggerValue);
		}
	}

	private boolean adapterCreated(String adapterName)
	{
		if (ProjectRegistry.lookupAdapter(adapterName) != null)
			return true;
		else
			return false;
	}

	private boolean multiAdapterCreated(String multiAdapterName)
	{
		if (ProjectRegistry.lookupMultiAdapter(multiAdapterName) != null)
			return true;
		else
			return false;
	}

	private void loadSimulationElement()
	{
		simulationElement = projectElement.element("Simulation");

		// The ECS world needs to be initialized with certain starting parameters. If
		// these are absent from the project file, load stored defaults.
		if (simulationElement == null)
		{
			LOGGER.warn("No parameters are specified for the simulation engine. Using default values instead.");
			loadSimulationElementDefaults();
			return;
		}

		Element engineElement = simulationElement.element("Engine");

		if (engineElement == null)
		{
			LOGGER.warn("No parameters are specified for the simulation engine. Using default values instead.");
			loadSimulationElementDefaults();
			return;
		}

		String minEntities = engineElement.attributeValue("MinEntities");
		String maxEntities = engineElement.attributeValue("MaxEntities");
		String minComponents = engineElement.attributeValue("MinComponents");
		String maxComponents = engineElement.attributeValue("MaxComponents");

		if (minEntities == null || minEntities.isEmpty())
		{
			ProjectSettings.MIN_ENTITIES = DEFAULT_MIN_ENTITIES;
			LOGGER.warn("Missing MinEntities attribute for <Engine> element. Using default value ({}) instead", DEFAULT_MIN_ENTITIES);
		}
		else
			ProjectSettings.MIN_ENTITIES = toInteger("MinEntities", minEntities);

		if (maxEntities == null || maxEntities.isEmpty())
		{
			ProjectSettings.MAX_ENTITIES = DEFAULT_MAX_ENTITIES;
			LOGGER.warn("Missing MaxEntities attribute for <Engine> element. Using default value ({}) instead", DEFAULT_MAX_ENTITIES);
		}
		else
			ProjectSettings.MAX_ENTITIES = toInteger("MaxEntities", maxEntities);

		if (minComponents == null || minComponents.isEmpty())
		{
			ProjectSettings.MIN_COMPONENTS = DEFAULT_MIN_COMPONENTS;
			LOGGER.warn("Missing MinComponents attribute for <Engine> element. Using default value ({}) instead", DEFAULT_MIN_COMPONENTS);
		}
		else
			ProjectSettings.MIN_COMPONENTS = toInteger("MinComponents", minComponents);

		if (maxComponents == null || maxComponents.isEmpty())
		{
			ProjectSettings.MAX_COMPONENTS = DEFAULT_MAX_COMPONENTS;
			LOGGER.warn("Missing MaxComponents attribute for <Engine> element. Using default value ({}) instead", DEFAULT_MAX_COMPONENTS);
		}
		else
			ProjectSettings.MAX_COMPONENTS = toInteger("MaxComponents", maxComponents);

		boundsCheck("MinEntities", "MaxEntities", ProjectSettings.MIN_ENTITIES, ProjectSettings.MAX_ENTITIES);
		boundsCheck("MinComponents", "MaxComponents", ProjectSettings.MIN_COMPONENTS, ProjectSettings.MAX_COMPONENTS);
	}

	private void loadSimulationElementDefaults()
	{
		ProjectSettings.MIN_ENTITIES = DEFAULT_MIN_ENTITIES;
		ProjectSettings.MAX_ENTITIES = DEFAULT_MAX_ENTITIES;
		ProjectSettings.MIN_COMPONENTS = DEFAULT_MIN_COMPONENTS;
		ProjectSettings.MAX_COMPONENTS = DEFAULT_MAX_COMPONENTS;
	}

	private void boundsCheck(String minName, String maxName, int min, int max)
	{
		if (min < 0)
		{
			LOGGER.error("Project initialization failed\n[REASON] Cannot accept value a value ({}) for \"{}\" that is negative", min, minName);
			System.exit(1);
		}

		if (max < 0)
		{
			LOGGER.error("Project initialization failed\n[REASON] Cannot accept value a value ({}) for \"{}\" that is negative", max, maxName);
			System.exit(1);
		}

		if (!(min < max))
		{
			LOGGER.error("Project initialization failed\n[REASON] Cannot accept a value ({}) for \"{}\" which exceeds the value of \"{}\" ({})", min, minName, maxName, max);
			System.exit(1);
		}
	}

	private int toInteger(String attributeName, String value)
	{
		int integerValue = -1;

		try
		{
			integerValue = Integer.parseInt(value);
		}
		catch (NumberFormatException e)
		{
			LOGGER.error("Project initialization failed\n[REASON] Encountered unexpected value \"{}\" for \"{}\" that cannot be converted to an integer value", value, attributeName);
			System.exit(1);
		}

		return integerValue;
	}

	private double duration(long startTime)
	{
		long endTime = System.currentTimeMillis();
		double duration = (endTime - startTime) / 1000.0;

		return duration;
	}
}
