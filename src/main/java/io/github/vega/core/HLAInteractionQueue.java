package io.github.vega.core;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;

import io.github.vega.components.HLAInteractionComponent;
import io.github.vega.utils.FrameworkObjects;

/**
 * The interaction queue holds all incoming interactions from the RTI into an <tt>ArrayList</tt>.
 * New interactions are added to the queue at each time step of the simulation.
 * 
 * @author Hridyanshu Aatreya
 * @since 1.0.0
 */
public final class HLAInteractionQueue
{
	private static final Logger LOGGER = LogManager.getLogger();
	
	private static ComponentMapper<HLAInteractionComponent> INTERACTION_MAPPER = FrameworkObjects.getHLAInteractionComponentMapper();
	private static ArrayList<Entity> interactionQueue = new ArrayList<Entity>();

	protected static void addInteraction(Entity entity)
	{
		HLAInteractionComponent interactionComponent = INTERACTION_MAPPER.get(entity);
		
		try
		{
			if (!checkInteraction(interactionComponent))
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
	
	private static boolean checkInteraction(HLAInteractionComponent interactionComponent)
	{
		if (ProjectRegistry.getInteractionClass(interactionComponent.className) == null)
			return false;
		else
			return true;
	}
	
	/**
	 * Gets all interactions currently in the queue.
	 * Note that once you have read from the list returned by this method,
	 * you must call {@link #free(ArrayList)} to prevent the ECS object pool
	 * from completely filled up.
	 * @see #filterByClassName(String)
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
				
				iterator.remove();
			}
		}
		
		return interactionQueueCopy;
	}
	
	/**
	 * Gets all interactions matching a specific class.
	 * Note that once you have read from the list returned by this method,
	 * you must call {@link #free(ArrayList)} to prevent the ECS object pool
	 * from completely filled up.
	 * @param interactionClassName HLA interaction class name to be used as filter.
	 */
	public static ArrayList<Entity> filterByClassName(String interactionClassName)
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
					iterator.remove();
				}
			}
		}
		
		return interactionQueueCopy;
	}
	
	/**
	 * Clears the interaction queue. Use with caution as this
	 * removes all interactions entirely.
	 */
	public static void clear()
	{
		interactionQueue.clear();
	}
	
	/**
	 * A utility method to safely dispose of a queue containing a subset
	 * of interactions acquired from this class.
	 * @param queue the <tt>ArrayList</tt> of interactions to be freed.
	 */
	public static void free(ArrayList<Entity> queue)
	{
		Engine engine = FrameworkObjects.getEngine();
		
		queue.forEach((entity) ->
		{
			entity.removeAll();
			engine.removeEntity(entity);
		});
	}
}
