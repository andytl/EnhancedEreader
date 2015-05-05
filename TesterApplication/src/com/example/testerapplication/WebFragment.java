package com.example.testerapplication;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.Mat;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebFragment extends Fragment implements OnTouchListener, CvCameraViewListener2, NewReadCallback{

	private ReadingMonitor mMonitor;
	private Mat                    mRgba;
	private Mat                    mGray;
    public CameraBridgeViewBase   mOpenCvCameraView = null;
	
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mMonitor = new ReadingMonitor(R.id.web_view, R.id.web_circle_overlay, R.id.web_color_overlay, activity);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View rootView = inflater.inflate(R.layout.web_reader,
				container, false);
		rootView.findViewById(R.id.web_view).setOnTouchListener(this);
		ReaderActivity ra = (ReaderActivity)getActivity();
		mOpenCvCameraView = (CameraBridgeViewBase) rootView.findViewById(R.id.web_camera_view);
		mOpenCvCameraView.setCvCameraViewListener(this);
		
		return rootView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		final View rootView = getView();
		final Context context = getActivity();
		WebView wv = (WebView) rootView.findViewById(R.id.web_view);
		wv.setWebViewClient(new WebViewClient());
		//wv.loadData("<p>asdf</p>", "text/html", null);
		wv.loadUrl("http://www.google.com");
		rootView.post(new Runnable() {
			@Override
			public void run() {
				mMonitor.createColorOverlay(rootView, context);
//				createOverlay(rootView, R.id.web_color_overlay);
			}
		});
		if (((ReaderActivity)getActivity()).connected) {
			enableCameraView();
		}
	}
	
	public void enableCameraView() {
		if (mOpenCvCameraView != null) {
			mOpenCvCameraView.enableView();
		}
	}
	
//	private void createOverlay(View rootView, int tableId) {
//		int top = rootView.getTop();
//		int bottom = rootView.getBottom();
//		int left = rootView.getLeft();
//		int right = rootView.getRight();
//		System.out.println("Overlay dims:");
//		System.out.println("\tleft: \t" + left );
//		System.out.println("\tright: \t" + right );
//		System.out.println("\ttop: \t" + top );
//		System.out.println("\tbottom: \t" + bottom );
//		Context context = getActivity();
//		TableLayout table = (TableLayout)rootView.findViewById(tableId);
//		for (int i = 0; i < (bottom -top)/10; i++) {
//			TableRow tr = new TableRow(context);
//			tr.setLayoutParams(new TableRow.LayoutParams(right-left, 10));
//			tr.setBackgroundColor(0x00000000);
//			TextView tv = new TextView(context);
//			tv.setText("");
//			tr.addView(tv);
//			table.addView(tr);
//		}
//	}
	
	// Takes a point between -1 and 1
	public void newReadPosition(double x, double y) {
		mMonitor.newReadPosition(getView(), x, y);
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
//		int id = v.getId();
//		int action = event.getAction();
//		PointerCoords pc0 = new PointerCoords();
//		event.getPointerCoords(0, pc0);
//		mMonitor.newReadPosition(getView(), pc0.x, pc0.y);
		return false;
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

	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		System.out.println(inputFrame.hashCode());
		View rootView = getView();
		new CVProcessingThread(inputFrame.gray(), (ReaderActivity)getActivity(), this,  
				rootView.getTop(), rootView.getBottom(), rootView.getLeft(), rootView.getRight()).start();
		return inputFrame.rgba();
	}
}
