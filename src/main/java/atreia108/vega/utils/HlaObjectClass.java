package atreia108.vega.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class HlaObjectClass {
	private String className;
	private Map<String, HlaMessageModel> attributeMap;
	
	public HlaObjectClass(String className) {
		attributeMap = new HashMap<String, HlaMessageModel>();
		this.className = className;
	}
	
	public String getName() { return className; }
	
	public void registerAttribute(String attributeName, HlaMessageModel messagePattern) {
		attributeMap.put(attributeName, messagePattern);
	}
	
	public Set<String> getSubscribedAttributes() {
		Set<String> subscriptionSet = new HashSet<String>();
		attributeMap.forEach((attribute, pattern) -> { 
			if (pattern == HlaMessageModel.SUBSCRIBE_ONLY || pattern == HlaMessageModel.PUBLISH_SUBSCRIBE)
				subscriptionSet.add(attribute); 
			});
		return subscriptionSet;
	}
	
	public Set<String> getPublishedAttributes() {
		Set<String> publicationSet = new HashSet<String>();
		attributeMap.forEach((attribute, pattern) -> {
			if (pattern == HlaMessageModel.PUBLISH_ONLY || pattern == HlaMessageModel.PUBLISH_SUBSCRIBE)
				publicationSet.add(attribute);
		});
		return publicationSet;
	}
	
	public Map<String, HlaMessageModel> getAllAttributes() {
		return attributeMap;
	}
}
