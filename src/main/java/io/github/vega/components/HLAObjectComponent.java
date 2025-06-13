package io.github.vega.components;

import com.badlogic.ashley.core.Component;

import hla.rti1516e.ObjectInstanceHandle;

public class HLAObjectComponent implements Component
{
	public String className = "";
	public String instanceName = "";
	public ObjectInstanceHandle instanceHandle = null;
}
