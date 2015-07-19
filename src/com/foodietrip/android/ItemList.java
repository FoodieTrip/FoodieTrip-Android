package com.foodietrip.android;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.actionbarsherlock.app.ActionBar;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.actionbarsherlock.app.SherlockActivity;
import com.dafruits.android.library.widgets.ExtendedListView;
import com.foodietrip.android.library.*;
import com.foodietrip.android.library.ShakeListener.OnShakeListener;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.viewpagerindicator.CirclePageIndicator;

import uk.co.senab.actionbarpulltorefresh.extras.actionbarsherlock.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class ItemList extends SherlockActivity implements LocationListener{
	JSONArray store_temp;    //存放所有店家
	UserFunctions userFunctions;
    boolean loginCheck, thereIsNoItem = false, afterSplash = false, pullToRefresh = false, searchMode = false, isBackingUp = false;
	ActionBar actionbar;
	ExtendedListView lv;
	LinearLayout listViewFoot,bigCataView;
	TableLayout smallCataView;
	Button btn_ReadMore,btn_secSideLogout,btn_secSideChangeData,btn_secSideLogin,btn_sideClear;
	Button btn_noFoundRefresh,btn_serverNotFound,btn_HereNoItemlogin,btn_HereNoItemAdd,btn_UserRefresh;
	TextView progressText,noItemHaveFound,secSide_userName,secSide_guest, HereHaveNoItemText;
	AutoCompleteTextView search_keyword;
	View loadingView,listItem_progress,btn_search,noItemHaveFoundAlert,hereHaveNoItemImage;
	MenuItem refresh_list;
	RadioGroup radioGroup;
	ArrayList<HashMap<String,String>> all_item_list,bigTagCatagory,smallTagCatagory;
	ArrayList<String> smallTags;
	ArrayAdapter<String> adapterName;
	List<String> storeNameList;
	AllItemAdapter allItemAdapter;
	private static final String TAG_ITEM = "Store";    //表格名稱
	private static final int AFTER_SETTING = 10;
	private static final int GO_DETAIL = 11;
	private static final int PAGE_UNIT = 10;
	SharedPreferences sPreferences,sprfSetting;
	//private static final int ONE_MINUTE = 1000 * 60;
	private PullToRefreshLayout pullToRefreshLayout;
	private Dialog tutorialDialog;
	SlidingMenu slidingMenu;
	LocationManager locateManager;
	NetworkState networkState;
	String fineProvide,networkProvide,search_keywordString, smallTagsString="", userCountryCode, userLanguage, lowProvider;
	String[] storeNames,helloWords;
	Double double_latitude = 0.0,double_longitude = 0.0;
	int loading_switch = 0, bigTagCatagoryValue, page = 0;    //目前讀取了多少，控制ListView位置用
    ImageView serverError;
    Animation pushEffect;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.all_item);    //將Layout 設定成所有Item那頁
		sprfSetting = PreferenceManager.getDefaultSharedPreferences(this);
		boolean tutorialViewed = sprfSetting.getBoolean("TutorialHasViewed", false);
		if (!tutorialViewed) showTutorial();
		//jsonParser = new JSONParser(getApplicationContext());
		slidingMenu = new SlidingMenu(this);
		slidingMenu.setMode(SlidingMenu.LEFT_RIGHT);
		slidingMenu.setSlidingEnabled(false);
		slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
		slidingMenu.setShadowWidthRes(R.dimen.shadowUse);
		slidingMenu.setShadowDrawable(R.drawable.shadow);
		slidingMenu.setBehindOffsetRes(R.dimen.sidebarUse);
		slidingMenu.setFadeDegree(0.35f);
		slidingMenu.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);
		slidingMenu.setMenu(R.layout.sidebar);
		slidingMenu.setSecondaryMenu(R.layout.second_sidebar);
		slidingMenu.setSecondaryShadowDrawable(R.drawable.shadow_second);
		helloWords = getResources().getStringArray(R.array.helloWord);
		int ramdomNum = (int) Math.ceil(Math.random() * 14);
		pullToRefreshLayout = (PullToRefreshLayout) findViewById(R.id.pulltorefresh);
		bigCataView = (LinearLayout) findViewById(R.id.bigCatagoryView);
		smallCataView = (TableLayout) findViewById(R.id.smallCataTableLayout);
		btn_search = (View) findViewById(R.id.search_button);
		btn_search.setOnClickListener(search_listener);
		btn_sideClear = (Button) findViewById(R.id.sidebar_Clear);
		btn_serverNotFound = (Button) findViewById(R.id.network_errorButton);
		serverError = (ImageView) findViewById(R.id.network_errorImage);
		HereHaveNoItemText = (TextView) findViewById(R.id.here_is_new_text);
		hereHaveNoItemImage = (View) findViewById(R.id.here_is_new_image);
		btn_HereNoItemAdd = (Button) findViewById(R.id.button_usetAddItem);
		btn_HereNoItemlogin = (Button) findViewById(R.id.button_userLogin);
		btn_UserRefresh = (Button) findViewById(R.id.button_useRefresh);
		//檢查Intent是否有資料
		Intent intent = this.getIntent();
		Bundle bundle = intent.getExtras();
		if (bundle != null) {
			afterSplash = bundle.getBoolean("afterSplash", false);
			double_latitude = bundle.getDouble("sLatitude");
			double_longitude = bundle.getDouble("sLongitude");
		}
		else {
			getLocationFromsPre();
		}
		networkState = new NetworkState(this);
		boolean isOnline = networkState.checkInternet();
		if (isOnline){    //檢查是不是有連線
			//取得介面元件
			lv = (ExtendedListView)findViewById(R.id.listView);
			lv.setFadingEdgeLength(100);
			listItem_progress = (View) findViewById(R.id.listview_progressbar);
			progressText = (TextView)findViewById(R.id.listview_progresstext);
			progressText.setText(helloWords[ramdomNum]);
			lv.setOnScrollListener(listview_ScrollListener);
			lv.setOnPositionChangedListener(scollpanelListener);
			lv.setFastScrollEnabled(true);
			lv.setSmoothScrollbarEnabled(true);
			lv.setScrollingCacheEnabled(true);    //scrollingCache
			LayoutInflater inflater = ItemList.this.getLayoutInflater();
			listViewFoot = (LinearLayout)inflater.inflate(R.layout.footer, null);
			lv.addFooterView(listViewFoot);
			btn_ReadMore = (Button)listViewFoot.findViewById(R.id.btn_readMore);
			loadingView = (View)listViewFoot.findViewById(R.id.progressBar1);
			noItemHaveFound = (TextView) findViewById(R.id.listview_noItemFound);
			noItemHaveFoundAlert = (View) findViewById(R.id.listview_alertIcon);
			btn_noFoundRefresh = (Button) findViewById(R.id.listview_noItemRefresh);
			search_keyword = (AutoCompleteTextView) findViewById(R.id.search_keywords);
			search_keyword.setOnEditorActionListener(editorListener);
			btn_ReadMore.setOnClickListener(new Button.OnClickListener() {
				@Override
				public void onClick(View v) {
					btn_ReadMore.setVisibility(View.GONE);
					loadingView.setVisibility(View.VISIBLE);
					new LoadNextItem().execute();
				}
			});
			//取得 ActionBar Object Reference
			actionbar = getSupportActionBar();
			actionbar.setDisplayHomeAsUpEnabled(false);
 			actionbar.setHomeButtonEnabled(false);
			//設定監聽
			lv.setOnItemClickListener(listener);    //選擇一個Item，開啟編輯視窗
			lv.setOnItemLongClickListener(long_listener);
			//LoginCheck
			userFunctions = new UserFunctions(getApplicationContext());
			loginCheck = userFunctions.isUserLoggedIn(getApplicationContext());
			setSecSide();
			//Log.v("user logged check = ", ""+loginCheck);
            ShakeListener shakeListener = new ShakeListener(this);
            shakeListener.setOnShakeListener(new OnShakeListener() {
				@Override
				public void onShake() {
					if (searchMode) {
			    		search_keyword.setText("");
			    		search_keywordString = "";
			    		clearBigTagChoose();
			    		bigTagCatagoryValue = 0;
                        clearSmallTagItem();
			    		searchMode = false;
			    		refresh_list.setVisible(false);
						refreshFromMenu();
			    		onVibrator();
					}
				}
			});
            if (savedInstanceState == null) {
    			all_item_list = new ArrayList<HashMap<String,String>>();
    			bigTagCatagory = new ArrayList<HashMap<String,String>>();
    			smallTagCatagory = new ArrayList<HashMap<String,String>>();
    			smallTags = new ArrayList<String>();
    		    if (double_latitude == 0.0 && double_longitude == 0.0)
    		    	gpsFix();
    			new SearchTags().execute();
    			new LoadAllItem().execute();
            }
            else {
            	if (!slidingMenu.isSlidingEnabled()) {
         			actionbar.setDisplayHomeAsUpEnabled(true);
         			actionbar.setHomeButtonEnabled(true);
         			slidingMenu.setSlidingEnabled(true);
         		}
            }
            pushEffect = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.image_effect);
		}
		else{
			new AlertDialog.Builder(ItemList.this)
			.setTitle(getResources().getString(R.string.itemList_alertNetErrorTitle))
			.setMessage(getResources().getString(R.string.itemList_alertNetErrorMes))
			.setCancelable(false)
			.setPositiveButton(getResources().getString(R.string.alertDialogOkay), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
				    finish();
				}
			})
			.show();
		}
		ActionBarPullToRefresh.from(this).theseChildrenArePullable(R.id.listView)
		.listener(new OnRefreshListener() {
			@Override
			public void onRefreshStarted(View view) {
				gpsFix();
				all_item_list = new ArrayList<HashMap<String,String>>();
				ArrayAdapter<HashMap<String,String>> adapter = new ArrayAdapter<HashMap<String,String>>(ItemList.this,R.layout.list_item, all_item_list);
				lv.setAdapter(adapter);
				adapter.notifyDataSetChanged();
				page = 0;
				pullToRefresh = true;
				if (thereIsNoItem) {
					lv.addFooterView(listViewFoot);
					thereIsNoItem = false;
				}
				new LoadAllItem().execute();
			}
		}).setup(pullToRefreshLayout);
	}

	private ExtendedListView.OnPositionChangedListener scollpanelListener = new ExtendedListView.OnPositionChangedListener() {
		@Override
		public void onPositionChanged(ExtendedListView listView, int position,
				View scrollBarPanel) {
			if (!isBackingUp) {
				String txt_avg = getResources().getString(R.string.scrollpanel_text);
				String txt_noRating = getResources().getString(R.string.scrollpanel_noScore);
				String useForShow = txt_noRating;
				//一定要改掉喔，避免讀取太快position的問題
				try {
					String avgRating = all_item_list.get(position).get("Store_avg_rating");
					if (!avgRating.equals("0") && !avgRating.startsWith("0") && avgRating.trim().length() > 0)
						useForShow = txt_avg +DecimalHelper(avgRating);
				}
				catch (IndexOutOfBoundsException e) {
					useForShow = "Loading";
				}
				((TextView)scrollBarPanel).setText(useForShow+"   ");
			}
			else {
				((TextView)scrollBarPanel).setText("");
				scrollBarPanel.setVisibility(View.GONE);
				if (position == 0) {
					((TextView)scrollBarPanel).setText("Loading   ");
					scrollBarPanel.setVisibility(View.VISIBLE);
					isBackingUp = false;
				}
			}
		}
	};

	private String DecimalHelper(String ratingAvg) {
		double ratingDouble = Double.valueOf(ratingAvg);
		String formatedRatingAvg = new DecimalFormat("#.0").format(ratingDouble*1.0);
		return formatedRatingAvg;
	}

	private void getLocationFromsPre() {
		sPreferences = getSharedPreferences("foodbook_pref", MODE_PRIVATE);
		String spre_latitude = sPreferences.getString("userLatitude", "0.0");
		String spre_longitude = sPreferences.getString("userLongitude", "0.0");
		userCountryCode = sPreferences.getString("countryCode", "TW");
		userLanguage = sPreferences.getString("userLanguage", "zh");
		double_latitude = Double.parseDouble(spre_latitude);
		double_longitude = Double.parseDouble(spre_longitude);
		if (double_latitude == 0.0 || double_longitude == 0.0) {
			LocationManager locateManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
			Location lastlocation = locateManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (lastlocation != null) {
				double_latitude = lastlocation.getLatitude();
				double_longitude = lastlocation.getLongitude();
			}
			else {
				Location lastlocation_net = locateManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				if (lastlocation_net != null) {
					double_latitude = lastlocation_net.getLatitude();
					double_longitude = lastlocation_net.getLongitude();
				}
			}
		}
	}

	private ListView.OnItemLongClickListener long_listener = new ListView.OnItemLongClickListener() {
		@SuppressWarnings("deprecation")
		@SuppressLint("NewApi")
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View v,
				int position, long id) {
			String sName = all_item_list.get(position).get("sName");
			int api_version = Build.VERSION.SDK_INT;    //API版本
			String android_version = Build.VERSION.RELEASE;    //Android版本
			if(api_version > Build.VERSION_CODES.GINGERBREAD_MR1 && !android_version.matches("(1|2)\\.+")) {
				ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
				ClipData clip = ClipData.newPlainText("sName", sName);
				clipboardManager.setPrimaryClip(clip);
			}
			else {
				android.text.ClipboardManager clipboardManager = (android.text.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
				clipboardManager.setText(sName);
			}
			Toast.makeText(getApplicationContext(), sName +getResources().getString(R.string.sName_has_copied), Toast.LENGTH_SHORT).show();
			return true;
		}
	};

	private void setSecSide() {
		secSide_userName = (TextView) findViewById(R.id.secSide_userName);
		secSide_guest = (TextView) findViewById(R.id.secSide_Guest);
		btn_secSideChangeData = (Button) findViewById(R.id.secSide_setUserData);
		btn_secSideLogout = (Button) findViewById(R.id.secSide_logout);
		btn_secSideLogin = (Button) findViewById(R.id.secSide_login);
		btn_secSideChangeData.setOnClickListener(secSideListener);
		btn_secSideLogin.setOnClickListener(secSideListener);
		btn_secSideLogout.setOnClickListener(secSideListener);
		if (loginCheck) {
			UserFunctions userFunctions = new UserFunctions(getApplicationContext());
			String nick_name = userFunctions.getUserName(getApplicationContext());
			secSide_userName.setText(nick_name);
			secSide_userName.setVisibility(View.VISIBLE);
			btn_secSideChangeData.setVisibility(View.VISIBLE);
			btn_secSideLogout.setVisibility(View.VISIBLE);
			secSide_guest.setVisibility(View.GONE);
			btn_secSideLogin.setVisibility(View.GONE);
		}
		else {
			secSide_userName.setVisibility(View.GONE);
			btn_secSideChangeData.setVisibility(View.GONE);
			btn_secSideLogout.setVisibility(View.GONE);
			secSide_guest.setVisibility(View.VISIBLE);
			btn_secSideLogin.setVisibility(View.VISIBLE);
		}
	}

	private Button.OnClickListener secSideListener = new Button.OnClickListener() {
		@Override
		public void onClick(View v) {
			Animation pushEffect = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.image_effect);
			switch (v.getId()) {
			case R.id.secSide_login:
				btn_secSideLogin.startAnimation(pushEffect);
		   		Intent go_login_page = new Intent(ItemList.this,LoginActivity.class);
	    		startActivity(go_login_page);
	    		overridePendingTransition(R.anim.detail_in, R.anim.storelist_leave);
	    		finish();
				break;
			case R.id.secSide_logout:
				userLogout();
				break;
			case R.id.secSide_setUserData:
				btn_secSideChangeData.startAnimation(pushEffect);
		   		Intent go_modify_page = new Intent(ItemList.this,Modify_userData.class);
	    		startActivity(go_modify_page);
	    		overridePendingTransition(R.anim.detail_in, R.anim.storelist_leave);
	    		finish();
				break;
			}
		}
	};

	private OnEditorActionListener editorListener = new OnEditorActionListener() {
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if (actionId == EditorInfo.IME_ACTION_SEARCH) {
				search_keywordString = search_keyword.getText().toString();
				if (search_keywordString.equals("") || search_keywordString == null) {
					Toast.makeText(getApplicationContext(), getResources().getString(R.string.search_noString), Toast.LENGTH_SHORT).show();
				}
				else {
					//Hide Visual Keyboard
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(search_keyword.getWindowToken(), 0);
					searchMode = true;
					refresh_list.setVisible(true);
					refreshFromMenu();    //搜尋！
					slidingMenu.showContent();
				}
			}
			return false;
		}
	};

	private View.OnClickListener search_listener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			btn_search.startAnimation(pushEffect);
			search_keywordString = search_keyword.getText().toString();
			searchMode = true;
			refresh_list.setVisible(true);
			refreshFromMenu();    //搜尋！
			slidingMenu.showContent();
		}
	};

	//set for GPS provider
	public void setLocationFix() {
		//GPS定位使用
		locateManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		Criteria fine_criteria = new Criteria();    //由GPS取得
		fine_criteria.setAccuracy(Criteria.ACCURACY_FINE);
		fineProvide = locateManager.getBestProvider(fine_criteria, false);
		Criteria low_criteria = new Criteria();
		low_criteria.setAccuracy(Criteria.ACCURACY_MEDIUM);
		lowProvider = locateManager.getBestProvider(low_criteria, false);    //Low Provider
		Criteria network_criteria = new Criteria();    //由 WiFi/3G取得位置
		network_criteria.setAccuracy(Criteria.ACCURACY_COARSE);
		network_criteria.setPowerRequirement(Criteria.POWER_LOW);
		networkProvide = locateManager.getBestProvider(network_criteria, false);
	}

	//取得GPS位置
	public void gpsFix() {
        setLocationFix();
		if (locateManager.isProviderEnabled(fineProvide)) {
			locateManager.requestLocationUpdates(networkProvide, 7000, 100, this);
			locateManager.requestLocationUpdates(fineProvide, 7000, 100, this);
			return;
		}
		else {
			locateManager.requestLocationUpdates(networkProvide, 8000, 100, this);
			Toast.makeText(getApplicationContext(), getResources().getString(R.string.gpsRequest), Toast.LENGTH_SHORT).show();
		}
	}

	/*設定按下 清單上 的物件的時候，會發生什麼事*/
	private ListView.OnItemClickListener listener = new ListView.OnItemClickListener(){
		@Override
		public void onItemClick(AdapterView<?> parent, View v, int position,long id) {
			String sid = ((TextView) v.findViewById(R.id.listViewRow_sid)).getText().toString();
			//從選擇的ListView中取得資訊
			Intent go_detail_item = new Intent(ItemList.this,DetailItem.class);
			//Sending sid to next activity
			double distance_double = Double.valueOf(all_item_list.get(position).get("distance_raw"));
			Bundle bundle = new Bundle();
			bundle.putString("sID", sid);
			bundle.putDouble("distance", distance_double);
			bundle.putSerializable("BigCatagory", bigTagCatagory);
			bundle.putSerializable("SmallCatagory", smallTagCatagory);
			go_detail_item.putExtras(bundle);
			//運行該Activity，如果修改成功需要回傳資料
			startActivityForResult(go_detail_item,100);
			overridePendingTransition(R.anim.detail_in, R.anim.storelist_leave);
		}
	};
	//對應EditItem 的回應
	protected void onActivityResult(int requestCode,int resultCode,Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case AFTER_SETTING:
			refreshTheScreen();
			break;
		case GO_DETAIL:
			if (resultCode == RESULT_OK) {
                refreshTheScreen();
			}
			else if (resultCode == RESULT_CANCELED) {
				return;
			}
			break;
		default:
			if (resultCode == RESULT_OK) {
		    	refreshTheScreen();
			}
			break;
		}
	}

	private void refreshTheScreen() {
    	search_keyword.setText("");
    	search_keywordString = "";
    	clearBigTagChoose();
    	bigTagCatagoryValue = 0;
    	clearSmallTagItem();
    	searchMode = false;
    	if (refresh_list != null)
    		refresh_list.setVisible(false);
		refreshFromMenu();
		sprfSetting = PreferenceManager.getDefaultSharedPreferences(this);
		boolean tutorialViewed = sprfSetting.getBoolean("TutorialHasViewed", false);
		if (!tutorialViewed) showTutorial();
	}

	public void setAdapter() {
		//在背景更新介面
		runOnUiThread(new Runnable(){
			@Override
			public void run() {
			    allItemAdapter = new AllItemAdapter(ItemList.this, all_item_list, getApplicationContext());
				//設定ListView內容
				int currentPosition = lv.getFirstVisiblePosition();
				lv.setAdapter(allItemAdapter);
				if (loading_switch == 1) {
					lv.setSelectionFromTop(currentPosition, 0);
				}
			}
		});
	}

	/*讀取用的*/
	class LoadNextItem extends AsyncTask<String, String, String> {
		boolean noItemAtAll = false;
		boolean success = false;
		int nextStores, httpResponseCode;
		//在背景開始讀取以前，顯示Progress Dialog(一個提醒用的對話框)
		@Override
		protected void onPreExecute(){
			super.onPreExecute();
			store_temp = null;
			page++;
		}
		//Get All Item from URL
		@Override
		protected String doInBackground(String... args) {
			//Building Parameters, 應該是一個儲存資料的資料結構，用來丟給jsonParser傳給遠方伺服器
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("page", String.valueOf(page)));
			if(searchMode) {
				String bigTagsString = "";
				if (bigTagCatagoryValue != 0) bigTagsString = String.valueOf(bigTagCatagoryValue);
				params.add(new BasicNameValuePair("keyword", search_keywordString));
				params.add(new BasicNameValuePair("MainCategory", bigTagsString));
				params.add(new BasicNameValuePair("SubCategories", smallTagsString));
			}
			else {
				params.add(new BasicNameValuePair("keyword", ""));
				params.add(new BasicNameValuePair("MainCategory", ""));
				params.add(new BasicNameValuePair("SubCategories", ""));
			}
            //位置資訊
			params.add(new BasicNameValuePair("userLatitude", Double.toString(double_latitude)));
			params.add(new BasicNameValuePair("userLongitude", Double.toString(double_longitude)));
			params.add(new BasicNameValuePair("userCountryCode", userCountryCode));
			params.add(new BasicNameValuePair("userLanguage", userLanguage));
			//Log.e("JSON Tester: ", json.toString());
			try{
				JSONParser jsonParser = new JSONParser(getApplicationContext());
				JSONObject json = jsonParser.makeHttpRequest("/list", "GET" ,params);
				if (json == null) {
					Log.e("JSON Object is null", "when doing background task ItemList 555");
					noItemAtAll = true;
					success = false;
					httpResponseCode = jsonParser.getHttpResponseCode();
				    return null;
				}
				//找到Item清單，Getting Array of Items
				store_temp = json.getJSONArray(TAG_ITEM);    //Page++後的清單
				if (store_temp == null)
				    nextStores = 0;
				else
				    nextStores = store_temp.length();
				adapterName = new ArrayAdapter<String>(getApplicationContext(), R.layout.my_list_layout, storeNames);
				//looping though all item
				for (int i=0 ; i<store_temp.length() ; i++) {
				    JSONObject c = store_temp.getJSONObject(i);
					all_item_list.add(ListAdapter(c,0));    //Loading HashMap to ArrayList
				}
			    success = true;
			    httpResponseCode = jsonParser.getHttpResponseCode();
			}
			catch(JSONException e){
				e.printStackTrace();
				//Log.d("All Items: ", json.toString());
			}
			return null;
		}
		protected void onPostExecute(String file_url){
			if (success) {
				loading_switch = 1;
				btn_ReadMore.setVisibility(View.VISIBLE);
				loadingView.setVisibility(View.GONE);
				setAdapter();
				if (pullToRefresh) {
					pullToRefresh = false;
					pullToRefreshLayout.setRefreshComplete();
				}
				//少於10代表之後一定沒東西
				if (nextStores < PAGE_UNIT) {
					lv.removeFooterView(listViewFoot);
					thereIsNoItem = true;
				}
				runOnUiThread(new Runnable() {
					public void run() {
						search_keyword.setAdapter(adapterName);    //自動完成列新增關鍵字
					}
				});
			}
			else {
				//沒有東西了
				if (noItemAtAll) {
					lv.removeFooterView(listViewFoot);
					thereIsNoItem = true;
				    return;
				}
				if (httpResponseCode == 901) {
					setNetworkServerErrorUI(listItem_progress, progressText);    //404找不到
					return;
				}
				if (httpResponseCode == 900) {
					//連線逾時
					Toast.makeText(getApplicationContext(), getResources().getString(R.string.ConnectionTimeOut), Toast.LENGTH_SHORT).show();
					return;
				}
				if (httpResponseCode == HttpStatus.SC_NOT_FOUND) {
					lv.removeFooterView(listViewFoot);
					thereIsNoItem = true;
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

	/*在背景藉由 HTTP Request 讀取Item清單的Method*/
	class LoadAllItem extends AsyncTask<String,String,String>{
		boolean success = false;
		int httpResponseCode;
		//在背景開始讀取以前，顯示Progress Dialog(一個提醒用的對話框)
		@Override
		protected void onPreExecute(){
			super.onPreExecute();
			lv.setVisibility(View.GONE);
    		noItemHaveFound.setVisibility(View.GONE);
    		noItemHaveFoundAlert.setVisibility(View.GONE);
    		HereHaveNoItemText.setVisibility(View.GONE);
    		hereHaveNoItemImage.setVisibility(View.GONE);
    		btn_HereNoItemAdd.setVisibility(View.GONE);
    		btn_HereNoItemlogin.setVisibility(View.GONE);
    		btn_UserRefresh.setVisibility(View.GONE);
    		btn_noFoundRefresh.setVisibility(View.GONE);
    		int ramdomNum = (int) Math.ceil(Math.random() * 14);
    		progressText.setText(helloWords[ramdomNum]);
			progressText.setVisibility(View.VISIBLE);
            listItem_progress.setVisibility(View.VISIBLE);
		}
		//Get All Item from URL
		@Override
		protected String doInBackground(String... args) {
			long start = System.currentTimeMillis();
			long end = start + 25*1000;    //25 Second * 1000ms/sec
			back: {
				while (System.currentTimeMillis() < end) {
					if (double_latitude != 0.0 && double_longitude != 0.0)
						break back;
				}
				getLocationFromsPre();
				if (double_latitude == 0.0 && double_longitude == 0.0) {
					success = false;
					httpResponseCode = 999;
					return null;
				}
			}
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			//String timeZone = TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT);    //getTimeZone
			params.add(new BasicNameValuePair("page", String.valueOf(page)));
			try {
				int verstionInt = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
				String verstionString = String.valueOf(verstionInt);
				params.add(new BasicNameValuePair("VersionCode", verstionString));
			}
			catch (NameNotFoundException e1) {
				e1.printStackTrace();
			}
			if(searchMode) {
				String bigTagsString = "";
				if (bigTagCatagoryValue != 0) bigTagsString = String.valueOf(bigTagCatagoryValue);
				params.add(new BasicNameValuePair("keyword", search_keywordString));
				params.add(new BasicNameValuePair("MainCategory", bigTagsString));
				params.add(new BasicNameValuePair("SubCategories", smallTagsString));
			}
			else {
				params.add(new BasicNameValuePair("keyword", ""));
				params.add(new BasicNameValuePair("MainCategory", ""));
				params.add(new BasicNameValuePair("SubCategories", ""));
			}
			//String action = "get_store_list";
			params.add(new BasicNameValuePair("userLatitude", Double.toString(double_latitude)));
			params.add(new BasicNameValuePair("userLongitude", Double.toString(double_longitude)));
			params.add(new BasicNameValuePair("userCountryCode", userCountryCode));
			params.add(new BasicNameValuePair("userLanguage", userLanguage));
			try{
				JSONParser jsonParser = new JSONParser(getApplicationContext());
				JSONObject json = jsonParser.makeHttpRequest("/list" , "GET",params);
				if (json == null) {
					Log.e("JSON Object is null", "when doing background task ItemList 656");
					httpResponseCode = jsonParser.getHttpResponseCode();
					success = false;
				    return null;
				}
				//Log.e("JSON Tester: ", json.toString());
				//找到Item清單，Getting Array of Items
				store_temp = json.getJSONArray(TAG_ITEM);    //Page0 清單
				//處裡名稱的狀況
				if (!searchMode) {
					String storeNameTempString = json.getString("Stores_Name_List");
					if (!storeNameTempString.equals("")) {
						storeNameList = Arrays.asList(storeNameTempString.split("\\s*,\\s*"));
						storeNames = new String[storeNameList.size()];
						for (int i=0; i<storeNameList.size(); i++)
							storeNames[i] = storeNameList.get(i);
					}
				}
				adapterName = new ArrayAdapter<String>(getApplicationContext(), R.layout.my_list_layout, storeNames);
				//looping though all item
				for (int i=0 ; i<store_temp.length() ; i++) {
				    JSONObject c = store_temp.getJSONObject(i);
					all_item_list.add(ListAdapter(c,0));    //Loading HashMap to ArrayList
				}
				success = true;
				httpResponseCode = jsonParser.getHttpResponseCode();
			}
			catch(JSONException e){
				e.printStackTrace();
				//Log.d("All Items: ", json.toString());
			}
			return null;
		}
		protected void onPostExecute(String file_url){
			if (success) {
				slidingMenu.setSlidingEnabled(true);
				actionbar.setDisplayHomeAsUpEnabled(true);
     			actionbar.setHomeButtonEnabled(true);
				listItem_progress.setVisibility(View.GONE);
				progressText.setVisibility(View.GONE);
				Animation fadeEffect = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fadein);
				lv.setVisibility(View.VISIBLE);
			    lv.startAnimation(fadeEffect);
				setAdapter();
				if (pullToRefresh) {
					pullToRefresh = false;
					pullToRefreshLayout.setRefreshComplete();
				}
				//少於10代表之後一定沒東西
				if (store_temp.length() < PAGE_UNIT) {
					lv.removeFooterView(listViewFoot);
					thereIsNoItem = true;
				}
				runOnUiThread(new Runnable() {
					public void run() {
						search_keyword.setAdapter(adapterName);
					}
				});
			}
			else {
				if (httpResponseCode == 999) {
					setLocationFix();
					locateManager.requestLocationUpdates(lowProvider, 0, 0, ItemList.this);
					getLocationFromsPre();
					Toast.makeText(getApplicationContext(), getResources().getString(R.string.gps_is_current_fixing), Toast.LENGTH_SHORT).show();
					progressText.setText(getResources().getString(R.string.gps_is_current_fixing));
		            listItem_progress.setVisibility(View.GONE);
		            btn_UserRefresh.setVisibility(View.VISIBLE);
					btn_UserRefresh.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							refreshFromMenu();
						}
					});
					return;
				}
				if (httpResponseCode == 901) {
					setNetworkServerErrorUI(listItem_progress, progressText);    //Server 空值
					return;
				}
				if (httpResponseCode == 900) {
					//連線逾時
					Toast.makeText(getApplicationContext(), getResources().getString(R.string.ConnectionTimeOut), Toast.LENGTH_SHORT).show();
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
				if (httpResponseCode == HttpStatus.SC_NOT_FOUND) {
					if (searchMode) {
						//搜尋沒找到東西的情況
						if (!slidingMenu.isSlidingEnabled())
							slidingMenu.setSlidingEnabled(true);
			    		search_keyword.setText("");
			    		search_keywordString = "";
			    		searchMode = false;
			    		clearBigTagChoose();
			    		bigTagCatagoryValue = 0;
			    		clearSmallTagItem();
			    		refresh_list.setVisible(false);
						progressText.setVisibility(View.GONE);
			            listItem_progress.setVisibility(View.GONE);
			    		noItemHaveFound.setVisibility(View.VISIBLE);
			    		noItemHaveFoundAlert.setVisibility(View.VISIBLE);
			    		btn_noFoundRefresh.setVisibility(View.VISIBLE);
			    		btn_noFoundRefresh.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								refreshFromMenu();
							}
						});
						Toast.makeText(getApplicationContext(), getResources().getString(R.string.search_noItem), Toast.LENGTH_SHORT).show();
					}
					else {
						userFunctions = new UserFunctions(getApplicationContext());
						boolean loginCheck = userFunctions.isUserLoggedIn(getApplicationContext());
						progressText.setVisibility(View.GONE);
			            listItem_progress.setVisibility(View.GONE);
			            HereHaveNoItemText.setVisibility(View.VISIBLE);
			            hereHaveNoItemImage.setVisibility(View.VISIBLE);
						if (loginCheck) {
							btn_HereNoItemAdd.setVisibility(View.VISIBLE);
							btn_HereNoItemAdd.setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View v) {
						    		Intent go_add_page = new Intent(ItemList.this,AddItem_tab.class);
						    		Bundle myPlace_add = new Bundle();
									myPlace_add.putDouble("latitude", double_latitude);
									myPlace_add.putDouble("longitude", double_longitude);
									myPlace_add.putSerializable("BigCatagory", bigTagCatagory);
									myPlace_add.putSerializable("SmallCatagory", smallTagCatagory);
									go_add_page.putExtras(myPlace_add);
						    		startActivity(go_add_page);
						    		overridePendingTransition(R.anim.detail_in, R.anim.storelist_leave);
								}
							});
							btn_UserRefresh.setVisibility(View.VISIBLE);
							btn_UserRefresh.setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View v) {
									refreshFromMenu();
								}
							});
					    }
						else {
							btn_HereNoItemlogin.setVisibility(View.VISIBLE);
							btn_HereNoItemlogin.setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View v) {
						    		Intent go_login_page = new Intent(ItemList.this,LoginActivity.class);
						    		startActivity(go_login_page);
						    		overridePendingTransition(R.anim.detail_in, R.anim.storelist_leave);
						    		finish();
								}
							});
							btn_UserRefresh.setVisibility(View.VISIBLE);
							btn_UserRefresh.setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View v) {
									refreshFromMenu();
								}
							});
						}
					}
				    return;
				}
			}
		}
	}
	/*Will Delete after Lazy Loading finish*/
	class GetAllItemTemp extends AsyncTask<String, Void, Boolean> {
		int httpResponseCode;
		@Override
		protected Boolean doInBackground(String... url) {
			boolean successBoolean = false;
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			//params.add(new BasicNameValuePair("action", "get_store_list"));    //set for action
			params.add(new BasicNameValuePair("page", String.valueOf(page)));
			if(searchMode) {
				String bigTagsString = "";
				if (bigTagCatagoryValue != 0) bigTagsString = String.valueOf(bigTagCatagoryValue);
				params.add(new BasicNameValuePair("keyword", search_keywordString));
				params.add(new BasicNameValuePair("MainCategory", bigTagsString));
				params.add(new BasicNameValuePair("SubCategories", smallTagsString));
			}
			else {
				params.add(new BasicNameValuePair("keyword", ""));
				params.add(new BasicNameValuePair("MainCategory", ""));
				params.add(new BasicNameValuePair("SubCategories", ""));
			}
			params.add(new BasicNameValuePair("userLatitude", Double.toString(double_latitude)));
			params.add(new BasicNameValuePair("userLongitude", Double.toString(double_longitude)));
			params.add(new BasicNameValuePair("userCountryCode", userCountryCode));
			params.add(new BasicNameValuePair("userLanguage", userLanguage));
			try{
				JSONParser jsonParser = new JSONParser(getApplicationContext());
				JSONObject json = jsonParser.makeHttpRequest("/list" ,"GET",params);
				if (json == null) {
					Log.e("JSON Object is null", "when doing background task ItemList");
					httpResponseCode = jsonParser.getHttpResponseCode();
				    return false;
				}
				store_temp = json.getJSONArray(TAG_ITEM);
				successBoolean = true;
				httpResponseCode = jsonParser.getHttpResponseCode();
			}
			catch (JSONException e) {
				e.printStackTrace();
			}
			return successBoolean;
		}
		@Override
		protected void onPostExecute(Boolean success) {
			if (success) {
				if (pullToRefresh) {
					pullToRefresh = false;
					pullToRefreshLayout.setRefreshComplete();
				}
				if (store_temp.length() < PAGE_UNIT) {
					lv.removeFooterView(listViewFoot);
					thereIsNoItem = true;
				}
			}
			else {
				if (httpResponseCode == 901) {
					setNetworkServerErrorUI(listItem_progress, progressText);    //404找不到
					return;
				}
				if (httpResponseCode == 900) {
					//連線逾時
					Toast.makeText(getApplicationContext(), getResources().getString(R.string.ConnectionTimeOut), Toast.LENGTH_SHORT).show();
					return;
				}
				if (httpResponseCode == HttpStatus.SC_NOT_FOUND) {
					if (searchMode) {
						//搜尋沒找到東西的情況
			    		search_keyword.setText("");
			    		search_keywordString = "";
			    		searchMode = false;
			    		clearBigTagChoose();
			    		bigTagCatagoryValue = 0;
			    		clearSmallTagItem();
			    		refresh_list.setVisible(false);
						progressText.setVisibility(View.GONE);
			            listItem_progress.setVisibility(View.GONE);
			    		noItemHaveFound.setVisibility(View.VISIBLE);
			    		noItemHaveFoundAlert.setVisibility(View.VISIBLE);
			    		btn_noFoundRefresh.setVisibility(View.VISIBLE);
			    		btn_noFoundRefresh.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								refreshFromMenu();
							}
						});
						Toast.makeText(getApplicationContext(), getResources().getString(R.string.search_noItem), Toast.LENGTH_SHORT).show();
					}
					else {
						userFunctions = new UserFunctions(getApplicationContext());
						boolean loginCheck = userFunctions.isUserLoggedIn(getApplicationContext());
						progressText.setVisibility(View.GONE);
			            listItem_progress.setVisibility(View.GONE);
			            HereHaveNoItemText.setVisibility(View.VISIBLE);
			            hereHaveNoItemImage.setVisibility(View.VISIBLE);
						if (loginCheck) {
							btn_HereNoItemAdd.setVisibility(View.VISIBLE);
							btn_HereNoItemAdd.setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View v) {
						    		Intent go_add_page = new Intent(ItemList.this,AddItem_tab.class);
						    		Bundle myPlace_add = new Bundle();
									myPlace_add.putDouble("latitude", double_latitude);
									myPlace_add.putDouble("longitude", double_longitude);
									myPlace_add.putSerializable("BigCatagory", bigTagCatagory);
									myPlace_add.putSerializable("SmallCatagory", smallTagCatagory);
									go_add_page.putExtras(myPlace_add);
						    		startActivity(go_add_page);
						    		overridePendingTransition(R.anim.detail_in, R.anim.storelist_leave);
								}
							});
					    }
						else {
							btn_HereNoItemlogin.setVisibility(View.VISIBLE);
							btn_HereNoItemlogin.setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View v) {
						    		Intent go_login_page = new Intent(ItemList.this,LoginActivity.class);
						    		startActivity(go_login_page);
						    		overridePendingTransition(R.anim.detail_in, R.anim.storelist_leave);
						    		finish();
								}
							});
						}
					}
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

	/*Adapter Worker*/
	private HashMap<String,String> ListAdapter(JSONObject jsonObject,int toggle) {
	    //儲存每個項目
		HashMap<String,String> maps = new HashMap<String,String>();
		if (toggle == 0) {
		    String id="",name="",distance="",bigTagsString="",scID=""
		    		,distance_raw = "",rating = "",imageUrl = ""
		    		,startTime = "",closeTime = "",status = "";
			try {
				id = jsonObject.getString("sID");
			    name = jsonObject.getString("sName");
				distance_raw = jsonObject.getString("Distance");
				rating = jsonObject.getString("Store_avg_rating");
				imageUrl = jsonObject.getString("Latest_image_path");
				startTime = jsonObject.getString("startTime");
				closeTime = jsonObject.getString("closeTime");
				status = jsonObject.getString("status");
				//Is24hr = jsonObject.getString("is24Hours");
				bigTagsString = jsonObject.getString("MainCategory");
				if (bigTagsString.equals("null"))
					bigTagsString = getResources().getString(R.string.tagDoesntSet);
				JSONArray smallTagJson = jsonObject.getJSONArray("SubCategories");
				//Log.e("smallTagLength = ", String.valueOf(smallTagJson.length()));
				scID = "";
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
				distance = DistanceText(distance_raw);
			}
			catch (JSONException e) {
				e.printStackTrace();
			}
			maps.put("sID",id);
			maps.put("sName", name);
			maps.put("bigTags", bigTagsString);
			maps.put("smallTags", scID);
			maps.put("distance", distance);
			maps.put("distance_raw", distance_raw);
			maps.put("Store_avg_rating", rating);
			maps.put("Latest_image_path", imageUrl);
			maps.put("startTime", startTime);
			maps.put("closeTime", closeTime);
			maps.put("status", status);
			//maps.put("is24Hours", Is24hr);
		}
		else if(toggle == 1) {
			String name="",mcID="";
			try {
				name = jsonObject.getString("Name");
				mcID = jsonObject.getString("mcID");
			}
			catch (JSONException e) {
				e.printStackTrace();
			}
			maps.put("Name", name);
			maps.put("mcID", mcID);
		}
		else if(toggle == 2) {
			String name="",mcID="",scID="";
			try {
				name = jsonObject.getString("Name");
				mcID = jsonObject.getString("mcID");
				scID = jsonObject.getString("scID");
			}
			catch (JSONException e) {
				e.printStackTrace();
			}
			maps.put("Name", name);
			maps.put("mcID", mcID);
			maps.put("scID", scID);
		}
        return maps;
	}
    /*Action Bar*/
	public boolean onOptionsItemSelected(MenuItem item){
    	switch(item.getItemId()){
    	case android.R.id.home:
    		int position = lv.getLastVisiblePosition();
    		if (position < 10)
    		    slidingMenu.showMenu();
    		else if (position > 15) {
    			isBackingUp = true;
    			lv.setSelection(0);
    		}
    		else {
    			isBackingUp = true;
    			lv.smoothScrollToPosition(0);
    		}
    		break;
    	case R.id.add_object:
    		Intent go_add_page = new Intent(ItemList.this,AddItem_tab.class);
    		Bundle myPlace_add = new Bundle();
			myPlace_add.putDouble("latitude", double_latitude);
			myPlace_add.putDouble("longitude", double_longitude);
			myPlace_add.putSerializable("BigCatagory", bigTagCatagory);
			myPlace_add.putSerializable("SmallCatagory", smallTagCatagory);
			go_add_page.putExtras(myPlace_add);
    		startActivity(go_add_page);
    		overridePendingTransition(R.anim.detail_in, R.anim.storelist_leave);
    		//Toast.makeText(getApplicationContext(), "經:" +double_longitude +",緯:" +double_latitude, Toast.LENGTH_SHORT).show();
    		break;
    	case R.id.login_user:
    		Intent go_login_page = new Intent(ItemList.this,LoginActivity.class);
    		startActivity(go_login_page);
    		overridePendingTransition(R.anim.detail_in, R.anim.storelist_leave);
    		finish();
    		break;
    	case R.id.logout_user:
            userLogout();
    		break;
    	case R.id.reflesh_object:
            refreshFromMenu();
    		break;
    	case R.id.reflesh_list:
    		search_keyword.setText("");
    		search_keywordString = "";
    		searchMode = false;
    		clearBigTagChoose();
    		bigTagCatagoryValue = 0;
    		clearSmallTagItem();
    		refresh_list.setVisible(false);
    		refreshFromMenu();
    		break;
    	case R.id.map_interface:
    		if (double_latitude == 0.0 && double_longitude == 0.0) {
    			Toast.makeText(getApplicationContext(), getResources().getString(R.string.itemList_goMapInterfaceError), Toast.LENGTH_SHORT).show();
    		}
    		else {
    			Intent go_map_interface = new Intent(ItemList.this, MapInterface.class);
    			Bundle myPlace = new Bundle();
    			myPlace.putDouble("latitude", double_latitude);
    			myPlace.putDouble("longitude", double_longitude);
    			myPlace.putString("tag", "all");
    			go_map_interface.putExtras(myPlace);
    			startActivity(go_map_interface);
    			overridePendingTransition(R.anim.detail_in, R.anim.storelist_leave);
    			//Toast.makeText(getApplicationContext(), "經:" +double_longitude +",緯:" +double_latitude, Toast.LENGTH_SHORT).show();
    		}
    		break;
    	case R.id.action_about:    //關於選單
			Intent go_about = new Intent(ItemList.this,About.class);
			startActivity(go_about);
			overridePendingTransition(R.anim.detail_in, R.anim.storelist_leave);
			break;
    	case R.id.action_setting:
    		Intent go_setting = new Intent(ItemList.this,Setting.class);
    		startActivityForResult(go_setting, AFTER_SETTING);
    		overridePendingTransition(R.anim.detail_in, R.anim.storelist_leave);
    		break;
    	default:
    		return super.onOptionsItemSelected(item);
    	}
		return true;
    }

	private void userLogout() {
		new AlertDialog.Builder(ItemList.this)
		.setTitle(getResources().getString(R.string.itemList_alertLogoutTitle))
		.setIcon(R.drawable.ic_launcher)
		.setMessage(getResources().getString(R.string.itemList_alertLogoutMes))
		.setPositiveButton(getResources().getString(R.string.alertDialogOkay), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				userFunctions.logoutUser(getApplicationContext());
	    		Intent intent = getIntent();
				/*重新啟動這頁，關閉後重新開啟*/
				finish();
				overridePendingTransition(0, 0);
				startActivity(intent);
			}
		})
		.setNegativeButton(getResources().getString(R.string.alertDialogCancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		})
		.show();
	}

	//依登入狀態隱藏選單元件
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem logout = menu.findItem(R.id.logout_user);
		MenuItem login = menu.findItem(R.id.login_user);
		MenuItem addItem = menu.findItem(R.id.add_object);
		refresh_list = menu.findItem(R.id.reflesh_list);
		userFunctions = new UserFunctions(getApplicationContext());
		loginCheck = userFunctions.isUserLoggedIn(getApplicationContext());
		if (loginCheck == true) {
			login.setVisible(false);
			logout.setVisible(true);
			addItem.setVisible(true);
		}
		else {
			login.setVisible(true);
			logout.setVisible(false);
			addItem.setVisible(false);
		}
		return true;
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void refreshFromMenu() {
		gpsFix();
		page = 0;
		all_item_list = new ArrayList<HashMap<String,String>>();
		ArrayAdapter<HashMap<String,String>> adapter = new ArrayAdapter<HashMap<String,String>>(this,
				R.layout.list_item, all_item_list);
		lv.setAdapter(adapter);
		adapter.notifyDataSetChanged();
		if (thereIsNoItem) {
			lv.addFooterView(listViewFoot);
			thereIsNoItem = false;
		}
		new LoadAllItem().execute();
	}

	@Override
	public void onLocationChanged(Location location) {
		sPreferences = getSharedPreferences("foodbook_pref", MODE_PRIVATE);
		Editor preEditor = sPreferences.edit();
		preEditor.putString("userLatitude", Double.toString(double_latitude));
		preEditor.putString("userLongitude", Double.toString(double_longitude));
		preEditor.commit();
		double_latitude = location.getLatitude();
		double_longitude = location.getLongitude();
	}
	@Override
	public void onProviderDisabled(String provider) {
	}
	@Override
	public void onProviderEnabled(String provider) {
	}
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Criteria fine_criteria = new Criteria();    //由GPS取得
		fine_criteria.setAccuracy(Criteria.ACCURACY_FINE);
		fineProvide = locateManager.getBestProvider(fine_criteria, false);
		Criteria network_criteria = new Criteria();    //由 WiFi/3G取得位置
		network_criteria.setAccuracy(Criteria.ACCURACY_COARSE);
		networkProvide = locateManager.getBestProvider(network_criteria, false);
	}

	@Override
	protected void onResume() {
		super.onResume();
		networkState = new NetworkState(this);
		boolean isOnline = networkState.checkInternet();
		if (isOnline && afterSplash == false) {
			setLocationFix();
			locateManager.requestLocationUpdates(networkProvide, 0, 0, this);
		}
		if (!isOnline) {
			new AlertDialog.Builder(ItemList.this)
			.setTitle(getResources().getString(R.string.splash_alertNetErrorTitle))
			.setIcon(R.drawable.ic_launcher)
			.setMessage(getResources().getString(R.string.splash_alertNetErrorMes))
			.setPositiveButton(getResources().getString(R.string.alertDialogOkay), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent intent = new Intent(ItemList.this, splash.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					intent.putExtra("EXIT", true);
					startActivity(intent);
					overridePendingTransition(0, 0);
					finish();
				}
			})
			.show();
		}
		if (!slidingMenu.isSlidingEnabled() && !afterSplash) {
			actionbar.setDisplayHomeAsUpEnabled(true);
			actionbar.setHomeButtonEnabled(true);
			slidingMenu.setSlidingEnabled(true);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (locateManager != null) locateManager.removeUpdates(this);
	}

	//離開檢查
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (slidingMenu.isMenuShowing()) {
				slidingMenu.showContent();
				return true;
			}
			if (slidingMenu.isSecondaryMenuShowing()) {
				slidingMenu.showContent();
				return true;
			}
			if (searchMode) {
	    		search_keyword.setText("");
	    		search_keywordString = "";
	    		clearBigTagChoose();
	    		bigTagCatagoryValue = 0;
                clearSmallTagItem();
	    		searchMode = false;
	    		refresh_list.setVisible(false);
				refreshFromMenu();
	    		onVibrator();
	    		return true;
			}
			new AlertDialog.Builder(ItemList.this)
			.setTitle(getResources().getString(R.string.itemList_alertExitAppTitle))
			.setIcon(R.drawable.ic_launcher)
			.setMessage(getResources().getString(R.string.itemList_alertExitAppMes))
			.setPositiveButton(getResources().getString(R.string.itemList_alertExitAppOkay), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					sprfSetting = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
					sprfSetting.edit().remove("isTheFirstRun").commit();
					sprfSetting.edit().remove("prefFacebook").commit();
					finish();
					overridePendingTransition(R.anim.finish_in, R.anim.finish_out);
				}
			})
			.setNegativeButton(getResources().getString(R.string.alertDialogCancel), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			})
			.show();
			return true;
		}
		if (keyCode == KeyEvent.KEYCODE_SEARCH) {
			if (slidingMenu.isMenuShowing()) {
				slidingMenu.showContent();
				return true;
			}
			else {
				slidingMenu.showMenu();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	//地圖位置相關(格式化)
	private String DistanceText(String distance) {
		sprfSetting = PreferenceManager.getDefaultSharedPreferences(this);
		boolean isImperialUnits = sprfSetting.getBoolean("prefDistanceUnit", false);
		double distance_original = Double.valueOf(distance);
		if(!isImperialUnits) {
			if (distance_original < 1000)
				return String.valueOf((int)distance_original) + getResources().getString(R.string.m);
			else
				return new DecimalFormat("#.00").format(distance_original/1000) + getResources().getString(R.string.km);
		}
		else {
			double distance_ft = distance_original * 3.28;
			if (distance_ft < 5280)
				return String.valueOf((int)distance_original) + getResources().getString(R.string.ft);
			else
				return new DecimalFormat("#.00").format(distance_ft/5280) + getResources().getString(R.string.mile);
		}
	}

    private ListView.OnScrollListener listview_ScrollListener = new ListView.OnScrollListener() {
		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
		}
		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			if (scrollState == SCROLL_STATE_TOUCH_SCROLL) {
				if (lv.getLastVisiblePosition() >= lv.getCount() - 2 && !thereIsNoItem) {
					btn_ReadMore.setVisibility(View.GONE);
					loadingView.setVisibility(View.VISIBLE);
					new LoadNextItem().execute();
				}
			}
			else if (scrollState == SCROLL_STATE_IDLE) {
				if (lv.getLastVisiblePosition() >= lv.getCount() - 1 && !thereIsNoItem) {
					btn_ReadMore.setVisibility(View.GONE);
					loadingView.setVisibility(View.VISIBLE);
					new LoadNextItem().execute();
				}
			}
		}
    };

	//振動
	private void onVibrator() {
		Vibrator vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
		if (vibrator == null) {
			Vibrator localVibrator = (Vibrator) this.getApplicationContext().getSystemService("vibrator");
		    vibrator = localVibrator;
		}
		vibrator.vibrate(100L);
	}

	//Bulid for search tags
	class SearchTags extends AsyncTask<String,String,String>{
		@Override
		protected String doInBackground(String... arg0) {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			//params.add(new BasicNameValuePair("action", "get_categories"));    //set for action
			try{
				JSONParser jsonParser = new JSONParser(getApplicationContext());
				JSONObject json = jsonParser.makeHttpRequest("/get_categories" ,"GET",params);
				if (json == null) {
					Log.e("JSON Object is null", "when doing getCatagories");
				    return null;
				}
				JSONArray bigTags = json.getJSONArray("MainCategories");
				JSONArray smallTags = json.getJSONArray("SubCategories");
				//getBigCatagory
				for (int i=0; i < bigTags.length() ; i++) {
					JSONObject temp = bigTags.getJSONObject(i);
					bigTagCatagory.add(ListAdapter(temp, 1));
				}
				for (int i=0; i < smallTags.length() ; i++) {
					JSONObject temp = smallTags.getJSONObject(i);
					smallTagCatagory.add(ListAdapter(temp, 2));
				}
			}
			catch(JSONException e){
				e.printStackTrace();
				//Log.d("All Items: ", json.toString());
			}
			catch (NullPointerException e) {
				Log.e("Server is error >:", "Search Tag");
				cancel(true);
			}
			return null;
		}

		protected void onPostExecute(String file_url){
			//設定ActionBar 可以按 App 圖示回到首頁
			actionbar.setDisplayHomeAsUpEnabled(true);
			actionbar.setHomeButtonEnabled(true);
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
                    prepareBigCata();
				}
			});
		}
	}

	private void prepareBigCata() {
		final RadioButton[] radioButton = new RadioButton[bigTagCatagory.size()];
		radioGroup = new RadioGroup(getApplicationContext());
		radioGroup.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
		radioGroup.setOrientation(RadioGroup.HORIZONTAL);
		for (int i=0 ; i<bigTagCatagory.size(); i++) {
			final int position = i;
			radioButton[i] = new RadioButton(getApplicationContext());
			radioButton[i].setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
			radioButton[i].setText(bigTagCatagory.get(i).get("Name"));
			radioButton[i].setTextColor(Color.BLACK);
			radioButton[i].setTextSize(24);
			radioButton[i].setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if (isChecked) {
						bigTagCatagoryValue = Integer.parseInt(bigTagCatagory.get(position).get("mcID"));
						prepareSmallCata();
						smallTags.clear();
						btn_sideClear.setVisibility(View.VISIBLE);
						//Toast.makeText(getApplicationContext(), "Check id = " +bigTagCatagoryValue, Toast.LENGTH_SHORT).show();
					}
				}
			});
			radioButton[i].setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
                    radioButton[position].startAnimation(pushEffect);
				}
			});
			radioGroup.addView(radioButton[i]);
		}
		bigCataView.addView(radioGroup);
		btn_sideClear.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				clearBigTagChoose();
				bigTagCatagoryValue = 0;
				onVibrator();
			}
		});
	}

	private void prepareSmallCata() {
		if (smallCataView != null) smallCataView.removeAllViews();
		int matchTimes = 0;
		boolean firstTime = true;
		TableRow tableRow = null;
		final ToggleButton[] toggleButton = new ToggleButton[smallTagCatagory.size()];
		for (int i= 0 ; i < smallTagCatagory.size() ; i++) {
			final int position = i;
			int mcID = Integer.valueOf(smallTagCatagory.get(i).get("mcID"));
			if (matchTimes == 2 || firstTime) {
				if(!firstTime) smallCataView.addView(tableRow, new TableLayout.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
				tableRow = new TableRow(getApplicationContext());
				tableRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
			    firstTime = false;
			    matchTimes = 0;
			}
			if (mcID == bigTagCatagoryValue) {
				toggleButton[i] = new ToggleButton(getApplicationContext());
				toggleButton[i].setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
				String text = smallTagCatagory.get(i).get("Name");
				toggleButton[i].setText(text);
				toggleButton[i].setTextOn(text);
				toggleButton[i].setTextOff(text);
				toggleButton[i].setTextSize(19);
				toggleButton[i].setTextColor(Color.BLACK);
				toggleButton[i].setBackgroundColor(Color.TRANSPARENT);
				toggleButton[i].setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						String checkedvaule = smallTagCatagory.get(position).get("scID");
						if (isChecked) {
                            smallTags.add(checkedvaule);
                            toggleButton[position].setBackgroundResource(R.drawable.detail_corner);
						}
						else{
                            smallTags.remove(checkedvaule);
                            toggleButton[position].setBackgroundColor(Color.TRANSPARENT);
						}
						smallTagsString = getSmallString();
					//Toast.makeText(getApplicationContext(), smallTagsString, Toast.LENGTH_SHORT).show();
					}
				});
				toggleButton[i].setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						toggleButton[position].startAnimation(pushEffect);
					}
				});
				tableRow.addView(toggleButton[i]);
				matchTimes++;
		    }
		}
		smallCataView.setGravity(Gravity.CENTER_HORIZONTAL);
		smallCataView.addView(tableRow, new TableLayout.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
	}

    private String getSmallString() {
    	String smallTagString;
    	if (smallTags.size() == 0) {
    		smallTagString = "";
    		return smallTagString;
    	}
    	ArrayList<Integer> arrayOfInt = new ArrayList<Integer>();
    	for (Object str : smallTags) {
    		arrayOfInt.add(Integer.parseInt((String)str));
    	}
    	Collections.sort(arrayOfInt);
    	smallTagString = arrayOfInt.toString();
    	smallTagString = smallTagString.substring(1, smallTagString.length() - 1);
    	return smallTagString;
    }

    private void clearBigTagChoose() {
    	if (radioGroup != null) radioGroup.clearCheck();
    	btn_sideClear.setVisibility(View.GONE);
    	if (smallCataView != null) smallCataView.removeAllViews();
    }

    public void setNetworkServerErrorUI(View pBar, TextView searchTxt) {
    	final View progressBar = pBar;
    	final TextView progressText = searchTxt;
    	runOnUiThread(new Runnable() {
			@Override
			public void run() {
				progressText.setVisibility(View.GONE);
		        progressBar.setVisibility(View.GONE);
		        serverError.setVisibility(View.VISIBLE);
		        btn_serverNotFound.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						finish();
					}
				});
		        btn_serverNotFound.setVisibility(View.VISIBLE);
		        actionbar.hide();
			}
		});
    }

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		try {
			ArrayList<HashMap<String, String>> adapterTemp = allItemAdapter.getDataItems();
			outState.putSerializable("listViewItem", adapterTemp);
		}
		catch (NullPointerException e) {
			outState = null;
			return;
		}
		int index = lv.getFirstVisiblePosition();
		outState.putInt("position", index);
		outState.putInt("pageLoaded", page);
		outState.putDouble("Latitude", double_latitude);
		outState.putDouble("Longitude", double_longitude);
		outState.putSerializable("bigTags", bigTagCatagory);
		outState.putSerializable("smallTags", smallTagCatagory);
		outState.putStringArrayList("smallTagsList", smallTags);
		outState.putStringArray("storeNames", storeNames);
		if (searchMode) {
			String bigTagString = "";
			if (bigTagCatagoryValue != 0)
				bigTagString = String.valueOf(bigTagCatagoryValue);
			outState.putBoolean("search", searchMode);
			outState.putString("keyword", search_keywordString);
			outState.putString("MainCategory", bigTagString);
			outState.putString("SubCategories", smallTagsString);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
        	double_latitude = savedInstanceState.getDouble("Latitude");
        	double_longitude = savedInstanceState.getDouble("Longitude");
        	searchMode = savedInstanceState.getBoolean("search", false);
        	storeNames = savedInstanceState.getStringArray("storeNames");
        	if (storeNames != null) {
        		adapterName = new ArrayAdapter<String>(getApplicationContext(), R.layout.my_list_layout, storeNames);
        	    search_keyword.setAdapter(adapterName);
        	}
        	if (searchMode) {
        		search_keywordString = savedInstanceState.getString("keyword");
        		String bigTagTemp = savedInstanceState.getString("MainCategory");
        		bigTagCatagoryValue = Integer.parseInt(bigTagTemp);
        		smallTagsString = savedInstanceState.getString("SubCategories");
        	}
        	all_item_list = (ArrayList<HashMap<String, String>>) savedInstanceState.getSerializable("listViewItem");
        	bigTagCatagory = (ArrayList<HashMap<String, String>>) savedInstanceState.getSerializable("bigTags");
        	smallTagCatagory = (ArrayList<HashMap<String, String>>) savedInstanceState.getSerializable("smallTags");
        	smallTags = savedInstanceState.getStringArrayList("smallTagsList");
        	int index = savedInstanceState.getInt("position");
        	page = savedInstanceState.getInt("pageLoaded", 0);
        	if (all_item_list != null) {
        		allItemAdapter = new AllItemAdapter(ItemList.this, all_item_list, getApplicationContext());
				lv.setAdapter(allItemAdapter);
				lv.setSelectionFromTop(index, 0);
				//new GetAllItemTemp().execute();    //Test for orientation change
        	}
        	else {
        		all_item_list = new ArrayList<HashMap<String,String>>();
        		new LoadAllItem().execute();
        	}
        	if (bigTagCatagory != null && smallTagCatagory != null && smallTags != null)
        		prepareBigCata();
        	else {
    			bigTagCatagory = new ArrayList<HashMap<String,String>>();
    			smallTagCatagory = new ArrayList<HashMap<String,String>>();
    			smallTags = new ArrayList<String>();
        		new SearchTags().execute();
        	}
        }
	}

	private void clearSmallTagItem() {
		smallTagsString = "";
		smallTags.clear();
		smallTags = new ArrayList<String>();
	}

	public void showTutorial() {
		tutorialDialog = new Dialog(this, R.style.lightbox_dialog);
		tutorialDialog.setContentView(R.layout.lightbox_dialog);
		tutorialDialog.setCancelable(false);
		tutorialDialog.setCanceledOnTouchOutside(false);
		ViewPager pager = (ViewPager) tutorialDialog.findViewById(R.id.gallery_viewPager);
		TutorialViewPager tutorialViewPager = new TutorialViewPager(this);
		pager.setAdapter(tutorialViewPager);
		final Button tutorial_button = (Button) tutorialDialog.findViewById(R.id.tutorial_button);
		tutorial_button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sprfSetting = PreferenceManager.getDefaultSharedPreferences(ItemList.this);
				Editor editor = sprfSetting.edit();
				editor.putBoolean("TutorialHasViewed", true);
				editor.commit();
				tutorialDialog.cancel();
			}
		});
		CirclePageIndicator pIndicator = (CirclePageIndicator) tutorialDialog.findViewById(R.id.gallery_indicator);
		pIndicator.setViewPager(pager);
		pIndicator.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
			    if (position == 5)
			    	tutorial_button.setVisibility(View.VISIBLE);
			    else
			    	tutorial_button.setVisibility(View.GONE);
			  //Toast.makeText(getApplicationContext(), ""+position, Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

			@Override
			public void onPageScrollStateChanged(int state) {}
		});
		tutorialDialog.show();
	}

}
