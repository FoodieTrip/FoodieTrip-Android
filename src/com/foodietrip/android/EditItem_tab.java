package com.foodietrip.android;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.foodietrip.android.library.*;


public class EditItem_tab extends SherlockFragmentActivity {
	UserFunctions userFunctions;
	int account_uID = 0;
  	private ProgressDialog pDialog;
	ViewPager mViewPager;
	TabsAdapter mTabsAdapter;
	TextView tabCenter,tabText;
  	//sID
  	String sid,current_country;
  	//ActionBar
  	ActionBar actionBar;
  	SharedPreferences editItemPre;
  	private static final String TAG_PID = "sID";
  	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		editItemPre = getSharedPreferences("editItem_tmp", MODE_PRIVATE);
		//取出使用者的uID
		userFunctions = new UserFunctions(getApplicationContext());
		boolean loginCheck = userFunctions.isUserLoggedIn(getApplicationContext());
		if (loginCheck == true) {
			account_uID = userFunctions.getUserUid(getApplicationContext());
		}
		mViewPager = new ViewPager(this);
		mViewPager.setId(R.id.edit_item_pager);
		setContentView(mViewPager);
		actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setIcon(R.drawable.ic_action_edit);
  		actionBar.setDisplayHomeAsUpEnabled(true);
  		actionBar.setHomeButtonEnabled(true);
  		mTabsAdapter = new TabsAdapter(this, mViewPager);
		//從Intent取得Item資訊
		Intent i = this.getIntent();
		Bundle bundle = i.getExtras();
		//從Intent取得Item sid 和 目前選擇的直轄市縣
		sid = bundle.getString(TAG_PID);
		setActionBarTabs();
  	}

  	public void setActionBarTabs() {
  		mTabsAdapter.addTabs(actionBar.newTab().setText(getResources().getString(R.string.ItemTab_tabName1)).setIcon(R.drawable.ic_action_about),EditItem.class, null, "tab1");
        mTabsAdapter.addTabs(actionBar.newTab().setText(getResources().getString(R.string.ItemTab_tabName2)).setIcon(R.drawable.ic_action_time),EditItem_page2.class, null, "tab2");
        mTabsAdapter.addTabs(actionBar.newTab().setText(getResources().getString(R.string.ItemTab_tabName3)).setIcon(R.drawable.ic_action_storage),EditItem_page3.class, null, "tab3");
  	}

	//TabAdapter
	public static class TabsAdapter extends FragmentPagerAdapter implements ActionBar.TabListener,ViewPager.OnPageChangeListener {
        private final Context mContext;
        private final ActionBar mActionBar;
        private final ViewPager mViewPager;
        private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();
        private SparseArray<Fragment> map = new SparseArray<Fragment>();
        private String currentTabTag;
        static final class TabInfo {
        	private final Class<?> clss;
        	private final Bundle args;
        	private final String tags;
        	TabInfo(Class<?> _cClass, Bundle _args, String _tags) {
				clss = _cClass;
				args = _args;
				tags = _tags;
			}
        }
		public TabsAdapter(SherlockFragmentActivity activity, ViewPager pager) {
			super(activity.getSupportFragmentManager());
			mContext = activity;
			mActionBar = activity.getSupportActionBar();
			mViewPager = pager;
			mViewPager.setAdapter(this);
			mViewPager.setOnPageChangeListener(this);
			mViewPager.setOffscreenPageLimit(2);    //Access the behind page
		}

		public void addTabs(ActionBar.Tab tab,Class<?> clss,Bundle args, String tags) {
			TabInfo info = new TabInfo(clss, args, tags);
			tab.setTag(tags);
			tab.setTabListener(this);
			mTabs.add(info);
			mActionBar.addTab(tab);
			notifyDataSetChanged();
		}

		@Override
		public void onPageScrollStateChanged(int state) {}
		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
		@Override
		public void onPageSelected(int position) {
			mActionBar.setSelectedNavigationItem(position);
		}
		@Override
		public void onTabSelected(Tab tab,FragmentTransaction ft) {
		    Object tag = tab.getTag();
		    for (int i = 0;i < mTabs.size();i++) {
		    	if (mTabs.get(i).tags  == tag) {
		    		mViewPager.setCurrentItem(i);
		    		currentTabTag = tag.toString();
		    	}
		    }
		}
		@Override
		public void onTabUnselected(Tab tab,FragmentTransaction ft) {}
		@Override
		public void onTabReselected(Tab tab,FragmentTransaction ft) {}
		@Override
		public Fragment getItem(int position) {
			TabInfo info = mTabs.get(position);
			Fragment fragment = Fragment.instantiate(mContext, info.clss.getName(), info.args);
			map.put(position, fragment);
			return fragment;
		}
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			map.remove(position);
			super.destroyItem(container, position, object);
		}
		public Fragment getFragment(int position){
			return map.get(position);
		}
        public String getCurrentTabTag() {
        	return currentTabTag;
        }
        public void setCurrentTabByTag(String tag) {
		    for (int i = 0;i < mTabs.size();i++) {
		    	if (mTabs.get(i).tags.equals(tag)) {
		    		mViewPager.setCurrentItem(i);
		    		currentTabTag = tag.toString();
		    	}
		    }
        }
        public void setFragment(int position, Fragment fragment) {
        	map.put(position, fragment);
        }
		@Override
		public int getCount() {
			return mTabs.size();
		}
	}

  	/*在背景藉由 AsyncTask方法，儲存修改過的內容*/
  	class SaveItemDetails extends AsyncTask<String,String,String>{
  	    int httpResponseCode;
  	    boolean success;
  		@Override
  		protected void onPreExecute(){
  			super.onPreExecute();
  			pDialog = new ProgressDialog(EditItem_tab.this);
  			pDialog.setMessage(getResources().getString(R.string.editItemTab_saving));
  			pDialog.setIndeterminate(false);
  			pDialog.setCancelable(true);
  			pDialog.show();
  		}
  		//儲存資料
		@Override
		protected String doInBackground(String... args) {
			//從Bundle 之中取得資訊
			editItemPre = getSharedPreferences("editItem_tmp", MODE_PRIVATE);
			EditItem editItem = (EditItem) mTabsAdapter.getFragment(0);
			EditItem_page2 editItemP2 = (EditItem_page2) mTabsAdapter.getFragment(1);
			EditItem_page3 editItemP3 = (EditItem_page3) mTabsAdapter.getFragment(2);
			View page1 = editItem.getView();
			View page2 = editItemP2.getView();
			View page3 = editItemP3.getView();
			EditText edt_county = (EditText)page1.findViewById(R.id.editText_county_editItem);
			EditText edt_township = (EditText)page1.findViewById(R.id.editText_township_editItem);
			EditText edt_location = (EditText)page1.findViewById(R.id.editText_address_editItem);
			EditText edt_Memo = (EditText)page2.findViewById(R.id.editText_sMemo2);
			EditText edt_email = (EditText)page3.findViewById(R.id.editText_email_editItem);
			EditText edt_web = (EditText)page3.findViewById(R.id.editText_store_web_editItem);
			EditText edt_price = (EditText)page3.findViewById(R.id.editText_price_editItem);
			EditText edt_phone = (EditText)page3.findViewById(R.id.editItem_phone);
            String price,phone,sCountry,sTownship,sLocation,startTime,closeTime,RestDay,sMemo,sEmail,sURL,sDili,sTogo;
            sTownship = edt_township.getText().toString();
            sLocation = edt_location.getText().toString();
            sMemo = edt_Memo.getText().toString();
            sEmail = edt_email.getText().toString();
            price = edt_price.getText().toString();
            phone = edt_phone.getText().toString();
            sURL = edt_web.getText().toString();
            sCountry = edt_county.getText().toString();
            startTime = editItemPre.getString("startTime", "");
            closeTime = editItemPre.getString("closeTime", "");
            String store_status = editItemPre.getString("status", "active");
            String is24Hours = Integer.toString(editItemPre.getInt("is24Hours", 0));
            RestDay = editItemPre.getString("RestDay_ID_org", "");
			String mcID = editItemPre.getString("mcID", "");
			String scID = editItemPre.getString("scID", "");
            sDili = Integer.toString(editItemPre.getInt("sCanDelivery", 0));
            sTogo = Integer.toString(editItemPre.getInt("sCanToGo", 0));
            //Log.d("sID = ", ""+sid);
			//Building Parameters，一個儲存資料的資料結構
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			//params.add(new BasicNameValuePair("action", "updateItem"));    //set for action
			params.add(new BasicNameValuePair("id", sid));    //Update必要
			params.add(new BasicNameValuePair("uID", Integer.toString(account_uID)));
			params.add(new BasicNameValuePair("sMinCharge", price));  //低銷
			params.add(new BasicNameValuePair("sPhone", phone));   //電話
			params.add(new BasicNameValuePair("sCountry", sCountry));    //直轄市縣
			params.add(new BasicNameValuePair("sTownship", sTownship));    //鄉鎮市區地址
			params.add(new BasicNameValuePair("sLocation", sLocation));    //後墜地址
			params.add(new BasicNameValuePair("is24Hours", is24Hours));
		    params.add(new BasicNameValuePair("startTime", startTime));    //開始營業時間
			params.add(new BasicNameValuePair("closeTime", closeTime));    //結束營業時間
			params.add(new BasicNameValuePair("RestDay_ID", RestDay));    //休息日
			params.add(new BasicNameValuePair("sMemo", sMemo));    //備註
    		params.add(new BasicNameValuePair("sEmail", sEmail));    //E-mail
    		params.add(new BasicNameValuePair("sURL", sURL));    //網站
    		params.add(new BasicNameValuePair("mcID",mcID));    //Tag Big
    		params.add(new BasicNameValuePair("scID",scID));    //Tag Small
    		params.add(new BasicNameValuePair("status",store_status));    //Tag Small
    		params.add(new BasicNameValuePair("sCanDelivery", sDili));    //可外送
    		params.add(new BasicNameValuePair("sCanToGo", sTogo));    //可外帶
			//藉由Http Request 發送修改資訊
			//Notice that update item url accepts POST method
    		//String action = "updateItem";    //救急措施
    		JSONParser jsonParser = new JSONParser(getApplicationContext());
			JSONObject json = jsonParser.makeHttpRequest("/update" ,"POST",params);
			if (json == null) {
				Log.e("JSON Object is null", "when doing background task EditItem Tab");
				success = false;
				httpResponseCode = jsonParser.getHttpResponseCode();
			    return null;
			}
			success = true;
			httpResponseCode = jsonParser.getHttpResponseCode();
			//Log.d("Edit Item Detial", json.toString());
			//成功更新！
			//Intent go_item_list = new Intent(EditItem_tab.this,ItemList.class);
			Intent i = getIntent();
			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			setResult(RESULT_OK, i);
			//startActivity(go_item_list);
			overridePendingTransition(R.anim.finish_in, R.anim.finish_out);
			editItemPre = getSharedPreferences("editItem_tmp", MODE_PRIVATE);
			editItemPre.edit().clear().commit();    //清空
			finish();
			return null;
		}
		//完成背景作業後，防止對話框跳出來
		protected void onPostExecute(String file_url) {
		    //忽略掉對話框
			pDialog.dismiss();
			if (!success) {
				if (httpResponseCode == 900) {
					//連線逾時
					Toast.makeText(getApplicationContext(), getResources().getString(R.string.ConnectionTimeOut), Toast.LENGTH_SHORT).show();
					return;
				}
				if (httpResponseCode == HttpStatus.SC_BAD_REQUEST) {
					Toast.makeText(getApplicationContext(), getResources().getString(R.string.editItem_httpResponse_404), Toast.LENGTH_SHORT).show();
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
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.edit_page, menu);
		return true;
	}
  	@Override
  	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
		case android.R.id.home:
			new AlertDialog.Builder(EditItem_tab.this)
			.setTitle(getResources().getString(R.string.editItemTab_alertAskForExitTitle))
			.setMessage(getResources().getString(R.string.editItemTab_alertAskForExitMes))
			.setPositiveButton(getResources().getString(R.string.alertDialogOkay), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					editItemPre = getSharedPreferences("editItem_tmp", MODE_PRIVATE);
					editItemPre.edit().clear().commit();    //清空
					setResult(RESULT_CANCELED);
					finish();
					EditItem_tab.this.overridePendingTransition(R.anim.finish_in, R.anim.finish_out);
				}
			})
			.setNegativeButton(getResources().getString(R.string.alertDialogCancel), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			})
			.show();
			break;
		case R.id.edit_object_save:
			//執行儲存變更的動作
			editItemPre = getSharedPreferences("editItem_tmp", MODE_PRIVATE);
			EditItem editItem = (EditItem) mTabsAdapter.getFragment(0);
			EditItem_page3 editItemP3 = (EditItem_page3) mTabsAdapter.getFragment(2);
			View page1 = editItem.getView();
			View page3 = editItemP3.getView();
			EditText edt_township = (EditText)page1.findViewById(R.id.editText_township_editItem);
			EditText edt_location = (EditText)page1.findViewById(R.id.editText_address_editItem);
			EditText edt_county = (EditText)page1.findViewById(R.id.editText_county_editItem);
			EditText edt_phone = (EditText)page3.findViewById(R.id.editItem_phone);
			String name = editItemPre.getString("sName", "");
			String phone = edt_phone.getText().toString();
			String sTownship = edt_township.getText().toString();
			String sLocation = edt_location.getText().toString();
			String sCountry = edt_county.getText().toString();
			String startTime = editItemPre.getString("startTime", "");
			String closeTime = editItemPre.getString("closeTime", "");
			new AlertDialog.Builder(EditItem_tab.this)
			.setTitle(getResources().getString(R.string.alertComfirmTitle))
			.setIcon(R.drawable.ic_launcher)
			.setMessage(getResources().getString(R.string.editItemTab_confirmMessage)+name
					+getResources().getString(R.string.ItemTab_comfirmMessage4)+phone
					+getResources().getString(R.string.ItemTab_comfirmMessage5)+sCountry+sTownship+sLocation
					+getResources().getString(R.string.ItemTab_comfirmMessage6)+startTime
					+getResources().getString(R.string.ItemTab_comfirmMessage7)+closeTime)
			.setNegativeButton(getResources().getString(R.string.alertDialogCancel), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			})
			.setPositiveButton(getResources().getString(R.string.alertDialogOkay), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					//在背景處理：新增一個Item
					new SaveItemDetails().execute();
				}
			})
			.show();
			break;
		case R.id.edit_marker_drag:
			Intent intent = new Intent(EditItem_tab.this,MapDragger.class);
			Bundle bundle = new Bundle();
			bundle.putString("sid", sid);
			bundle.putString("tag", "EditItem");
			intent.putExtras(bundle);
			startActivity(intent);
			overridePendingTransition(R.anim.detail_in, R.anim.storelist_leave);
			finish();
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
  		return true;
  	}

	//離開提示
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
		    new AlertDialog.Builder(EditItem_tab.this)
		    .setTitle(getResources().getString(R.string.ItemTab_cancelToEdit))
		    .setIcon(R.drawable.ic_launcher)
		    .setMessage(getResources().getString(R.string.ItemTab_cancelToEditMessage))
		    .setPositiveButton(getResources().getString(R.string.alertDialogOkay), new DialogInterface.OnClickListener() {
			    @Override
			    public void onClick(DialogInterface dialog, int which) {
					editItemPre = getSharedPreferences("editItem_tmp", MODE_PRIVATE);
					editItemPre.edit().clear().commit();    //清空
					setResult(RESULT_CANCELED);
				    finish();
				    EditItem_tab.this.overridePendingTransition(R.anim.finish_in, R.anim.finish_out);
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
	return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		try{
			pDialog.dismiss();
		}
		catch(Exception e){
		}
		outState.putString("CurrentTab", mTabsAdapter.currentTabTag);
		getSupportFragmentManager().putFragment(outState, EditItem.class.getName(), mTabsAdapter.getFragment(0));
		getSupportFragmentManager().putFragment(outState, EditItem_page2.class.getName(), mTabsAdapter.getFragment(1));
		getSupportFragmentManager().putFragment(outState, EditItem_page3.class.getName(), mTabsAdapter.getFragment(2));
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		if (savedInstanceState != null) {
			EditItem editItem = (EditItem) getSupportFragmentManager().getFragment(savedInstanceState, EditItem.class.getName());
			if (editItem != null)
				mTabsAdapter.setFragment(0, editItem);
			EditItem_page2 editItemP2 = (EditItem_page2) getSupportFragmentManager().getFragment(savedInstanceState, EditItem_page2.class.getName());
			if (editItemP2 != null)
				mTabsAdapter.setFragment(1, editItemP2);
			EditItem_page3 editItemP3 = (EditItem_page3) getSupportFragmentManager().getFragment(savedInstanceState, EditItem_page3.class.getName());
			if (editItemP3 != null)
				mTabsAdapter.setFragment(2, editItemP3);
			mTabsAdapter.setCurrentTabByTag(savedInstanceState.getString("CurrentTab"));
		}
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
					Intent intent = new Intent(EditItem_tab.this, splash.class);
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

}
