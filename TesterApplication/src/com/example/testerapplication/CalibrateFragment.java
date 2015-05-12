package com.example.testerapplication;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.example.testerapplication.display.CircleView;

public class CalibrateFragment extends Fragment implements CvCameraViewListener2 {

	private Mat                    mRgba;
	private Mat                    mGray;
    public CameraBridgeViewBase   mOpenCvCameraView = null;
    private double curX;
    private double curY;
    private double frameCount;
	private static final int CALIBRATE_FRAME_COUNT = 100;
	private boolean complete = false;
	private ReaderActivity ra;
    private ExecutorService threadPool;


    @Override
    public void onAttach(Activity activity) {
    	super.onAttach(activity);
    	try {
    		this.ra = (ReaderActivity)activity;
    	} catch (ClassCastException e) {
    		e.printStackTrace();
    	}
    }
	
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.calibrate_fragment, container, false);
    	mOpenCvCameraView = (CameraBridgeViewBase) rootView.findViewById(R.id.web_camera_view);
		mOpenCvCameraView.setCvCameraViewListener(this);
		mOpenCvCameraView.setCameraIndex(1);
		this.frameCount = 0;
		threadPool = Executors.newFixedThreadPool(100);
		return rootView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (ra.connected) {
			enableCameraView();
		}
		calibrateTopLeft();
	}
	
	public void enableCameraView() {
		if (mOpenCvCameraView != null) {
			mOpenCvCameraView.enableView();
		}
	}
	
	public void disableCameraView() {
		if (mOpenCvCameraView != null) {
			mOpenCvCameraView.disableView();
		}
	}

	@Override
	public void onCameraViewStarted(int width, int height) {
	    mGray = new Mat();
        mRgba = new Mat();		
	}

	@Override
	public void onCameraViewStopped() {
		mGray.release();
		mRgba.release();		
	}
	
	private void calibrateTopLeft() {
		curX = -1;
		curY = -1;
		final View rootView = getView();
		ra.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				drawCircle(ra, rootView, rootView.getLeft(), rootView.getTop());
			}
		});
		
	}
	
	private void calibrateTopRight()  {
		curX = 1;
		curY = -1;
		final View rootView = getView();
		ra.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				drawCircle(ra, rootView, rootView.getRight()-10, rootView.getTop()+10);
			}
		});
	}
	
	private void calibrateBottomLeft()  {
		curX = -1;
		curY = 1;
		final View rootView = getView();
		ra.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				drawCircle(ra, rootView, rootView.getLeft()+10, rootView.getBottom()-10);
			}
		});
	}
	
	private void calibrateBottomRight() {
		curX = 1;
		curY = 1;
		final View rootView = getView();
		ra.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				drawCircle(ra, rootView, rootView.getRight()-10, rootView.getBottom()-10);
			}
		});
	}
	
	private void drawCircle(Context context, View rootView, double x, double y) {
		RelativeLayout rl = (RelativeLayout) rootView.findViewById(R.id.calibrate_circle_overlay);
		CircleView cv = new CircleView(context, (int)x, (int)y, 100, 0xFFFF0000);
		rl.removeAllViews();
		rl.addView(cv);
	}
	
	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		// TODO Auto-generated method stub
		if (complete) {			
			ra.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					disableCameraView();
					ra.enterWebMode();
				}
			});
			return null;		
		} else {
			Mat gray = inputFrame.gray();
			Mat grayT = gray.t();
			Core.flip(gray.t(), grayT, -1);
			Imgproc.resize(grayT, grayT, gray.size());
			gray.release();
			if (frameCount > CALIBRATE_FRAME_COUNT) {
				frameCount = 0;
				setNextCalibratePosition(getActivity(), getView());
			}
			
			
			threadPool.execute(new CalibratePositionThread(curX, curY, grayT));
			frameCount++;
			
			return grayT;
		}
	}
	
	private void setNextCalibratePosition(Context context, View rootView) {
		if (curX == -1 && curY == -1) {
			calibrateTopRight();
		} else if (curX == 1 && curY == -1) {
			calibrateBottomLeft(); 
		} else if (curX == -1 && curY == 1) {
			calibrateBottomRight();
		} else {
			complete = true;
		}
	}

	
}
