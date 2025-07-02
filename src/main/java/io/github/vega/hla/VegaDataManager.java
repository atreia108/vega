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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;

import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.RTIambassador;
import io.github.vega.utils.ProjectRegistry;

public class VegaDataManager
{
	private static final Logger LOGGER = LogManager.getLogger();
	
	private static final ComponentMapper<HLAObjectComponent> objectComponentMapper = ComponentMapper.getFor(HLAObjectComponent.class);
	private static final ComponentMapper<HLAInteractionComponent> interactionComponentMapper = ComponentMapper.getFor(HLAInteractionComponent.class);
	
	private static int registeredInstancesCount = 0;
	
	public static boolean registerObjectInstance(Entity entity)
	{
		HLAObjectComponent objectComponent = objectComponentMapper.get(entity);
		
		if (isInstanceAlive(objectComponent))
		{
			LOGGER.warn("Omitted registration for the object instance <{}>\n[REASON]It has already been registered with the RTI", objectComponent.instanceName);
			return false;
		}
		
		Object nameReservationSemaphore = VegaCallbackManager.getNameReservationSemaphore();
		RTIambassador rtiAmbassador = VegaRTIAmbassador.instance();
		
		try
		{
			synchronized (nameReservationSemaphore)
			{
				rtiAmbassador.reserveObjectInstanceName(objectComponent.instanceName);
				awaitReservation();
			}
			
			boolean nameReservationStatus = VegaCallbackManager.getNameReservationStatus();
			if (!nameReservationStatus)
			{
				LOGGER.warn("Failed to reserve the name <{}>. The corresponding HLA object instance for this entity was not created", objectComponent.instanceName);
				return false;
			}
			
			VegaObjectClass objectClass = ProjectRegistry.getObjectClass(objectComponent.className);
			ObjectClassHandle classHandle = objectClass.classHandle;
			
			ObjectInstanceHandle instanceHandle = rtiAmbassador.registerObjectInstance(classHandle, objectComponent.instanceName);
			objectComponent.handle = instanceHandle;
			
			LOGGER.info("Created the HLA object instance \"{}\" of the class <{}>", objectComponent.instanceName, objectComponent.className);
			registeredInstancesCount += 1;
		}
		catch (Exception e)
		{
			LOGGER.warn("Failed to reserve the name <{}>. The corresponding HLA object instance for this entity was not created\n[REASON]", objectComponent.instanceName, e);
			return false;
		}
		
		return false;
	}
	
	private static void awaitReservation()
	{
		Object nameReservationSemaphore = VegaCallbackManager.getNameReservationSemaphore();

		try
		{
			synchronized (nameReservationSemaphore)
			{
				nameReservationSemaphore.wait();
			}
		}
		catch (InterruptedException e)
		{
			LOGGER.error("Unexpected interrupt while waiting for the reservation of an object instance's name\n[REASON]", e);
			System.exit(1);
		}
	}
	
	private static boolean isInstanceAlive(HLAObjectComponent objectComponent)
	{
		if (objectComponent.handle != null)
			return true;
		
		return false;
	}
	
	public static boolean destroyObjectInstance(Entity entity)
	{
		--registeredInstancesCount;
		return false;
	}
	
	public static boolean updateObjectInstance(Entity entity)
	{
		return false;
	}
	
	public static boolean sendInteraction(Entity entity)
	{
		return false;
	}
	
	public static int getRegisteredInstancesCount()
	{
		return registeredInstancesCount;
	}
}
