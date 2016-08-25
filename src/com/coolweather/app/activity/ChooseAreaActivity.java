package com.coolweather.app.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.coolweather.app.R;
import com.coolweather.app.model.City;
import com.coolweather.app.model.CoolWeatherDB;
import com.coolweather.app.model.County;
import com.coolweather.app.model.Province;
import com.coolweather.app.utils.HttpCallBackListener;
import com.coolweather.app.utils.HttpUtil;
import com.coolweather.app.utils.Utility;

public class ChooseAreaActivity extends Activity {
	private static final int LEVEL_PROVINCE = 0;
	private static final int LEVEL_CITY = 1;
	private static final int LEVEL_COUNTY = 2;

	private ProgressDialog progressDialog;
	private CoolWeatherDB coolWeatherDB;
	private TextView title_text;
	private ListView listView;
	private ArrayAdapter<String> arrayAdapter;
	private List<String> dataList = new ArrayList<String>();
	/**
	 * ʡ���б�
	 */
	private List<Province> provinceList;
	/**
	 * �м��б�
	 */
	private List<City> cityList;
	/**
	 * �ؼ��б�
	 */
	private List<County> countyList;
	/**
	 * ѡ�е�ʡ��
	 */
	private Province selectedProvince;
	/**
	 * ѡ�е���
	 */
	private City selectedCity;

	/**
	 * ��ǰѡ�еļ���
	 */
	private int currentLevel;
	
	/**
	 * �Ƿ��weatheractivity��ת����
	 * 
	 */
	private boolean isFromWeatherActivity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		isFromWeatherActivity = getIntent().getBooleanExtra("from_weather_activity", false);
		
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(this);
		if (pref.getBoolean("city_selected", false)&&!isFromWeatherActivity) {
			Intent intent = new Intent(this, WeatherActivity.class);
			startActivity(intent);
			finish();
			return;
		}
		// ȡ�����}��
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		// ���ؿؼ�
		title_text = (TextView) findViewById(R.id.title_text);
		listView = (ListView) findViewById(R.id.listView);

		arrayAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, dataList);
		listView.setAdapter(arrayAdapter);
		coolWeatherDB = CoolWeatherDB.getInstance(this);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (currentLevel == LEVEL_PROVINCE) {
					selectedProvince = provinceList.get(position);
					queryCities();
				} else if (currentLevel == LEVEL_CITY) {
					selectedCity = cityList.get(position);
					queryCounties();
				} else if (currentLevel == LEVEL_COUNTY) {
					String countyCode = countyList.get(position)
							.getCountyCode();
					Intent intent = new Intent(ChooseAreaActivity.this,
							WeatherActivity.class);
					intent.putExtra("county_code", countyCode);
					startActivity(intent);
					finish();
				}

			}
		});
		queryProvinces();
	}

	/**
	 * ��ѯȫ�����е�ʡ�����ȴ����ݿ��в�ѯ�����û���ٵ��������ϲ�ѯ
	 */
	private void queryProvinces() {
		provinceList = coolWeatherDB.loadProvince();
		if (provinceList.size() > 0) {
			dataList.clear();
			for (Province province : provinceList) {
				dataList.add(province.getProvinceName());
			}
			arrayAdapter.notifyDataSetChanged();
			listView.setSelection(0);
			title_text.setText("�й�");
			currentLevel = LEVEL_PROVINCE;
		} else {
			// �ӷ������ж�ȡ����
			queryFromServer(null, "Province");
		}
	}

	/**
	 * ��ѯĳʡ�������е��У����ȴ����ݿ��в�ѯ�����û���ٵ��������ϲ�ѯ
	 */
	private void queryCities() {
		cityList = coolWeatherDB.loadCity(selectedProvince.getId());
		if (cityList.size() > 0) {
			dataList.clear();
			for (City city : cityList) {
				dataList.add(city.getCityName());
			}
			arrayAdapter.notifyDataSetChanged();
			title_text.setText(selectedProvince.getProvinceName());
			listView.setSelection(0);
			currentLevel = LEVEL_CITY;
		} else {
			// �ķ������Ы@ȡ����
			queryFromServer(selectedProvince.getProvinceCode(), "City");
		}

	}

	/**
	 * ��ѯĳ���������е��أ����ȴ����ݿ��в�ѯ�����û���ٵ��������ϲ�ѯ
	 */

	private void queryCounties() {
		countyList = coolWeatherDB.loadCounty(selectedCity.getId());
		if (countyList.size() > 0) {
			dataList.clear();
			for (County county : countyList) {
				dataList.add(county.getCountyName());
			}
			arrayAdapter.notifyDataSetChanged();
			title_text.setText(selectedCity.getCityName());
			listView.setSelection(0);
			currentLevel = LEVEL_COUNTY;
		} else {
			queryFromServer(selectedCity.getCityCode(), "County");

		}
	}

	/**
	 * �ķ�������ֱ�Ӽ��d����ʡ�пh����Ϣ
	 */
	private void queryFromServer(final String code, final String type) {
		String address;
		if (code != null) {
			address = "http://www.weather.com.cn/data/list3/city" + code
					+ ".xml";
		} else {
			address = "http://www.weather.com.cn/data/list3/city.xml";
		}
		// �������ȶԻ���
		showProgressDialog();
		HttpUtil.snedHttpRequest(address, new HttpCallBackListener() {

			@Override
			public void onFinish(String response) {
				boolean result = false;
				if ("Province".equals(type)) {
					result = Utility.handleProvincesResponse(response,
							coolWeatherDB);
				} else if ("City".equals(type)) {
					result = Utility.handleCitiesResponse(response,
							coolWeatherDB, selectedProvince.getId());
				} else if ("County".equals(type)) {
					result = Utility.handleCountiesResponse(response,
							coolWeatherDB, selectedCity.getId());
				}
				if (result) {
					// �ص����߳��д�������
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							// �رս��ȶԻ���
							closeProgressDialog();
							if ("Province".equals(type)) {
								// ������ؽ��Ϊ���������ѭ��
								queryProvinces();
							} else if ("City".equals(type)) {
								queryCities();
							} else if ("County".equals(type)) {
								queryCounties();
							}
						}
					});
				}
			}

			@Override
			public void onError(Exception e) {
				// TODO Auto-generated method stub
				// �ص����̴߳����߼�
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "����ʧ��...",
								Toast.LENGTH_SHORT).show();
					}
				});

			}
		});

	}

	/**
	 * �@ʾ�M�Ȍ�Ԓ��
	 */
	private void showProgressDialog() {
		if (progressDialog == null) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("���ڼ���");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}

	/**
	 * �P�]�M�Ȍ�Ԓ��
	 */
	private void closeProgressDialog() {
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
	}

	/**
	 * ����back�������ݵ�ǰ�ļ������жϣ���ʱӦ�÷������б�ʡ�б�����ֱ���˳�
	 */
	@Override
	public void onBackPressed() {
		if (currentLevel == LEVEL_COUNTY) {
			queryCities();
		} else if (currentLevel == LEVEL_CITY) {
			queryProvinces();
		} else {
			if (isFromWeatherActivity) {
				Intent intent = new Intent(this,WeatherActivity.class);
				startActivity(intent);
			}
			finish();
		}
	}
}
