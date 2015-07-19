package com.foodietrip.android;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.actionbarsherlock.app.SherlockActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.actionbarsherlock.view.Window;
import com.foodietrip.android.library.NetworkState;

public class splash extends SherlockActivity implements LocationListener{
	LocationManager locateManager;
	
	String netProvider,countryCode,userLanguage,low_provider;
	SharedPreferences sPreferences;
	double userLatitude = 0.0,userLongitude = 0.0;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (this.getIntent().getBooleanExtra("EXIT", false)) {
			finish();
			overridePendingTransition(R.anim.finish_in, R.anim.finish_out);
		    return;
		}
		NetworkState networkState = new NetworkState(this);
		boolean isOnline = networkState.checkInternet();
		if(!isOnline) {
			new AlertDialog.Builder(splash.this)
			.setTitle(getResources().getString(R.string.splash_alertNetErrorTitle))
			.setIcon(R.drawable.ic_launcher)
			.setMessage(getResources().getString(R.string.splash_alertNetErrorMes))
			.setPositiveButton(getResources().getString(R.string.alertDialogOkay), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			})
			.show();
		}
		else {
			this.requestWindowFeature(Window.FEATURE_NO_TITLE);   //隱藏標題列
			setContentView(R.layout.splash);
			int currentOrientation = getResources().getConfiguration().orientation;
			if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE)
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
			else
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
			SharedPreferences sprfSetting = PreferenceManager.getDefaultSharedPreferences(this);
			boolean isHyperMode = sprfSetting.getBoolean("prefHyperMode", false);
			if (!networkState.checkWifiState())
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.wifi_connect_suggest), Toast.LENGTH_SHORT).show();
			getNetworkFix();
			userLanguage = Locale.getDefault().getLanguage();
	        Thread userLocationFix = new Thread() {
				@Override
	        	public void run() {
					try {
						long start = System.currentTimeMillis();
						long end = start + 100*1000;    //25 Second * 1000ms/sec
						back: {
							while (System.currentTimeMillis() < end) {
								if (userLatitude != 0.0 && userLongitude != 0.0)
									break back;
							}
						}
						if (userLatitude == 0.0 && userLongitude == 0.0)
							getLastLocation();
					}
					catch (Exception e){
						e.printStackTrace();
					}
					finally {
						Intent intent = new Intent(splash.this,ItemList.class);
						Bundle bundle = new Bundle();
						bundle.putBoolean("afterSplash", true);
						bundle.putDouble("sLatitude", userLatitude);
						bundle.putDouble("sLongitude", userLongitude);
						intent.putExtras(bundle);
						startActivity(intent);
						overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);    //動畫
						finish();
					}
				}
	        };
	        if (!isHyperMode)
	            userLocationFix.start();
	        else
	        	hyperMode();
		}
	}

	public void getLastLocation() {
		locateManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		Location lastlocation = locateManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (lastlocation != null) {
			userLatitude = lastlocation.getLatitude();
			userLongitude = lastlocation.getLongitude();
		}
		else {
			Location lastlocation_net = locateManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			if (lastlocation_net != null) {
				userLatitude = lastlocation_net.getLatitude();
				userLongitude = lastlocation_net.getLongitude();
			}
			else {
				sPreferences = getSharedPreferences("foodbook_pref", MODE_PRIVATE);
				String lat = sPreferences.getString("userLatitude", "0.0");
				String lon = sPreferences.getString("userLongitude", "0.0");
				userLatitude = Double.valueOf(lat);
				userLongitude = Double.valueOf(lon);
			}
		}
	}

	public void hyperMode() {
		getLastLocation();
		while (userLatitude == 0.0 && userLongitude == 0.0) {}
		Intent intent = new Intent(splash.this,ItemList.class);
		Bundle bundle = new Bundle();
		bundle.putBoolean("afterSplash", true);
		bundle.putDouble("sLatitude", userLatitude);
		bundle.putDouble("sLongitude", userLongitude);
		intent.putExtras(bundle);
		startActivity(intent);
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);    //動畫
		finish();
	}

	public void getNetworkFix() {
		locateManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		if (locateManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			locateManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
		}
		else
			getLastLocation();
	}

	@Override
	public void onLocationChanged(Location location) {
		userLatitude = location.getLatitude();
		userLongitude = location.getLongitude();
		Geocoder geocoder = new Geocoder(getBaseContext());
		List<Address> addresses = null;
		if (userLatitude != 0.0 || userLongitude != 0.0) {
			try {
				addresses = geocoder.getFromLocation(userLatitude, userLongitude, 1);
				countryCode = addresses.get(0).getCountryCode();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		sPreferences = getSharedPreferences("foodbook_pref", MODE_PRIVATE);
		Editor preEditor = sPreferences.edit();
		preEditor.putString("userLatitude", Double.toString(userLatitude));
		preEditor.putString("userLongitude", Double.toString(userLongitude));
		preEditor.putString("countryCode", countryCode);
		preEditor.putString("userLanguage", userLanguage);
		preEditor.commit();
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Criteria netCriteria = new Criteria();
		netCriteria.setAccuracy(Criteria.ACCURACY_MEDIUM);    //使用3G/Wifi定位
		netCriteria.setPowerRequirement(Criteria.POWER_LOW);
		netProvider = locateManager.getBestProvider(netCriteria, false);
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (locateManager != null) locateManager.removeUpdates(this);
	}
}
