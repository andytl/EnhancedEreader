package com.example.enhancedereader.datastructures;

import org.opencv.core.Mat;

public class MatTime {
	public Mat mat;
	public long time;
	
	public MatTime(Mat mat, long time) {
		this.mat = mat;
		this.time = time;
	}
}
