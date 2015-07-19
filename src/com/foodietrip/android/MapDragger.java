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
import com.foodietrip.android.library.UserFunctions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.actionbarsherlock.app.ActionBar;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MapDragger extends SherlockFragmentActivity implements OnMarkerDragListener {
	private ProgressDialog pDialog;
	UserFunctions userFunctions;
	int account_uID = 0, height, width;
	TextView txt_AddressSample;
	Button btn_reset;
	LatLng myPlace = null;
	LocationManager locationManager;
	Location location;
	Marker newer_marker,last_opened = null;
	private GoogleMap googleMap;
	Double original_latitude, original_longitude, newer_latitude,newer_longitude;
	String sid,tag;
	boolean userHasPressedOk = false;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_makerdragger);
		SharedPreferences sPreferences = getSharedPreferences("foodbook_pref", MODE_PRIVATE);
		boolean neverShowAttention_MapDrag = sPreferences.getBoolean("neverShowAttention_MapDrag", false);
        if (!neverShowAttention_MapDrag && !userHasPressedOk) showAlertDialog();
		// 從介面取得元件
		txt_AddressSample = (TextView) findViewById(R.id.mapdragger_sample_address);
		btn_reset = (Button) findViewById(R.id.btn_drag_reset);
		btn_reset.setOnClickListener(btn_listener);
		//取出使用者的uID
		userFunctions = new UserFunctions(getApplicationContext());
		boolean loginCheck = userFunctions.isUserLoggedIn(getApplicationContext());
		if (loginCheck == true) {
			account_uID = userFunctions.getUserUid(getApplicationContext());
		}
		// ActionBar
		ActionBar actionBar = getSupportActionBar();
		actionBar.setIcon(R.drawable.ic_action_place);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		Intent mapInterface = this.getIntent();
		Bundle myPosition = mapInterface.getExtras();
		tag = myPosition.getString("tag");
		if (tag.equals("EditItem")) {
			sid = myPosition.getString("sid"); // 取得是哪一個店家
		}
		else {
			original_latitude = myPosition.getDouble("oriLatitude");
			original_longitude = myPosition.getDouble("oriLongitude");
		}
		newer_latitude = original_latitude;
		newer_longitude = original_longitude;
		setUpMapifNeeded();
	}

	// 按鈕監聽
	private Button.OnClickListener btn_listener = new Button.OnClickListener() {
		@Override
		public void onClick(View v) {
			newer_latitude = original_latitude;
			newer_longitude = original_longitude;
			LatLng oriPlace = new LatLng(original_latitude, original_longitude);
			newer_marker.setPosition(oriPlace);
			txt_AddressSample.setText("");
		}
	};

	// Set up map
	public void setUpMapifNeeded() {
		if (googleMap == null) {
			googleMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_dragger)).getMap();
			googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);    //混合圖
			googleMap.moveCamera(CameraUpdateFactory.zoomTo(16));    //test for bug
			googleMap.setOnMarkerDragListener(this);
			googleMap.setMyLocationEnabled(true); // 顯示「自己」的定位點
			googleMap.setTrafficEnabled(false); // 顯示交通資訊
			googleMap.setOnMapLongClickListener(longClickListener);
			// 設定UI
			UiSettings uiSettings = googleMap.getUiSettings();
			uiSettings.setCompassEnabled(true);
			uiSettings.setScrollGesturesEnabled(false);
			uiSettings.setTiltGesturesEnabled(false);
			uiSettings.setRotateGesturesEnabled(true);
			uiSettings.setZoomGesturesEnabled(false);
			uiSettings.setMyLocationButtonEnabled(false); // 我在哪裡定位按鈕關閉
			uiSettings.setZoomControlsEnabled(false); // 縮放控制區域關閉
			if (tag.equals("EditItem"))  new GetAllStore().execute();
			if (tag.equals("AddItem")) setMapforComfirm();
		}
	}

	//AlertDialog
	private void showAlertDialog() {
		new AlertDialog.Builder(MapDragger.this)
		.setTitle(getResources().getString(R.string.mapdragger_alertCautionTitle))
		.setIcon(R.drawable.ic_launcher)
		.setMessage(getResources().getString(R.string.mapdragger_alertCautionMessage))
		.setPositiveButton(getResources().getString(R.string.alertDialogOkay), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				userHasPressedOk = true;
			}
		})
        .setNeutralButton(getResources().getString(R.string.attention_checkbox), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				SharedPreferences sPreferences = getSharedPreferences("foodbook_pref", MODE_PRIVATE);
				Editor spWriter = sPreferences.edit();
				spWriter.putBoolean("neverShowAttention_MapDrag", true);
				spWriter.commit();
			}
		})
		.show();
	}

	private GoogleMap.OnMapLongClickListener longClickListener = new GoogleMap.OnMapLongClickListener() {
		@Override
		public void onMapLongClick(LatLng arg0) {
			final String[] items = new String[] {getResources().getString(R.string.mapType_normal),getResources().getString(R.string.mapType_hybird),getResources().getString(R.string.mapType_terrain)};
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(MapDragger.this, android.R.layout.simple_dropdown_item_1line,items);
			new AlertDialog.Builder(MapDragger.this)
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

	//AddItem 確認地址時使用
	private void setMapforComfirm() {
		myPlace = new LatLng(original_latitude, original_longitude);
		googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPlace, 16));  //Zoom
		newer_marker = googleMap.addMarker(new MarkerOptions()
		.title(getResources().getString(R.string.map_markerForConfirm))
		.position(new LatLng(original_latitude,original_longitude))
		.draggable(true));
	}

	// 取得參考地址
	private String getMyAddress(LatLng location) {
		Geocoder geocoder = new Geocoder(getApplicationContext());
		String address = "";
		List<Address> addresses = null;
		try {
			addresses = geocoder.getFromLocation(location.latitude,location.longitude, 1);
			address = addresses.get(0).getAddressLine(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return address;
	}

	// 在背景讀取項目
	class GetAllStore extends AsyncTask<String, String, String> {
		JSONObject json;
		int httpResponseCode;
		boolean success;
		@Override
		protected void onPreExecute() {
			Toast.makeText(getBaseContext(), getResources().getString(R.string.map_getInformation), Toast.LENGTH_SHORT).show();
		}
		@Override
		protected String doInBackground(String... args) {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			//params.add(new BasicNameValuePair("action", "get_store_details"));    //set for action
			params.add(new BasicNameValuePair("id", sid));    //detail 必要條件
			params.add(new BasicNameValuePair("uID", "0"));
			//String action = "get_store_details";    //救急措施
			JSONParser jsonParser = new JSONParser(getApplicationContext());
			json = jsonParser.makeHttpRequest("/details" ,"GET", params);
			if (json == null) {
				Log.e("JSON Object is null", "when doing background task MapDragger");
				success = false;
				httpResponseCode = jsonParser.getHttpResponseCode();
			    return null;
			}
			success = true;
			httpResponseCode = jsonParser.getHttpResponseCode();
			//Log.e("Single Map Interface: ", json.toString());
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					try {
						createMarkerFromJSON(json);
					} catch (JSONException e) {
						Log.e("Map Interface", "Error procesing JSON.", e);
					}
				}
			});
			return null;
		}
		// 將Camera移動至現在位置
		@Override
		protected void onPostExecute(String file_url) {
			if (success) {
				myPlace = new LatLng(original_latitude, original_longitude);
				googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPlace, 16));  //Zoom
			}
			else {
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
			// de-serialize the JSON string into an array of store object
			//Log.e("Map Interface", json.toString());
			JSONArray jsonArray = json.getJSONArray("Store");
			for (int i = 0; i < jsonArray.length(); i = i + 1) {
				// create marker from each store in the store data
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				original_latitude = Double.parseDouble(jsonObject.getString("sLatitude"));
				original_longitude = Double.parseDouble(jsonObject.getString("sLongitude"));
				newer_marker = googleMap.addMarker(new MarkerOptions()
						.title(jsonObject.getString("sName"))
						.position(new LatLng(original_latitude,original_longitude))
						.draggable(true));
			}
		}
	}

 	/*在背景藉由 AsyncTask方法，儲存修改過的內容*/
  	class SaveItemDetails extends AsyncTask<String,String,String>{
  	    int httpResponseCode;
  	    boolean success;
  		@Override
  		protected void onPreExecute(){
  			super.onPreExecute();
  			pDialog = new ProgressDialog(MapDragger.this);
  			pDialog.setMessage(getResources().getString(R.string.mapdragger_updatingOurLocation));
  			pDialog.setIndeterminate(false);
  			pDialog.setCancelable(false);
  			pDialog.show();
  		}
  		//儲存資料
		@Override
		protected String doInBackground(String... args) {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			//params.add(new BasicNameValuePair("action", "updateItemPosition"));    //set for action
			params.add(new BasicNameValuePair("uID", Integer.toString(account_uID)));
			params.add(new BasicNameValuePair("id", sid));    //必要條件
			params.add(new BasicNameValuePair("sLatitude", Double.toString(newer_latitude)));
			params.add(new BasicNameValuePair("sLongitude", Double.toString(newer_longitude)));
			//藉由Http Request 發送修改資訊
			//Notice that update item url accepts POST method
			//String action = "updateItemPosition";    //救急措施
			JSONParser jsonParser = new JSONParser(getApplicationContext());
			JSONObject json = jsonParser.makeHttpRequest("/update_position" ,"POST" ,params);
			if (json == null) {
				Log.e("JSON Object is null", "when doing background task MapDragger");
				success = false;
				httpResponseCode = jsonParser.getHttpResponseCode();
			    return null;
			}
			success = true;
			httpResponseCode = jsonParser.getHttpResponseCode();
			Intent backToList = new Intent(MapDragger.this, ItemList.class);
			backToList.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			//代表成功更新，為提醒前一個Activity，故回傳100
			setResult(RESULT_OK,backToList);
			startActivity(backToList);
			finish();
			MapDragger.this.overridePendingTransition(R.anim.finish_in, R.anim.finish_out);
			return null;
		}
		//完成背景作業後，防止對話框跳出來
		protected void onPostExecute(String file_url) {
			pDialog.dismiss();
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
					Intent intent = new Intent(MapDragger.this, splash.class);
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
	public void onMarkerDrag(Marker marker) {
	}

	@Override
	public void onMarkerDragEnd(Marker marker) {
		txt_AddressSample.setText(getMyAddress(marker.getPosition()));
		newer_latitude = marker.getPosition().latitude;
		newer_longitude = marker.getPosition().longitude;
	}

	@Override
	public void onMarkerDragStart(Marker marker) {
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.drag_item, menu);
		return true;
	}

	/* ActionBar */
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.drag_marker_save:
			new AlertDialog.Builder(MapDragger.this)
			.setTitle(getResources().getString(R.string.alertComfirmTitle))
			.setMessage(getResources().getString(R.string.mapdragger_updateLocationMes))
			.setPositiveButton(getResources().getString(R.string.alertDialogOkay), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					new SaveItemDetails().execute();
				}
			})
			.setNegativeButton(getResources().getString(R.string.alertDialogCancel), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {}
			})
			.show();
			break;
		case R.id.drag_marker_confirm:
			Intent intent = getIntent();
			intent.putExtra("sLatitude", newer_latitude);
			intent.putExtra("sLongitude", newer_longitude);
			setResult(RESULT_OK,intent);
			finish();
			this.overridePendingTransition(R.anim.finish_in, R.anim.finish_out);
		case R.id.drag_marker_cancel:
			setResult(RESULT_CANCELED);
			finish();
			this.overridePendingTransition(R.anim.finish_in, R.anim.finish_out);
			break;
		case android.R.id.home:
			setResult(RESULT_CANCELED);
			finish();
			this.overridePendingTransition(R.anim.finish_in, R.anim.finish_out);
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.finish_in, R.anim.finish_out);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("DialogBoolean", userHasPressedOk);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		if (savedInstanceState != null)
			userHasPressedOk = savedInstanceState.getBoolean("DialogBoolean");
	}

	//依點選狀態隱藏選單元件
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem EditSave = menu.findItem(R.id.drag_marker_save);
		MenuItem AddComfirm = menu.findItem(R.id.drag_marker_confirm);
		if (tag.equals("EditItem"))
			AddComfirm.setVisible(false);
		else
			EditSave.setVisible(false);
		return true;
	}
}
