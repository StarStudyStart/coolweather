package com.coolweather.app.utils;

interface HttpCallBackListener {
	void onFinish(String response);

	void onError(Exception e);
}
