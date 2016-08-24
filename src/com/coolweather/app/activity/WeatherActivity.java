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
	 * ��ʾ��������
	 */
	private TextView cityName;

	/**
	 * ������ʾ����ʱ��
	 */
	private TextView publishText;

	/**
	 * ��ǰ����
	 */
	private TextView currentDate;

	/**
	 * ������ʾ�����������Ϣ
	 */
	private TextView weatherDesp;

	/**
	 * ������ʾ�������
	 */
	private TextView temp1;

	/**
	 * ������ʾ�������
	 */
	private TextView temp2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_weather);
		// Ѱ�ҿؼ�
		weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
		cityName = (TextView) findViewById(R.id.city_name);
		publishText = (TextView) findViewById(R.id.publish_text);
		currentDate = (TextView) findViewById(R.id.current_date);
		weatherDesp = (TextView) findViewById(R.id.weather_desp);
		temp1 = (TextView) findViewById(R.id.temp1);
		temp2 = (TextView) findViewById(R.id.temp2);
		String countyCode = getIntent().getStringExtra("county_code");
		if (!TextUtils.isEmpty(countyCode)) {
			// ���ÿؼ�����ʾ��Ϣ
			publishText.setText("ͬ����...");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityName.setVisibility(View.INVISIBLE);
			// �������󣬻�ȡ���ص�������
			// �������󣬻�ȡ������Ϣ��json����
			// �����ص�json���ݣ���ȡ������Ϣ�����Ҵ洢������
			queryWeatherCode(countyCode);
		} else {
			showWeather();
		}
	}

	/**
	 * ��ѯ��������
	 */
	private void queryWeatherCode(String countyCode) {
		String address = "http://www.weather.com.cn/data/list3/city"
				+ countyCode + ".xml";
		queryFormSever(address, "countyCode");
	}

	/**
	 * ��ѯ������Ϣ
	 */
	private void queryWeatherInfo(String weatherCode) {
		String address = "http://www.weather.com.cn/data/cityinfo/"
				+ weatherCode + ".html";
		queryFormSever(address, "weatherCode");
	}

	/**
	 * ��ѯ�ӷ��������ص����ݣ�������ؼ���������д������������룻 ������������룬����������������Ϣ
	 * 
	 * @param code
	 * @param type
	 */
	private void queryFormSever(final String address, final String type) {

		HttpUtil.snedHttpRequest(address, new HttpCallBackListener() {

			@Override
			public void onFinish(String response) {
				if ("weatherCode".equals(type)) {
					// ����������������ݣ���ȡ������Ϣ��
					Utility.handleWeatherInfoResponse(WeatherActivity.this,
							response);
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							// �����߳��и��ÿؼ�
							showWeather();
						}
					});

				} else if ("countyCode".equals(type)) {
					// ������������ص����ݣ���ȡ�������롣
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
						publishText.setText("ͬ��ʧ��...");
					}
				});

			}
		});

	}

	/**
	 * �ӱ���sharePreference�ж�ȡ������Ϣ
	 */
	private void showWeather() {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(this);
		cityName.setText(pref.getString("city_name", ""));
		publishText.setText("����" + pref.getString("publish_time", "") + "����");
		currentDate.setText(pref.getString("current_date", ""));
		weatherDesp.setText(pref.getString("weather_Desp", ""));
		temp1.setText(pref.getString("temp1", ""));
		temp2.setText(pref.getString("temp2", ""));
		weatherInfoLayout.setVisibility(View.VISIBLE);
		cityName.setVisibility(View.VISIBLE);

	}
}
