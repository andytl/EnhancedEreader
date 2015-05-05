package com.example.testerapplication;

import org.opencv.core.Mat;

public class NativeInterface {
	private int fooVal;
	public NativeInterface() {
		// TODO Auto-generated constructor stub
		fooVal = 0;
	}
	
	public void doFoo(){
		fooVal = foo();
	}
	
	public static DoublePoint onNewFrame(Mat mat) {
		DoublePoint dp = new DoublePoint(2000, 2000);
		int resultCode = nativeOnNewFrame(mat.nativeObj, dp);
		if (resultCode == 1) {
			return dp;
		} else {
			return null;
		}
	}
	
	public static void trainOnFrame(Mat mat, double x, double y) {
		int resultCode = nativeTrainOnFrame(mat.nativeObj, x, y);
	}
	
	
	public void trainOnFrame(Mat mat, DoublePoint dp) {
		
	}
	
	public int getFoo() {
		return fooVal;
	}
	
	private static native int nativeOnNewFrame(long mat, DoublePoint dp);
	private static native int nativeTrainOnFrame(long mat, double x, double y);
	private static native int foo();
}
