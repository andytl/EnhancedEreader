package com.example.testerapplication;

import java.io.File;

import org.opencv.core.Mat;

public class NativeInterface {

	public static DoublePoint onNewFrame(Mat mat) {
		DoublePoint dp = new DoublePoint(2000, 2000);
		int resultCode = nativeOnNewFrame(mat.nativeObj, dp);
		if (resultCode == 1) {
			return dp;
		} else {
			System.err.println("Try catch error");
			dp = new DoublePoint(-1, -1);
			return dp;
		}
	}
	
	public static boolean trainOnFrame(Mat mat, double x, double y) {
		System.err.println(mat.size());
		int resultCode = nativeTrainOnFrame(mat.nativeObj, x, y);
		if (resultCode == -1) {
			System.err.println("train failed");
			return false;
		} else {
			System.err.println("train success");
			return true;
		}
	}
	
	public static void trainNeuralNetwork() {
		int resultCode = nativeTrainNeuralNet();
	}

	
	public static int initializeTracker(String face, String eyes) {
		return nativeInitializeTracker(face, eyes);
	}
	
	private static native int nativeInitializeTracker(String face, String eyes);
	public static native int nativeOnNewFrame(long mat, DoublePoint dp);
	private static native int nativeTrainOnFrame(long mat, double x, double y);
	private static native int nativeTrainNeuralNet();
}
