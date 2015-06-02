package com.example.testerapplication;

import java.net.HttpURLConnection;

import org.json.JSONException;
import org.json.JSONObject;

public class WebCommNewUser extends WebCommBase implements Runnable {

	private UserProfile user;
	
	public WebCommNewUser(UserProfile user) {
		this.user = user;
	}
	
	@Override
	public void run() {
		String url = "http://attu4.cs.washington.edu:3777/api/user";
		connect(url, "POST", getJSON().toString());
	}
	
	public JSONObject getJSON() {
		JSONObject jobj  = null;
		try {
			jobj = new JSONObject();
			jobj.put("username", user.getUserName());
			jobj.put("password", user.getPassword());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return jobj;
	}
	
}
