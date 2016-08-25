package com.coolweather.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.coolweather.app.R;
import com.coolweather.app.utils.HttpCallBackListener;
import com.coolweather.app.utils.HttpUtil;
import com.coolweather.app.utils.Utility;

public class WeatherActivity extends Activity implements OnClickListener{
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
	
	/**
	 * 用于切换城市
	 */
	private ImageButton switch_city;
	
	/**
	 * 用于刷新天气信息
	 */
	private ImageButton refresh_weather;
	
	/**
	 * 用於接收choosAreaActivity中傳遞過來的countycode
	 */
	private String countyCode;

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
		switch_city = (ImageButton) findViewById(R.id.switch_city);
		refresh_weather = (ImageButton) findViewById(R.id.refresh_weather);
		
		switch_city.setOnClickListener(this);
		refresh_weather.setOnClickListener(this);
//		绑定监听器
		
		countyCode = getIntent().getStringExtra("county_code");
		if (!TextUtils.isEmpty(countyCode)) {
			// 设置控件的显示信息
			publishText.setText("同步中...");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityName.setVisibility(View.INVISIBLE);
			// 发送请求，获取返回的天气码
			// 发送请求，获取天气信息的json数据
			// 处理返回的json数据，获取天气信息，并且存储到本地
			queryWeatherCode(countyCode);
			Log.d("tag", countyCode);
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
					Log.d("tag", weatherCode);

				}
			
			}

			@Override
			public void onError(Exception e) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
						editor.clear();
						editor.commit();
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

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.switch_city:
			Intent intent = new Intent(this,ChooseAreaActivity.class);
			intent.putExtra("from_weather_activity", true);
			startActivity(intent);
			finish();
			break;
		case R.id.refresh_weather:
			publishText.setText("同步中...");
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			String weatherCode = prefs.getString("weather_code", "");
			if (!TextUtils.isEmpty(weatherCode)) {
				queryWeatherInfo(weatherCode);
			}else {
				queryWeatherCode(countyCode);
			}
			
			break;

		default:
			break;
		}
	}
}
