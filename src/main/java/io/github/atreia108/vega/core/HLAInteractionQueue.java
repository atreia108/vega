/*-
 * SPDX-License-Identifier: BSD-3-Clause
 * Copyright (c) 2025 Hridyanshu Aatreya <Hridyanshu.Aatreya2@brunel.ac.uk>
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

package io.github.atreia108.vega.core;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;

import io.github.atreia108.vega.components.HLAInteractionComponent;
import io.github.atreia108.vega.utils.VegaUtilities;

/**
 * The interaction queue holds all incoming interactions from the RTI into an
 * <code>ArrayList</code>. New interactions are added to the queue at each time
 * step of the simulation.
 * 
 * @author Hridyanshu Aatreya
 * @since 1.0.0
 */
public final class HLAInteractionQueue
{
	private static final Logger LOGGER = LogManager.getLogger();

	private static ComponentMapper<HLAInteractionComponent> INTERACTION_MAPPER = VegaUtilities.interactionComponentMapper();
	private static ArrayList<Entity> interactionQueue = new ArrayList<Entity>();

	protected static void add(Entity entity)
	{
		HLAInteractionComponent interactionComponent = INTERACTION_MAPPER.get(entity);

		try
		{
			if (!check(interactionComponent))
			{
				LOGGER.warn("The HLAInteractionComponent of the entity <{}> contains an invalid HLA interaction class name \"{}\"", entity, interactionComponent.className);
				return;
			}

			interactionQueue.add(entity);
		}
		catch (NullPointerException e)
		{
			LOGGER.error("The interaction <{}> could not be added to queue because the entity does not have an HLAInteractionComponent", entity);
		}
	}

	private static boolean check(HLAInteractionComponent interactionComponent)
	{
		if (ProjectRegistry.getInteractionClass(interactionComponent.className) == null)
			return false;
		else
			return true;
	}

	/**
	 * Gets all interactions currently in the queue. Note that once you have read
	 * from the list returned by this method, you must call {@link #free(ArrayList)}
	 * to prevent the ECS object pool from completely filled up.
	 * 
	 * @see #filter(String)
	 */
	public static ArrayList<Entity> poll()
	{
		ArrayList<Entity> interactionQueueCopy = new ArrayList<Entity>();

		synchronized (interactionQueue)
		{
			Iterator<Entity> iterator = interactionQueue.iterator();
			while (iterator.hasNext())
			{
				Entity interaction = iterator.next();
				interactionQueueCopy.add(interaction);

				// iterator.remove();
			}
		}

		return interactionQueueCopy;
	}

	/**
	 * Gets all interactions matching a specific class. Note that once you have read
	 * from the list returned by this method, you must call {@link #free(ArrayList)}
	 * to prevent the ECS object pool from completely filled up.
	 * 
	 * @param interactionClassName HLA interaction class name to be used as filter.
	 */
	public static ArrayList<Entity> filter(String interactionClassName)
	{
		ArrayList<Entity> interactionQueueCopy = new ArrayList<Entity>();

		synchronized (interactionQueue)
		{
			Iterator<Entity> iterator = interactionQueue.iterator();
			while (iterator.hasNext())
			{
				Entity interaction = iterator.next();

				HLAInteractionComponent interactionComponent = INTERACTION_MAPPER.get(interaction);

				if (interactionComponent.className.equals(interactionClassName))
				{
					interactionQueueCopy.add(interaction);
					// iterator.remove();
				}
			}
		}

		return interactionQueueCopy;
	}

	/**
	 * Clears the interaction queue. Use with caution as this removes all
	 * interactions entirely.
	 */
	public static void clear()
	{
		free(poll());
		interactionQueue.clear();
	}

	/**
	 * A utility method to safely dispose of a queue of entities representing miscellaneous interactions.
	 * 
	 * @param queue The <code>ArrayList</code> of interactions to be freed.
	 */
	public static void free(ArrayList<Entity> queue)
	{
		queue.forEach((entity) ->
		{
			entity.removeAll();
			remove(entity);
		});
	}
	
	/**
	 * A utility method to safely dispose of a single entity representing an HLA interaction.
	 * 
	 * @param entity The entity to be freed.
	 */
	public static void free(Entity entity)
	{
		entity.removeAll();
		remove(entity);
	}
	
	private static void remove(Entity entity)
	{
		synchronized (interactionQueue)
		{
			if (interactionQueue.contains(entity))
				interactionQueue.remove(entity);
		}
	}
}
