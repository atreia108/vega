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

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.PooledEngine;

import hla.rti1516e.RTIambassador;
import hla.rti1516e.RtiFactory;
import hla.rti1516e.RtiFactoryFactory;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.exceptions.RTIinternalError;
import io.github.atreia108.vega.components.ExCOComponent;
import io.github.atreia108.vega.components.HLAInteractionComponent;
import io.github.atreia108.vega.components.HLAObjectComponent;

/**
 * A collection of objects that are used internally by the framework and are
 * generally available for use within Vega projects.
 * 
 * @author Hridyanshu Aatreya
 * @since 1.0.0
 */
public final record VegaUtilities()
{
	private static RtiFactory rtiFactory;
	private static RTIambassador rtiAmbassador;
	private static EncoderFactory encoderFactory;

	private static PooledEngine engine;
	private static final ComponentMapper<HLAObjectComponent> HLA_OBJECT_MAPPER = ComponentMapper.getFor(HLAObjectComponent.class);
	private static final ComponentMapper<HLAInteractionComponent> HLA_INTERACTION_MAPPER = ComponentMapper.getFor(HLAInteractionComponent.class);
	private static final ComponentMapper<ExCOComponent> SPACEFOM_EXCO_MAPPER = ComponentMapper.getFor(ExCOComponent.class);

	static
	{
		try
		{
			rtiFactory = RtiFactoryFactory.getRtiFactory();
			rtiAmbassador = rtiFactory.getRtiAmbassador();
			encoderFactory = rtiFactory.getEncoderFactory();
		}
		catch (RTIinternalError e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static RtiFactory rtiFactory()
	{
		return rtiFactory;
	}

	public static RTIambassador rtiAmbassador()
	{
		return rtiAmbassador;
	}

	public static EncoderFactory encoderFactory()
	{
		return encoderFactory;
	}

	public static PooledEngine engine()
	{
		return engine;
	}

	public static ComponentMapper<HLAObjectComponent> objectComponentMapper()
	{
		return HLA_OBJECT_MAPPER;
	}

	public static ComponentMapper<HLAInteractionComponent> interactionComponentMapper()
	{
		return HLA_INTERACTION_MAPPER;
	}

	public static ComponentMapper<ExCOComponent> exCOComponentMapper()
	{
		return SPACEFOM_EXCO_MAPPER;
	}

	protected static void initEngineParameters(int minEntities, int maxEntities, int minComponents, int maxComponents)
	{
		if (engine != null)
			return;

		engine = new PooledEngine(minEntities, maxEntities, minComponents, maxComponents);
	}
}
