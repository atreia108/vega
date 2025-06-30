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

package io.github.vega.converters;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;

import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.encoding.HLAinteger16LE;
import hla.rti1516e.encoding.HLAinteger64BE;
import hla.rti1516e.encoding.HLAunicodeString;
import io.github.vega.components.ExCOComponent;
import io.github.vega.core.IMultiDataConverter;
import io.github.vega.data.ExecutionMode;

public class ExCOConverter implements IMultiDataConverter
{
	private static final Logger LOGGER = LogManager.getLogger();

	private ComponentMapper<ExCOComponent> mapper;

	public ExCOConverter()
	{
		mapper = ComponentMapper.getFor(ExCOComponent.class);
	}

	@Override
	public void decode(Entity entity, EncoderFactory encoder, byte[] buffer, int trigger)
	{
		ExCOComponent component = mapper.get(entity);

		switch (trigger)
		{
			case 0:
				decodeRootFrameName(encoder, buffer, component);
				break;
			case 1:
				decodeCurrentExecutionMode(encoder, buffer, component);
				break;
			case 2:
				decodeNextExecutionMode(encoder, buffer, component);
				break;
			case 3:
				decodeLeastCommonTimeStep(encoder, buffer, component);
				break;
			default:
				LOGGER.warn("Out of bounds value supplied for trigger ({}) in ExCOConverter. Only values between 0-3 are valid");
				break;
		}
	}

	private void decodeRootFrameName(EncoderFactory encoder, byte[] buffer, ExCOComponent component)
	{
		HLAunicodeString target = encoder.createHLAunicodeString();

		try
		{
			target.decode(buffer);
			component.rootFrameName = target.getValue();
		}
		catch (DecoderException e)
		{
			LOGGER.error("Failed to decode the root_frame_name field of the ExCO object instance\n[REASON]", e);
			System.exit(1);
		}
	}

	private void decodeCurrentExecutionMode(EncoderFactory encoder, byte[] buffer, ExCOComponent component)
	{
		HLAinteger16LE target = encoder.createHLAinteger16LE();

		try
		{
			target.decode(buffer);
			component.currentExecutionMode = ExecutionMode.get(target.getValue());
		}
		catch (DecoderException e)
		{
			LOGGER.error("Failed to decode the current_execution_mode field of the ExCO object instance\n[REASON]", e);
			System.exit(1);
		}
	}

	private void decodeNextExecutionMode(EncoderFactory encoder, byte[] buffer, ExCOComponent component)
	{
		HLAinteger16LE target = encoder.createHLAinteger16LE();

		try
		{
			target.decode(buffer);
			component.nextExecutionMode = ExecutionMode.get(target.getValue());
		}
		catch (DecoderException e)
		{
			LOGGER.error("Failed to decode the next_execution_mode field of the ExCO object instance\n[REASON]", e);
			System.exit(1);
		}
	}

	private void decodeLeastCommonTimeStep(EncoderFactory encoder, byte[] buffer, ExCOComponent component)
	{
		HLAinteger64BE target = encoder.createHLAinteger64BE();

		try
		{
			target.decode(buffer);
			component.leastCommonTimeStep = target.getValue();
		}
		catch (DecoderException e)
		{
			LOGGER.error("Failed to decode the least_common_time_step field of the ExCO object instance\n[REASON]", e);
			System.exit(1);
		}
	}

	@Override
	public byte[] encode(Entity entity, EncoderFactory encoder, int trigger)
	{
		// TODO Auto-generated method stub
		return null;
	}

}
