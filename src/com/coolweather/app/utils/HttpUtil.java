package com.coolweather.app.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpUtil {
	public static void snedHttpRequest(final String address,
			final HttpCallBackListener httpCallBackListener) {
		new Thread(new Runnable() {

			@Override
			public void run() {

				/*
				 * try { HttpClient httpClient = new DefaultHttpClient();
				 * HttpGet httpGet = new HttpGet(address);
				 * httpGet.setHeader("Accept-Language", "zh-CN"); HttpResponse
				 * httpResponse = httpClient.execute(httpGet); if
				 * (httpResponse.getStatusLine().getStatusCode() == 200) {
				 * HttpEntity entity = httpResponse.getEntity(); String response
				 * = EntityUtils.toString(entity, "utf-8"); if
				 * (httpCallBackListener != null) { // 回调onFinish方法
				 * httpCallBackListener.onFinish(response.toString()); } } }
				 * catch (Exception e) { // 回調onError方法 if (httpCallBackListener
				 * != null) { httpCallBackListener.onError(e); } }
				 */

				HttpURLConnection connection = null;
				try {
					URL url = new URL(address);
					connection = (HttpURLConnection) url.openConnection();
					connection.setRequestMethod("GET");
					connection.setConnectTimeout(8000);
					connection.setReadTimeout(8000);
					InputStream in = connection.getInputStream();
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(in));
					StringBuilder response = new StringBuilder();
					String line;
					while ((line = reader.readLine()) != null) {
						response.append(line);
					}
					if (httpCallBackListener != null) { // 回调onFinish方法
						httpCallBackListener.onFinish(response.toString());
					}
				} catch (Exception e) { // 回調onError方法
					if (httpCallBackListener != null) {
						httpCallBackListener.onError(e);
					}

				}finally{
					connection.disconnect();
				}

			}
		}).start();

	}
}
