package com.coolweather.app.activity;

import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.coolweather.app.R;
import com.coolweather.app.service.AutoUpdateWeatherService;
import com.coolweather.app.utils.HttpCallBackListener;
import com.coolweather.app.utils.HttpUtil;
import com.coolweather.app.utils.Utility;

public class WeatherActivity extends Activity implements OnClickListener {
	private LinearLayout weatherInfoLayout;
	private RelativeLayout weather_layout;

	/**
	 * 显示城市名称
	 */
	private TextView cityName;

	/**
	 * 用于显示当前日期
	 */
	private TextView publishText;

	/**
	 * 用于显示当前温度
	 */
	private TextView currentTemp;

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

	/**
	 * 用于接收返回的天气代码
	 */
	private String weatherCode = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_weather);
		// 寻找控件
		weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
		weather_layout = (RelativeLayout) findViewById(R.id.weather_layout);
		cityName = (TextView) findViewById(R.id.city_name);
		publishText = (TextView) findViewById(R.id.publish_text);
		currentTemp = (TextView) findViewById(R.id.current_temp);
		weatherDesp = (TextView) findViewById(R.id.weather_desp);
		temp1 = (TextView) findViewById(R.id.temp1);
		temp2 = (TextView) findViewById(R.id.temp2);
		switch_city = (ImageButton) findViewById(R.id.switch_city);
		refresh_weather = (ImageButton) findViewById(R.id.refresh_weather);

		switch_city.setOnClickListener(this);
		refresh_weather.setOnClickListener(this);
		// 绑定监听器

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
		String address = "http://wthrcdn.etouch.cn/weather_mini?citykey="
				+ weatherCode;
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
							response, weatherCode);
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
					weatherCode = array[1];
					queryWeatherInfo(weatherCode);

				}

			}

			@Override
			public void onError(Exception e) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						SharedPreferences.Editor editor = PreferenceManager
								.getDefaultSharedPreferences(
										WeatherActivity.this).edit();
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
	public void showWeather() {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(this);
		cityName.setText(pref.getString("city_name", ""));
		publishText.setText(pref.getString("current_date", ""));
		currentTemp.setText(pref.getString("current_temp", "") + "℃");
		String type = pref.getString("weather_Desp", "");
		weatherDesp.setText(type);
		temp1.setText(pref.getString("low_temp", ""));
		temp2.setText(pref.getString("high_temp", ""));
		// 获取当前时间
		long time = System.currentTimeMillis();
		final Calendar mCalendar = Calendar.getInstance();
		mCalendar.setTimeInMillis(time);
		// 获得24小时制的时间
		int hour = mCalendar.get(Calendar.HOUR_OF_DAY);
		if (hour > 18 || hour < 6) {
			setNightBackground(type);
		} else {
			setDayBackgroud(type);
		}
		weatherInfoLayout.setVisibility(View.VISIBLE);
		cityName.setVisibility(View.VISIBLE);
		// 自动更新天气
		/**
		 * 这里设置个boolean变量，存入preference中
		 * 
		 */
		autoUpdateWeather();

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.switch_city:
			Intent intent = new Intent(this, ChooseAreaActivity.class);
			intent.putExtra("from_weather_activity", true);
			startActivity(intent);
			finish();
			break;
		case R.id.refresh_weather:
			publishText.setText("同步中...");
			if (!TextUtils.isEmpty(weatherCode)) {
				queryWeatherInfo(weatherCode);
			} else {
				queryWeatherCode(countyCode);
			}

			break;

		default:
			break;
		}
	}

	/**
	 * 自动更新天气信息
	 */
	private void autoUpdateWeather() {
		Intent intentService = new Intent(this, AutoUpdateWeatherService.class);
		startService(intentService);
	}

	/**
	 * 根据得到的天气类型，设置白天的背景图片
	 * 
	 * @param type
	 */
	private void setDayBackgroud(String type) {
		if ("晴".equals(type)) {
			weather_layout.setBackgroundResource(R.drawable.sunny);
		} else if ("多云".equals(type)) {
			weather_layout.setBackgroundResource(R.drawable.overcast_sky);
		} else if ("雷阵雨".equals(type)) {
			weather_layout.setBackgroundResource(R.drawable.rainstorm);
		} else if ("小雨".equals(type) || "中雨".equals(type)) {
			weather_layout.setBackgroundResource(R.drawable.rain_m);
		} else if ("阵雨".equals(type) || "大雨".equals(type) || "暴雨".equals(type)
				|| "大暴雨".equals(type) || "特大暴雨".equals(type)) {
			weather_layout.setBackgroundResource(R.drawable.rain_l);
		} else if ("小雪".equals(type) || "中雪".equals(type)) {
			weather_layout.setBackgroundResource(R.drawable.snow_m);

		} else if ("大雪".equals(type) || "暴雪".equals(type) || "阵雪".equals(type)) {
			weather_layout.setBackgroundResource(R.drawable.snowy_l);
		} else if ("雾".equals(type) || "大雾".equals(type) || "霾".equals(type)) {
			weather_layout.setBackgroundResource(R.drawable.foggy);
		} else if ("阴".equals(type)) {
			weather_layout.setBackgroundResource(R.drawable.cloudy);

		} else if ("沙尘暴".equals(type)) {
			weather_layout.setBackgroundResource(R.drawable.dirt);
		}

	}

	/**
	 * 根据得到的天气类型，设置夜晚的背景图片
	 * 
	 * @param type
	 */
	private void setNightBackground(String type) {
		if ("晴".equals(type)) {
			weather_layout.setBackgroundResource(R.drawable.night_sunny);
		} else {
			weather_layout.setBackgroundResource(R.drawable.night_other);
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

}
