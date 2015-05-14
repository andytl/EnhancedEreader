package com.example.testerapplication;

import java.util.Random;

import org.opencv.core.Mat;

public class EyePositionThread implements Runnable {

	private Mat mat;
	private ReaderActivity ra;
	private NewReadCallback nrc;
	private boolean calibrate;

	
	public EyePositionThread(Mat mat, ReaderActivity ra, NewReadCallback nrc) {
		this.mat = mat;
		this.ra = ra;
		this.nrc = nrc;
	}
	
	
	@Override
	public void run() {
		// call Sunjays code 
		final DoublePoint dp = NativeInterface.onNewFrame(mat);
		System.err.println("x: " + dp.x + "\ty: " + dp.y);
		if (dp.x != 0 && dp.y != 0) {
		// send result back to UI thread. Shahar will do this
			ra.getHandler().post(new Runnable() {
				@Override
				public void run() {
					nrc.newReadPosition(dp.x, dp.y);
//					nrc.newReadPosition(Math.random() * (Math.random() > .5 ? 1 : -1), Math.random() * (Math.random() > .5 ? 1 : -1));
//					System.gc();
				}
			});
		}
	}
}
