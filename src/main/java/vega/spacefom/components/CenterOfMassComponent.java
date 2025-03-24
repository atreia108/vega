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

package vega.spacefom.components;

import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.encoding.HLAfixedArray;
import hla.rti1516e.encoding.HLAfloat64LE;
import vega.core.IComponent;
import vega.spacefom.types.Vector3;

public class CenterOfMassComponent implements IComponent
{
	public Vector3 position = new Vector3(0, 0, 0);
	
	@Override
	public void reset()
	{
		position.x = 0;
		position.y = 0;
		position.z = 0;
	}
	
	@Override
	public byte[] encode(EncoderFactory encoder)
	{
		HLAfixedArray<HLAfloat64LE> target = position.convert(encoder);
		return target.toByteArray();
	}
	
	@Override
	public void decode(byte[] data, EncoderFactory encoder)
	{
		HLAfixedArray<HLAfloat64LE> decodedVector = encoder.createHLAfixedArray
		(
				encoder.createHLAfloat64LE(),
				encoder.createHLAfloat64LE(),
				encoder.createHLAfloat64LE()
		);
		
		try
		{
			decodedVector.decode(data);
			position.x = decodedVector.get(0).getValue();
			position.y = decodedVector.get(1).getValue();
			position.z = decodedVector.get(2).getValue();
		}
		catch (DecoderException e)
		{
			e.printStackTrace();
		}
	}
}
