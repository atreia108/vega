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

package atreia108.vega.spacefom;

import hla.rti1516e.encoding.EncoderFactory;

public class ExecutionConfiguration
{
	protected EncoderFactory encoder;
	
	private String rootFrameName;
	private ExecutionMode currentExecutionMode;
	private ExecutionMode nextExecutionMode;
	private double nextModeScenarioTime;
	private long leastCommonTimeStep;
	
	public ExecutionConfiguration(String rootFrameName, ExecutionMode currentExecutionMode, ExecutionMode nextExecutionMode, double nextModeScenarioTime, long leastCommonTimeStep, EncoderFactory encoder)
	{
		this.rootFrameName = rootFrameName;
		this.currentExecutionMode = currentExecutionMode;
		this.nextExecutionMode = nextExecutionMode;
		this.nextModeScenarioTime = nextModeScenarioTime;
		this.leastCommonTimeStep = leastCommonTimeStep;
		this.encoder = encoder;
	}

	public String getRootFrameName()
	{
		return rootFrameName;
	}

	public void setRootFrameName(String rootFrameName)
	{
		this.rootFrameName = rootFrameName;
	}

	public ExecutionMode getCurrentExecutionMode()
	{
		return currentExecutionMode;
	}

	public void setCurrentExecutionMode(ExecutionMode currentExecutionMode)
	{
		this.currentExecutionMode = currentExecutionMode;
	}

	public ExecutionMode getNextExecutionMode()
	{
		return nextExecutionMode;
	}

	public void setNextExecutionMode(ExecutionMode nextExecutionMode)
	{
		this.nextExecutionMode = nextExecutionMode;
	}

	public double getNextModeScenarioTime()
	{
		return nextModeScenarioTime;
	}

	public void setNextModeScenarioTime(double nextModeScenarioTime)
	{
		this.nextModeScenarioTime = nextModeScenarioTime;
	}

	public long getLeastCommonTimeStep()
	{
		return leastCommonTimeStep;
	}

	public void setLeastCommonTimeStep(long leastCommonTimeStep)
	{
		this.leastCommonTimeStep = leastCommonTimeStep;
	}
}
