package com.coolweather.app.utils;

public interface HttpCallBackListener {
	void onFinish(String response);

	void onError(Exception e);
}
