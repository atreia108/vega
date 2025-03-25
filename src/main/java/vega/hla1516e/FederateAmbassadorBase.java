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

package vega.hla1516e;

import java.net.URL;
import java.util.Map;
import java.util.Set;

import hla.rti1516e.CallbackModel;
import hla.rti1516e.NullFederateAmbassador;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.exceptions.FederatesCurrentlyJoined;
import hla.rti1516e.exceptions.FederationExecutionAlreadyExists;
import hla.rti1516e.exceptions.FederationExecutionDoesNotExist;
import vega.core.EntityClass;
import vega.core.SimulationBase;
import vega.core.World;

public class FederateAmbassadorBase extends NullFederateAmbassador
{
	protected SimulationBase simulation;
	protected World world;

	protected RTIambassador rtiAmbassador;
	protected EncoderFactory encoder;

	protected String hostName;
	protected String port;
	protected String federationName;
	protected String federateType;
	protected URL[] fomModules;

	protected Set<EntityClass> objectClasses;

	public FederateAmbassadorBase(SimulationBase simulation)
	{
		this.simulation = simulation;
		world = simulation.getWorld();
		rtiAmbassador = simulation.getRtiAmbassador();
		encoder = simulation.getEncoder();

		unpackRtiSettings();
	}

	// TODO - Add remote entity support in near future
	/*
	protected <T extends IComponent> void discoverEntityInstance(String entityName, ObjectInstanceHandle entityHandle,
			ObjectClassHandle entityClassHandle)
	{
		String entityClassName = world.getEntityClassHandles().getKey(entityClassHandle);

		if (entityClassName != null)
		{
			Stream<EntityClass> entityClassStream = world.getEntityClasses().stream()
					.filter(entityClass -> entityClass.getName().equals(entityClassName));
			Optional<EntityClass> entityClassEntry = entityClassStream.findAny();
			EntityClass entityClass = entityClassEntry.get();

			Entity remoteEntity = world.createRemoteEntity(entityName, entityHandle);

			for (String componentName : entityClass.getComponentTypes())
			{
				try
				{
					// N.B. This cast is technically "safe" and there are no immediately dangerous implications.
					// To be resolved at a later date when a more elegant solution comes to mind.
					Class<T> componentClass = (Class<T>) Class.forName(componentName);
					world.addComponent(remoteEntity, componentClass);
					
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	*/
	
	@Override
	public void discoverObjectInstance(ObjectInstanceHandle theObject, ObjectClassHandle theObjectClass,
			String objectName)
	{
		// discoverEntityInstance(objectName, theObject, theObjectClass);
	}
	
	public void initialize()
	{
		try
		{
			rtiAmbassador.connect(this, CallbackModel.HLA_IMMEDIATE, "crcHost=" + hostName + "\n" + "crcPort=" + port);
			try
			{
				rtiAmbassador.destroyFederationExecution(federationName);
			}
			catch (FederatesCurrentlyJoined e) {}
			catch (FederationExecutionDoesNotExist e) {}

			try
			{
				rtiAmbassador.createFederationExecution(federationName, fomModules);
			}
			catch (FederationExecutionAlreadyExists e) {}

			rtiAmbassador.joinFederationExecution(federateType, federationName);
		}
		catch (Exception e) { e.printStackTrace(); }

		world.createEntityClasses();
		simulation.initialize();
		simulation.play();
	}

	public String getFederationName()
	{
		return federationName;
	}

	public String getFederateType()
	{
		return federateType;
	}

	public String getHostName()
	{
		return hostName;
	}

	public String getPort()
	{
		return port;
	}

	@Override
	public void objectInstanceNameReservationFailed(String objectName)
	{
		world.notifyEntityNameReservationStatus(false);
	}

	@Override
	public void objectInstanceNameReservationSucceeded(String objectName)
	{
		world.notifyEntityNameReservationStatus(true);
	}

	private void unpackRtiSettings()
	{
		Map<String, String> rtiSettings = simulation.getParser().getRtiParameters();
		hostName = rtiSettings.get("Host");
		port = rtiSettings.get("Port");
		federationName = rtiSettings.get("Federation");
		federateType = rtiSettings.get("FederateType");
		fomModules = simulation.getParser().getFomModules();
		objectClasses = simulation.getParser().getObjectClasses();
	}
}
