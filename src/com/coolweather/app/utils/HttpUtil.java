package com.coolweather.app.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

public class HttpUtil {
	public static void snedHttpRequest(final String address,
			final HttpCallBackListener httpCallBackListener){
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				try {
					HttpClient httpClient = new DefaultHttpClient();
					HttpGet httpGet = new HttpGet(address);
					HttpResponse httpResponse = httpClient.execute(httpGet);
					if (httpResponse.getStatusLine().getStatusCode() == 200) {
						HttpEntity entity = httpResponse.getEntity();
						String response = EntityUtils.toString(entity,"utf-8");
						if (httpCallBackListener != null) {
//							回调onFinish方法
							httpCallBackListener.onFinish(response.toString());
						}
					}
				} catch (Exception e) {
//					回調onError方法
					if (httpCallBackListener != null) {
						httpCallBackListener.onError(e);
					}
				} 
				/*HttpURLConnection connection = null;
				try {
					URL url = new URL(address);
					connection = (HttpURLConnection) url.openConnection();
					connection.setRequestMethod("GET");
					connection.setConnectTimeout(8000);
					connection.setReadTimeout(8000);
					InputStream in = connection.getInputStream();
					BufferedReader reader = new BufferedReader(new InputStreamReader(in));
					StringBuilder response = new StringBuilder();
					String line ;
					while((line = reader.readLine()) != null){
						response.append(line);
					}
					if (httpCallBackListener != null) {
//						回调onFinish方法
						httpCallBackListener.onFinish(response.toString());
					}
				} catch (Exception e) {
//					回調onError方法
					if (httpCallBackListener != null) {
						httpCallBackListener.onError(e);
					}
					
				}*/
				
			}
		}).start();
		
	}
}
