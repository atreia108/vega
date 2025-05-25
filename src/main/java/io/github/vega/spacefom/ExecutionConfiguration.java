package io.github.vega.spacefom;

import hla.rti1516e.ObjectInstanceHandle;

public class ExecutionConfiguration
{
	private ObjectInstanceHandle rtiHandle;
	private String rootFrameName;
	private long leastCommonTimeStep;
	
	public ExecutionConfiguration(String rootFrameName, long leastCommonTimeStep, ObjectInstanceHandle rtiHandle)
	{
		this.rootFrameName = rootFrameName;
		this.leastCommonTimeStep = leastCommonTimeStep;
		this.rtiHandle = rtiHandle;
	}
	
	public String getRootFrameName() { return rootFrameName; }
	
	public long getLeastCommonTimeStep() { return leastCommonTimeStep; }
	
	public ObjectInstanceHandle getRtiHandle() { return rtiHandle; }
}
