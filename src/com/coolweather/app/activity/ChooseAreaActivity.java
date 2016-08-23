package com.coolweather.app.activity;

import java.util.ArrayList;
import java.util.List;

import com.coolweather.app.R;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

public class ChooseAreaActivity extends Activity {
	private TextView title_text;
	private ListView listView;
	private List<String> dataList = new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.choose_area);
		// ╪сть©ь╪Ч
		title_text = (TextView) findViewById(R.id.title_text);
		listView = (ListView) findViewById(R.id.listView);

	}

}
