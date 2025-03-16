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

package atreia108.vega.core;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import atreia108.vega.hla1516e.HlaMessagePattern;

public class ConfigurationParser
{
	private final String CONFIG_FILE_PATH = "settings/Configuration.xml";
	private Element root;
	
	private URL[] fomModules;
	private Set<EntityClass> objectClasses;
	private Map<String, String> rtiParameters;
	private Map<String, String> simulationParameters;
	
	public ConfigurationParser()
	{
		try
		{
			SAXReader reader = new SAXReader();
			Document configFile = reader.read(CONFIG_FILE_PATH);
			root = configFile.getRootElement();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		objectClasses = new HashSet<EntityClass>();
		rtiParameters = new HashMap<String, String>();
		simulationParameters = new HashMap<String, String>();
		
		readRtiConfiguration();
		readFomModules();
		readSimulationParameters();
		readObjectClasses();
	}
	
	public URL[] getFomModules() { return fomModules; }
	
	public Set<EntityClass> getObjectClasses() { return objectClasses; }
	
	private HlaMessagePattern getPublishSubscribeFlags(boolean publishFlag, boolean subscribeFlag)
	{
		if (publishFlag && subscribeFlag)
			return HlaMessagePattern.PUBLISH_SUBSCRIBE;
		if (publishFlag && !subscribeFlag)
			return HlaMessagePattern.PUBLISH_ONLY;
		if (!publishFlag && subscribeFlag)
			return HlaMessagePattern.SUBSCRIBE_ONLY;
		return HlaMessagePattern.NONE;
	}
	
	public Map<String, String> getRtiParameters() { return rtiParameters; }
	
	public Map<String, String> getSimulationConfiguration() { return simulationParameters; }
	
	private void readFomModules()
	{
		Set<String> foms = new HashSet<String>();
		Element fomModulesElement = root.element("RtiConfiguration").element("FomModules");
		
		for (Iterator<Element> fomIterator = fomModulesElement.elementIterator(); fomIterator.hasNext();)
		{
			String filePath = fomIterator.next().attributeValue("FilePath");
			foms.add(filePath);
		}
		
		fomModules = new URL[foms.size()];
		int counter = 0;
		for (String fomFilePath : foms)
		{
			try
			{
				File fom = new File(fomFilePath);
				URL fomUrlPath = fom.toURI().toURL();
				fomModules[counter++] = fomUrlPath;
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	private void readObjectClasses()
	{
		Element objectClassElements = root.element("EntityClasses");
		
		for (Iterator<Element> objectClassElementsIterator = objectClassElements.elementIterator(); objectClassElementsIterator.hasNext();)
		{
			Element objectClassElement = objectClassElementsIterator.next();
			String objectClassName = objectClassElement.attributeValue("Name");
			EntityClass objectClass = new EntityClass(objectClassName);
			
			for (Iterator<Element> componentElementsIterator = objectClassElement.elementIterator(); componentElementsIterator.hasNext();)
			{
				try {
					Element componentElement = componentElementsIterator.next();
					String componentName = componentElement.attributeValue("Name");
					String fomAttributeName = componentElement.attributeValue("FomAttribute");
					boolean publishFlag = componentElement.attributeValue("Publish").equals("True");
					boolean subscribeFlag = componentElement.attributeValue("Subscribe").equals("True");
					HlaMessagePattern publishSubscribeFlags = getPublishSubscribeFlags(publishFlag, subscribeFlag);
					// Class<?> componentClass = Class.forName(componentName);
					
					objectClass.registerAttribute(fomAttributeName, publishSubscribeFlags, componentName);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			
			objectClasses.add(objectClass);
		}
	}
	
	private void readRtiConfiguration()
	{
		Element rtiConfigurationElement = root.element("RtiConfiguration");
		String hostName = rtiConfigurationElement.attributeValue("Host");
		String port = rtiConfigurationElement.attributeValue("Port");
		String federationName = rtiConfigurationElement.attributeValue("Federation");
		String federateType = rtiConfigurationElement.attributeValue("FederateType");
		
		rtiParameters.put("Host", hostName);
		rtiParameters.put("Port", port);
		rtiParameters.put("Federation", federationName);
		rtiParameters.put("FederateType", federateType);
	}
	
	private void readSimulationParameters()
	{
		Element simulationElement = root.element("Simulation");
		String frameRate = simulationElement.attributeValue("FrameRate");
		simulationParameters.put("FrameRate", frameRate);
	}
	
	public static void main(String[] args)
	{
		ConfigurationParser parser = new ConfigurationParser();
		parser.getObjectClasses().forEach((e) -> {
			System.out.println(e.getName());
			System.out.println(e.getComponentTypes());
		});
		
		parser.getSimulationConfiguration().forEach((k, v) -> {
			System.out.println(k + ": " + v);
		});
		
		parser.getRtiParameters().forEach((k, v) -> {
			System.out.println(k + ": " + v);
		});
		
		for (URL fom : parser.getFomModules()) {
			System.out.println(fom.toString());
		}
	}
}
