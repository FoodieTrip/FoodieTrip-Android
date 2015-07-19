package com.foodietrip.android;

import java.io.IOException;
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
import android.content.SharedPreferences.Editor;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.foodietrip.android.library.*;

public class AddItem_tab extends SherlockFragmentActivity implements LocationListener {
	UserFunctions userFunctions;
	ActionBar actionBar;
	//SherlockActionBar
	ViewPager mViewPager;
	TabsAdapter mTabsAdapter;
	TextView tabCenter,tabText;
	//Process Dialog ���bŪ������ܮ�
	private ProgressDialog pDialog;
	private static final int MODIFY_LOCATION = 1;
	//���GPS��T
	private static final int ONE_MINUTE = 1000 * 60;
	Location currentBestLocation = null;
	LocationManager locateManager = null;
	String fineProvide,networkProvide,countryCode;
	Double double_latitude = 0.0,double_longitude = 0.0;
	int account_uID = 0;
	boolean isLocationEdited = false, neverShowAttention = false;
	SharedPreferences addItemPre, sPreferences;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addItemPre = getSharedPreferences("addItem_tmp", MODE_PRIVATE);
		mViewPager = new ViewPager(this);
		mViewPager.setId(R.id.add_item_pager);
		setContentView(mViewPager);
		//ActionBar
		actionBar = getSupportActionBar();
		//�]�wActionBar����ܼҦ�
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
	    //�]�wActionBar �i�H��App�ϥܦ^�W�@��
		actionBar.setIcon(R.drawable.ic_action_new);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        mTabsAdapter = new TabsAdapter(this, mViewPager);
		//GPS�w��ϥ�
		locateManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		Criteria fine_criteria = new Criteria();    //��GPS���o
		fine_criteria.setAccuracy(Criteria.ACCURACY_FINE);
		fineProvide = locateManager.getBestProvider(fine_criteria, false);
		Criteria network_criteria = new Criteria();    //�� WiFi/3G���o��m
		network_criteria.setAccuracy(Criteria.ACCURACY_COARSE);
		network_criteria.setPowerRequirement(Criteria.POWER_LOW);
		networkProvide = locateManager.getBestProvider(network_criteria, false);
		gpsFix();    //Request �@��GPS��m
		//�qBundle �������X ItemList �����
		Intent mapInterface = this.getIntent();
		if (!isLocationEdited) {
			Bundle myPosition = mapInterface.getExtras();
			double_latitude = myPosition.getDouble("latitude", 23.979548);    //�n��
			double_longitude = myPosition.getDouble("longitude", 120.696745);    //�g��
		}
		getMyAddress();
		//���X�ϥΪ̪�uID
		userFunctions = new UserFunctions(getApplicationContext());
		boolean loginCheck = userFunctions.isUserLoggedIn(getApplicationContext());
		if (loginCheck == true) {
			account_uID = userFunctions.getUserUid(getApplicationContext());
			//Toast.makeText(getApplicationContext(), ""+account_uID, Toast.LENGTH_SHORT).show();
		}
        setActionbarTabs();
	}

	private void setActionbarTabs() {
        mTabsAdapter.addTabs(actionBar.newTab().setText(getResources().getString(R.string.ItemTab_tabName1)).setIcon(R.drawable.ic_action_about).setTag("tab1"),AddItem.class, null, "tab1");
        mTabsAdapter.addTabs(actionBar.newTab().setText(getResources().getString(R.string.ItemTab_tabName2)).setIcon(R.drawable.ic_action_time).setTag("tab2"),AddItem_page2.class, null, "tab2");
        mTabsAdapter.addTabs(actionBar.newTab().setText(getResources().getString(R.string.ItemTab_tabName3)).setIcon(R.drawable.ic_action_storage).setTag("tab3"),AddItem_page3.class, null, "tab3");
	}

	//���oGPS��m
	public void gpsFix() {
		if (locateManager.isProviderEnabled(fineProvide)) {
			locateManager.requestLocationUpdates(networkProvide, 15000, 100, this);
			locateManager.requestLocationUpdates(fineProvide, 15000, 100, this);
			return;
		}
		else {
			locateManager.requestLocationUpdates(networkProvide, 15000, 100, this);
			Toast.makeText(getApplicationContext(), getResources().getString(R.string.gpsRequest), Toast.LENGTH_SHORT).show();
		}
	}

	//TabAdapter
	public static class TabsAdapter extends FragmentPagerAdapter implements ActionBar.TabListener,ViewPager.OnPageChangeListener {
        private final Context mContext;
        private final ActionBar mActionBar;
        private final ViewPager mViewPager;
        private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();
        private SparseArray<Fragment> map = new SparseArray<Fragment>();
        private String currentTagTab;
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
		    		currentTagTab = tag.toString();
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
        	return currentTagTab;
        }

        public void setCurrentTabByTag(String tag) {
		    for (int i = 0;i < mTabs.size();i++) {
		    	if (mTabs.get(i).tags.equals(tag)) {
		    		mViewPager.setCurrentItem(i);
		    		currentTagTab = tag.toString();
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

	//Source code from my past version
	//�b�I������G�s�W�@��Item
	class CreateNewItem extends AsyncTask<String,String,String>{
		int httpResponseCode;
		boolean success;
		@Override
		protected void onPreExecute(){
			super.onPreExecute();
			pDialog = new ProgressDialog(AddItem_tab.this);
			pDialog.setMessage(getResources().getString(R.string.ItemTab_processing));
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}
		//�b�I����@
		@Override
		protected String doInBackground(String... args) {
			//�qBundle�����o����A�åB�ন�r���int
			addItemPre = getSharedPreferences("addItem_tmp", MODE_PRIVATE);
			AddItem addItemFragment = (AddItem) mTabsAdapter.getFragment(0);
			AddItem_page2 addItemP2Fragment = (AddItem_page2) mTabsAdapter.getFragment(1);
			AddItem_page3 addItemP3Fragment = (AddItem_page3) mTabsAdapter.getFragment(2);
			View page1 = addItemFragment.getView();
			View page2 = addItemP2Fragment.getView();
			View page3 = addItemP3Fragment.getView();
			EditText input_Name = (EditText)page1.findViewById(R.id.editText_name);
			EditText input_County = (EditText)page1.findViewById(R.id.editText_county);
			EditText input_Address = (EditText)page1.findViewById(R.id.editText_address);
			EditText input_Township = (EditText)page1.findViewById(R.id.editText_township);
			EditText edt_Memo = (EditText)page2.findViewById(R.id.editText_sMemo);
			EditText edt_email = (EditText)page3.findViewById(R.id.editText_email);
			EditText edt_web = (EditText)page3.findViewById(R.id.editText_store_web);
			EditText edt_phone = (EditText)page3.findViewById(R.id.editText_phone);
			EditText edt_price = (EditText)page3.findViewById(R.id.editText_price);
			String name = input_Name.getText().toString();
			String sMemo = edt_Memo.getText().toString();
			String sCountry = input_County.getText().toString();
			String sTownship = input_Township.getText().toString();
			String sLocation = input_Address.getText().toString();
			String price = edt_price.getText().toString();
			String phone = edt_phone.getText().toString();
			String email = edt_email.getText().toString();
			String webpage = edt_web.getText().toString();
			int Is24Hours = addItemPre.getInt("is24Hours", 0);
			String startTime = addItemPre.getString("startTime", "");
			String closeTime = addItemPre.getString("closeTime", "");
			String rest_day = addItemPre.getString("RestDay", "");
			String mcID = addItemPre.getString("mcID", "");
			String scID = addItemPre.getString("scID", "");
			int sCanDelivery = addItemPre.getInt("sCanDelivery",0);
			int sCanToGo = addItemPre.getInt("sCanToGo",0);
			//Building Parameters�A�@���x�s��ƪ���Ƶ��c
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			//params.add(new BasicNameValuePair("action", "create_store"));    //set for action
			params.add(new BasicNameValuePair("uID", Integer.toString(account_uID)));
			params.add(new BasicNameValuePair("sLatitude", Double.toString(double_latitude)));    //���n
			params.add(new BasicNameValuePair("sLongitude", Double.toString(double_longitude)));    //���n
			params.add(new BasicNameValuePair("sName",name));   //���a�W��,���n
			params.add(new BasicNameValuePair("sMinCharge",price));  //�C�P
			params.add(new BasicNameValuePair("sPhone",phone));   //�q��
			params.add(new BasicNameValuePair("sLocation",sLocation));    //��Y�a�}
			params.add(new BasicNameValuePair("sTownship",sTownship));    //�m���Ϧa�}
			params.add(new BasicNameValuePair("sCountry",sCountry));    //�[�J�a�}
			params.add(new BasicNameValuePair("is24Hours", Integer.toString(Is24Hours)));    //�O���O24�p��
			params.add(new BasicNameValuePair("startTime",startTime));    //�}�l��~�ɶ�
			params.add(new BasicNameValuePair("closeTime",closeTime));    //������~�ɶ�
			params.add(new BasicNameValuePair("RestDay_ID",rest_day));    //�𮧤�
			params.add(new BasicNameValuePair("sMemo",sMemo));    //�Ƶ�
			params.add(new BasicNameValuePair("sEmail",email));    //E-mail
			params.add(new BasicNameValuePair("sURL",webpage));    //����
			params.add(new BasicNameValuePair("mcID",mcID));    //Tag Big
			params.add(new BasicNameValuePair("scID",scID));    //Tag Small
			params.add(new BasicNameValuePair("sCanDelivery",Integer.toString(sCanDelivery)));    //�i�~�e
			params.add(new BasicNameValuePair("sCanToGo",Integer.toString(sCanToGo)));    //�i�~�a
			params.add(new BasicNameValuePair("sNation", countryCode));    //set for Nation
			//���oJSON����
			//Note that create item url accepts POST method
			//String action = "create_store";    //�ϫ汹�I
			JSONParser jsonParser = new JSONParser(getApplicationContext());
			JSONObject json = jsonParser.makeHttpRequest("/create","POST",params);
			if (json == null) {
				Log.e("JSON Object is null", "when doing background task AddItem_tab");
				success = false;
			    httpResponseCode = jsonParser.getHttpResponseCode();
			    return null;
			}
			//Log.d("Create Response",json.toString());
			//�إߦ��\�A�^�춵�زM�樺��
			success = true;
			httpResponseCode = jsonParser.getHttpResponseCode();
			Intent go_item_list = new Intent(AddItem_tab.this,ItemList.class);
			go_item_list.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(go_item_list);
			overridePendingTransition(R.anim.finish_in, R.anim.finish_out);
			addItemPre = getSharedPreferences("addItem_tmp", MODE_PRIVATE);
			addItemPre.edit().clear().commit();
			finish(); //�����o��Activity
			return null;
		}
		//�����I���@�~��A�����ܮظ��X��
		protected void onPostExecute(String file_url) {
			//��������ܮ�
			pDialog.dismiss();
			if (!success) {
				if (httpResponseCode == 900) {
					//�s�u�O��
					Toast.makeText(getApplicationContext(), getResources().getString(R.string.ConnectionTimeOut), Toast.LENGTH_SHORT).show();
					return;
				}
				if (httpResponseCode == HttpStatus.SC_BAD_REQUEST) {
					Toast.makeText(getApplicationContext(), getResources().getString(R.string.createNew_httpResponse_404), Toast.LENGTH_SHORT).show();
				    return;
				}
				if (httpResponseCode == HttpStatus.SC_UNAUTHORIZED) {
					//���v���ѡA�C�ӳ���
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
		getSupportMenuInflater().inflate(R.menu.add_page, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
		case android.R.id.home:
			Intent go_back = new Intent(AddItem_tab.this,ItemList.class);
			go_back.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			addItemPre = getSharedPreferences("addItem_tmp", MODE_PRIVATE);
			addItemPre.edit().clear().commit();
			startActivity(go_back);
			overridePendingTransition(R.anim.finish_in, R.anim.finish_out);
			break;
		case R.id.add_an_object:
			addItemPre = getSharedPreferences("addItem_tmp", MODE_PRIVATE);
			AddItem addItemFragment = (AddItem) mTabsAdapter.getFragment(0);
			View page1 = addItemFragment.getView();
			AddItem_page3 addItemP3Fragment = (AddItem_page3) mTabsAdapter.getFragment(2);
			View page3 = addItemP3Fragment.getView();
			EditText edt_phone = (EditText)page3.findViewById(R.id.editText_phone);
			EditText input_Name = (EditText)page1.findViewById(R.id.editText_name);
			EditText input_County = (EditText)page1.findViewById(R.id.editText_county);
			EditText input_Address = (EditText)page1.findViewById(R.id.editText_address);
			EditText input_Township = (EditText)page1.findViewById(R.id.editText_township);
			String name = input_Name.getText().toString();
			String phone = edt_phone.getText().toString();
			String sTownship = input_Township.getText().toString();
			String sLocation = input_Address.getText().toString();
			String sCountry = input_County.getText().toString();
			String startTime = addItemPre.getString("startTime", "");
			String closeTime = addItemPre.getString("closeTime", "");
			if (double_latitude == 0.0 && double_longitude == 0.0) {
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.waitForGps), Toast.LENGTH_SHORT).show();
				break;
			}
			if (name.equals("") || sCountry.equals(getResources().getString(R.string.ItemTab_countryNull))){
					Toast.makeText(getApplicationContext(), getResources().getString(R.string.ItemTab_nameOrCountryError), Toast.LENGTH_SHORT).show();
			}
			else{
				//Toast.makeText(getApplicationContext(), "�g:" +double_longitude +",�n:" +double_latitude, Toast.LENGTH_SHORT).show();    //Just Test
				new AlertDialog.Builder(AddItem_tab.this)
				.setTitle(getResources().getString(R.string.alertComfirmTitle))
				.setIcon(R.drawable.ic_launcher)
				.setMessage(getResources().getString(R.string.ItemTab_comfirmMessage1)
						+getResources().getString(R.string.ItemTab_comfirmMessage2)+name
						+getResources().getString(R.string.ItemTab_comfirmMessage4)+phone
						+getResources().getString(R.string.ItemTab_comfirmMessage5)+sCountry+sTownship+sLocation
						+getResources().getString(R.string.ItemTab_comfirmMessage6)+startTime
						+getResources().getString(R.string.ItemTab_comfirmMessage7)+closeTime
						+getResources().getString(R.string.ItemTab_comfirmMessage9))
				.setNegativeButton(getResources().getString(R.string.alertDialogCancel), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				})
				.setPositiveButton(getResources().getString(R.string.alertDialogOkay), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//�b�I���B�z�G�s�W�@��Item
						new CreateNewItem().execute();
					}
				})
				.show();
			}
			break;
		case R.id.addItem_modifyLocation:
			if (double_latitude != 0.0 || double_longitude != 0.0) {
				Intent location_confirm = new Intent(AddItem_tab.this,MapDragger.class);
				Bundle bundle = new Bundle();
				bundle.putString("tag", "AddItem");
				bundle.putDouble("oriLatitude", double_latitude);
				bundle.putDouble("oriLongitude", double_longitude);
				location_confirm.putExtras(bundle);
				startActivityForResult(location_confirm, MODIFY_LOCATION);
				overridePendingTransition(R.anim.detail_in, R.anim.storelist_leave);
			}
			else {
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.waitForGps), Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.addItem_refresh:
			isLocationEdited = false;
			gpsFix();
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	//�ϥΪ̰e�^�F��m��T
	protected void onActivityResult(int requestCode,int resultCode,Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case MODIFY_LOCATION:
			if (resultCode == RESULT_OK) {
				isLocationEdited = true;
				locateManager.removeUpdates(this);
				double_latitude = data.getDoubleExtra("sLatitude", 0.0);
				double_longitude = data.getDoubleExtra("sLongitude", 0.0);
				if (double_latitude == null || double_latitude == 0.0 ||
						double_longitude == null || double_longitude == 0.0) {
					gpsFix();    //�p�G�S�����o��ƨ���N���s�w��
					Toast.makeText(getApplicationContext(), getResources().getString(R.string.ItemTab_dataGetError), Toast.LENGTH_SHORT).show();
				}
				//Toast.makeText(getApplicationContext(), "�g��:" +double_longitude +"�n��:" +double_latitude, Toast.LENGTH_LONG).show();
			}
			else if (resultCode == RESULT_CANCELED) {
				return;
			}
			break;
		default:
			break;
		}
		return;
	}
	/*To fix the IllegalArgumentException�G��V���D*/
	@Override
	protected void onSaveInstanceState(Bundle outState){
		super.onSaveInstanceState(outState);
		try{
			pDialog.dismiss();
		}
		catch(Exception e){
		}
		outState.putString("CurrentTab", mTabsAdapter.getCurrentTabTag());
		getSupportFragmentManager().putFragment(outState, AddItem.class.getName(), mTabsAdapter.getFragment(0));
		getSupportFragmentManager().putFragment(outState, AddItem_page2.class.getName(), mTabsAdapter.getFragment(1));
		getSupportFragmentManager().putFragment(outState, AddItem_page3.class.getName(), mTabsAdapter.getFragment(2));
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		if (savedInstanceState != null) {
			AddItem addItem = (AddItem) getSupportFragmentManager().getFragment(savedInstanceState, AddItem.class.getName());
			if (addItem != null)
				mTabsAdapter.setFragment(0, addItem);
			AddItem_page2 addItemP2 = (AddItem_page2) getSupportFragmentManager().getFragment(savedInstanceState, AddItem_page2.class.getName());
			if (addItemP2 != null)
				mTabsAdapter.setFragment(1, addItemP2);
			AddItem_page3 addItemP3 = (AddItem_page3) getSupportFragmentManager().getFragment(savedInstanceState, AddItem_page3.class.getName());
			if (addItemP3 != null)
				mTabsAdapter.setFragment(2, addItemP3);
			mTabsAdapter.setCurrentTabByTag(savedInstanceState.getString("CurrentTab"));
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		double tempLatitude = location.getLatitude();
		double tempLongitude = location.getLongitude();
		if (isBetterLocation(location, currentBestLocation) == true) {
			currentBestLocation = location;
			double_latitude = location.getLatitude();
			double_longitude = location.getLongitude();
		}
		Geocoder geocoder = new Geocoder(getBaseContext());
		List<Address> addresses = null;
		if (tempLatitude != 0.0 || tempLongitude != 0.0) {
			try {
				addresses = geocoder.getFromLocation(tempLatitude, tempLongitude, 1);
				if (addresses.size() > 0)
				    countryCode = addresses.get(0).getCountryCode();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	@Override
	public void onProviderDisabled(String provider) {
	}
	@Override
	public void onProviderEnabled(String provider) {
	}
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Criteria fine_criteria = new Criteria();    //��GPS���o
		fine_criteria.setAccuracy(Criteria.ACCURACY_FINE);
		fineProvide = locateManager.getBestProvider(fine_criteria, false);
		Criteria network_criteria = new Criteria();    //�� WiFi/3G���o��m
		network_criteria.setAccuracy(Criteria.ACCURACY_COARSE);
		networkProvide = locateManager.getBestProvider(network_criteria, false);
	}
	//�O�_����n�� Location �ˬd
	protected boolean isBetterLocation(Location location,Location currentBestLocation) {
	    //Determine whether one Location reading is better than the current Location fix
		//@Param location The new location that you want to evaluate
		//@Param currentBestLocation The current Location fix, to which you want to compare the new one
		if (currentBestLocation == null) {
			//A new location is always better than no location
			return true;
		}

		//Check whether the new location fix is newer or older
		long timeDelta = location.getTime() - currentBestLocation.getTime();
		boolean isSegnificantlyNewer = timeDelta > ONE_MINUTE;
		boolean isSegnificantlyOlder = timeDelta < ONE_MINUTE;
		boolean isNewer = timeDelta > 0;

		//if it's been more than one minute since the current location,use the new location
		//because the user has likely moved
		if (isSegnificantlyNewer) {
			return true;
		}
		//if the new location is more than one minuted older, it must be worst
		else if (isSegnificantlyOlder) {
			return false;
		}
		//Check whether the new location fix is more or less accurate
		int accuracyDelta = (int)(location.getAccuracy() - currentBestLocation.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSegnificantlyLessAccurate = accuracyDelta > 200;
		//Check if the old and new location are from the same provider
		boolean isFromSameProvider = isSameProvider(location.getProvider(),currentBestLocation.getProvider());
		//Determine location quality using a combination of timeliness and accuracy
		if (isMoreAccurate) {
			return true;
		}
		else if (isNewer && !isLessAccurate) {
			return true;
		}
		else if (isNewer && !isSegnificantlyLessAccurate && isFromSameProvider) {
			return true;
		}
		return false;
	}
	/*�ˬd���Provider�O���O�P�@��*/
	private boolean isSameProvider(String provider1, String provider2) {
		if (provider1 == null) {
			return false;
		}
		return provider1.equals(provider2);
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
					Intent intent = new Intent(AddItem_tab.this, splash.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					intent.putExtra("EXIT", true);
					startActivity(intent);
					overridePendingTransition(0, 0);
					finish();
				}
			})
			.show();
		}
		//��ܤW���w�쪺��m
		if (!isLocationEdited) {
			Location lastLocation = locateManager.getLastKnownLocation(fineProvide);
			currentBestLocation = lastLocation;
			if (lastLocation != null) {
				double_latitude = lastLocation.getLatitude();
				double_longitude = lastLocation.getLongitude();
			}
			else {
				Location lastLocation_network = locateManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				currentBestLocation = lastLocation_network;
				if (lastLocation_network != null) {
					double_latitude = lastLocation_network.getLatitude();
					double_longitude = lastLocation_network.getLongitude();
				}
			}
		}
	}
	@Override
	protected void onStop() {
		super.onStop();
		locateManager.removeUpdates(this);
	}
    /*���o�ѦҦa�}*/
    public void getMyAddress() {
    		try {
    		Geocoder geocoder = new Geocoder(getBaseContext());
    		List<Address> addresses = null;
    		if (double_latitude != 0.0 && double_longitude != 0.0){
    			//Log.e("address lat and lon", "is not 0.0");
    			addresses = geocoder.getFromLocation(double_latitude, double_longitude, 1);
    			String subLocality = addresses.get(0).getSubLocality();
    			if (subLocality == null) subLocality = "";
    			addItemPre = getSharedPreferences("addItem_tmp", MODE_PRIVATE);
    			Editor preWriter = addItemPre.edit();    //�]�w�nEditor
    			preWriter.putString("sampleAdd1", addresses.get(0).getAddressLine(0));
    			preWriter.putString("sampleAdd2", addresses.get(0).getLocality());
    			preWriter.putString("sampleAdd3", subLocality +addresses.get(0).getThoroughfare() + addresses.get(0).getFeatureName() +getResources().getString(R.string.ItemTab_addressNo));
    			preWriter.putString("sampleAdd4", addresses.get(0).getAdminArea());
    			preWriter.commit();    //�g�J�I
    		}
    		else {
    			sPreferences = getSharedPreferences("foodbook_pref", MODE_PRIVATE);
    			String spre_latitude = sPreferences.getString("userLatitude", "0.0");
    			String spre_longitude = sPreferences.getString("userLongitude", "0.0");
    			double lat = Double.valueOf(spre_latitude);
    			double lon = Double.valueOf(spre_longitude);
        		if (lat != 0.0 && lon != 0.0){
        			addresses = geocoder.getFromLocation(lat, lon, 1);
        			String subLocality = addresses.get(0).getSubLocality();
        			if (subLocality == null) subLocality = "";
        			addItemPre = getSharedPreferences("addItem_tmp", MODE_PRIVATE);
        			Editor preWriter = addItemPre.edit();    //�]�w�nEditor
        			preWriter.putString("sampleAdd1", addresses.get(0).getAddressLine(0));
        			preWriter.putString("sampleAdd2", addresses.get(0).getLocality());
        			preWriter.putString("sampleAdd3", subLocality +addresses.get(0).getThoroughfare() + addresses.get(0).getFeatureName() +getResources().getString(R.string.ItemTab_addressNo));
        			preWriter.putString("sampleAdd4", addresses.get(0).getAdminArea());
        			preWriter.commit();    //�g�J�I
        		}
    		}
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    }

	//���}����
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
		    new AlertDialog.Builder(AddItem_tab.this)
		    .setTitle(getResources().getString(R.string.ItemTab_cancelToEdit))
		    .setIcon(R.drawable.ic_launcher)
		    .setMessage(getResources().getString(R.string.ItemTab_cancelToEditMessage))
		    .setPositiveButton(getResources().getString(R.string.alertDialogOkay), new DialogInterface.OnClickListener() {
			    @Override
			    public void onClick(DialogInterface dialog, int which) {
					addItemPre = getSharedPreferences("addItem_tmp", MODE_PRIVATE);
					addItemPre.edit().clear().commit();
				    finish();
				    AddItem_tab.this.overridePendingTransition(R.anim.finish_in, R.anim.finish_out);
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

}
