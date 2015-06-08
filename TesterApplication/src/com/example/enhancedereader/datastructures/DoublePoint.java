package com.example.enhancedereader.datastructures;

public class DoublePoint {
	public double x;
	public double y;
	
	public DoublePoint() {
		this(0,0);
	}
	public DoublePoint(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public String toString() {
		return "x: " + x + "\ty: " + y;
	}
	
}
