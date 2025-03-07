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

package atreia108.vega.core;

import com.badlogic.ashley.core.PooledEngine;

import atreia108.vega.utils.ProjectConfigParser;

/**
 * A base abstract class designed to be extended when creating a new simulation
 * using the Vega framework.
 * @author Hridyanshu Aatreya
 * @since 0.1
 */

public abstract class ASimulation {
	protected ProjectConfigParser configParser;
	private PooledEngine engine;
	private long FRAME_RATE_MS;
	
	public ASimulation() {
		configParser = new ProjectConfigParser();
		engine = new PooledEngine();
		initSimulation();
	}
	
	private void initSimulation() {
		String frameRateParameter = configParser.getSimConfig().get("FrameRate");
		FRAME_RATE_MS = (long) Math.ceil((1/Double.valueOf(frameRateParameter)) * 1000);
	}
	
	/**
	 * The main loop where the Entity Component System simulation and HLA update jobs are run at the frame rate specified in the project configuration.
	 */
	protected void update() {
		while (true) {
			try {
				engine.update(1.0f);
				// TODO -Receive HLA updates post-engine update
				Thread.sleep(FRAME_RATE_MS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public ProjectConfigParser getConfigParser() {
		return configParser;
	}
	
	public PooledEngine getEngine() {
		return engine;
	}
}
