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

package io.github.atreia108.vega.data;

/**
 * An enum representation of the various execution modes supported in the
 * SpaceFOM i.e., <code>EXEC_MODE_INITIALIZATING</code>,
 * <code>EXEC_MODE_RUNNING</code>, <code>EXEC_MODE_FREEZE</code>, and
 * <code>EXEC_MODE_SHUTDOWN</code>.
 * 
 * @author Hridyanshu Aatreya
 * @since 1.0.0
 */
public enum ExecutionMode
{
	EXEC_MODE_INITIALIZING((short) 1), EXEC_MODE_RUNNING((short) 2), EXEC_MODE_FREEZE((short) 3), EXEC_MODE_SHUTDOWN((short) 4);

	private short value;

	private ExecutionMode(short value)
	{
		this.value = value;
	}

	public static ExecutionMode get(short value)
	{
		for (ExecutionMode execMode : ExecutionMode.values())
		{
			if (execMode.value == value)
				return execMode;
		}

		return null;
	}

	public short getValue()
	{
		return value;
	}
}
