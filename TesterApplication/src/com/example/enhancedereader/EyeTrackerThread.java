package com.example.enhancedereader;

import org.opencv.core.Mat;

import com.example.enhancedereader.datastructures.CVTaskBuffer;
import com.example.enhancedereader.datastructures.DoublePoint;
import com.example.enhancedereader.datastructures.MatTime;

public class EyeTrackerThread extends Thread implements Runnable {

	private ReaderActivity ra;
	private NewReadCallback nrc;
	private CVTaskBuffer<MatTime> tasks;
	private static final int MAX_TIME = 500;
	
	public EyeTrackerThread(ReaderActivity ra, NewReadCallback nrc, CVTaskBuffer<MatTime> tasks) {
		this.ra = ra;
		this.nrc = nrc;
		this.tasks = tasks;
	}
	
	// grabs a frame to process and returns the point in 
	// the -1 to 1 coordinate system the frame matches to
	@Override
	public void run() {
		Mat mat;
		while (true) {
			try {
				MatTime mt = tasks.getTask();
				if (System.currentTimeMillis() - mt.time > MAX_TIME) {
					System.err.println("skipping mat");
					mt.mat.release();
					continue;
				}
				mat = mt.mat;
			} catch (InterruptedException e) {
				continue;
			}
			final DoublePoint dp = NativeInterface.onNewFrame(mat);
			mat.release();
			System.err.println(dp);
			if (dp.x != 0 && dp.y != 0) {
				// send result back to UI
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
