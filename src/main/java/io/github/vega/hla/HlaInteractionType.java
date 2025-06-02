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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ParameterHandle;

public class HlaInteractionType
{
	private String className;
	private InteractionClassHandle classHandle;
	private String assemblerName;
	private PubSubModel pubSub;
	private Map<String, String> parameterAdapterMap;
	private Map<String, ParameterHandle> parameterHandleMap;
	
	private boolean intentDeclaredToRti;

	public HlaInteractionType(String classType, String assembler, PubSubModel pubSubModel)
	{
		className = classType;
		assemblerName = assembler;
		pubSub = pubSubModel;
		parameterAdapterMap = new HashMap<String, String>();
		parameterHandleMap = new HashMap<String, ParameterHandle>();
		intentDeclaredToRti = false;
	}

	public void registerParameter(String parameterName, String adapterName)
	{
		parameterAdapterMap.put(parameterName, adapterName);
	}
	
	public void registerParameterHandle(String parameterName, ParameterHandle handle)
	{
		parameterHandleMap.put(parameterName, handle);
	}

	public Set<String> getParameterNames()
	{
		return parameterAdapterMap.keySet();
	}
	
	public Set<ParameterHandle> getParameterHandles()
	{
		Set<ParameterHandle> requiredSet = new HashSet<ParameterHandle>();
		
		parameterHandleMap.forEach((parameterName, parameterHandle) -> {
			requiredSet.add(parameterHandle);
		});
		
		return requiredSet;
	}

	public String getAdapterName(String parameterName)
	{
		return parameterAdapterMap.get(parameterName);
	}

	public String getName()
	{
		return className;
	}

	public String getAssemblerName()
	{
		return assemblerName;
	}

	public PubSubModel getPubSub()
	{
		return pubSub;
	}
	
	public boolean isPublisheable()
	{
		if (pubSub == PubSubModel.PUBLISH_ONLY || pubSub == PubSubModel.PUBLISH_SUBSCRIBE)
			return true;
		else
			return false;
	}

	public boolean isSubscribeable()
	{
		if (pubSub == PubSubModel.SUBSCRIBE_ONLY || pubSub == PubSubModel.PUBLISH_SUBSCRIBE)
			return true;
		else
			return false;
	}
	
	public Map<String, String> getParameterAdapterNameMap()
	{
		return parameterAdapterMap;
	}

	public Map<String, ParameterHandle> getParameterHandleMap()
	{
		return parameterHandleMap;
	}

	public ParameterHandle getParameterHandle(String parameterName)
	{
		return parameterHandleMap.get(parameterName);
	}

	public String printPubSub()
	{
		switch (pubSub)
		{
			case PubSubModel.PUBLISH_ONLY:
				return "Pub";
			case PubSubModel.SUBSCRIBE_ONLY:
				return "Sub";
			case PubSubModel.PUBLISH_SUBSCRIBE:
				return "Pub/Sub";
			default:
				return "N/A";
		}
	}
	
	public boolean getIntentDeclaredToRti()
	{
		return intentDeclaredToRti;
	}
	
	public void intentDeclared()
	{
		intentDeclaredToRti = true;
	}
	
	public void setRtiClassHandle(InteractionClassHandle handle)
	{
		classHandle = handle;
	}
	
	public InteractionClassHandle getRtiClassHandle()
	{
		return classHandle;
	}
}
