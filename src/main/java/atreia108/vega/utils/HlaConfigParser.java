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
				HlaMessageModel publishSubscribeFlags = evaluateMessagingPattern(toBePublished, toBeSubscribed);
				entityClass.registerAttribute(attributeName, publishSubscribeFlags);
			}
			objectClasses.add(entityClass);
		}
	}
	
	private HlaMessageModel evaluateMessagingPattern(boolean publish, boolean subscribe) {
		if (publish && subscribe) return HlaMessageModel.PUBLISH_SUBSCRIBE;
		if (publish && !subscribe) return HlaMessageModel.PUBLISH_ONLY;
		if (!publish && subscribe) return HlaMessageModel.SUBSCRIBE_ONLY;
		return HlaMessageModel.NONE;
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
