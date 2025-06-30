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

package io.github.vega.utils;

import java.net.URL;

public record ProjectSettings()
{
	public static String HOST_NAME;
	public static String PORT_NUMBER;
	public static String FEDERATION_NAME;
	public static String FEDERATE_NAME;
	public static URL[] FOM_MODULES;
	
	public static int MIN_ENTITIES;
	public static int MAX_ENTITIES;
	public static int MIN_COMPONENTS;
	public static int MAX_COMPONENTS;
	
	public static boolean REDUCED_LOGGING;
	
	private static String separatorStyle1 = "========================================";

	public static void print()
	{
		System.out.println(separatorStyle1);
		System.out.println("Settings for <" + FEDERATE_NAME + ">");
		System.out.println(separatorStyle1 + "\n");
		System.out.println("RTI Connection");
		System.out.println(separatorStyle1);
		System.out.println("Host: " + HOST_NAME);
		System.out.println("Port: " + PORT_NUMBER);
		System.out.println("Federation: " + FEDERATION_NAME + "\n");
		printFomModules();
		printEngineParameters();
	}

	public static void printFomModules()
	{
		System.out.println("HLA FOM Modules");
		System.out.println(separatorStyle1);
		
		if (FOM_MODULES == null)
		{
			System.out.println("None");
		}
		else
		{
			for (URL url : FOM_MODULES)
			{
				System.out.println(url);
			}
		}
		System.out.println();
	}
	
	public static void printEngineParameters()
	{
		System.out.println("Simulation Engine Parameters");
		System.out.println(separatorStyle1);
		
		System.out.println("Minimum Entities: " + MIN_ENTITIES);
		System.out.println("Maximum Entities: " + MAX_ENTITIES);
		System.out.println("Minimum Components: " + MIN_COMPONENTS);
		System.out.println("Maximum Components: " + MAX_COMPONENTS + "\n");
	}
}
