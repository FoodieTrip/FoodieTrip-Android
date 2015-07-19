package com.foodietrip.android;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.support.v4.view.ViewPager;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.ShareActionProvider;

import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.foodietrip.android.image_library.ImageLoader;
import com.foodietrip.android.image_library.ViewPagerGallery;
import com.foodietrip.android.library.*;
import com.viewpagerindicator.CirclePageIndicator;

@SuppressLint("SimpleDateFormat")
public class DetailItem extends SherlockActivity {
    private Uri mImageCaptureUri;
    private ImageLoader imageLoader;
	SharedPreferences editItemPre;
    EditText photoMessage;
    TextView txt_canDeli,txt_canGo,txt_opened,txt_closed,txt_times,txt_memo,txt_memoTitle,txt_tags,txt_actionTitle,txt_warningTxt;
    Button btn_call,btn_web,btn_mail,btn_Mes;
    ImageButton btn_share,btn_report;
    DisplayMetrics dm;
    Bitmap bitmap = null, bm = null;
	UserFunctions userFunctions;
	Boolean loginCheck;
	ActionBar actionbar;
    private TextView detail_Address;
    Double user_latitude = 0.0, user_longitude = 0.0, distance_double;
    String sid,account,storeCall,storeWeb,storeMail,sName,formatedTime,full_address,photoUrl,store_status,status1,status2,status3;
    //Process Dialog
  	private ProgressDialog pDialog;
  	RatingBar detail_RatingBar;
  	int account_uID = 0, bigImageIndex, photoOrientation;
  	//Load Photo Use
  	ArrayList<HashMap<String, String>> store_photos,bigCatagory,smallCatagory;
  	JSONArray all_Photo;
  	ProgressBar photoProgress,detail_MainProgressbar;
  	LinearLayout photoLayout,restDayLayout,mainLayout;
  	View horizontalScrollView;
  	TwitterHelper twitterHelper;
  	FacebookHelper facebookHelper;
  	File photoFile;
  	MenuItem shareButton, modifyItem;
  	private ShareActionProvider mShareActionProvider;
  	private Dialog lightBoxDialog;
  	boolean IsWebUrlEmpty = false, canDeli = false, canGo = false,twitterShare = false,facebookShare = false, hasModified = false;
    private static final int PICK_FROM_CAMERA = 1;
    private static final int PICK_FROM_FILE = 2;
    private static final int MODIFY_REFRESH = 10;
  	private static final String TAG_ITEM = "Store";    //表格名稱
  	private static final String TAG_PID = "sID";
    private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");
    private static final String PENDING_PUBLISH_KEY = "pendingPublishReauthorization";
    private boolean pendingPublishReauthorization = false;
  	@SuppressLint("NewApi")
	@SuppressWarnings({ "unchecked", "deprecation" })
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.detail_item);
		//從介面取得元件
		mainLayout = (LinearLayout) findViewById(R.id.detail_mainLayout);
		detail_MainProgressbar = (ProgressBar) findViewById(R.id.detail_mainProgressbar);
		detail_Address = (TextView)findViewById(R.id.textView_detail_address);
		txt_times = (TextView) findViewById(R.id.txt_detailTimes);
		btn_call = (Button) findViewById(R.id.btn_detail_dial);
		btn_mail = (Button) findViewById(R.id.btn_detail_mail);
		btn_web = (Button) findViewById(R.id.btn_detail_web);
		btn_Mes = (Button) findViewById(R.id.button_detail_Mes);
		txt_canDeli = (TextView) findViewById(R.id.txt_toDeli);
		txt_canGo = (TextView) findViewById(R.id.txt_togo);
		txt_opened = (TextView) findViewById(R.id.txt_storeOpened);
		txt_closed = (TextView) findViewById(R.id.txt_storeClosed);
		txt_tags = (TextView) findViewById(R.id.Detail_tags);
		txt_warningTxt = (TextView) findViewById(R.id.txt_warningTxt);
		txt_memo = (TextView) findViewById(R.id.textView_detail_memo);
		txt_memoTitle = (TextView) findViewById(R.id.textView_detail_memoTitle);
		detail_RatingBar = (RatingBar) findViewById(R.id.Detail_rating);
		btn_call.setOnClickListener(functionBtnListener);
		btn_mail.setOnClickListener(functionBtnListener);
		btn_web.setOnClickListener(functionBtnListener);
		btn_Mes.setOnClickListener(functionBtnListener);
		photoProgress = (ProgressBar)findViewById(R.id.Detail_progressBar);
		photoLayout = (LinearLayout)findViewById(R.id.Detail_gallery);
		restDayLayout = (LinearLayout) findViewById(R.id.detail_restDays);
		horizontalScrollView = (View)findViewById(R.id.Detail_horizontalScrollView);
		dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		store_photos = new ArrayList<HashMap<String,String>>();
		//Actionbar
		actionbar = getSupportActionBar();
		actionbar.setDisplayHomeAsUpEnabled(true);
		actionbar.setDisplayShowCustomEnabled(true);
		View customNav = LayoutInflater.from(this).inflate(R.layout.detail_item_customview, null);
		com.actionbarsherlock.app.ActionBar.LayoutParams actiobarParams = new com.actionbarsherlock.app.ActionBar.LayoutParams(Gravity.RIGHT);
		getSupportActionBar().setCustomView(customNav, actiobarParams);
		btn_share = (ImageButton) findViewById(R.id.btn_share);
		btn_report = (ImageButton) findViewById(R.id.btn_report);
		btn_share.setOnClickListener(functionBtnListener);
		btn_report.setOnClickListener(functionBtnListener);
		btn_report.setEnabled(false);
		status1 = getResources().getString(R.string.store_status_code1);    //move_away
		status2 = getResources().getString(R.string.store_status_code2);    //rest
		status3 = getResources().getString(R.string.store_status_code3);    //Unavailable
		//從Intent取得Item資訊
		Intent i = this.getIntent();
		Bundle bundle = i.getExtras();
		sid = bundle.getString(TAG_PID);
		distance_double = bundle.getDouble("distance");
		bigCatagory = (ArrayList<HashMap<String,String>>) bundle.getSerializable("BigCatagory");
		smallCatagory = (ArrayList<HashMap<String,String>>) bundle.getSerializable("SmallCatagory");
		if (distance_double >= 500.0) {
			btn_share.setEnabled(false);
			if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN)
				btn_share.setImageAlpha(75);
			else
				btn_share.setAlpha(75);
			btn_share.setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
		}
		final ImageView add_Photos = (ImageView)findViewById(R.id.add_Photo);
		if (distance_double <= 500.0) {
			add_Photos.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Animation effect = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.image_effect);
					add_Photos.startAnimation(effect);
					uploadImageAlertDialog();
				}
			});
		}
		else {
			add_Photos.setVisibility(View.INVISIBLE);
		}
		//從Intent取得商家sid
		//sid = i.getStringExtra(TAG_PID);
		twitterHelper = new TwitterHelper(getApplicationContext());
		facebookHelper = new FacebookHelper(getApplicationContext());
		userFunctions = new UserFunctions(getApplicationContext());
		loginCheck = userFunctions.isUserLoggedIn(getApplicationContext());
		//取出使用者的uID
		if (loginCheck == true) {
			account_uID = userFunctions.getUserUid(getApplicationContext());
			btn_share.setVisibility(View.VISIBLE);
			//Toast.makeText(getApplicationContext(), ""+account_uID, Toast.LENGTH_SHORT).show();
		}
		else {
			account_uID = 0;
		}
		imageLoader = new ImageLoader(this);
		//從背景Method 取得Item的詳細資料
		new GetItemDetails().execute();
  	}
  	/*功能按鈕監聽*/
  	private Button.OnClickListener functionBtnListener = new Button.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btn_detail_dial:
				if (!storeCall.startsWith("0")) storeCall = "0" +storeCall;
				Uri uri = Uri.parse("tel:"+storeCall);
				Intent goCall = new Intent(Intent.ACTION_DIAL, uri);
				startActivity(goCall);
				break;
			case R.id.btn_detail_mail:
				Intent sendMail = new Intent(Intent.ACTION_SEND);
				sendMail.setType("message/rfc822");
				sendMail.putExtra(Intent.EXTRA_EMAIL, new String[]{storeMail});
				try {
					startActivity(Intent.createChooser(sendMail, getResources().getString(R.string.detailItem_createChooser)));
				}
				catch(android.content.ActivityNotFoundException e) {
					Toast.makeText(getApplicationContext(), getResources().getString(R.string.detailItem_doNotFindAppforSend), Toast.LENGTH_SHORT).show();
				}
				break;
			case R.id.btn_detail_web:
				if(!IsWebUrlEmpty) {
					String storeWebTemp = storeWeb;
					if (!storeWeb.startsWith("http://")) {
						storeWebTemp = "http://" +storeWeb;
					}
					Uri webUri = Uri.parse(storeWebTemp);
					Intent goWeb = new Intent(Intent.ACTION_VIEW, webUri);
					startActivity(goWeb);
				}
				else {
					try {
						String keywordPart = getResources().getString(R.string.detailItem_SearchKeyword);
						String query = URLEncoder.encode(sName+" "+keywordPart, "utf-8");
						Uri urlString = Uri.parse("http://www.google.com/search?tbm=blg&q=" + query);
						Intent goSearchWeb = new Intent(Intent.ACTION_VIEW, urlString);
						startActivity(goSearchWeb);
					}
					catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}
				break;
			case R.id.btn_share:
				showShareDialog();
				break;
			case R.id.button_detail_Mes:
				Intent intent = new Intent(DetailItem.this,Message.class);
				Bundle bundle = new Bundle();
				bundle.putString("sID", sid);
				bundle.putDouble("distence", distance_double);
				intent.putExtras(bundle);
				startActivity(intent);
				overridePendingTransition(R.anim.detail_in, R.anim.storelist_leave);
				break;
			case R.id.btn_report:
				Intent goReport = new Intent(DetailItem.this, Report.class);
				Bundle reportBundle = new Bundle();
				reportBundle.putString("sID", sid);
				String uID = String.valueOf(account_uID);
				reportBundle.putString("uID", uID);
				reportBundle.putString("sName", sName);
				reportBundle.putDouble("sLatitude", user_latitude);
				reportBundle.putDouble("sLongitude", user_longitude);
				goReport.putExtras(reportBundle);
				startActivity(goReport);
				overridePendingTransition(R.anim.detail_in, R.anim.storelist_leave);
				break;
			}
		}
	};

	//ShareDialog
	private void showShareDialog() {
		final boolean isTwitterLogined = twitterHelper.isTwitterLoggedInAlready();
		final boolean isFacebookLogined = facebookHelper.isFacebookLogined();
		final Dialog shareDialog = new Dialog(DetailItem.this);
		shareDialog.setTitle(getResources().getString(R.string.shareDialog_title));
		shareDialog.setContentView(R.layout.message_add);
		RatingBar ratingBar = (RatingBar) shareDialog.findViewById(R.id.addMessage_rating);
		ratingBar.setVisibility(View.GONE);
		final CheckBox cbTwitter = (CheckBox) shareDialog.findViewById(R.id.AddMessage_twitter);
		if (isTwitterLogined) cbTwitter.setEnabled(true);
		final CheckBox cbFacebook = (CheckBox) shareDialog.findViewById(R.id.AddMessage_facebook);
		if (isFacebookLogined) cbFacebook.setEnabled(true);
		if (distance_double > 500) {
		    cbTwitter.setEnabled(false);
		    cbFacebook.setEnabled(false);
		}
		final EditText shareMessage = (EditText) shareDialog.findViewById(R.id.addMessage_edittext);
		Button shareButtonOkay = (Button) shareDialog.findViewById(R.id.AddMessage_okay);
		Button shareButtonCancel = (Button) shareDialog.findViewById(R.id.AddMessage_cancel);
		shareButtonCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
                shareDialog.cancel();
			}
		});
		shareButtonOkay.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!isTwitterLogined && !isFacebookLogined) {
					Toast.makeText(getApplicationContext(), getResources().getString(R.string.editItemP1_countryError), Toast.LENGTH_SHORT).show();
				}
				else {
					String shareMessageString = shareMessage.getText().toString();
					if (isFacebookLogined && cbFacebook.isChecked())
					    ShareToFacebook(shareMessageString);
					if (isTwitterLogined && cbTwitter.isChecked())
					    twitterHelper.makeTweet(DetailItem.this ,shareMessageString, sName, user_latitude, user_longitude);
				}
				shareDialog.dismiss();
			}
		});
		shareDialog.show();
	}

  	/*ActionBar*/
  	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
		case android.R.id.home:
			editItemPre = getSharedPreferences("editItem_tmp", MODE_PRIVATE);
			editItemPre.edit().clear().commit();    //清空
			if (hasModified)
				setResult(RESULT_OK);
			else
				setResult(RESULT_CANCELED);
			finish();
			this.overridePendingTransition(R.anim.finish_in, R.anim.finish_out);
			break;
		case R.id.modify_object:
			//從選擇的ListView中取得資訊
			//Start New Intent
			Intent go_modify_item = new Intent(DetailItem.this,EditItem_tab.class);
			//Sending sid to next activity
			Bundle go_to_modify = new Bundle();
			go_to_modify.putString(TAG_PID, sid);
			go_to_modify.putSerializable("BigCatagory", bigCatagory);
			go_to_modify.putSerializable("SmallCatagory", smallCatagory);
			go_modify_item.putExtras(go_to_modify);
			startActivityForResult(go_modify_item, MODIFY_REFRESH);
			overridePendingTransition(R.anim.detail_in, R.anim.storelist_leave);
			//finish();
			break;
		case R.id.detail_map_interface:
			if (user_latitude == 0.0 && user_longitude == 0.0) {
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.detailItem_gpsFixingNow), Toast.LENGTH_SHORT).show();
			}
			else {
    			Intent go_map_interface = new Intent(DetailItem.this, MapInterface.class);
    			Bundle myPlace = new Bundle();
    			myPlace.putDouble("latitude", user_latitude);
    			myPlace.putDouble("longitude", user_longitude);
    			myPlace.putString("tag", "single");
    			myPlace.putString("sid", sid);
    			go_map_interface.putExtras(myPlace);
    			startActivity(go_map_interface);
    			overridePendingTransition(R.anim.detail_in, R.anim.storelist_leave);
			}
			break;
		default:
			super.onOptionsItemSelected(item);
		}
  		return true;
  	}
	//檢查使用者有沒有修改資料
	protected void onActivityResult(int requestCode,int resultCode,Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == MODIFY_REFRESH) {
			modifyItem.setEnabled(false);
			if (resultCode == RESULT_OK) {
				restDayLayout.removeAllViews();
				if (photoLayout.getChildCount() > 1)
				    photoLayout.removeViews(1, all_Photo.length());
				new GetItemDetails().execute();    //重新整理
				detail_MainProgressbar.setVisibility(View.VISIBLE);
				hasModified = true;
				return;
			}
			else if (resultCode == RESULT_CANCELED) {
				restDayLayout.removeAllViews();
				if (photoLayout.getChildCount() > 1)
					photoLayout.removeViews(1, all_Photo.length());
				new GetItemDetails().execute();    //重新整理
				detail_MainProgressbar.setVisibility(View.VISIBLE);
				return;
			}
		}
		//圖片功能
		if (requestCode == PICK_FROM_CAMERA || requestCode == PICK_FROM_FILE) {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inPurgeable = true;    //如果記憶體不足可以先行回收
			options.inInputShareable = true;
			options.inSampleSize = 4;    //縮放等級
			options.inPreferredConfig = Bitmap.Config.RGB_565;
			Rect outPadding = null;
			if (resultCode != RESULT_OK) return;
			String path = "";
			if (requestCode == PICK_FROM_FILE) {
				InputStream inputStream;
				mImageCaptureUri = data.getData();
				path = getRealPathFromURI(mImageCaptureUri);   //from Gallery
				inputStream = getInputStream(path);
				if (path == null) {
					path = mImageCaptureUri.getPath();    //from File Manager
					inputStream = getInputStream(path);
				}
				if (path != null)
					bitmap = BitmapFactory.decodeStream(inputStream, outPadding, options);
			}
			else {
				if (mImageCaptureUri != null) {
					InputStream inputStream;
				    path = mImageCaptureUri.getPath();
				    inputStream = getInputStream(path);
				    bitmap = BitmapFactory.decodeStream(inputStream, outPadding, options);
				}
				else {
					//為了防止使用者轉向而製作
					InputStream inputStream = null;
					Cursor cursor = getContentResolver().query(Media.EXTERNAL_CONTENT_URI, new String[]{Media.DATA,Media.DATE_ADDED,MediaStore.Images.ImageColumns.ORIENTATION}, Media.DATE_ADDED, null, "Data Added ASC");
					if (cursor != null && cursor.moveToFirst()) {
						do {
							mImageCaptureUri = Uri.parse(cursor.getString(cursor.getColumnIndex(Media.DATA)));
							path = mImageCaptureUri.getPath();
							inputStream = getInputStream(path);
						} while (cursor.moveToNext());
					    cursor.close();
					}
					bitmap = BitmapFactory.decodeStream(inputStream, outPadding, options);
				}
			}
			if (IsImageSizeAvaliable(path)) {
				if (path != null) photoFile = new File(path);
				int orientation = DetectPhotoOrientation(path);
				if (bitmap != null) ImagePreview(bitmap,orientation);
			}
			else
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.imageSizeIsSmall), Toast.LENGTH_SHORT).show();
		}
		else {
			Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
		}
	}

	public boolean IsImageSizeAvaliable(String path) {
		boolean isImageAvaliable = false;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		InputStream inputStream = getInputStream(path);
		BitmapFactory.decodeStream(inputStream, null, options);
		int width = options.outWidth;
		int height = options.outHeight;
		int orientation = DetectPhotoOrientation(path);
		switch (orientation) {
		case 0:
			if (width >= 1280 || height >= 960)
				isImageAvaliable = true;
			break;
		case 90:
			if (width >= 960 || height >= 1280)
				isImageAvaliable = true;
			break;
		case 180:
			if (width >= 1280 || height >= 960)
				isImageAvaliable = true;
			break;
		case 270:
			if (width >= 960 || height >= 1280)
			    isImageAvaliable = true;
			break;
		}
		return isImageAvaliable;
	}

	public InputStream getInputStream(String path) {
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(path);
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return inputStream;
	}

  	/*在背景藉由 AsyncTask方法，讀取Item詳細資訊*/
  	class GetItemDetails extends AsyncTask<String,String,String>{
  	    int httpResponseCode;
  	    boolean success;
  		@Override
  		protected void onPreExecute(){
  			super.onPreExecute();
  			Calendar calendar = Calendar.getInstance();
  			SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
  			formatedTime = timeFormat.format(calendar.getTime());
  			mainLayout.setVisibility(View.GONE);
  		}
  		//在背景取得詳細資料
		@Override
		protected String doInBackground(String... args) {
			//在背景執行緒更新UI
			try{
				//建立 Parameter,用來存放資料得資料結構
				List<NameValuePair> params1 = new ArrayList<NameValuePair>();
				//params1.add(new BasicNameValuePair("action", "get_store_details"));    //set for action
				params1.add(new BasicNameValuePair("id",sid));    //Detail必要
				params1.add(new BasicNameValuePair("uID", Integer.toString(account_uID)));
				//藉由發送HTTP Request，取得Item 詳細資訊
				// Note that product details url will use GET request
				//String action = "get_store_details";    //救急措施
				JSONParser jsonParser = new JSONParser(getApplicationContext());
				JSONObject json = jsonParser.makeHttpRequest("/details" ,"GET", params1);
				if (json == null) {
					Log.e("JSON Object is null", "when doing background task DetailItem Get Detail Item");
					httpResponseCode = jsonParser.getHttpResponseCode();
					success = false;
				    return null;
				}
				httpResponseCode = jsonParser.getHttpResponseCode();
				success = true;
				//Log.d("Store sID", sid.toString());
				//Log.e("Single Item Detial", json.toString());
				//成功的獲取了資訊
				JSONArray itemObj = json.getJSONArray(TAG_ITEM);    //JSON Array
				//從JSON Array取得第一個Item 物件
				final JSONObject first_item = itemObj.getJSONObject(0);
				final JSONArray smallTagJson = first_item.getJSONArray("SubCategories");
				//Log.e("smallTagLength = ", String.valueOf(smallTagJson.length()));
				String scID = "";
				if (smallTagJson.length() ==1 && smallTagJson.getString(0).equals("null")) {
					scID = "";
				}
				else {
					for (int i=0 ; i<smallTagJson.length() ; i++) {
						String temp = smallTagJson.getString(i);
						if (temp != null)
							scID += temp;
						else
							scID += "";
						if (i != smallTagJson.length()-1) scID += ",";
					}
				}
				final String scID_tryUse = scID;
				//從Pid找到的Item
				runOnUiThread(new Runnable(){
					public void run() {
						//將資料顯示於TextView之中
						try{
							sName = first_item.getString("sName");
							store_status = first_item.getString("status");
							actionbar.setTitle(sName);
							int GuestViews = Integer.parseInt(first_item.getString("GuestViews"));
							int KeeperViews = Integer.parseInt(first_item.getString("KeeperViews"));
							int UserViews = Integer.parseInt(first_item.getString("UserViews"));
							int ViewTimes = GuestViews + KeeperViews + UserViews;
							txt_times.setText(""+ViewTimes);
						}
						catch(JSONException e){
							e.printStackTrace();
						}
						try{
							full_address = first_item.getString("sCountry")
									+ first_item.getString("sTownship")
									+ first_item.getString("sLocation");
							detail_Address.setText(full_address);
							String restDayRaw = first_item.getString("RestDay");
							if (restDayRaw.trim().length() > 0) {
								String[] restDays = restDayRaw.split(" ");
								for (int i=0 ; i<restDays.length ; i++) {
									TextView textView = new TextView(getApplicationContext());
									android.widget.LinearLayout.LayoutParams lParams = new android.widget.LinearLayout.LayoutParams
											(android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
											, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
									lParams.setMargins(getPixels(8), 0, 0, 0);
									textView.setLayoutParams(lParams);
									textView.setText(restDays[i]);
									textView.setTextColor(Color.BLACK);
									textView.setBackgroundResource(R.drawable.detail_corner_close);
									restDayLayout.addView(textView);
								}
							}
							else {
								TextView textView = new TextView(getApplicationContext());
								LayoutParams lParams = new LayoutParams
										(android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
										, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
								textView.setLayoutParams(lParams);
								textView.setTextColor(Color.BLACK);
								textView.setText(getResources().getString(R.string.detailItem_noRest));
								textView.setBackgroundResource(R.drawable.detail_corner);
								restDayLayout.addView(textView);
							}
						}
						catch(JSONException e){
							e.printStackTrace();
						}
						try{
							user_latitude = Double.parseDouble(first_item.getString("sLatitude"));
							user_longitude = Double.parseDouble(first_item.getString("sLongitude"));
						    String memo = first_item.getString("sMemo");
						    if (memo.trim().length() > 0) {
						    	txt_memo.setText(memo);
						    	txt_memoTitle.setVisibility(View.VISIBLE);
						    	txt_memo.setVisibility(View.VISIBLE);
						    }
						}
						catch(JSONException e){
							e.printStackTrace();
						}
						try{
							String mcID = first_item.getString("MainCategory");
							if (mcID.equals("null"))
								mcID = getResources().getString(R.string.tagDoesntSet);
							String tagString;
							if (scID_tryUse.equals(""))
								tagString = mcID;
							else
							    tagString = "[" +mcID +"] " +scID_tryUse;
							txt_tags.setText(tagString);
							String ratingAvg = first_item.getString("Store_avg_rating");
							float ratingAvgFloat = Float.parseFloat(ratingAvg);
							detail_RatingBar.setRating(ratingAvgFloat);
						}
						catch(JSONException e){
							e.printStackTrace();
						}
						try{
							storeCall = first_item.getString("sPhone");
							storeWeb = first_item.getString("sURL");
							storeMail = first_item.getString("sEmail");
							int canDeliInt = Integer.parseInt(first_item.getString("sCanDelivery"));
							int canGoInt = Integer.parseInt(first_item.getString("sCanToGo"));
							if (storeCall.trim().length() > 0) btn_call.setVisibility(View.VISIBLE);
							if (storeMail.trim().length() > 0) btn_mail.setVisibility(View.VISIBLE);
							if (storeWeb.trim().length() > 0) {
								IsWebUrlEmpty = false;
								btn_web.setVisibility(View.VISIBLE);
							}
							else {
								btn_web.setText(getResources().getString(R.string.detailItem_webSearch));
								btn_web.setVisibility(View.VISIBLE);
								IsWebUrlEmpty = true;
							}
							if (canDeliInt == 1) txt_canDeli.setVisibility(View.VISIBLE);
							if (canGoInt == 1) txt_canGo.setVisibility(View.VISIBLE);
						}
						catch(JSONException e){
							e.printStackTrace();
						}
						try{
                               String startTime = first_item.getString("startTime");
                               String closeTime = first_item.getString("closeTime");
                               if (!startTime.equals("")|| !closeTime.equals("")) {
                            	   String timeRange = startTime +" ~ " +closeTime;
                                   actionbar.setSubtitle(timeRange);
                               }
                               String is24HoursAvaliable = first_item.getString("is24Hours");
                               if (is24HoursAvaliable.equals("1")) {
                                   txt_opened.setVisibility(View.VISIBLE);
                                   actionbar.setSubtitle("24Hour");
                               }
                               else {
                               if (startTime.trim().length() > 0 && closeTime.trim().length() > 0) {
                                   	String closeTimeCompareString = closeTime;
                                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                                    Date startDate = timeFormat.parse(startTime);
                                    if (closeTime.equals("0:00")||closeTime.equals("00:00"))
                                      	closeTimeCompareString = "24:00";
                                    Date closeDate = timeFormat.parse(closeTimeCompareString);
                                    Date nowDate = timeFormat.parse(formatedTime);
                                    int NowToStart = nowDate.compareTo(startDate);
                                    int NowToClose = nowDate.compareTo(closeDate);
                                    if (NowToStart > 0 && NowToClose < 0)
                                      	txt_opened.setVisibility(View.VISIBLE);
                                    else
                                      	txt_closed.setVisibility(View.VISIBLE);
                                }
                            }
						}
						catch(JSONException e){
						    e.printStackTrace();
						}
						catch (ParseException e) {
						    e.printStackTrace();
						}
					}
					});
				    putDataIn(first_item, scID);
			}
			catch(JSONException e){
				e.printStackTrace();
			}
			return null;
		}
		//完成背景作業後，防止對話框跳出來
		protected void onPostExecute(String file_url) {
			if (success) {
				detail_MainProgressbar.setVisibility(View.GONE);
				Animation fadeEffect = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fadein);
			    mainLayout.setVisibility(View.VISIBLE);
			    mainLayout.startAnimation(fadeEffect);
			    btn_report.setEnabled(true);
				new LoadAllPhoto().execute();
				runOnUiThread(new Runnable() {
					public void run() {
						if (!store_status.equals("active")) {
							txt_opened.setVisibility(View.GONE);
							txt_closed.setVisibility(View.GONE);
							if (store_status.equals("move_away")) txt_warningTxt.setText(status1);
							else if (store_status.equals("temp_rest")) txt_warningTxt.setText(status2);
							else if (store_status.equals("unavailable")) txt_warningTxt.setText(status3);
							txt_warningTxt.setVisibility(View.VISIBLE);
						}
					    Intent intent = getDefaultShareIntent();
			            if (intent != null)
				        mShareActionProvider.setShareIntent(intent);
			            shareButton.setVisible(true);
			            userFunctions = new UserFunctions(getApplicationContext());
			    		loginCheck = userFunctions.isUserLoggedIn(getApplicationContext());
			            if (distance_double >= 500.0) {
			            	if (loginCheck == true) {
			            		modifyItem.getIcon().setAlpha(75);
				    			modifyItem.getIcon().setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
			            	}
			            }
			            else {
			            	if (loginCheck == true) {
			            		modifyItem.setVisible(true);
				            	modifyItem.setEnabled(true);
			            	}
			            }
					}
				});
			}
			else {
				if (httpResponseCode == 900) {
					//連線逾時
					Toast.makeText(getApplicationContext(), getResources().getString(R.string.ConnectionTimeOut), Toast.LENGTH_SHORT).show();
					return;
				}
				if (httpResponseCode == HttpStatus.SC_NOT_FOUND) {
					Toast.makeText(getApplicationContext(), getResources().getString(R.string.getDetail_httpResponse_404), Toast.LENGTH_SHORT).show();
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

	/*讀取所有的照片*/
	class LoadAllPhoto extends AsyncTask<String, String, Boolean> {
		@Override
		protected Boolean doInBackground(String... params) {
			boolean thereHavePhoto = false;
			//Bulid Parameters
			List<NameValuePair> params_photo = new ArrayList<NameValuePair>();
			//params_photo.add(new BasicNameValuePair("action", "get_store_images"));
			params_photo.add(new BasicNameValuePair("id", sid));
			//Get JSON String from URL
			JSONParser jsonParser = new JSONParser(getApplicationContext());
			JSONObject json = jsonParser.makeHttpRequest("/get_image" ,"GET", params_photo);
			if (json == null) {
				Log.e("JSON Object is null", "when doing background task DetailItem");
			    return false;    //沒有照片
			}
			//Log.e("Photo JSON test: ", json.toString());
			try {
				all_Photo = json.getJSONArray("StoreImage");
				thereHavePhoto = true;
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			return thereHavePhoto;
		}

		@Override
		protected void onPostExecute(Boolean thereHavePhoto) {
			if (thereHavePhoto) {
				runOnUiThread(new Runnable() {
					public void run() {
						storePhotoHelper();
					}
				});
			}
		}
	}

	class insertPhotoTask extends AsyncTask<String, Void, Bitmap> {
        ImageView imageView;
        ProgressBar progressBar;
        Context context;
        public insertPhotoTask(Context _context, ImageView _imageView, ProgressBar _progressBar) {
        	imageView = _imageView;
        	progressBar = _progressBar;
        	context = _context;
        }
		@Override
		protected void onPreExecute() {
			imageView.setVisibility(View.GONE);
		}
		@Override
		protected Bitmap doInBackground(String... urls) {
			Bitmap bitmap_small = imageLoader.getBitmap(urls[0]);
			return bitmap_small;
		}
		@Override
		protected void onPostExecute(Bitmap resultBitmap) {
            if (imageView != null) {
            	imageView.setImageBitmap(resultBitmap);
            	Animation fade_animation = AnimationUtils.loadAnimation(context, R.anim.fadein);
            	imageView.setVisibility(View.VISIBLE);
            	imageView.setAnimation(fade_animation);
            	progressBar.setVisibility(View.GONE);
            }
		}
	}

	/*照片處理者*/
	public View insertPhoto(String url,int index) {
		final int number = index;
		LinearLayout layout = new LinearLayout(getApplicationContext());
		layout.setLayoutParams(new LayoutParams(getPixels(250),getPixels(250)));
		layout.setGravity(Gravity.CENTER);
		final ImageView imageView = new ImageView(getApplicationContext());
		imageView.setLayoutParams(new LayoutParams(getPixels(220),getPixels(220)));
		imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
		imageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				bigImageIndex = number;
				showLightBoxDialog(number);
				Animation effect = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.image_effect);
				imageView.startAnimation(effect);
				//Toast.makeText(getApplicationContext(), "這是照片:" +number, Toast.LENGTH_SHORT).show();
			}
		});
		imageView.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				reportInvalidPhoto(number);
				Animation effect = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.image_effect);
				imageView.startAnimation(effect);
				return true;
			}
		});
		ProgressBar progressBar = new ProgressBar(getApplicationContext(), null, android.R.attr.progressBarStyle);
		insertPhotoTask photoTask = new insertPhotoTask(getApplicationContext(), imageView, progressBar);
		photoTask.execute(url);
		layout.addView(progressBar);
		layout.addView(imageView);
		return layout;
	}

	public void storePhotoHelper() {
		try {
			for (int i=0;i < all_Photo.length();i++) {
				JSONObject jObject = all_Photo.getJSONObject(i);
				store_photos.add(ListAdapter(jObject));
				photoLayout.addView(insertPhoto(jObject.getString("thumb_path"), i));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*Adapter Worker*/
	private HashMap<String, String> ListAdapter(JSONObject jsonObject) {
		//儲存每個項目
		String photoId = "", storeId = "", upload_userId = "", message = "",
				bigSizeUrl = "", uploadTime = "";
		try {
			photoId = jsonObject.getString("iID");
			storeId = jsonObject.getString("sID");
			upload_userId = jsonObject.getString("uID");
			message = jsonObject.getString("message");
			bigSizeUrl = jsonObject.getString("path");
			//smallSizeUrl = jsonObject.getString("thumb_path");
			uploadTime = jsonObject.getString("uploadTime");
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
		HashMap<String, String> maps = new HashMap<String, String>();
		maps.put("photoId", photoId);
		maps.put("storeId", storeId);
		maps.put("upload_userId", upload_userId);
		maps.put("message", message);
		maps.put("bigSizeUrl", bigSizeUrl);
		//maps.put("smallSizeUrl", smallSizeUrl);
		maps.put("uploadTime", uploadTime);
		return maps;
	}

	//依登入狀態隱藏選單元件
	public boolean onPrepareOptionsMenu(Menu menu) {
		modifyItem = menu.findItem(R.id.modify_object);
		userFunctions = new UserFunctions(getApplicationContext());
		loginCheck = userFunctions.isUserLoggedIn(getApplicationContext());
		if (loginCheck == true) {
			//modifyItem.setVisible(true);
			modifyItem.setEnabled(false);
		}
		else {
			modifyItem.setVisible(false);
		}
		return true;
	}
	//Menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.detail_menu, menu);
		shareButton = menu.findItem(R.id.detail_share);
		mShareActionProvider = (ShareActionProvider) shareButton.getActionProvider();
		return true;
	}
	//Return a share intent
	private Intent getDefaultShareIntent() {
		String title = getResources().getString(R.string.shareIntentTitle);
		String head = getResources().getString(R.string.shareIntentText);
		String middle = getResources().getString(R.string.shareIntentMiddle);
		String message = head +sName +middle +full_address+"\n via FoodieTrip";
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_SUBJECT, title);
		intent.putExtra(Intent.EXTRA_TEXT, message);
		return intent;
	}

	/*To fix the IllegalArgumentException*/
	@Override
	protected void onSaveInstanceState(Bundle outState){
		super.onSaveInstanceState(outState);
		outState.putBoolean(PENDING_PUBLISH_KEY, pendingPublishReauthorization);    //FacebookUse
		try{
			pDialog.dismiss();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		if (bigCatagory != null && smallCatagory != null) {
			outState.putSerializable("BigCatagory", bigCatagory);
			outState.putSerializable("SmallCatagory", smallCatagory);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		if (savedInstanceState != null) {
			bigCatagory = (ArrayList<HashMap<String, String>>) savedInstanceState.getSerializable("bigCatagory");
			smallCatagory = (ArrayList<HashMap<String, String>>) savedInstanceState.getSerializable("SmallCatagory");
		}
	}

	public void uploadImageAlertDialog() {
		final String[] items = new String[] {getResources().getString(R.string.detailItem_takeaPicture),getResources().getString(R.string.detailItem_choosePhotos)};
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item,items);
		new AlertDialog.Builder(DetailItem.this)
		.setTitle(getResources().getString(R.string.detailItem_choosePhotos))
		.setAdapter(adapter, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int item) {
				if (item == 0) {
					Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					//檔案名稱tmp_avatar_時間.jpg
					String imagePath = Environment.getExternalStorageDirectory() +"/foodietrip/";
					File imageFolder = new File(imagePath);
					imageFolder.mkdirs();
					File file = new File(imagePath,"tmp_foodietrip_"+String.valueOf(System.currentTimeMillis())+".jpg");
					mImageCaptureUri = Uri.fromFile(file);
					try {
						intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,mImageCaptureUri);
						intent.putExtra("return-data", true);
						startActivityForResult(intent, PICK_FROM_CAMERA);
					}
					catch (Exception e) {
						e.printStackTrace();
					}
					dialog.cancel();
				}
				else {
					Intent intent = new Intent();
					intent.setType("image/*");
					intent.setAction(Intent.ACTION_GET_CONTENT);
					int api_version = Build.VERSION.SDK_INT;    //API版本
					String android_version = Build.VERSION.RELEASE;    //Android版本
					//Log.e("android_version Check:", "API:" +api_version +" ,release:" +android_version);
					if(api_version > Build.VERSION_CODES.GINGERBREAD_MR1 && !android_version.matches("(1|2)\\..+"))
						intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);    //限定本機物件
					startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.detailItem_alertPlzChoosePitureApp)), PICK_FROM_FILE);
					dialog.cancel();
				}
			}
		})
		.show();
	}
	public String getRealPathFromURI(Uri contentUri) {
		String[] proj = {MediaStore.Images.Media.DATA};
		Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
		if (cursor == null) return null;
		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}
	//圖片預覽
	private void ImagePreview(Bitmap bitmap,int orientation) {
		final Dialog imgPreview = new Dialog(DetailItem.this);
		imgPreview.setTitle(getResources().getString(R.string.detailItem_imagePreview));
		imgPreview.setContentView(R.layout.photo_preview);
		photoMessage = (EditText)imgPreview.findViewById(R.id.photoMessage);
		ImageView Bitmap_preview = (ImageView) imgPreview.findViewById(R.id.imagePreview);
		Button btn_upload = (Button) imgPreview.findViewById(R.id.imgUpload_ok);
		Button btn_cancel = (Button) imgPreview.findViewById(R.id.imgUpload_cancel);
		CheckBox shareToTwitter = (CheckBox) imgPreview.findViewById(R.id.cbox_ShareToTwitter);
		if (twitterHelper.isTwitterLoggedInAlready())
			shareToTwitter.setEnabled(true);
		shareToTwitter.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked)
					twitterShare = true;
				else
					twitterShare = false;
			}
		});
		CheckBox shareToFacebook = (CheckBox) imgPreview.findViewById(R.id.cbox_ShareToFacebook);
		if (facebookHelper.isFacebookLogined())
			shareToFacebook.setEnabled(true);
		shareToFacebook.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked)
					facebookShare = true;
				else
					facebookShare = false;
			}
		});
		btn_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				imgPreview.cancel();
			}
		});
		btn_upload.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new SaveItemDetails().execute();    //上傳照片
				imgPreview.dismiss();
			}
		});
		//調整大小
		photoOrientation = orientation;
		double width = bitmap.getWidth();
		double height = bitmap.getHeight();
		double ratio = 300/width;
		int newerHeight = (int)(ratio * height);
		Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 300, newerHeight, true);    //重設大小
		Bitmap_preview.setImageBitmap(resizedBitmap);
		int api_version = Build.VERSION.SDK_INT;    //API版本
		String android_version = Build.VERSION.RELEASE;    //Android版本
		if(api_version > Build.VERSION_CODES.GINGERBREAD_MR1 && !android_version.matches("(1|2)\\.+"))
			Bitmap_preview.setRotation(orientation);
		imgPreview.show();
	}

	//處理方向的問題
	@SuppressLint("NewApi")
	private int DetectPhotoOrientation(String path) {
		int orientation = 0;
		try {
			ExifInterface eInterface = new ExifInterface(path);
			int orientationExif = eInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
			//Log.e("Photo OrientationExif", "" +orientationExif);
			switch (orientationExif) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				orientation = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				orientation = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				orientation = 270;
				break;
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return orientation;
	}
	//上傳照片
  	class SaveItemDetails extends AsyncTask<String,String,String>{
  	    int httpResponseCode;
  	    boolean success;
  		@Override
  		protected void onPreExecute(){
  			super.onPreExecute();
  			pDialog = new ProgressDialog(DetailItem.this);
  			pDialog.setMessage(getResources().getString(R.string.detailItem_sendingPhoto));
  			pDialog.setIndeterminate(false);
  			pDialog.setCancelable(false);
  			pDialog.show();
  		}
  		//儲存資料
		@Override
		protected String doInBackground(String... args) {
            //檢查bitmap 是否為空
			if (bitmap == null) return null;
			//Building Parameters，一個儲存資料的資料結構
			String message = photoMessage.getText().toString();    //儲存留言
			if (message.equals("") || message == null) message = "";
			ByteArrayOutputStream imgOpStream = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.JPEG, 85, imgOpStream);
			byte[] imgByte = imgOpStream.toByteArray();
			String imgByteString = Base64.encodeToString(imgByte,Base64.DEFAULT);
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			//params.add(new BasicNameValuePair("action", "upload_store_image"));    //set for action
			params.add(new BasicNameValuePair("id", sid));    //必要
			params.add(new BasicNameValuePair("uID", Integer.toString(account_uID)));
			params.add(new BasicNameValuePair("image", imgByteString));
		    params.add(new BasicNameValuePair("message", message));
		    params.add(new BasicNameValuePair("orientation", String.valueOf(photoOrientation)));
		    //Log.e("Photo Orientation Int", ""+photoOrientation);
			//藉由Http Request 發送修改資訊
			//Notice that update item url accepts POST method
		    //String action = "uploadPhoto";    //救急措施
		    String hostUrl = "http://192.241.211.104/~proposal/api/index.php/upload/store_image";
		    JSONParser jsonParser = new JSONParser(getApplicationContext());
			JSONObject json = jsonParser.httpRequestRest(hostUrl, "POST", params);
			if (json == null) {
				Log.e("JSON Object is null", "when doing background task Update photo");
				httpResponseCode = jsonParser.getHttpResponseCode();
				success = false;
			    return null;
			}
			//Log.d("Photo Update", json.toString());
			httpResponseCode = jsonParser.getHttpResponseCode();
			success = true;
			try{
				photoUrl = json.getString("thumb_path");
				if (!twitterShare || !facebookShare) {
					Intent i = getIntent();
					//setResult(100,i);
					finish();
					overridePendingTransition(0, 0);
					startActivity(i);
				}
			}
			catch (JSONException e){
				e.printStackTrace();
			}
			return null;
		}
		//完成背景作業後，防止對話框跳出來
		protected void onPostExecute(String file_url) {
			pDialog.dismiss();
			if (success) {
				String message = photoMessage.getText().toString();    //儲存留言
				if (message.equals("") || message == null) message = "";
				if (twitterShare) twitterHelper.makePhotoTweet(DetailItem.this ,message, sName, user_latitude, user_longitude, photoFile);
			    if (facebookShare) ShareToFacebook(message);
			}
			else {
				if (httpResponseCode == 900) {
					//連線逾時
					Toast.makeText(getApplicationContext(), getResources().getString(R.string.ConnectionTimeOut), Toast.LENGTH_SHORT).show();
					return;
				}
				if (httpResponseCode == HttpStatus.SC_BAD_REQUEST) {
					Toast.makeText(getApplicationContext(), getResources().getString(R.string.loadPhoto_httpResponse_400), Toast.LENGTH_SHORT).show();
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

  	//Get The Data and put in sharePre
  	public void putDataIn(JSONObject target_item, String scID) {
		editItemPre = getSharedPreferences("editItem_tmp", MODE_PRIVATE);
		Editor editWriter = editItemPre.edit();
		try {
			editWriter.putString("sName", target_item.getString("sName"));
			editWriter.putString("sMinCharge", target_item.getString("sMinCharge"));
			editWriter.putString("sPhone", target_item.getString("sPhone"));
			editWriter.putString("sCountry", target_item.getString("sCountry"));
			editWriter.putString("sTownship", target_item.getString("sTownship"));
			editWriter.putString("sLocation", target_item.getString("sLocation"));
			editWriter.putString("startTime", target_item.getString("startTime"));
			editWriter.putString("closeTime", target_item.getString("closeTime"));
			editWriter.putString("RestDay", target_item.getString("RestDay"));
			editWriter.putString("RestDay_ID_org", target_item.getString("RestDay_ID"));
			editWriter.putString("sMemo", target_item.getString("sMemo"));
			editWriter.putString("sEmail", target_item.getString("sEmail"));
			editWriter.putString("sURL", target_item.getString("sURL"));
			editWriter.putString("mcID", target_item.getString("MainCategory"));
			editWriter.putString("scID", scID);
			editWriter.putString("scIDNumber", target_item.getString("scID"));
			editWriter.putString("status", target_item.getString("status"));
			int sCanDelivery = Integer.parseInt(target_item.getString("sCanDelivery"));
			editWriter.putInt("sCanDelivery", sCanDelivery);
			int sCanToGo = Integer.parseInt(target_item.getString("sCanToGo"));
			editWriter.putInt("sCanToGo", sCanToGo);
			int is24Hours = Integer.parseInt(target_item.getString("is24Hours"));
			editWriter.putInt("is24Hours", is24Hours);
			editWriter.commit();    //寫入！
		}
		catch (Exception e) {
			e.printStackTrace();
		}
  	}

  	//GetDpiToPixels
  	public int getPixels(int dipValue) {
  		Resources resources = getResources();
  		int px = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, resources.getDisplayMetrics());
  		return px;
  	}

  	//LightBox Dialog
  	public void showLightBoxDialog(int position) {
  		lightBoxDialog = new Dialog(this, R.style.lightbox_dialog);
  		lightBoxDialog.setContentView(R.layout.lightbox_dialog);
  		lightBoxDialog.setCanceledOnTouchOutside(true);
  		lightBoxDialog.setCancelable(true);
  		ViewPager gallery = (ViewPager) lightBoxDialog.findViewById(R.id.gallery_viewPager);
  		//String uID = String.valueOf(account_uID);
  		ViewPagerGallery viewPagerGallery = new ViewPagerGallery(this, store_photos, dm);
  		gallery.setAdapter(viewPagerGallery);
  		gallery.setCurrentItem(position);
  		CirclePageIndicator gIndicator = (CirclePageIndicator) lightBoxDialog.findViewById(R.id.gallery_indicator);
  		gIndicator.setViewPager(gallery);
  		gIndicator.setCurrentItem(position);
  		lightBoxDialog.show();
  	}

  	private void reportInvalidPhoto(final int position) {
  		final String[] reportItems = new String[] {getResources().getString(R.string.report_invalid_photo)};
  		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, reportItems);
  		new AlertDialog.Builder(DetailItem.this)
  		.setAdapter(adapter, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//Send Invalid message
				String uID = String.valueOf(account_uID);
				ReportTask reportTask = new ReportTask(getApplicationContext(), 5, uID, sid);
				reportTask.addPhotoUrl(store_photos.get(position).get("bigSizeUrl"));
				reportTask.execute();
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.Report_error_toast), Toast.LENGTH_SHORT).show();
			}
		})
		.show();
  	}

  	//按下離開
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			editItemPre = getSharedPreferences("editItem_tmp", MODE_PRIVATE);
			editItemPre.edit().clear().commit();    //清空
			if (hasModified) setResult(RESULT_OK);
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		if (hasModified) setResult(RESULT_OK);
		overridePendingTransition(R.anim.finish_in, R.anim.finish_out);
	}

	@Override
	protected void onResume() {
		super.onResume();
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
					Intent intent = new Intent(DetailItem.this, splash.class);
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

	/*Facebook uses*/
    //Po 文
	public void ShareToFacebook(String mes) {
		Session session = Session.getActiveSession();
		if (session != null) {
			// Check publish permission
			List<String> permisssion = session.getPermissions();
	        if (!isSubsetOf(PERMISSIONS, permisssion)) {
	            pendingPublishReauthorization = true;
	            Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(this, PERMISSIONS);
	            session.requestNewPublishPermissions(newPermissionsRequest);
	            return;
	        }
			//Share a link
			String title = getResources().getString(R.string.Twitter_iAmAt) +" " +sName;
			String urlString = String.format("http://maps.google.com/maps?q=%f+%f",
					user_latitude,user_longitude);
			Bundle parameter = new Bundle();
			parameter.putString("name", title);
			parameter.putString("caption", "via FoodieTrip!");
			parameter.putString("description", mes);
			parameter.putString("link", urlString);
			if (facebookShare)
				parameter.putString("picture", photoUrl);
			Request.Callback callback = new Request.Callback() {
				@Override
				public void onCompleted(Response response) {
					if (response != null) {
						//JSONObject graphResponse = response.getGraphObject().getInnerJSONObject();
						//String postId = null;
						/*try {
							postId = graphResponse.getString("id");
						}
						catch (Exception e) {
							Log.e("Facebook Share JSON Error", e.getMessage());
						}*/
						FacebookRequestError error = response.getError();
						if (error != null) {
							Toast.makeText(getApplicationContext(), error.getErrorMessage(), Toast.LENGTH_SHORT).show();
						}
						else {
							//Toast.makeText(getApplicationContext(), postId, Toast.LENGTH_SHORT).show();
							Toast.makeText(getApplicationContext(), getResources().getString(R.string.Facebook_post_success), Toast.LENGTH_SHORT).show();
						}
						Log.e("reponse is not null", response.toString());
					}
					else {
						Log.e("reponse is null", "");
					}
				}
			};
			Request request = new Request(session, "me/feed", parameter, HttpMethod.POST, callback);
			RequestAsyncTask task = new RequestAsyncTask(request);
			task.execute();
		}
	}
	//確認目前的登入狀態，並且檢查Permission 看看是不是可以Po文
	private boolean isSubsetOf(Collection<String> subset,Collection<String> superset) {
		for (String string : subset) {
			if (!superset.contains(string)) {
				return false;
			}
		}
		return true;
	}

}

