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

package io.github.vega.core;

import com.badlogic.ashley.core.Entity;

import hla.rti1516e.encoding.EncoderFactory;

/**
 * <p>
 * This interface should be used to provide instructions to the framework for
 * how data from components should be translated to a varying number of HLA data
 * types. They are also applicable conversely when writing translated data from
 * the RTI back into components. The "trigger" parameter in the interface's
 * methods are included to help signify which case is being dealt with. Take for
 * example, when the trigger is 0, the string field is to be read from Component
 * A whereas if the trigger is 1, the same would be read from another Component
 * B. It is anticipated that a switch statement will be used inside the encode
 * and decode methods to handle different cases.
 * </p>
 * 
 * <p>
 * For instance, it is highly desirable to use a single converter for all
 * attributes/parameters that are of the type HLAunicodeString. If a
 * <code>IDataConverter</code> is used, multiple converters are needed because
 * the source string is spread across different components, requiring the
 * creation of an inordinate number of converters.
 * </p>
 * 
 * <p>
 * This model is extremely powerful in the sense that it can be used for many
 * HLA class attributes/parameters at once. An entire project could potentially
 * use a single class that implements this interface for all of its conversion
 * processes. Keep in mind that multi-converters should be kept as simple as
 * possible, since burdening a single converter to manage too many attributes
 * risks increase its complexity.
 * </p>
 * 
 * @see io.github.vega.core.IDataConverter
 * 
 * @author Hridyanshu Aatreya
 * @since 1.0.0
 */
public interface IMultiDataConverter
{
	public void decode(Entity entity, EncoderFactory encoder, byte[] buffer, int trigger);

	public byte[] encode(Entity entity, EncoderFactory encoder, int trigger);
}
