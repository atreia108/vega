package atreia108.vega.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

public class ObjectClassTests {
	private HlaObjectClass testEntityClass;
	private Set<String> subscribedSet;
	private Set<String> publishedSet;
	
	public ObjectClassTests() {
		testEntityClass = new HlaObjectClass("HLAobjectRoot.PhysicalEntity");
		testEntityClass.registerAttribute("name", HlaMessageModel.PUBLISH_SUBSCRIBE);
		testEntityClass.registerAttribute("parent_reference_frame", HlaMessageModel.PUBLISH_SUBSCRIBE);
		testEntityClass.registerAttribute("state", HlaMessageModel.PUBLISH_SUBSCRIBE);
		testEntityClass.registerAttribute("center_of_mass", HlaMessageModel.PUBLISH_ONLY);
		testEntityClass.registerAttribute("acceleration", HlaMessageModel.NONE);
		testEntityClass.registerAttribute("rotational_acceleration", HlaMessageModel.SUBSCRIBE_ONLY);
		
		subscribedSet = new HashSet<String>();
		subscribedSet.add("name");
		subscribedSet.add("parent_reference_frame");
		subscribedSet.add("state");
		subscribedSet.add("rotational_acceleration");
		
		publishedSet = new HashSet<String>();
		publishedSet.add("name");
		publishedSet.add("parent_reference_frame");
		publishedSet.add("state");
		publishedSet.add("center_of_mass");
	}
	
	@Test
	public void testAttributeMethods() {
		assertEquals("HLAobjectRoot.PhysicalEntity", testEntityClass.getName());
		assertEquals(subscribedSet, testEntityClass.getSubscribedAttributes());
		assertEquals(publishedSet, testEntityClass.getPublishedAttributes());
	}
}
