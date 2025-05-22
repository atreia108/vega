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

package io.github.vega.hla;

import java.util.HashMap;
import java.util.Map;

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.ObjectClassHandle;

public class HlaObject
{
	private String name;
	private String assembler;
	private ObjectClassHandle classHandle;
	private Map<String, String> attributeAdapters;
	private Map<String, HlaShareType> attributeShareTypes;
	private Map<String, AttributeHandle> attributeHandles;
	
	public String getClassName() { return name; }
	
	public String getAssemblerName() { return assembler; }
	
	public ObjectClassHandle getClassHandle() { return classHandle; }
	
	public Map<String, String> getAttributeAdapters() { return attributeAdapters; }
	
	public Map<String, HlaShareType> getAttributeShareTypes() { return attributeShareTypes; }
	
	public Map<String, AttributeHandle> getAttributeHandles() { return attributeHandles; }
	
	public HlaObject(String className, String assemblerName)
	{
		name = className;
		assembler = assemblerName;
		attributeAdapters = new HashMap<String, String>();
		attributeShareTypes = new HashMap<String, HlaShareType>();
	}
	
	public void registerAttribute(String attributeName, String adapterName, HlaShareType attributeShareType)
	{
		attributeAdapters.put(attributeName, adapterName);
		attributeShareTypes.put(attributeName, attributeShareType);
	}
	
	public void registerAttributeHandle(String attributeName, AttributeHandle handle)
	{
		attributeHandles.put(attributeName, handle);
	}
}