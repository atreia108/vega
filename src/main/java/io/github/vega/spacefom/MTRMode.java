package io.github.vega.spacefom;

public enum MTRMode
{
	MTR_GOTO_RUN((short) 2),
	MTR_GOTO_FREEZE((short) 3),
	MTR_GOTO_SHUTDOWN((short) 4);
	
	private short value;
	
	MTRMode(short value)
	{
		this.value = value;
	}
	
	public static MTRMode find(short value)
	{
		for (MTRMode mode : MTRMode.values())
		{
			if (mode.value == value)
				return mode;
		}
		
		return null;
	}
}
