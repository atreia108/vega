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

package io.github.atreia108.vega.core;

import com.badlogic.ashley.core.Entity;

import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.EncoderFactory;

/**
 * <p>
 * This interface should be used to provide instructions to the framework for
 * how data from components should be translated to a single fixed HLA data
 * type. It is also applicable conversely when writing translated data from the
 * RTI back into components.
 * </p>
 * 
 * <p>
 * The HLA uses object-oriented design: under this model, objects and
 * interactions are classes which have fixed attributes/parameters. Vega on the
 * other hand, employs the Entity Component System (ECS) architecture, which
 * falls under data-oriented design (DoD). In ECS, entities contain components
 * which can store multiple fields of data. These do not directly correspond to
 * the single field (attribute/parameter) of a class.
 * </p>
 * 
 * 
 * @see io.github.atreia108.vega.core.IMultiDataConverter
 * 
 * @author Hridyanshu Aatreya
 * @since 1.0.0
 */
public interface IDataConverter
{
	public void decode(Entity entity, EncoderFactory encoderFactory, byte[] buffer) throws DecoderException;

	public byte[] encode(Entity entity, EncoderFactory encoderFactory);
}
