package com.example.testerapplication;

import org.opencv.core.Mat;

public class CalibratePositionThread extends Thread implements Runnable {
	private double x;
	private double y;
	private Mat mat;
	
	public CalibratePositionThread(double x, double y, Mat mat) {
		this.x = x;
		this.y = y;
		this.mat = mat;
	}
	
	@Override
	public void run() {
		NativeInterface.trainOnFrame(mat, x, y);
	}
	
	
	
}
