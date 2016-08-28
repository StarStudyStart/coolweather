package com.coolweather.app.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.coolweather.app.model.City;
import com.coolweather.app.model.CoolWeatherDB;
import com.coolweather.app.model.County;
import com.coolweather.app.model.Province;

public class Utility {
	/**
	 * �����ʹ�����������ص�ʡ������
	 */
	public synchronized static boolean handleProvincesResponse(String response,
			CoolWeatherDB coolWeatherDB) {
		if (!TextUtils.isEmpty(response)) {
			String[] allProvinces = response.split(",");
			if (allProvinces != null && allProvinces.length > 0) {
				for (String p : allProvinces) {
					String[] array = p.split("\\|");
					Province province = new Province();
					province.setProvinceCode(array[0]);
					province.setProvinceName(array[1]);
					// �����������ĵ����ݴ洢��Province����
					coolWeatherDB.saveProvince(province);
				}
				return true;
			}

		}
		return false;

	}

	/**
	 * �����ʹ�����������ص��м�����
	 */
	public static boolean handleCitiesResponse(String response,
			CoolWeatherDB coolWeatherDB, int provinceId) {
		if (!TextUtils.isEmpty(response)) {
			String[] allCities = response.split(",");
			if (allCities != null && allCities.length > 0) {
				for (String p : allCities) {
					String[] array = p.split("\\|");
					City city = new City();
					city.setCityCode(array[0]);
					city.setCityName(array[1]);
					city.setProvinceId(provinceId);
					// �����������ĵ����ݴ洢��City����
					coolWeatherDB.saveCity(city);
				}
				return true;
			}

		}
		return false;

	}

	/**
	 * �����ʹ�����������ص��ؼ�����
	 */
	public static boolean handleCountiesResponse(String response,
			CoolWeatherDB coolWeatherDB, int cityId) {
		if (!TextUtils.isEmpty(response)) {
			String[] allCounties = response.split(",");
			if (allCounties != null && allCounties.length > 0) {
				for (String p : allCounties) {
					String[] array = p.split("\\|");
					County county = new County();
					county.setCountyCode(array[0]);
					county.setCountyName(array[1]);
					county.setCityId(cityId);
					// �����������ĵ����ݴ洢��County����
					coolWeatherDB.saveCounty(county);
				}
				return true;
			}

		}
		return false;

	}

	/**
	 * �������������ص�json���ݣ����ҽ������������ݴ洢������
	 */
	public static boolean handleWeatherInfoResponse(Context context,
			String response, String weatherCode) {
		try {
			JSONObject jsonObject = new JSONObject(response);
			JSONObject weatherInfo = jsonObject.getJSONObject("data");
			String cityName = weatherInfo.getString("city");
			String currentTemp = weatherInfo.getString("wendu");
			JSONArray jsonArray = weatherInfo.getJSONArray("forecast");
			
			String temp1 = jsonArray.getJSONObject(0).getString("low");
			String[] array1 = temp1.split(" ");
			String lowTemp = array1[1];
			
			String temp2 = jsonArray.getJSONObject(0).getString("high");
			String[] array2 = temp2.split(" ");
			String highTemp = array2[1];
			
			String weatherDesp = jsonArray.getJSONObject(0).getString("type");
			saveWeatherInfo(context, cityName, currentTemp, lowTemp, highTemp,
					weatherDesp, weatherCode);
			if (cityName != null && weatherDesp != null && weatherDesp != null) {
				return true;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * ��һϵ��������Ϣ���뵽
	 */
	public static void saveWeatherInfo(Context context, String cityName,
			String currentTemp, String lowTemp, String highTemp, String weatherDesp,
			String weatherCode) {
		// ��ȡ��ǰʱ��
		SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy��M��d��",
				Locale.CHINA);
		String currentDate = simpleFormat.format(new Date());
		// ��ȡ���ش洢����
		SharedPreferences.Editor editor = PreferenceManager
				.getDefaultSharedPreferences(context).edit();
		editor.clear();
		editor.putString("city_name", cityName);
		editor.putString("current_temp", currentTemp);
		editor.putString("low_temp", lowTemp);
		editor.putString("high_temp", highTemp);
		editor.putString("weather_Desp", weatherDesp);
		editor.putString("current_date", currentDate);
		editor.putString("weather_code", weatherCode);
		editor.putBoolean("city_selected", true);

		editor.commit();

	}

}
