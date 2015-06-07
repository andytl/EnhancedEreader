package com.example.testerapplication.webcommunication;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class WebCommBase extends Thread implements Runnable {
	protected HttpURLConnection connect(String urlString, String method, String body) {
		try {
			URL url = new URL(urlString);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(10000);
			conn.setConnectTimeout(15000);
			conn.setRequestMethod(method);
			 conn.setDoInput(true);
             conn.setDoOutput(true);
             conn.setUseCaches(false);
			if (body != null) {
	            conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
				OutputStream request = conn.getOutputStream();
				request.write(body.getBytes("UTF-8"));
				request.flush();
				request.close();
				System.err.println(body);
			}
			conn.connect();
			return conn;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	protected HttpURLConnection connect(String urlString, String method, byte[] data) {
		try {
			URL url = new URL(urlString);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//			conn.setReadTimeout(10000);
//			conn.setConnectTimeout(15000);
			conn.setRequestMethod(method);
			 conn.setDoInput(true);
             conn.setDoOutput(true);
             conn.setUseCaches(false);
			if (data != null) {
				OutputStream request = conn.getOutputStream();
				request.write(data);
				request.flush();
				request.close();
			}
			conn.connect();
			return conn;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
