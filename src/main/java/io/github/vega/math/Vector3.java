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

package io.github.vega.math;

import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.encoding.HLAfixedArray;
import hla.rti1516e.encoding.HLAfloat64LE;
import io.github.vega.core.IEncodeable;

public class Vector3 implements IEncodeable<HLAfixedArray<HLAfloat64LE>>
{
	public double x;
	public double y;
	public double z;

	public static final Vector3 BACK = new Vector3(0, 0, -1);
	public static final Vector3 DOWN = new Vector3(0, -1, 0);
	public static final Vector3 FORWARD = new Vector3(0, 0, 1);
	public static final Vector3 LEFT = new Vector3(-1, 0, 0);
	public static final Vector3 NEGATIVE_INFINITY = new Vector3(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
	public static final Vector3 ONE = new Vector3(1, 1, 1);
	public static final Vector3 POSITIVE_INFINITY = new Vector3(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
	public static final Vector3 RIGHT = new Vector3(1, 0, 0);
	public static final Vector3 UP = new Vector3(0, 1, 0);
	public static final Vector3 ZERO = new Vector3(0, 0, 0);

	public Vector3(double x, double y, double z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public double magnitude()
	{
		return (x * x) + (y * y) + (z * z);
	}

	@Override
	public HLAfixedArray<HLAfloat64LE> asHLADataElement(EncoderFactory encoder)
	{
		HLAfixedArray<HLAfloat64LE> encodedVector = encoder.createHLAfixedArray(encoder.createHLAfloat64LE(x), encoder.createHLAfloat64LE(y), encoder.createHLAfloat64LE(z));
		return encodedVector;
	}
}
