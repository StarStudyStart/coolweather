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
	 * ��ʾ��������
	 */
	private TextView cityName;

	/**
	 * ������ʾ��ǰ����
	 */
	private TextView publishText;

	/**
	 * ������ʾ��ǰ�¶�
	 */
	private TextView currentTemp;

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

	/**
	 * �����л�����
	 */
	private ImageButton switch_city;

	/**
	 * ����ˢ��������Ϣ
	 */
	private ImageButton refresh_weather;

	/**
	 * ��춽���choosAreaActivity�Ђ��f�^���countycode
	 */
	private String countyCode;

	/**
	 * ���ڽ��շ��ص���������
	 */
	private String weatherCode = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_weather);
		// Ѱ�ҿؼ�
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
		// �󶨼�����

		countyCode = getIntent().getStringExtra("county_code");
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
		String address = "http://wthrcdn.etouch.cn/weather_mini?citykey="
				+ weatherCode;
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
							response, weatherCode);
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
						publishText.setText("ͬ��ʧ��...");
					}
				});

			}
		});

	}

	/**
	 * �ӱ���sharePreference�ж�ȡ������Ϣ
	 */
	public void showWeather() {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(this);
		cityName.setText(pref.getString("city_name", ""));
		publishText.setText(pref.getString("current_date", ""));
		currentTemp.setText(pref.getString("current_temp", "") + "��");
		String type = pref.getString("weather_Desp", "");
		weatherDesp.setText(type);
		temp1.setText(pref.getString("low_temp", ""));
		temp2.setText(pref.getString("high_temp", ""));
		// ��ȡ��ǰʱ��
		long time = System.currentTimeMillis();
		final Calendar mCalendar = Calendar.getInstance();
		mCalendar.setTimeInMillis(time);
		// ���24Сʱ�Ƶ�ʱ��
		int hour = mCalendar.get(Calendar.HOUR_OF_DAY);
		if (hour > 18 || hour < 6) {
			setNightBackground(type);
		} else {
			setDayBackgroud(type);
		}
		weatherInfoLayout.setVisibility(View.VISIBLE);
		cityName.setVisibility(View.VISIBLE);
		// �Զ���������
		/**
		 * �������ø�boolean����������preference��
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
			publishText.setText("ͬ����...");
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
	 * �Զ�����������Ϣ
	 */
	private void autoUpdateWeather() {
		Intent intentService = new Intent(this, AutoUpdateWeatherService.class);
		startService(intentService);
	}

	/**
	 * ���ݵõ����������ͣ����ð���ı���ͼƬ
	 * 
	 * @param type
	 */
	private void setDayBackgroud(String type) {
		if ("��".equals(type)) {
			weather_layout.setBackgroundResource(R.drawable.sunny);
		} else if ("����".equals(type)) {
			weather_layout.setBackgroundResource(R.drawable.overcast_sky);
		} else if ("������".equals(type)) {
			weather_layout.setBackgroundResource(R.drawable.rainstorm);
		} else if ("С��".equals(type) || "����".equals(type)) {
			weather_layout.setBackgroundResource(R.drawable.rain_m);
		} else if ("����".equals(type) || "����".equals(type) || "����".equals(type)
				|| "����".equals(type) || "�ش���".equals(type)) {
			weather_layout.setBackgroundResource(R.drawable.rain_l);
		} else if ("Сѩ".equals(type) || "��ѩ".equals(type)) {
			weather_layout.setBackgroundResource(R.drawable.snow_m);

		} else if ("��ѩ".equals(type) || "��ѩ".equals(type) || "��ѩ".equals(type)) {
			weather_layout.setBackgroundResource(R.drawable.snowy_l);
		} else if ("��".equals(type) || "����".equals(type) || "��".equals(type)) {
			weather_layout.setBackgroundResource(R.drawable.foggy);
		} else if ("��".equals(type)) {
			weather_layout.setBackgroundResource(R.drawable.cloudy);

		} else if ("ɳ����".equals(type)) {
			weather_layout.setBackgroundResource(R.drawable.dirt);
		}

	}

	/**
	 * ���ݵõ����������ͣ�����ҹ��ı���ͼƬ
	 * 
	 * @param type
	 */
	private void setNightBackground(String type) {
		if ("��".equals(type)) {
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
