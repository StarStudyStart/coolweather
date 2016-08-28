package com.coolweather.app.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.coolweather.app.receiver.UpdateReceiver;
import com.coolweather.app.utils.HttpCallBackListener;
import com.coolweather.app.utils.HttpUtil;
import com.coolweather.app.utils.Utility;

public class AutoUpdateWeatherService extends Service {
	private AlarmManager alarmManager;
	private PendingIntent pi;

	// private boolean isUpdateSucc;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		/**
		 * hour时间之后再次更新天气
		 */
		new Thread(new Runnable() {
			@Override
			public void run() {
				// 更新天气
				SharedPreferences pref = PreferenceManager
						.getDefaultSharedPreferences(AutoUpdateWeatherService.this);
				String weatherCode = pref.getString("weather_code", "");
				updateWeatherInfo(weatherCode);
			}
		}).start();
		alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		int anHour = 60 * 60 * 1000;
		long trriger = SystemClock.elapsedRealtime() + anHour;
		Intent intentBroad = new Intent(AutoUpdateWeatherService.this,
				UpdateReceiver.class);
		pi = PendingIntent.getBroadcast(AutoUpdateWeatherService.this, 0,
				intentBroad, 0);
		alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, trriger, pi);

		return super.onStartCommand(intent, flags, startId);

	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		alarmManager.cancel(pi);
	}

	private void updateWeatherInfo(final String weatherCode) {
		if (!TextUtils.isEmpty(weatherCode)) {
			String address = "http://wthrcdn.etouch.cn/weather_mini?citykey="
					+ weatherCode;
			HttpUtil.snedHttpRequest(address, new HttpCallBackListener() {

				@Override
				public void onFinish(String response) {
					Utility.handleWeatherInfoResponse(
							AutoUpdateWeatherService.this, response,
							weatherCode);
					/*
					 * if (isUpdateSucc) {
					 * Toast.makeText(AutoUpdateWeatherService.this,
					 * "随心行-天气更新成功", Toast.LENGTH_SHORT).show(); } else {
					 * Toast.makeText(AutoUpdateWeatherService.this,
					 * "随心行-天气读取数据失败", Toast.LENGTH_SHORT).show(); }
					 */
				}

				@Override
				public void onError(Exception e) {
					Toast.makeText(AutoUpdateWeatherService.this, "自动更新失败",
							Toast.LENGTH_SHORT).show();
				}
			});
		} else {
			// Toast.makeText(this, "天气代码为空", Toast.LENGTH_SHORT).show();
		}
	}

}
