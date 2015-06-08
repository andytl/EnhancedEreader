package com.example.enhancedereader;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.example.enhancedereader.datastructures.CVTaskBuffer;
import com.example.enhancedereader.datastructures.DoublePoint;
import com.example.enhancedereader.datastructures.MatPoint;
import com.example.enhancedereader.display.CircleView;
import com.example.testerapplication.R;

public class CalibrateFragment extends Fragment implements CvCameraViewListener2, OnTouchListener{

	private Mat mGrayT;
	private Mat mGray;
	private Mat mBlack = null;
    public CameraBridgeViewBase   mOpenCvCameraView = null;
	public boolean validFrame = false;
	private ReaderActivity ra;
    private CVTaskBuffer<MatPoint> tasks;
    private EyeTrainerThread trainerThread;
    private CalibrationState cState;
    private boolean visible = true;
    
    
    public CalibrateFragment() {
    	super();
    	tasks = new CVTaskBuffer<MatPoint>();
    }
    
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
		rootView.setOnTouchListener(this);
		return rootView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (ra.connected) {
			enableCameraView();
		}
		cState = new CalibrationState();
		updateCircle(cState.getCurrentCoordinate());
		new StartCalibrationDialogFragment().show(getFragmentManager(), "start_callibartion");
	}
	
	private void startCalibration() {
		cState = new CalibrationState();
		trainerThread = new EyeTrainerThread(tasks, cState, this, ra);
		trainerThread.start();
		updateCircle(cState.getCurrentCoordinate());
		validFrame = true;
	}
	
	@Override
	public void onPause() {
		super.onPause();
		if (trainerThread != null) {
			trainerThread.finish();
		}
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
		if (rootView != null) {
			RelativeLayout rl = (RelativeLayout) rootView.findViewById(R.id.calibrate_circle_overlay);
			if (rl != null) {
				CircleView cv = new CircleView(context, (int)x, (int)y, 100, 0xFF008AE6);
				rl.removeAllViews();
				rl.addView(cv);
			}
		}
	}
	
	public void updateCircle(DoublePoint dp) {
		drawCircle(getActivity(), getView(), dp.x, dp.y);
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
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		if (ReaderActivity.FLIP) {
			mGray = inputFrame.gray();
			Mat square = new Mat(mGray, getCropArea(mGray));
			square = square.clone();
			Mat tempT = square.t();
			Mat squareT = square.t();
			Core.flip(tempT,  squareT, -1);
			
			square.release();
			tempT.release();
			Imgproc.resize(squareT, mGray, mGray.size());
			if (cState !=  null) {
				if (!cState.initialized()) {
					View rootView = getView();
					cState.setDimensions(rootView.getWidth(), rootView.getHeight());
				}
				if (validFrame) {
					validFrame = false;
					DoublePoint dp = cState.getCurrentCoordinate();
					View rootView = getView();
					tasks.addTask(new MatPoint(squareT, interpolateX(dp.x, rootView), interpolateY(dp.y, rootView), cState.getPositionID()));
				} else {
					squareT.release();
				}
			}
			if (visible) {
				return mGray;
			} else {
				if (mBlack == null) {
					mBlack = Mat.zeros(mGray.size(), 0);
				}
				mGray.release();
				return mBlack;
			}	
		} else {
			mGray = inputFrame.gray();
			Mat square = new Mat(mGray, getCropArea(mGray));
			square = square.clone();
			Core.flip(square, square, 0);
			Core.flip(square.t(), square, 0);
//			Core.flip(square,  square, -1);			
			Imgproc.resize(square, mGray, mGray.size());
			if (cState !=  null) {
				if (!cState.initialized()) {
					View rootView = getView();
					cState.setDimensions(rootView.getWidth(), rootView.getHeight());
				}
				if (validFrame) {
					validFrame = false;
					DoublePoint dp = cState.getCurrentCoordinate();
					View rootView = getView();
					tasks.addTask(new MatPoint(square, interpolateX(dp.x, rootView), interpolateY(dp.y, rootView), cState.getPositionID()));
				} else {
					square.release();
				}
			}
			if (visible) {
				return mGray;
			} else {
				if (mBlack == null) {
					mBlack = Mat.zeros(mGray.size(), 0);
				}
				mGray.release();
				return mBlack;
			}
		}
	}
	
	private Rect getCropArea(Mat m) {
		int width = m.cols();int height= m.rows();
		if (width >  height) {
			int start = (width-height)/2;
			return new Rect(start, 0 , height, height);
		} else {
			int start = (height - width) /2 ;
			return new Rect( 0, start ,width, width);
		}
	}
	
	private void toggleCameraView() {
		visible = !visible;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		toggleCameraView();
		return false;
	}
	
	private class StartCalibrationDialogFragment extends DialogFragment {
		@Override
	    public Dialog onCreateDialog(Bundle savedInstanceState) {
	        
	        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	        builder.setTitle("Calibration Phase")
	        	   .setMessage("Follow the blue dot with your eyes")
	               .setPositiveButton("Start Calibration", new DialogInterface.OnClickListener() {
	                   public void onClick(DialogInterface dialog, int id) {
	                	   startCalibration();
	                   }
	               });
	        return builder.create();
	    }

	}
}
