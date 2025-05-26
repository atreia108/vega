package io.github.vega.outpost.components;

import com.badlogic.ashley.core.Component;

import io.github.vega.math.Vector3;

public class PhysicsComponent implements Component
{
	public String parentReferenceFrame = null;
	public Vector3 centerOfMass = null;
	public Vector3 velocity = null;
	public double scalar = 0;
	public Vector3 vector = null;
	public Vector3 angularRate = null;
	public double time = 0;
}
