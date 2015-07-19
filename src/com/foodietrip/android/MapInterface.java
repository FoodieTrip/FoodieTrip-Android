package com.foodietrip.android;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.foodietrip.android.library.JSONParser;
import com.foodietrip.android.library.NetworkState;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.actionbarsherlock.app.ActionBar;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import android.widget.ArrayAdapter;
import android.widget.Toast;

public class MapInterface extends SherlockFragmentActivity implements LocationListener{
	LatLng myPlace = null;
	LocationManager locationManager;
	Location location;
	Criteria criteria;
	private GoogleMap googleMap;
	Double my_latitude,my_longitude,single_latitude,single_longitude;
	String sid,tag,myProvide;
	boolean userHasPressOk = false;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_interface);
		SharedPreferences sPreferences = getSharedPreferences("foodbook_pref", MODE_PRIVATE);
		boolean neverShowAttention_MapInter = sPreferences.getBoolean("neverShowAttention_MapInter", false);
        if (!neverShowAttention_MapInter && !userHasPressOk) showAlertDialog();
		//ActionBar
		ActionBar actionBar = getSupportActionBar();
		actionBar.setIcon(R.drawable.ic_action_map);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		Intent mapInterface = this.getIntent();
		Bundle myPosition = mapInterface.getExtras();
		tag = myPosition.getString("tag");
		if (tag.equals("")) tag = "all";
		my_latitude = myPosition.getDouble("latitude", 23.979548);    //單一模式, 店家緯度。全覽模式，使用者緯度
		my_longitude = myPosition.getDouble("longitude", 120.696745);    //單一模式, 店家經度。全覽模式，使用者經度
		myPlace = new LatLng(my_latitude, my_longitude);
		if (tag.equals("single")) {
			sid = myPosition.getString("sid");
			//取得自己的位置
			locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			criteria = new Criteria();
			criteria.setPowerRequirement(Criteria.POWER_LOW);
			criteria.setAccuracy(Criteria.ACCURACY_COARSE);
			myProvide = locationManager.getBestProvider(criteria, false);
			locationManager.requestLocationUpdates(myProvide, 15000, 100, this);
		}
		setUpMapifNeeded();
	}

	//Set up map
	public void setUpMapifNeeded() {
		if (googleMap == null) {
			googleMap = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
			googleMap.setMyLocationEnabled(true);    //顯示「自己」的定位點
			googleMap.setTrafficEnabled(true);    //顯示交通資訊
			googleMap.setIndoorEnabled(true);
			googleMap.setOnMapLongClickListener(longClickListener);
			//設定UI
			UiSettings uiSettings = googleMap.getUiSettings();
			uiSettings.setCompassEnabled(true);
			uiSettings.setTiltGesturesEnabled(false);    //把傾斜手勢取消掉
			uiSettings.setRotateGesturesEnabled(true);    //把旋轉手勢取消
			//Move camera instantly to myPlace with a zoom 15
			googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPlace, 15));
			new GetAllStore().execute();
		}
	}

	private GoogleMap.OnMapLongClickListener longClickListener = new GoogleMap.OnMapLongClickListener() {
		@Override
		public void onMapLongClick(LatLng arg0) {
			final String[] items = new String[] {getResources().getString(R.string.mapType_normal),getResources().getString(R.string.mapType_hybird),getResources().getString(R.string.mapType_terrain)};
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(MapInterface.this, android.R.layout.simple_dropdown_item_1line,items);
			new AlertDialog.Builder(MapInterface.this)
			.setAdapter(adapter, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int item) {
					switch (item) {
					case 0:
						googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
						break;
					case 1:
						googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
						break;
					case 2:
						googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
						break;
					}
				}
			})
			.show();
		}
	};

	//AlertDialog
	private void showAlertDialog() {
		new AlertDialog.Builder(MapInterface.this)
		.setTitle(getResources().getString(R.string.mapdragger_alertCautionTitle))
		.setIcon(R.drawable.ic_launcher)
		.setMessage(getResources().getString(R.string.map_alertCautionMessage))
		.setPositiveButton(getResources().getString(R.string.alertDialogOkay), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				userHasPressOk = true;
			}
		})
		.setNeutralButton(getResources().getString(R.string.attention_checkbox), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				SharedPreferences sPreferences = getSharedPreferences("foodbook_pref", MODE_PRIVATE);
				Editor spWriter = sPreferences.edit();
				spWriter.putBoolean("neverShowAttention_MapInter", true);
				spWriter.commit();
			}
		})
		.show();
	}

    /*Action Bar*/
  	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.single_map, menu);
		return true;
	}

	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem navigation = menu.findItem(R.id.navgation_map);
		MenuItem streetView = menu.findItem(R.id.streetView_map);
		//設定如果是大地圖就看不到
		if (tag.equals("all")) {
			navigation.setVisible(false);
			streetView.setVisible(false);
		}
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item){
    	switch(item.getItemId()){
    	case android.R.id.home:
    		finish();
    		this.overridePendingTransition(R.anim.finish_in, R.anim.finish_out);
    		break;
    	case R.id.navgation_map:
    		//設定要前往的URL(single > my)
    		String urlString = String.format("http://maps.google.com/maps?saddr=%f,%f&daddr=%f,%f",
    				single_latitude, single_longitude, my_latitude, my_longitude);
    		Intent navi_intent = new Intent();
    		//交由Google 地圖應用程式接手
    		navi_intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
    		//ActionView: 呈現資料給使用者觀看
    		navi_intent.setAction(android.content.Intent.ACTION_VIEW);
    		//將URL資訊附加在Intent上
    		navi_intent.setData(Uri.parse(urlString));
    		startActivity(navi_intent);
    		break;
    	case R.id.streetView_map:
    		String strUrl = "google.streetview:cbll=" +my_latitude +"," +my_longitude;
    		Intent goStreetView = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(strUrl));
    		startActivity(goStreetView);
    		break;
    	}
		return true;
	}


	//在背景讀取項目
	class GetAllStore extends AsyncTask<String, String, String> {
		JSONObject json;
		int httpResponseCode;
		boolean success;
		@Override
		protected void onPreExecute(){
			Toast.makeText(getBaseContext(), getResources().getString(R.string.map_getInformation), Toast.LENGTH_SHORT).show();
		}
		@Override
		protected String doInBackground(String... args) {
			if (tag.equals("single")) {
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				//params.add(new BasicNameValuePair("action", "get_store_details"));    //set for action
				params.add(new BasicNameValuePair("id",sid));    //Detail 必要條件
				params.add(new BasicNameValuePair("uID", "0"));
				//String action = "get_store_details";     //救急措施
				JSONParser jsonParser = new JSONParser(getApplicationContext());
				json = jsonParser.makeHttpRequest("/details" ,"GET", params);
				if (json == null) {
					Log.e("JSON Object is null", "when doing background task MapInterface");
				    success = false;
				    httpResponseCode = jsonParser.getHttpResponseCode();
					return null;
				}
				success = true;
			    httpResponseCode = jsonParser.getHttpResponseCode();
				//Log.e("Single Map Interface: ", json.toString());
			}
			else {
				List<NameValuePair> item_list = new ArrayList<NameValuePair>();
				//item_list.add(new BasicNameValuePair("action", "get_store_list"));    //set for action
				item_list.add(new BasicNameValuePair("userLatitude", Double.toString(my_latitude)));
				item_list.add(new BasicNameValuePair("userLongitude", Double.toString(my_longitude)));
				JSONParser jsonParser = new JSONParser(getApplicationContext());
				json = jsonParser.makeHttpRequest("/list" ,"GET", item_list);
				if (json == null) {
					Log.e("JSON Object is null", "when doing background task MapInterface");
					success = false;
				    httpResponseCode = jsonParser.getHttpResponseCode();
					return null;
				}
				success = true;
			    httpResponseCode = jsonParser.getHttpResponseCode();
				//Log.e("All Map Interface: ", json.toString());
			}
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					try {
						createMarkerFromJSON(json);
					}
					catch (JSONException e) {
						Log.e("Map Interface", "Error procesing JSON.", e);
					}
				}
			});
			return null;
		}
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (!success) {
				if (httpResponseCode == 900) {
					//連線逾時
					Toast.makeText(getApplicationContext(), getResources().getString(R.string.ConnectionTimeOut), Toast.LENGTH_SHORT).show();
					return;
				}
				if (httpResponseCode == HttpStatus.SC_NOT_FOUND) {
					Toast.makeText(getApplicationContext(), getResources().getString(R.string.default_httpResponse_404), Toast.LENGTH_SHORT).show();
				    return;
				}
				if (httpResponseCode == HttpStatus.SC_UNAUTHORIZED) {
					//授權失敗，每個都有
					Toast.makeText(getApplicationContext(), getResources().getString(R.string.login_httpResponse_401), Toast.LENGTH_SHORT).show();
					return;
				}
				if (httpResponseCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
					Toast.makeText(getApplicationContext(), getResources().getString(R.string.httpResponse_500), Toast.LENGTH_SHORT).show();
				    return;
				}
			}
		}
		void createMarkerFromJSON(JSONObject json) throws JSONException {
			//de-serialize the JSON string into an array of store object
			//Log.e("Map Interface", json.toString());
			JSONArray jsonArray = json.getJSONArray("Store");
			for (int i = 0;i < jsonArray.length(); i = i+1) {
				//create marker from each store in the store data
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				googleMap.addMarker(new MarkerOptions()
				.title(jsonObject.getString("sName"))
				.position(new LatLng(Double.parseDouble(jsonObject.getString("sLatitude")), Double.parseDouble(jsonObject.getString("sLongitude"))))
				);
			}
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.finish_in, R.anim.finish_out);
	}

	@Override
	protected void onResume() {
		super.onResume();
		setUpMapifNeeded();
		NetworkState networkState = new NetworkState(this);
		boolean isOnline = networkState.checkInternet();
		if (!isOnline) {
			new AlertDialog.Builder(this)
			.setTitle(getResources().getString(R.string.splash_alertNetErrorTitle))
			.setIcon(R.drawable.ic_launcher)
			.setMessage(getResources().getString(R.string.splash_alertNetErrorMes))
			.setPositiveButton(getResources().getString(R.string.alertDialogOkay), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent intent = new Intent(MapInterface.this, splash.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					intent.putExtra("EXIT", true);
					startActivity(intent);
					overridePendingTransition(0, 0);
					finish();
				}
			})
			.show();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("DialogBooleanInter", userHasPressOk);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		if (savedInstanceState != null)
			userHasPressOk = savedInstanceState.getBoolean("DialogBooleanInter");
	}

	@Override
	public void onLocationChanged(Location location) {
		single_latitude = location.getLatitude();
		single_longitude = location.getLongitude();
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		criteria = new Criteria();
		criteria.setPowerRequirement(Criteria.POWER_LOW);
		criteria.setAccuracy(Criteria.ACCURACY_COARSE);
		myProvide = locationManager.getBestProvider(criteria, false);
	}
}
