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

package io.github.atreia108.vega.utils;

import java.util.concurrent.CountDownLatch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A global CountDownLatch object to resume/pause simulation execution.
 * 
 * @author Hridyanshu Aatreya
 * @since 1.0.0
 */
public final class ExecutionLatch
{
	private static final Logger LOGGER = LogManager.getLogger();

	private static CountDownLatch latch;

	/**
	 * Enables the execution latch causing program execution to halt at that point. Some other operation (possibly on another thread)
	 * has to disable it for the program to resume past that point.
	 */
	public static void enable()
	{
		if (isActive())
			LOGGER.warn("The execution latch cannot be enabled because it is waiting for some operation to terminate and disable it.");

		latch = new CountDownLatch(1);

		try
		{
			latch.await();
		}
		catch (InterruptedException e)
		{
			LOGGER.error("Simulation was terminated prematurely\n[REASON]", e);
			System.exit(1);
		}
	}

	/**
	 * Disables the execution latch causing the program to resume from where the latch was previously enabled.
	 */
	public static void disable()
	{
		if (!isActive())
			LOGGER.warn("The execution latch cannot be disabled since it has not been enabled yet.");
		else
		{
			latch.countDown();
			latch = null;
		}
	}

	/**
	 * Returns the status of the execution latch.
	 * @return A boolean value indicating the status of the latch.
	 */
	public static boolean isActive()
	{
		if (latch != null)
			return true;
		else
			return false;
	}
}
