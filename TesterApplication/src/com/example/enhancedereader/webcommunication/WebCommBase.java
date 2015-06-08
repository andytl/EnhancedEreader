package com.example.enhancedereader.webcommunication;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

public class WebCommBase extends Thread implements Runnable {
	
	// sets up connections with the given url, using the given type of connection, with the given body
	protected HttpURLConnection connect(String urlString, String method, String body) {
		try {
            HttpURLConnection conn = setupConnection(urlString, method);
            conn.setReadTimeout(10000);
 			conn.setConnectTimeout(15000);
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
	
	private HttpURLConnection setupConnection (String urlString, String method) throws IOException {
		URL url = new URL(urlString);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod(method);
		 conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setUseCaches(false);
        return conn;
	}
	
	// sets up connections with the given url, using the given type of connection, with the given body
	protected HttpURLConnection connect(String urlString, String method, byte[] data) {
		try {
			HttpURLConnection conn = setupConnection(urlString ,method);
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
