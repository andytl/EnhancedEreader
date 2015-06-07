package com.example.testerapplication.webcommunication;

import java.io.IOException;
import java.net.HttpURLConnection;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.testerapplication.CalibrateFragment;
import com.example.testerapplication.R;
import com.example.testerapplication.ReaderActivity;
import com.example.testerapplication.datastructures.UserProfile;

public class WebCommNewUser extends WebCommBase implements Runnable {

	private UserProfile user;
	private ReaderActivity ra;
	
	public WebCommNewUser(UserProfile user, ReaderActivity ra) {
		this.user = user;
		this.ra = ra;
	}
	
	@Override
	public void run() {
		String url = "http://attu4.cs.washington.edu:3777/api/user";
		HttpURLConnection conn = connect(url, "POST", getJSON().toString());
		System.out.println(conn);
		try {
			int responsecode = conn.getResponseCode();
		} catch (IOException | NullPointerException e) {
			e.printStackTrace();
		}
		ra.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				ra.getFragmentManager().beginTransaction()
				.replace(R.id.container,  new CalibrateFragment(), ra.CALIBRATE_MODE)
				.commit();
			}
		});
		
	}
	
	public JSONObject getJSON() {
		try {
			JSONObject jobj = new JSONObject();
			jobj.put("username", user.getUserName());
			jobj.put("password", user.getPassword());
			return jobj;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}		
	}
	
}
