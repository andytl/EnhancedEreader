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
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.example.testerapplication.display.CircleView;

public class CalibrateFragment extends Fragment implements CvCameraViewListener2, OnTouchListener {

	private Mat                    mGrayT;
	private Mat                    mGray;
    public CameraBridgeViewBase   mOpenCvCameraView = null;
    private double curX;
    private double curY;
    private double frameCount;
	private static final int CALIBRATE_FRAME_COUNT = 100;
	private boolean validFrame = false;
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
		rootView.setOnTouchListener(this);
		return rootView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (ra.connected) {
			enableCameraView();
		}
//		calibrateTopLeft();
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
        mGrayT = new Mat();		
	}

	@Override
	public void onCameraViewStopped() {
		mGray.release();
		mGrayT.release();		
	}

	private void drawCircle(Context context, View rootView, double x, double y) {
		RelativeLayout rl = (RelativeLayout) rootView.findViewById(R.id.calibrate_circle_overlay);
		CircleView cv = new CircleView(context, (int)x, (int)y, 100, 0xFFFF0000);
		rl.removeAllViews();
		rl.addView(cv);
	}
	
	public double interpolateX(double x, View v) {
		double right = v.getRight();
		double left = v.getLeft();
		return (x/(right-left)) * 2 -1;
	}
	
	public double interpolateY(double y, View v) {
		double bottom = v.getBottom();
		double top = v.getTop();
		return (y/(bottom-top)) * 2 -1;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		drawCircle(ra, v, event.getX(), event.getY());
		curX = interpolateX(event.getX(), v);
		curY = interpolateY(event.getY(), v);
		validFrame = true;
		return true;
	}
	
	
	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
			mGray = inputFrame.gray();
			Mat temp1 = mGray.t();
			mGrayT = mGray.t();
			Core.flip(temp1,  mGrayT,  -1);
			Imgproc.resize(mGrayT, mGray, mGray.size());
			temp1.release();
			mGrayT.release();
			if (validFrame) {
				validFrame = false;
				threadPool.execute(new CalibratePositionThread(curX, curY, mGray));
			}
			return mGray;
//		}
	}


	
//	private void setNextCalibratePosition(Context context, View rootView) {
//		if (curX == -1 && curY == -1) {
//			calibrateTopRight();
//		} else if (curX == 1 && curY == -1) {
//			calibrateBottomLeft(); 
//		} else if (curX == -1 && curY == 1) {
//			calibrateBottomRight();
//		} else {
//			complete = true;
//		}
//	}
	
	

	
}
