package io.github.vega.spacefom.components;

import com.badlogic.ashley.core.Component;

import io.github.vega.spacefom.MTRMode;

public class MTRComponent implements Component
{
	public MTRMode executionMode = MTRMode.MTR_GOTO_RUN;
}
