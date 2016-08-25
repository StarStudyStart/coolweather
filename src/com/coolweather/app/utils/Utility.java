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
	 * 解析和处理服务器返回的省级数据
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
					// 将解析出来的的数据存储到Province表中
					coolWeatherDB.saveProvince(province);
				}
				return true;
			}

		}
		return false;

	}

	/**
	 * 解析和处理服务器返回的市级数据
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
					// 将解析出来的的数据存储到City表中
					coolWeatherDB.saveCity(city);
				}
				return true;
			}

		}
		return false;

	}

	/**
	 * 解析和处理服务器返回的县级数据
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
					// 将解析出来的的数据存储到County表中
					coolWeatherDB.saveCounty(county);
				}
				return true;
			}

		}
		return false;

	}

	/**
	 * 解析服务器返回的json数据，并且将解析出的数据存储到本地
	 */
	public static void handleWeatherInfoResponse(Context context,
			String response) {
		try {
			JSONObject jsonObject = new JSONObject(response);
			JSONObject weatherInfo = jsonObject.getJSONObject("data");
			String cityName = weatherInfo.getString("city");
			String currentTemp = weatherInfo.getString("wendu");
			JSONArray jsonArray = weatherInfo.getJSONArray("forecast");
			String temp1 = jsonArray.getJSONObject(0).getString("low");
			String temp2 = jsonArray.getJSONObject(0).getString("high");
			String weatherDesp = jsonArray.getJSONObject(0).getString("type");
			saveWeatherInfo(context, cityName, currentTemp, temp1, temp2,
					weatherDesp);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 将一系列天气信息存入到
	 */
	public static void saveWeatherInfo(Context context, String cityName,
			String currentTemp, String temp1, String temp2, String weatherDesp) {
		// 获取当前时间
		SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy年M月d日",
				Locale.CHINA);
		String currentDate = simpleFormat.format(new Date());
		// 获取本地存储对象
		SharedPreferences.Editor editor = PreferenceManager
				.getDefaultSharedPreferences(context).edit();
		editor.clear();
		editor.putString("city_name", cityName);
		editor.putString("current_temp", currentTemp);
		editor.putString("temp1", temp1);
		editor.putString("temp2", temp2);
		editor.putString("weather_Desp", weatherDesp);
		editor.putString("current_date", currentDate);
		editor.putBoolean("city_selected", true);
		editor.commit();

	}

}
