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

import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ParameterHandle;

public class HlaInteraction
{
	private String name;
	private String assembler;
	private InteractionClassHandle classHandle;
	private HlaShareType shareType;
	private Map<String, String> parameterAdapters;
	private Map<String, ParameterHandle> parameterHandles;
	
	public HlaInteraction(String className, String assemblerName, HlaShareType interactionShareType)
	{
		name = className;
		assembler = assemblerName;
		shareType = interactionShareType;
		parameterAdapters = new HashMap<String, String>();
	}
	
	public String getClassName() { return name; }
	
	public String getAssemblerName() { return assembler; }
	
	public InteractionClassHandle getClassHandle() { return classHandle; }
	
	public HlaShareType getShareType() { return shareType; }
	
	public Map<String, String> getParameterAdapters() { return parameterAdapters; }
	
	public Map<String, ParameterHandle> getParameterHandles() { return parameterHandles; }
	
	public void registerParameter(String parameterName, String adapterName)
	{
		parameterAdapters.put(parameterName, adapterName);
	}
}
