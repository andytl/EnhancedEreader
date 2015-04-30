package com.example.testerapplication;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class ReaderActivity extends Activity implements CvCameraViewListener2 {


	public static final String WEB_MODE = "WEB_MODE";
	public static final String SCROLL_MODE = "SCROLL_MODE";
	public static final String CAMERA_MODE = "CAMERA_MODE";
	public static String CUR_MODE = CAMERA_MODE;
	
	
	/**********      camera stuff      ***************************/
	private Mat                    mRgba;
	private Mat                    mGray;
    public CameraBridgeViewBase   mOpenCvCameraView = null;
    /***********************************************************/

	private BaseLoaderCallback	mLoaderCallback = new BaseLoaderCallback(this) {
			@Override
			public void onManagerConnected(int status) {
					switch (status) {
							case LoaderCallbackInterface.SUCCESS:
							{
									Log.i("TesterApplication", "OpenCV loaded successfully");
									System.loadLibrary("TesterApplication"); 
									// Load native library after(!) OpenCV initialization
									NativeInterface tester = new NativeInterface();
									tester.doFoo();
									System.out.println("JNI Loaded, foo= " + tester.getFoo());
									if (mOpenCvCameraView != null) {
										mOpenCvCameraView.enableView();
									}

							} break;
							default:
							{
									super.onManagerConnected(status);
							} break;
					}
			}
	};

	@Override
	protected void onResume() {
			super.onResume();
		// Load Opencv
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
	}
	
	 @Override
	    public void onPause()
	    {
	        super.onPause();
	        if (mOpenCvCameraView != null) {
	            mOpenCvCameraView.disableView();
	        }
	    }
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		System.out.println("on create");
		setContentView(R.layout.activity_reader);
		
		if (savedInstanceState == null) {
			FragmentManager fm = getFragmentManager();
			if (CUR_MODE.equals(SCROLL_MODE)) {
				fm.beginTransaction()
					.add(R.id.container, new ReaderFragment())
					.commit();
			} else if (CUR_MODE.equals(CAMERA_MODE)) {
				//TODO: camera view
				setContentView(R.layout.camera_layout);
				mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.fd_activity_surface_view);
			    mOpenCvCameraView.setCvCameraViewListener(this);
		      
			} else if (CUR_MODE.equals(WEB_MODE)) {
				getFragmentManager().beginTransaction()
					.add(R.id.container,  new WebFragment())
					.commit();
			}
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.reader, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
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
		new CVProcessingThread(inputFrame.gray()).start();
		return inputFrame.rgba();
	}
}
