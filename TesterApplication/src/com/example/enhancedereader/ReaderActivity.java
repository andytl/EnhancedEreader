package com.example.enhancedereader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import com.example.enhancedereader.datastructures.UserProfile;
import com.example.enhancedereader.webcommunication.WebCommNewUser;


public class ReaderActivity extends Activity {

	public static final boolean VERBOSE = false; 
	public static final boolean OFFLOAD = true;
	public static final boolean FLIP = true;

	public static final String WEB_MODE = "WEB_MODE";
	public static final String LOGIN_MODE = "LOGIN_MODE";
	public static final String CALIBRATE_MODE = "CALIBRATE_MODE";
	
	private boolean show = true;
	
	
	private DbHelper dbHelper;
	private UserProfile currentUser;
	
	private ProgressDialog dialog = null;
	
	public File mCascadeFace;
	public File mCascadeEyes;
	
	private Handler mHandler = null;
    public boolean connected = false;

    
    
    /* ************* OpenCv Setup ***************/
	private BaseLoaderCallback	mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
				case LoaderCallbackInterface.SUCCESS:
				{
					Log.i("TesterApplication", "OpenCV loaded successfully");
					System.loadLibrary("EnhancedEReader"); 
					try {
						File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFace = new File(cascadeDir, "local_eyes.xml");
                        mCascadeEyes = new File(cascadeDir, "local_eyes");
						
						InputStream is = getResources().openRawResource(R.raw.haarcascade_frontalface_alt2);
                    
                        FileOutputStream os = new FileOutputStream(mCascadeFace);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();
                        
                        is = getResources().openRawResource(R.raw.haarcascade_mcs_lefteye);
                        os = new FileOutputStream(mCascadeEyes);
                        
                        while ((bytesRead = is.read(buffer)) != -1) {
                        	os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();
                        int result = NativeInterface.initializeTracker(mCascadeFace.getAbsolutePath(), mCascadeEyes.getAbsolutePath());
                		if (result == 0) {
                			System.err.println("Something failed to load!!!!");
                			return;
                		}
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					// Load native library after(!) OpenCV initialization
					System.out.println("JNI Loaded");
					enableCameraView();
					connected = true;
				} break;
				default:
				{
						super.onManagerConnected(status);
				} break;
			}
		}
	};	
	
	public void enableCameraView() {
		if (isWebMode()) {
			((WebFragment)getFragmentManager().findFragmentByTag(WEB_MODE)).enableCameraView();
		} else if (isCalibrateMode()) {
			((CalibrateFragment)getFragmentManager().findFragmentByTag(CALIBRATE_MODE)).enableCameraView();
		}
	}
	/* ****************************************/
	
	
	/* ********** Android Lifecycle *****************/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		System.out.println("on create");
		setContentView(R.layout.activity_reader);
		
        dbHelper = new DbHelper(this);
        dbHelper.open();
		currentUser = null;
		mHandler = new Handler(Looper.getMainLooper());

		if (currentUser == null) {
			getFragmentManager().beginTransaction()
				.replace(R.id.container,  new LoginFragment(), LOGIN_MODE)
				.commit();
		} else {
			getFragmentManager().beginTransaction()
				.replace(R.id.container, new WebFragment(), WEB_MODE)
				.commit();
		}
	}
	
	@Override
	protected void onResume() {
			super.onResume();
		// Load Opencv
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
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
	/* **********************************/
	
	
	
	// dispatches the read position to the web fragment
	public void newReadPosition(double x, double y) {
		((WebFragment)getFragmentManager().findFragmentByTag(WEB_MODE)).newReadPosition(x, y);
	}
	
	// loads up the profile for the selected user
	public void selectUser(UserProfile user) {
		currentUser = user;
		if (user != null) {
			NativeInterface.loadUserProfile(createLocalFile(user.getUserName()));
			cancelDialog();
			getFragmentManager().beginTransaction()
				.replace(R.id.container, new WebFragment(), WEB_MODE)
				.commit();
		}
	}
	
	// adds the new user to the database and selects it as the current user. 
	public void createNewUser(String userName, String password) {
		UserProfile user = new UserProfile(userName, password);
		currentUser = user;
		boolean result = dbHelper.addUser(currentUser);
		if (result) {
			new WebCommNewUser(user, this).start();
//			getFragmentManager().beginTransaction()
//				.replace(R.id.container,  new CalibrateFragment(), CALIBRATE_MODE)
//				.commit();
		}
	}
	
	public void enterWebMode() {
		getFragmentManager().beginTransaction()
			.replace(R.id.container, new WebFragment(), WEB_MODE)
			.commit();
	}	
	
	// returns a map of username to complete profile for all users in the local db
	public Map<String, UserProfile> getProfiles() {
		Map<String, UserProfile> result = new HashMap<String,UserProfile>();
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		if (!isTableExists(DbHelper.USER_TABLE_NAME, db)) {
			db.execSQL(DbHelper.SQL_CREATE_USERS_TABLE);
		} else {
			db.beginTransaction();
			try {
				String sql = "Select " + DbHelper.USER_ID + ", " + DbHelper.PASSWORD;
				sql += " from " + DbHelper.USER_TABLE_NAME;
				sql += " order by " + DbHelper.USER_ID;
				Cursor cursor = db.rawQuery(sql, null);
				if (cursor != null && cursor.getCount() > 0) {
					cursor.moveToFirst();
					while (!cursor.isAfterLast()) {
						String userName = cursor.getString(0);
						String password = cursor.getString(1);
						UserProfile user = new UserProfile(userName, password);
						result.put(userName, user);
						cursor.moveToNext();
					}
					db.setTransactionSuccessful();
				}
			} finally {
				db.endTransaction();
			}

		}
		return result;
	}
	
	// deletes the parameter profile from the local db
	public boolean removeProfile(UserProfile user) {
		dbHelper.deleteUser(user);
		return true;
	}
	
	private boolean isTableExists(String tableName, SQLiteDatabase db) {
	    boolean result = false;
		Cursor cursor = db.rawQuery(
				"select DISTINCT tbl_name from sqlite_master where tbl_name = '"+tableName+"'", null);
	    if (cursor != null) {
	    	if (cursor.getCount() > 0) {
	    		result = true;
	    	}
	    	cursor.close();
	    }
	    return result;
	}
	

	private boolean isWebMode() {
		return isFragmentMode(WEB_MODE);
	}
	
	private boolean isCalibrateMode() {
		return isFragmentMode(CALIBRATE_MODE);
	}
	
	private boolean isFragmentMode(String fragmentMode) {
		try {
			return getFragmentManager().findFragmentByTag(fragmentMode).isVisible();
		} catch (NullPointerException e) {
			return false;
		}
	}
	
	public Handler getHandler() {
		return mHandler;
	}
	
	public String getUserName() {
		if (currentUser != null) {
			return currentUser.getUserName();
		} else {
			return null;
		}
	}
	
	public String getPassword() {
		if (currentUser != null) {
			return currentUser.getPassword();
		} else {
			return null;
		}
	}
	
	public UserProfile getUserProfile() {
		return currentUser;
	}
	
	// returns the complete path for a new file in the local dir 
	public String createLocalFile(String filename) {
		return getFilesDir() + "/" + filename + ".fann";
	}
	
	// creates a loading dialog
	public void createDialog(String text) {
		dialog = new ProgressDialog(this);
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("Loading. Please wait...");
        dialog.setIndeterminate(true);
	}
	
	// hides the loading dialog
	public void cancelDialog() {
		if (dialog.isShowing()) {
			dialog.cancel();
		}
	}
	
	// shows the loading dialog
	public void displayDialog() {
		dialog.show();
	}
	
	// handles pressing back. Goes back through web pages if it can. 
	// Else, goes back to the login screen
	@Override 
	public void onBackPressed() {
		FragmentManager fm = getFragmentManager();
		WebFragment wf = (WebFragment) fm.findFragmentByTag(WEB_MODE);
		if (!(wf != null && wf.isVisible() && wf.goBack())) {
			LoginFragment lf = (LoginFragment) fm.findFragmentByTag(LOGIN_MODE);
			if (lf == null || !lf.isVisible()) {
				fm.beginTransaction()
					.replace(R.id.container,  new LoginFragment(), LOGIN_MODE)
					.commit();
			} else {
				super.onBackPressed();
			}
		}
	}	
	
	// hides or displays the camera view in the background and the dot of where someone is looking
	 @Override
	 public boolean onKeyDown(int keyCode, KeyEvent event) {
		 WebFragment wf = ((WebFragment)getFragmentManager().findFragmentByTag(WEB_MODE));
		 if (wf.isVisible()) {
		 	 if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)){
		 		 show = false;
		 		 wf.showCameraView(false);
		     } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
		    	 show = true;
		    	 wf.showCameraView(true);
		     }
		     return true;
		 }
		 return false;
	 }
	
	// returns whether or not to show the camera data
	public boolean showCameraView() {
		return show;
	}
	
}

