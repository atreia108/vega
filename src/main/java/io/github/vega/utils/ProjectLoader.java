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

import io.github.vega.core.IDataConverter;
import io.github.vega.core.IEntityArchetype;
import io.github.vega.core.IMultiDataConverter;
import io.github.vega.hla.VegaInteractionClass;
import io.github.vega.hla.VegaObjectClass;
import io.github.vega.hla.SharingModel;

public class ProjectLoader
{
	private static final Logger LOGGER = LogManager.getLogger();

	private Document projectFile;

	private Element simulationElement;
	private Element rtiConfigurationElement;
	private Element fomModulesElement;
	private Element requiredObjectsElement;
	private Element objectClassesElement;
	private Element interactionClassesElement;

	private Element engineElement;

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
		// Items to be loaded into project settings
		loadSimulationElement();
		loadRtiConfigElement();
		loadFomModulesElement();

		// Items to be loaded into the project registry
		loadRequiredObjectsElement();
		loadObjectClassesElement();
		loadInteractionClassesElement();
		loadEngineElement();
	}

	private void loadSimulationElement()
	{
		simulationElement = projectFile.getRootElement();
		String projectName = simulationElement.attributeValue("Name");

		if (simulationElement == null)
		{
			LOGGER.error("Project initialization failed\n[REASON] The project file does not contain a root <Simulation> element");
			System.exit(1);
		}

		nullOrEmptyAttribute("Simulation", "Name", projectName);
		ProjectSettings.FEDERATE_NAME = projectName;
	}

	private void loadRtiConfigElement()
	{
		rtiConfigurationElement = simulationElement.element("RtiConfiguration");

		if (rtiConfigurationElement == null)
		{
			LOGGER.error("Project initialization failed\n[REASON] The project file does not contain an <RtiConfiguration> element");
			System.exit(1);
		}

		String hostName = rtiConfigurationElement.attributeValue("Host");
		nullOrEmptyAttribute("RtiConfiguration", "Host", hostName);

		String portNumber = rtiConfigurationElement.attributeValue("Port");
		nullOrEmptyAttribute("RtiConfiguration", "Port", portNumber);

		String federationName = rtiConfigurationElement.attributeValue("Federation");
		nullOrEmptyAttribute("RtiConfiguration", "Federation", federationName);

		ProjectSettings.HOST_NAME = hostName;
		ProjectSettings.PORT_NUMBER = portNumber;
		ProjectSettings.FEDERATION_NAME = federationName;
	}

	private void nullOrEmptyAttribute(String elementName, String attributeName, String attributeValue)
	{
		if (attributeValue == null || attributeValue.isEmpty())
		{
			LOGGER.error("Project initialization failed\n[REASON] The <{}> element is missing the \"{}\" attribute", elementName, attributeName);
			System.exit(1);
		}
	}

	private void loadFomModulesElement()
	{
		fomModulesElement = simulationElement.element("FomModules");

		if (fomModulesElement == null)
		{
			if (!ProjectSettings.REDUCED_LOGGING)
				LOGGER.warn("No FOM modules are specified. Assuming no FOM data extensions are used in this simulation");
			return;
		}

		Iterator<Element> iterator = fomModulesElement.elementIterator();

		if (!iterator.hasNext())
		{
			if (!ProjectSettings.REDUCED_LOGGING)
				LOGGER.warn("No FOM modules are specified. Assuming no FOM data extensions are used in this simulation");
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
		requiredObjectsElement = simulationElement.element("RequiredObjects");

		if (requiredObjectsElement == null)
		{
			if (!ProjectSettings.REDUCED_LOGGING)
				LOGGER.warn("No required objects are specified. Assuming there are no object instances that must be discovered before starting the simulation.");
			return;
		}

		Iterator<Element> iterator = requiredObjectsElement.elementIterator();

		if (!iterator.hasNext())
		{
			if (!ProjectSettings.REDUCED_LOGGING)
				LOGGER.warn("No required objects are specified. Assuming there are no object instances that must be discovered before starting the simulation.");
			return;
		}

		Set<String> requiredObjects = new HashSet<String>();

		while (iterator.hasNext())
		{
			Element requiredObjectElement = iterator.next();

			if (elementNameCheck(requiredObjectElement, "Object"))
			{
				String objectName = requiredObjectElement.attributeValue("Name");
				nullOrEmptyAttribute("Object", "Name", objectName);
				requiredObjects.add(objectName);
			}
		}

		ProjectRegistry.requiredObjects = requiredObjects;
	}

	private boolean elementNameCheck(Element element, String comparison)
	{
		String elementName = element.getName();

		if (elementName.equals(comparison))
			return true;
		else
			return false;
	}

	private void loadObjectClassesElement()
	{
		objectClassesElement = simulationElement.element("ObjectClasses");

		if (objectClassesElement == null)
		{
			if (!ProjectSettings.REDUCED_LOGGING)
				LOGGER.warn("No HLA object classes are specified. Automatic publish/subscribe will be skipped and no updates will be sent or received for any object");
			return;
		}

		Iterator<Element> iterator = objectClassesElement.elementIterator();

		if (!iterator.hasNext())
		{
			if (!ProjectSettings.REDUCED_LOGGING)
				LOGGER.warn("No HLA object classes are specified. Automatic publish/subscribe will be skipped and no updates will be sent or received for any object");
			return;
		}

		while (iterator.hasNext())
			createObjectClass(iterator.next());
	}

	private void createObjectClass(Element objectClassElement)
	{
		if (elementNameCheck(objectClassElement, "ObjectClass"))
		{
			String className = objectClassElement.attributeValue("Name");
			nullOrEmptyAttribute("ObjectClass", "Name", className);

			if (duplicateObjectClass(className))
			{
				if (!ProjectSettings.REDUCED_LOGGING)
					LOGGER.warn("Skipping duplicate definition for the HLA object class <{}>", className);
				return;
			}

			String archetypeName = objectClassElement.attributeValue("Archetype");
			nullOrEmptyAttribute("ObjectClass", "Archetype", archetypeName);

			if (!classExists(archetypeName))
			{
				LOGGER.error("Project initialization failed\n[REASON] The Java class definition for the archetype \"{}\" was not found", archetypeName);
				System.exit(1);
			}

			if (!archetypeCreated(archetypeName))
				createArchetype(archetypeName);

			Element declarationElement = objectClassElement.element("DeclarationDisabled");
			VegaObjectClass newObjectClass = null;

			if (declarationElement != null)
				newObjectClass = new VegaObjectClass(className, archetypeName, false);
			else
				newObjectClass = new VegaObjectClass(className, archetypeName, true);

			loadObjectAttributes(objectClassElement, newObjectClass);

			ProjectRegistry.addObjectClass(newObjectClass);
		}
	}

	private boolean duplicateObjectClass(String className)
	{
		if (ProjectRegistry.getObjectClass(className) != null)
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

	private void createArchetype(String archetypeName)
	{
		try
		{
			Class<?> archetypeClass = Class.forName(archetypeName);
			IEntityArchetype archetype = (IEntityArchetype) archetypeClass.getDeclaredConstructor().newInstance();
			ProjectRegistry.addArchetype(archetypeName, archetype);
		}
		catch (ClassCastException e) 
		{
			LOGGER.error("Project Initialization failed\n[REASON] Could not create the Entity Archetype \"{}\". The Java class provided as source is not of the type <IEntityArchetype>", archetypeName);
			System.exit(1);
		}
		catch (Exception e)
		{
			LOGGER.error("Project initialization failed\n[REASON]", e);
			System.exit(1);
		}
	}

	private void loadObjectAttributes(Element objectClassElement, VegaObjectClass objectClass)
	{
		Iterator<Element> iterator = objectClassElement.elementIterator();

		if (!iterator.hasNext())
		{
			if (!ProjectSettings.REDUCED_LOGGING)
				LOGGER.warn("No attributes specified for the HLA object class <" + objectClass.name + ">. Updates for instances of this type will likely not be sent or received");
			return;
		}

		while (iterator.hasNext())
		{
			Element nextElement = iterator.next();
			if (elementNameCheck(nextElement, "Attribute"))
				loadObjectAttribute(nextElement, objectClass);
		}
	}

	private void loadObjectAttribute(Element attributeElement, VegaObjectClass objectClass)
	{

		String objectAttributeName = attributeElement.attributeValue("Name");
		nullOrEmptyAttribute("Attribute", "Name", objectAttributeName);

		if (duplicateObjectAttribute(objectClass, objectAttributeName))
		{
			if (!ProjectSettings.REDUCED_LOGGING)
				LOGGER.warn("Skipping duplicate attribute definition \"{}\" in the HLA object class <{}>", objectAttributeName, objectClass.name);
			return;
		}

		String sharingIntentValue = attributeElement.attributeValue("Sharing");
		nullOrEmptyAttribute("Attribute", "Sharing", objectAttributeName);
		SharingModel sharingModel = sharingModelValue(sharingIntentValue);

		objectClass.addAttribute(objectAttributeName, sharingModel);

		loadAttributeConverter(attributeElement, objectAttributeName, objectClass);
	}

	private boolean duplicateObjectAttribute(VegaObjectClass objectClass, String objectAttributeName)
	{
		boolean exists = objectClass.attributeNames.contains(objectAttributeName);
		if (exists)
			return true;
		else
			return false;
	}

	private SharingModel sharingModelValue(String sharingValue)
	{
		sharingModelCheck(sharingValue);

		switch (sharingValue)
		{
			case "Publish":
				return SharingModel.PUBLISH_ONLY;
			case "Subscribe":
				return SharingModel.SUBSCRIBE_ONLY;
			default:
				return SharingModel.PUBLISH_SUBSCRIBE;
		}
	}

	private void sharingModelCheck(String sharingValue)
	{
		if (!(sharingValue.equals("Publish") || sharingValue.equals("Subscribe") || sharingValue.equals("PublishSubscribe")))
		{
			LOGGER.error("Project initialization failed\n[REASON] Unrecognized value \"{}\" for the \"Sharing\" attribute. Only \"Publish\", \"Subscribe\" or \"PublishSubscribe\" are considered valid", sharingValue);
			System.exit(1);
		}
	}

	private void loadAttributeConverter(Element attributeElement, String objectAttributeName, VegaObjectClass objectClass)
	{
		Element converterElement = attributeElement.element("DataConverter");
		String converterClassName = converterElement.attributeValue("Source");
		nullOrEmptyAttribute("DataConverter", "Source", converterClassName);

		String converterTrigger = converterElement.attributeValue("Trigger");

		if (converterTrigger == null)
		{
			if (!converterCreated(converterClassName))
			{
				classExists(converterClassName);
				createDataConverter(converterClassName);
			}
			objectClass.addConverter(objectAttributeName, converterClassName);
		}
		else
		{
			int triggerValue = toInteger("Trigger", converterTrigger);

			if (!multiConverterCreated(converterClassName))
			{
				classExists(converterClassName);
				createMultiDataConverter(converterClassName);
			}
			objectClass.addMultiConverter(objectAttributeName, converterClassName, triggerValue);
		}
	}

	private void createDataConverter(String converterName)
	{
		try
		{
			Class<?> converterClass = Class.forName(converterName);
			IDataConverter converter = (IDataConverter) converterClass.getDeclaredConstructor().newInstance();
			ProjectRegistry.addDataConverter(converterName, converter);
		}
		catch (ClassCastException e) 
		{
			LOGGER.error("Project Initialization failed\n[REASON] Could not create the data converter \"{}\". The Java class provided as source is not of the type <IDataConverter>", converterName);
			System.exit(1);
		}
		catch (Exception e)
		{
			LOGGER.error("Project initialization failed\n[REASON]", e);
			System.exit(1);
		}
	}

	private void createMultiDataConverter(String converterName)
	{
		try
		{
			Class<?> multiConverterClass = Class.forName(converterName);
			IMultiDataConverter multiConverter = (IMultiDataConverter) multiConverterClass.getDeclaredConstructor().newInstance();
			ProjectRegistry.addMultiConverter(converterName, multiConverter);
		}
		catch (ClassCastException e) 
		{
			LOGGER.error("Project Initialization failed\n[REASON] Could not create the data converter \"{}\". The Java class provided as source is not of the type <IMultiDataConverter>", converterName);
			System.exit(1);
		}
		catch (Exception e)
		{
			LOGGER.error("Project initialization failed\n[REASON]", e);
			System.exit(1);
		}
	}

	private void loadInteractionClassesElement()
	{
		interactionClassesElement = simulationElement.element("InteractionClasses");

		if (interactionClassesElement == null)
		{
			if (!ProjectSettings.REDUCED_LOGGING)
				LOGGER.warn("No HLA interaction classes are specified. Automatic publish/subscribe will be skipped and no interactions will be sent or received");
			return;
		}

		Iterator<Element> iterator = interactionClassesElement.elementIterator();

		if (!iterator.hasNext())
		{
			if (!ProjectSettings.REDUCED_LOGGING)
				LOGGER.warn("No HLA interaction classes are specified. Automatic publish/subscribe will be skipped and no interactions will be sent or received");
			return;
		}

		while (iterator.hasNext())
			createInteractionClass(iterator.next());
	}

	private void createInteractionClass(Element interactionClassElement)
	{
		if (elementNameCheck(interactionClassElement, "InteractionClass"))
		{
			String className = interactionClassElement.attributeValue("Name");
			nullOrEmptyAttribute("InteractionClass", "Name", className);

			if (duplicateInteractionClass(className))
			{
				if (!ProjectSettings.REDUCED_LOGGING)
					LOGGER.warn("Skipping duplicate definition for the HLA interaction class <{}>", className);
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
				createArchetype(archetypeName);

			String sharingIntentValue = interactionClassElement.attributeValue("Sharing");
			nullOrEmptyAttribute("InteractionClass", "Sharing", sharingIntentValue);
			SharingModel sharingModel = sharingModelValue(sharingIntentValue);

			Element declarationElement = interactionClassElement.element("DeclarationDisabled");
			VegaInteractionClass newInteractionClass = null;

			if (declarationElement != null)
				newInteractionClass = new VegaInteractionClass(className, archetypeName, sharingModel, false);
			else
				newInteractionClass = new VegaInteractionClass(className, archetypeName, sharingModel, true);

			loadInteractionParameters(interactionClassElement, newInteractionClass);

			ProjectRegistry.addInteractionClass(newInteractionClass);
		}
	}

	private boolean duplicateInteractionClass(String className)
	{
		if (ProjectRegistry.getInteractionClass(className) != null)
			return true;
		else
			return false;
	}

	private boolean archetypeCreated(String archetypeName)
	{
		if (ProjectRegistry.getArchetype(archetypeName) != null)
			return true;
		else
			return false;
	}

	private void loadInteractionParameters(Element interactionClassElement, VegaInteractionClass interactionClass)
	{
		Iterator<Element> iterator = interactionClassElement.elementIterator();

		if (!iterator.hasNext())
		{
			if (!ProjectSettings.REDUCED_LOGGING)
				LOGGER.warn("No parameters specified for the HLA interaction class <" + interactionClass.name + ">. Interactions of this type will likely not be received");
			return;
		}

		while (iterator.hasNext())
		{
			Element nextElement = iterator.next();
			if (elementNameCheck(nextElement, "Parameter"))
				createInteractionParameter(nextElement, interactionClass);
		}
	}

	private void createInteractionParameter(Element parameterElement, VegaInteractionClass interactionClass)
	{
		String interactionParameterName = parameterElement.attributeValue("Name");
		nullOrEmptyAttribute("Parameter", "Name", interactionParameterName);

		if (duplicateInteractionParameter(interactionClass, interactionParameterName))
		{
			if (!ProjectSettings.REDUCED_LOGGING)
				LOGGER.warn("Skipping duplicate parameter definition \"{}\" in the HLA interaction class <{}>", interactionParameterName, interactionClass.name);
			return;
		}

		interactionClass.addParameter(interactionParameterName);

		loadParameterConverter(parameterElement, interactionParameterName, interactionClass);
	}

	private boolean duplicateInteractionParameter(VegaInteractionClass interactionClass, String parameterName)
	{
		boolean exists = interactionClass.parameterNames.contains(parameterName);
		if (exists)
			return true;
		else
			return false;
	}

	private void loadParameterConverter(Element parameterElement, String parameterName, VegaInteractionClass interactionClass)
	{	
		Element converterElement = parameterElement.element("DataConverter");
		String converterClassName = converterElement.attributeValue("Source");
		nullOrEmptyAttribute("DataConverter", "Source", converterClassName);

		String converterTrigger = converterElement.attributeValue("Trigger");
		
		if (converterTrigger == null)
		{
			if (!converterCreated(converterClassName))
			{
				classExists(converterClassName);
				createDataConverter(converterClassName);
			}
			interactionClass.addConverter(parameterName, converterClassName);
		}
		else
		{
			int triggerValue = toInteger("Trigger", converterTrigger);

			if (!multiConverterCreated(converterClassName))
			{
				classExists(converterClassName);
				createMultiDataConverter(converterClassName);
			}
			interactionClass.addMultiConverter(parameterName, converterClassName, triggerValue);
		}
	}

	private boolean converterCreated(String converterName)
	{
		if (ProjectRegistry.getDataConverter(converterName) != null)
			return true;
		else
			return false;
	}

	private boolean multiConverterCreated(String converterName)
	{
		if (ProjectRegistry.getMultiConverter(converterName) != null)
			return true;
		else
			return false;
	}

	private void loadEngineElement()
	{
		engineElement = simulationElement.element("Engine");

		// The ECS world needs to be initialized with certain starting parameters. If
		// these are absent from the project file, load stored defaults.
		if (engineElement == null)
		{
			if (!ProjectSettings.REDUCED_LOGGING)
				LOGGER.warn("No parameters are specified for the simulation engine. Using default values instead.");
			loadEngineElementDefaults();
			return;
		}

		String minEntities = engineElement.attributeValue("MinEntities");
		String maxEntities = engineElement.attributeValue("MaxEntities");
		String minComponents = engineElement.attributeValue("MinComponents");
		String maxComponents = engineElement.attributeValue("MaxComponents");

		if (minEntities == null || minEntities.isEmpty())
		{
			ProjectSettings.MIN_ENTITIES = DEFAULT_MIN_ENTITIES;
			if (!ProjectSettings.REDUCED_LOGGING)
				LOGGER.warn("Missing MinEntities attribute for <Engine> element. Using default value ({}) instead", DEFAULT_MIN_ENTITIES);
		}
		else
			ProjectSettings.MIN_ENTITIES = toInteger("MinEntities", minEntities);

		if (maxEntities == null || maxEntities.isEmpty())
		{
			ProjectSettings.MAX_ENTITIES = DEFAULT_MAX_ENTITIES;
			if (!ProjectSettings.REDUCED_LOGGING)
				LOGGER.warn("Missing MaxEntities attribute for <Engine> element. Using default value ({}) instead", DEFAULT_MAX_ENTITIES);
		}
		else
			ProjectSettings.MAX_ENTITIES = toInteger("MaxEntities", maxEntities);

		if (minComponents == null || minComponents.isEmpty())
		{
			ProjectSettings.MIN_COMPONENTS = DEFAULT_MIN_COMPONENTS;
			if (!ProjectSettings.REDUCED_LOGGING)
				LOGGER.warn("Missing MinComponents attribute for <Engine> element. Using default value ({}) instead", DEFAULT_MIN_COMPONENTS);
		}
		else
			ProjectSettings.MIN_COMPONENTS = toInteger("MinComponents", minComponents);

		if (maxComponents == null || maxComponents.isEmpty())
		{
			ProjectSettings.MAX_COMPONENTS = DEFAULT_MAX_COMPONENTS;
			if (!ProjectSettings.REDUCED_LOGGING)
				LOGGER.warn("Missing MaxComponents attribute for <Engine> element. Using default value ({}) instead", DEFAULT_MAX_COMPONENTS);
		}
		else
			ProjectSettings.MAX_COMPONENTS = toInteger("MaxComponents", maxComponents);

		boundsCheck("MinEntities", "MaxEntities", ProjectSettings.MIN_ENTITIES, ProjectSettings.MAX_ENTITIES);
		boundsCheck("MinComponents", "MaxComponents", ProjectSettings.MIN_COMPONENTS, ProjectSettings.MAX_COMPONENTS);
	}

	private void loadEngineElementDefaults()
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
