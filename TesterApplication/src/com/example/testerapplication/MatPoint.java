package com.example.testerapplication;

import org.opencv.core.Mat;

public class MatPoint {
	public Mat mat;
	public double x;
	public double y;
	
	public MatPoint(Mat mat, double x, double y) {
		this.mat = mat;
		this.x = x;
		this.y = y;
	}
	
}
