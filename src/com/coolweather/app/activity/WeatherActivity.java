package com.coolweather.app.activity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.coolweather.app.R;
import com.coolweather.app.utils.HttpCallBackListener;
import com.coolweather.app.utils.HttpUtil;
import com.coolweather.app.utils.Utility;

public class WeatherActivity extends Activity {
	private LinearLayout weatherInfoLayout;

	/**
	 * 显示城市名称
	 */
	private TextView cityName;

	/**
	 * 用于显示发布时间
	 */
	private TextView publishText;

	/**
	 * 当前日期
	 */
	private TextView currentDate;

	/**
	 * 用于显示具体的天气信息
	 */
	private TextView weatherDesp;

	/**
	 * 用于显示最低气温
	 */
	private TextView temp1;

	/**
	 * 用于显示最高气温
	 */
	private TextView temp2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_weather);
		// 寻找控件
		weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
		cityName = (TextView) findViewById(R.id.city_name);
		publishText = (TextView) findViewById(R.id.publish_text);
		currentDate = (TextView) findViewById(R.id.current_date);
		weatherDesp = (TextView) findViewById(R.id.weather_desp);
		temp1 = (TextView) findViewById(R.id.temp1);
		temp2 = (TextView) findViewById(R.id.temp2);
		String countyCode = getIntent().getStringExtra("county_code");
		if (!TextUtils.isEmpty(countyCode)) {
			// 设置控件的显示信息
			publishText.setText("同步中...");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityName.setVisibility(View.INVISIBLE);
			// 发送请求，获取返回的天气码
			// 发送请求，获取天气信息的json数据
			// 处理返回的json数据，获取天气信息，并且存储到本地
			queryWeatherCode(countyCode);
		} else {
			showWeather();
		}
	}

	/**
	 * 查询天气代码
	 */
	private void queryWeatherCode(String countyCode) {
		String address = "http://www.weather.com.cn/data/list3/city"
				+ countyCode + ".xml";
		queryFormSever(address, "countyCode");
	}

	/**
	 * 查询天气信息
	 */
	private void queryWeatherInfo(String weatherCode) {
		String address = "http://www.weather.com.cn/data/cityinfo/"
				+ weatherCode + ".html";
		queryFormSever(address, "weatherCode");
	}

	/**
	 * 查询从服务器返回的数据，如果是县级代码则进行处理返回天气代码； 如果是天气代码，则解析但会的天气信息
	 * 
	 * @param code
	 * @param type
	 */
	private void queryFormSever(final String address, final String type) {

		HttpUtil.snedHttpRequest(address, new HttpCallBackListener() {

			@Override
			public void onFinish(String response) {
				if ("weatherCode".equals(type)) {
					// 处理服务器返回数据，获取天气信息。
					Utility.handleWeatherInfoResponse(WeatherActivity.this,
							response);
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							// 在主线程中更该控件
							showWeather();
						}
					});

				} else if ("countyCode".equals(type)) {
					// 处理服务器返回的数据，获取天气代码。
					String[] array = response.split("\\|");
					String weatherCode = array[1];
					queryWeatherInfo(weatherCode);

				}
			}

			@Override
			public void onError(Exception e) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						publishText.setText("同步失败...");
					}
				});

			}
		});

	}

	/**
	 * 从本地sharePreference中读取天气信息
	 */
	private void showWeather() {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(this);
		cityName.setText(pref.getString("city_name", ""));
		publishText.setText("今日" + pref.getString("publish_time", "") + "发布");
		currentDate.setText(pref.getString("current_date", ""));
		weatherDesp.setText(pref.getString("weather_Desp", ""));
		temp1.setText(pref.getString("temp1", ""));
		temp2.setText(pref.getString("temp2", ""));
		weatherInfoLayout.setVisibility(View.VISIBLE);
		cityName.setVisibility(View.VISIBLE);

	}
}
