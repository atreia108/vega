package io.github.vega.core;

import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadLatch
{
	private static final Logger logger = LoggerFactory.getLogger(ThreadLatch.class);

	private static CountDownLatch latch;
	
	public static void start()
	{
		if (latch != null)
			logger.warn("The latch cannot be started as it is still waiting for an operation to finish.");
		
		latch = new CountDownLatch(1);
		
		try
		{
			latch.await();
		}
		catch (InterruptedException e)
		{
			logger.error("Terminated prematurely\n[REASON]\n");
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public static void stop()
	{
		latch.countDown();
		latch = null;
	}
}
