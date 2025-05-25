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

package io.github.vega.configuration;

import java.net.URL;

import io.github.vega.core.EntityDatabase;

public record Configuration()
{
	private static String HOST_NAME;
	private static int PORT;
	private static String FEDERATE_NAME;
	private static String FEDERATION_NAME;
	private static URL[] FOM_MODULES;
	
	private static int MIN_SIMULATED_ENTITIES;
	private static int MAX_SIMULATED_ENTITIES;
	private static int MIN_COMPONENTS;
	private static int MAX_COMPONENTS;
	
	public static String getHostName() { return HOST_NAME; }
	
	public static void setHostName(String hostName) { HOST_NAME = hostName; }
	
	public static int getPort() { return PORT; }
	
	public static void setPort(int port) { PORT = port; }
	
	public static String getFederateName() { return FEDERATE_NAME; }
	
	public static void setFederateName(String federateName) { FEDERATE_NAME = federateName; }
	
	public static String getFederationName() { return FEDERATION_NAME; }
	
	public static void setFederationName(String federationName) { FEDERATION_NAME = federationName; }
	
	public static URL[] getFomModules() { return FOM_MODULES; }
	
	public static void setFomModules(URL[] urlArray) { FOM_MODULES = urlArray; }
	
	public static void printFomModules()
	{
		System.out.println("HLA FOM Modules in-use");
		System.out.println("***********************");
		for (URL fomUrl : FOM_MODULES)
		{
			String fomFilePath = fomUrl.getPath();
			System.out.println(fomFilePath);
		}
	}
	
	public static int getMinSimulatedEntities () { return MIN_SIMULATED_ENTITIES; }
	
	public static void setMinSimulatedEntities(int value) { MIN_SIMULATED_ENTITIES = value; }
	
	public static int getMaxSimulatedEntities() { return MAX_SIMULATED_ENTITIES; }
	
	public static void setMaxSimulatedEntities(int value) { MAX_SIMULATED_ENTITIES = value; }
	
	public static int getMinComponents() { return MIN_COMPONENTS; }
	
	public static void setMinComponents(int value) { MIN_COMPONENTS = value; }
	
	public static int getMaxComponents() { return MAX_COMPONENTS; }
	
	public static void setMaxComponents(int value) { MAX_COMPONENTS = value; }
	
	public static void get()
	{
		String separator = "*************************************";
		System.out.println("Configuration for <" + FEDERATE_NAME + ">");
		System.out.println(separator);
		System.out.println("Host: " + HOST_NAME);
		System.out.println("Port: " + PORT);
		System.out.println("HLA Federate Name: " + FEDERATE_NAME);
		System.out.println("HLA Federation Execution: " + FEDERATION_NAME + "\n");
		
		printFomModules();
		System.out.println();
		EntityDatabase.printAdapters();
		System.out.println();
		EntityDatabase.printAssemblers();
		System.out.println();
		EntityDatabase.printObjectTypes();
		System.out.println();
		EntityDatabase.printInteractionTypes();
	}
}
