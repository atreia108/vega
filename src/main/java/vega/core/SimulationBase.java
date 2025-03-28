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

package vega.core;

import hla.rti1516e.RTIambassador;
import hla.rti1516e.RtiFactoryFactory;
import hla.rti1516e.encoding.EncoderFactory;

public abstract class SimulationBase implements Runnable
{
	protected World world;

	protected RTIambassador rtiAmbassador;
	protected EncoderFactory encoder;
	protected ConfigurationParser parser;

	Thread simulationLoopThread;
	private long FRAME_RATE;

	public SimulationBase()
	{
		simulationLoopThread = new Thread(this);
		parser = new ConfigurationParser();
		
		try
		{
			rtiAmbassador = RtiFactoryFactory.getRtiFactory().getRtiAmbassador();
			encoder = RtiFactoryFactory.getRtiFactory().getEncoderFactory();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		world = new World(this);
		calculateFrameRate();
	}
	
	private void calculateFrameRate()
	{
		String frameRateParameter = parser.getSimulationConfiguration().get("FrameRate");
		FRAME_RATE = (long) Math.ceil((1/Double.valueOf(frameRateParameter)) * 1000);
	}
	
	public long getSimulationFrameRate() { return FRAME_RATE; }
	
	public abstract void initialize();

	public EncoderFactory getEncoder() { return encoder; }

	public World getWorld() { return world; }

	public RTIambassador getRtiAmbassador() { return rtiAmbassador; }

	public ConfigurationParser getParser() { return parser; }

	public void run()
	{
		System.out.println("[INFO] Simulation loop is now running");
		try
		{
			while (true)
			{
				world.update();
				Thread.sleep(FRAME_RATE);
			}
		}
		catch (InterruptedException e)
		{
			System.out.println("[INFO] Simulation loop was terminated prematurely.");
		}
	}

	public void play() 
	{
		simulationLoopThread.start();
	}
	
	public void pause() { simulationLoopThread.interrupt(); }
}
