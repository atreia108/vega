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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import atreia108.vega.core.HlaMessagePattern;
import atreia108.vega.core.HlaObjectClass;

public class ObjectClassTests {
	private HlaObjectClass testEntityClass;
	private Set<String> subscribedSet;
	private Set<String> publishedSet;
	
	public ObjectClassTests() {
		testEntityClass = new HlaObjectClass("HLAobjectRoot.PhysicalEntity");
		testEntityClass.registerAttribute("name", HlaMessagePattern.PUBLISH_SUBSCRIBE);
		testEntityClass.registerAttribute("parent_reference_frame", HlaMessagePattern.PUBLISH_SUBSCRIBE);
		testEntityClass.registerAttribute("state", HlaMessagePattern.PUBLISH_SUBSCRIBE);
		testEntityClass.registerAttribute("center_of_mass", HlaMessagePattern.PUBLISH_ONLY);
		testEntityClass.registerAttribute("acceleration", HlaMessagePattern.NONE);
		testEntityClass.registerAttribute("rotational_acceleration", HlaMessagePattern.SUBSCRIBE_ONLY);
		
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
