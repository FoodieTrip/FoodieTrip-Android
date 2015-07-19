package com.foodietrip.android;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.foodietrip.android.library.DatabaseHandler;
import com.foodietrip.android.library.JSONParser;
import com.foodietrip.android.library.UserFunctions;
import com.viewpagerindicator.TitlePageIndicator;

public class Modify_userData extends SherlockActivity{
	ActionBar actionBar;
    ViewPager mViewPager;
    TitlePageIndicator titlePageIndicator;
    UserFunctions userFunctions;
    String uID;
    TextView phoneNumPage1, phoneNumPage2;
    EditText edt_email, edt_nickName, edt_newPassword, edt_newPasswordAgain;
	RadioGroup radGroup_sex;
	RadioButton radBtn_female, radBtn_male, radBtn_xgender;
	int gender = 3, page;
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.modify_user);
		actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		mViewPager = (ViewPager) findViewById(R.id.modify_pager);
		titlePageIndicator = (TitlePageIndicator) findViewById(R.id.modify_indicator);
		titlePageIndicator.setTextColor(Color.BLACK);
		titlePageIndicator.setSelectedColor(Color.BLACK);
		userFunctions = new UserFunctions(getApplicationContext());
		boolean isLogined = userFunctions.isUserLoggedIn(getApplicationContext());
		if (!isLogined) {
			Toast.makeText(getApplicationContext(), getResources().getString(R.string.modify_userNotLogin), Toast.LENGTH_SHORT).show();
			finish();
		}
		uID = String.valueOf(userFunctions.getUserUid(getApplicationContext()));
		setUpViewPager();
	}

	private void setUpViewPager() {
		ModifyDataPager modifyDataPager = new ModifyDataPager(this);
		mViewPager.setAdapter(modifyDataPager);
		mViewPager.setOffscreenPageLimit(2);
		titlePageIndicator.setViewPager(mViewPager);
		titlePageIndicator.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				page = position;
			}
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
			@Override
			public void onPageScrollStateChanged(int state) {}
		});
		phoneNumPage1 = (TextView) modifyDataPager.findViewById(0, R.id.modify_phoneNumberLocked);
		phoneNumPage2 = (TextView) modifyDataPager.findViewById(1, R.id.modify_p2_phoneNumberLocked);
		edt_email = (EditText) modifyDataPager.findViewById(0, R.id.modify_email);
		edt_nickName = (EditText) modifyDataPager.findViewById(0, R.id.modify_nickname);
		radGroup_sex = (RadioGroup) modifyDataPager.findViewById(0, R.id.modify_radiogroup);
		radBtn_female = (RadioButton) modifyDataPager.findViewById(0, R.id.modify_female);
		radBtn_male = (RadioButton) modifyDataPager.findViewById(0, R.id.modify_male);
		radBtn_xgender = (RadioButton) modifyDataPager.findViewById(0, R.id.modify_xgender);
		edt_newPassword = (EditText) modifyDataPager.findViewById(1, R.id.modify_password);
		edt_newPasswordAgain = (EditText) modifyDataPager.findViewById(1, R.id.modify_passwordAgain);
		radGroup_sex.setOnCheckedChangeListener(radioGroup_listener);
		radBtn_female.setOnClickListener(radio_animation);
		radBtn_male.setOnClickListener(radio_animation);
		radBtn_xgender.setOnClickListener(radio_animation);
		GetUserInformation getUserInformation = new GetUserInformation();
		getUserInformation.execute(uID);
	}

	private RadioButton.OnClickListener radio_animation = new RadioButton.OnClickListener() {
		@Override
		public void onClick(View v) {
			Animation pushEffect = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.image_effect);
			switch (v.getId()) {
			case R.id.modify_female:
				radBtn_female.startAnimation(pushEffect);
				break;
            case R.id.modify_male:
				radBtn_male.startAnimation(pushEffect);
				break;
            case R.id.modify_xgender:
				radBtn_xgender.startAnimation(pushEffect);
				break;
			}
		}
	};

	private RadioGroup.OnCheckedChangeListener radioGroup_listener = new RadioGroup.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			switch (checkedId) {
			case R.id.modify_female:
				gender = 0;
				break;
            case R.id.modify_male:
				gender = 1;
				break;
            case R.id.modify_xgender:
				gender = 2;
				break;
			}
		}
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent go_back = new Intent(Modify_userData.this,ItemList.class);
			go_back.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(go_back);
			overridePendingTransition(R.anim.finish_in, R.anim.finish_out);
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
		case android.R.id.home:
			Intent go_back = new Intent(Modify_userData.this,ItemList.class);
			go_back.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(go_back);
			overridePendingTransition(R.anim.finish_in, R.anim.finish_out);
			finish();
			break;
		case R.id.modify_send:
			if (page == 1) {
				String pw_temp = edt_newPassword.getText().toString();
				String pwAgain_temp = edt_newPasswordAgain.getText().toString();
				if (pw_temp.equals(pwAgain_temp)) {
					comfirmUserCurrentPassword();
				}
				else {
					Toast.makeText(getApplicationContext(), getResources().getString(R.string.register_passwordNotSame), Toast.LENGTH_SHORT).show();
				}
			}
			else if (page == 0) {
				String nickName_temp = edt_nickName.getText().toString();
				String email_temp = edt_email.getText().toString();
				boolean IsEmailOk = isEmailValid(email_temp);
				if (nickName_temp.trim().length() == 0 || email_temp.trim().length() == 0) {
					Toast.makeText(getApplicationContext(), getResources().getString(R.string.register_toastEmailFormatOrEmpty), Toast.LENGTH_SHORT).show();
				}
				else if (!IsEmailOk) {
					Toast.makeText(getApplicationContext(), getResources().getString(R.string.email_format_isinValid), Toast.LENGTH_SHORT).show();
				}
				else {
					comfirmUserCurrentPassword();
				}
			}
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	//檢查E-mail格式是否正確
	public static boolean isEmailValid(String email) {
		boolean isValid = false;
		String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
		CharSequence inputStr = email;
		Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(inputStr);
		if (matcher.matches()) {
			isValid = true;
		}
		return isValid;
	}

	private void comfirmUserCurrentPassword() {
		final Dialog comfirmUserPw = new Dialog(Modify_userData.this);
		comfirmUserPw.setTitle(getResources().getString(R.string.Register_phoneShortButton));
		comfirmUserPw.setContentView(R.layout.modify_enterpw);
		final EditText edt_pw = (EditText) comfirmUserPw.findViewById(R.id.modify_pwComfirm);
		Button btn_okay = (Button) comfirmUserPw.findViewById(R.id.modify_okay);
		Button btn_cancel = (Button) comfirmUserPw.findViewById(R.id.modify_cancel);
		btn_okay.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String pw_temp = edt_pw.getText().toString();
				UpdateUserData updateUserData = new UpdateUserData(page);
				updateUserData.execute(pw_temp);
				comfirmUserPw.dismiss();
			}
		});
		btn_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				comfirmUserPw.dismiss();
			}
		});
		comfirmUserPw.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.modify_user, menu);
		return true;
	}

	public class ModifyDataPager extends PagerAdapter {
        public static final int MODIFY_DATA = 0;
        public static final int MODIFY_PASSWORD = 1;
        private final String[] mPageTitles;
        private final List<View> mPageViews;

        public ModifyDataPager(Context _context) {
        	LayoutInflater inflater = LayoutInflater.from(_context);
        	mPageTitles = _context.getResources().getStringArray(R.array.modify_title);
        	mPageViews = new ArrayList<View>(mPageTitles.length);
        	mPageViews.add(inflater.inflate(R.layout.modify_page1, null));
        	mPageViews.add(inflater.inflate(R.layout.modify_page2, null));
        }
		@Override
		public int getCount() {
			return mPageTitles.length;
		}
		@Override
		public CharSequence getPageTitle(int position) {
			return mPageTitles[position];
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			View view = mPageViews.get(position);
			container.addView(view);
			return view;
		}
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}
		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view.equals(object);
		}
		public View findViewById(int position, int id) {
			return mPageViews.get(position).findViewById(id);
		}
	}

	class GetUserInformation extends AsyncTask<String, Integer, Void> {
        private ProgressDialog pDialog;
        int httpReponseCode, original_genderCode = 3;
        boolean success;
        String original_email = "", original_nickname = "", phoneNumber = "";
		public GetUserInformation() {
		}

        @Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(Modify_userData.this);
			pDialog.setMessage(getResources().getString(R.string.modify_getUserData));
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}
		@Override
		protected Void doInBackground(String... uid) {
			String hostUrl = "http://192.241.211.104/~proposal/api/index.php/user?a=a";
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("id", uid[0]));
			JSONParser jsonParser = new JSONParser(getApplicationContext());
			JSONObject json = jsonParser.httpRequestRest(hostUrl, "GET", params);
			if (json == null) {
				Log.e("JSONObject Get User Information have problem", "");
				success = false;
				httpReponseCode = jsonParser.getHttpResponseCode();
				return null;
			}
			success = true;
			httpReponseCode = jsonParser.getHttpResponseCode();
			try {
				phoneNumber = json.getString("uPhone");
				phoneNumber = phoneNumber.replace("886", "0");
				original_email = json.getString("uEmail");
				original_nickname = json.getString("uNickName");
				original_genderCode = Integer.parseInt(json.getString("uGender"));
			}
			catch (JSONException e) {
				e.printStackTrace();
			}
			return null;
		}
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if (success) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						pDialog.dismiss();
						edt_email.setText(original_email);
						edt_nickName.setText(original_nickname);
						phoneNumPage1.setText(phoneNumber);
						phoneNumPage2.setText(phoneNumber);
						gender = original_genderCode;
						switch (original_genderCode) {
						case 0:
							radBtn_female.setChecked(true);
							break;
						case 1:
							radBtn_male.setChecked(true);
							break;
						case 2:
							radBtn_xgender.setChecked(true);
							break;
						}
					}
				});
			}
			else {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						pDialog.dismiss();
						if (httpReponseCode == 900) {
							//連線逾時
							Toast.makeText(getApplicationContext(), getResources().getString(R.string.ConnectionTimeOut), Toast.LENGTH_SHORT).show();
							return;
						}
						if (httpReponseCode == HttpStatus.SC_NOT_FOUND) {
							Toast.makeText(getApplicationContext(), getResources().getString(R.string.getUserData_httpResponse_404), Toast.LENGTH_SHORT).show();
						    return;
						}
						if (httpReponseCode == HttpStatus.SC_UNAUTHORIZED) {
							//授權失敗，每個都有
							Toast.makeText(getApplicationContext(), getResources().getString(R.string.login_httpResponse_401), Toast.LENGTH_SHORT).show();
							return;
						}
						if (httpReponseCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
							Toast.makeText(getApplicationContext(), getResources().getString(R.string.httpResponse_500), Toast.LENGTH_SHORT).show();
						    return;
						}

					}
				});
			}
		}
	}
	class UpdateUserData extends AsyncTask<String, Integer, Void> {
        private final int MODE_UPDATE = 0;
        private final int MODE_NEWPASSWORD = 1;
        private ProgressDialog pDialog;
        int mode, httpResponseCode;
        boolean success;
		public UpdateUserData(int page) {
        	mode = page;
        }
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(Modify_userData.this);
			pDialog.setMessage(getResources().getString(R.string.modify_isUpdating));
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}
		@Override
		protected Void doInBackground(String... pass) {
			String newer_email = "",newer_nickName = "", newer_gender = "";
			String hostUrl = "http://192.241.211.104/~proposal/api/index.php/user/update";
			String hostUrlPw = "http://192.241.211.104/~proposal/api/index.php/user/change_pw";
			if (mode == MODE_UPDATE) {
				newer_email = edt_email.getText().toString();
				newer_nickName = edt_nickName.getText().toString();
				newer_gender = String.valueOf(gender);
			    List<NameValuePair> params = new ArrayList<NameValuePair>();
			    params.add(new BasicNameValuePair("id", uID));
			    params.add(new BasicNameValuePair("password", pass[0]));
			    params.add(new BasicNameValuePair("nickname", newer_nickName));
			    params.add(new BasicNameValuePair("mail", newer_email));
			    params.add(new BasicNameValuePair("gender", newer_gender));
			    JSONParser jsonParser = new JSONParser(getApplicationContext());
				JSONObject json = jsonParser.httpRequestRest(hostUrl, "POST", params);
				if (json == null) {
					Log.e("Error when Updating user information.", "");
					httpResponseCode = jsonParser.getHttpResponseCode();
					success = false;
					return null;
				}
				httpResponseCode = jsonParser.getHttpResponseCode();
				success = true;
				userFunctions = new UserFunctions(getApplicationContext());
				userFunctions.logoutUser(getApplicationContext());    //清除本機資料
				//寫入SQLite
				DatabaseHandler db = new DatabaseHandler(getApplicationContext());
				try {
					db.addUser(json.getString("uNickName"), json.getString("uEmail"), uID, json.getString("uPhone"));
				}
				catch (JSONException e) {
					e.printStackTrace();
				}
			}
			else if (mode == MODE_NEWPASSWORD) {
				String newPassword = "";
				newPassword = edt_newPassword.getText().toString();
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("id", uID));
				params.add(new BasicNameValuePair("password", pass[0]));
				params.add(new BasicNameValuePair("new_password", newPassword));
				JSONParser jsonParser = new JSONParser(getApplicationContext());
				JSONObject json = jsonParser.httpRequestRest(hostUrlPw, "POST", params);
				if (json == null) {
					Log.e("Error when Updating user password", "");
					httpResponseCode = jsonParser.getHttpResponseCode();
					success = false;
					return null;
				}
				httpResponseCode = jsonParser.getHttpResponseCode();
				success = true;
			}
			return null;
		}
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if (success) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						pDialog.dismiss();
						Toast.makeText(getApplicationContext(), getResources().getString(R.string.modify_success), Toast.LENGTH_SHORT).show();
						Intent goBack = new Intent(Modify_userData.this, ItemList.class);
						goBack.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(goBack);
						overridePendingTransition(R.anim.finish_in, R.anim.finish_out);
						finish();
					}
				});
			}
			else {
			    runOnUiThread(new Runnable() {
					@Override
					public void run() {
						pDialog.dismiss();
						if (httpResponseCode == 900) {
							//連線逾時
							Toast.makeText(getApplicationContext(), getResources().getString(R.string.ConnectionTimeOut), Toast.LENGTH_SHORT).show();
							return;
						}
						if (httpResponseCode == HttpStatus.SC_NOT_FOUND) {
							Toast.makeText(getApplicationContext(), getResources().getString(R.string.modify_unsuccess), Toast.LENGTH_SHORT).show();
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
				});
			}
		}
	}
}
