package com.coolweather.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.coolweather.app.service.AutoUpdateWeatherService;

public class UpdateReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Intent intentSerivce = new Intent(context,
				AutoUpdateWeatherService.class);
		context.startService(intentSerivce);
	}

}
