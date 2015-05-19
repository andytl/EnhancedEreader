package com.example.testerapplication;

import org.opencv.core.Mat;

public class EyeTrackerThread extends Thread implements Runnable {

	private ReaderActivity ra;
	private NewReadCallback nrc;
	private CVTaskBuffer<Mat> tasks;
	
	public EyeTrackerThread(ReaderActivity ra, NewReadCallback nrc, CVTaskBuffer<Mat> tasks) {
		this.ra = ra;
		this.nrc = nrc;
		this.tasks = tasks;
	}
	
	@Override
	public void run() {
		Mat mat;
		while (true) {
			try {
				mat = tasks.getTask();
			} catch (InterruptedException e) {
				continue;
			}
			final DoublePoint dp = NativeInterface.onNewFrame(mat);
			System.err.print(dp);
			if (dp.x != 0 && dp.y != 0) {
				// send result back to UI thread. Shahar will do this
					ra.getHandler().post(new Runnable() {
						@Override
						public void run() {
							nrc.newReadPosition(dp.x, dp.y);
						}
					});
				}
		}
	}
	
}
