package com.example.testerapplication;

import org.opencv.core.Mat;

public class MatPoint {
	public Mat mat;
	public double x;
	public double y;
	public int positionID;
	
	public MatPoint(Mat mat, double x, double y, int positionID) {
		this.mat = mat;
		this.x = x;
		this.y = y;
		this.positionID = positionID;
	}
	
}
