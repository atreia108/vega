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

package atreia108.vega.utils;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class HlaConfigParser {
	private Document projectConfig;
	private Element root;
	
	private Map<String, String> rtiConfig;
	private Map<String, String> simConfig;
	private Set<String>	fomModules;
	private Set<HlaObjectClass> objectClasses;
	// private Set<HLAInteractionClass> interactionClasses;
	
	public HlaConfigParser() {
		rtiConfig = new HashMap<String, String>();
		fomModules = new HashSet<String>();
		simConfig = new HashMap<String, String>();
		objectClasses = new HashSet<HlaObjectClass>();
		initParser();
		createRtiConfig();
		createSimConfig();
		createObjectClasses();
	}
	
	private void initParser() {
		try {
			SAXReader reader = new SAXReader();
			projectConfig = reader.read("settings/HlaConfig.xml");
			root = projectConfig.getRootElement();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void createRtiConfig() {
		Element rtiData = root.element("RtiConfiguration");
		for (Iterator<Attribute> iterator = rtiData.attributeIterator(); iterator.hasNext();) {
			Attribute setting = iterator.next();
			String settingName = setting.getName();
			String settingValue = setting.getValue();
			rtiConfig.put(settingName, settingValue);
		}
		
		Element fomData = rtiData.element("FomModules");
		for (Iterator<Element> iterator = fomData.elementIterator(); iterator.hasNext();) {
			Element fom = iterator.next();
			String filePath = fom.attributeValue("FilePath");
			fomModules.add(filePath);
		}
	}
	
	private void createSimConfig() {
		Element simData = root.element("Simulation");
		for (Iterator<Attribute> iterator = simData.attributeIterator(); iterator.hasNext();) {
			Attribute setting = iterator.next();
			String settingName = setting.getName();
			String settingValue = setting.getValue();
			simConfig.put(settingName, settingValue);
		}
	}
	
	private void createObjectClasses() {
		Element entities = root.element("Entities");
		for (Iterator<Element> iterator = entities.elementIterator(); iterator.hasNext();) {
			Element entity = iterator.next();
			String entityClassName = entity.attributeValue("Class");
			HlaObjectClass entityClass = new HlaObjectClass(entityClassName);
			for (Iterator<Element> iterator2 = entity.elementIterator(); iterator2.hasNext();) {
				Element entityAttribute = iterator2.next();
				String attributeName = entityAttribute.attributeValue("Name");
				String publishQuery = entityAttribute.attributeValue("Publish");
				String subscribeQuery = entityAttribute.attributeValue("Subscribe");
				boolean toBePublished = (publishQuery.equals("True")) ? true : false;
				boolean toBeSubscribed = (subscribeQuery.equals("True")) ? true : false;
				HlaMessagePattern publishSubscribeFlags = evaluateMessagingPattern(toBePublished, toBeSubscribed);
				entityClass.registerAttribute(attributeName, publishSubscribeFlags);
			}
			objectClasses.add(entityClass);
		}
	}
	
	private HlaMessagePattern evaluateMessagingPattern(boolean publish, boolean subscribe) {
		if (publish && subscribe) return HlaMessagePattern.PUBLISH_SUBSCRIBE;
		if (publish && !subscribe) return HlaMessagePattern.PUBLISH_ONLY;
		if (!publish && subscribe) return HlaMessagePattern.SUBSCRIBE_ONLY;
		return HlaMessagePattern.NONE;
	}
	
	public Map<String, String> getRtiConfig() { return rtiConfig; }
	
	public Set<HlaObjectClass> getObjectClasses() { return objectClasses; }
	
	public URL[] getFomsUrlPath() {
		URL[] foms = new URL[fomModules.size()];
		int counter = 0;
		for (String fomFilePath : fomModules) {
			try {
				File fom = new File(fomFilePath);
				URL fomUrlPath = fom.toURI().toURL();
				foms[counter++] = fomUrlPath;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return foms;
	}
	
	public Document getProjectConfig() { return projectConfig; }
}
