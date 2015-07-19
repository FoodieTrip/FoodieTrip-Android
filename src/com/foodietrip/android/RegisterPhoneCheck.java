package com.foodietrip.android;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.foodietrip.android.library.JSONParser;
import com.foodietrip.android.library.NetworkState;

public class RegisterPhoneCheck extends SherlockActivity{
	ActionBar actionBar;
	Button btn_ComfirmPhone;
	EditText edt_phone;
	String shortCode = "", phoneFromServer = "";
	long currentTime;
	boolean suspended = false;
	SharedPreferences sPreferences;
	MakeButtonDisable makeButtonDisable;
	@SuppressLint("InlinedApi")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register_phone_check);
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2)
		    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		else
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
		actionBar = getSupportActionBar();
        actionBar.setIcon(R.drawable.ic_action_phone);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		btn_ComfirmPhone = (Button) findViewById(R.id.register_phoneButton);
		edt_phone = (EditText) findViewById(R.id.register_phoneEditText);
		checkButtonStatus();
		btn_ComfirmPhone.setOnClickListener(listener);
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(edt_phone, InputMethodManager.SHOW_IMPLICIT);    //自動打開鍵盤
	}

	private void checkButtonStatus() {
		if (makeButtonDisable != null) {
			if (!makeButtonDisable.isCancelled()
				    && makeButtonDisable.getStatus().equals(AsyncTask.Status.RUNNING)) {
				makeButtonDisable.cancel(true);
			}
		}
		sPreferences = getSharedPreferences("foodbook_pref", MODE_PRIVATE);
		currentTime = sPreferences.getLong("smsCurrentTime", 0);
		suspended = sPreferences.getBoolean("isSuspended", false);
		if (currentTime != 0 || suspended) {
			btn_ComfirmPhone.setEnabled(false);
			makeButtonDisable = new MakeButtonDisable(currentTime);
			makeButtonDisable.execute();
		}
	}

	private Button.OnClickListener listener = new Button.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (edt_phone.getText().toString().trim().length() > 9
					&& edt_phone.getText().toString().trim().length() < 12
					&& edt_phone.getText().toString().startsWith("0")) {
				String phoneNumber = edt_phone.getText().toString();
				GetShortCode getShortCode = new GetShortCode(phoneNumber);
				getShortCode.execute(phoneNumber);
				suspended = true;
				sPreferences = getSharedPreferences("foodbook_pref", MODE_PRIVATE);
				sPreferences.edit().putBoolean("isSuspended", true).commit();
				btn_ComfirmPhone.setEnabled(false);
				makeButtonDisable = new MakeButtonDisable(currentTime);
				makeButtonDisable.execute();
			}
			else {
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.Register_phoneNumberEmpty), Toast.LENGTH_SHORT).show();
			}
		}
	};

	private void comfirmShortCodeDialog(final String phone) {
		final Dialog shortCodeComfirm = new Dialog(RegisterPhoneCheck.this);
		shortCodeComfirm.setTitle(getResources().getString(R.string.Register_phoneShortButton));
		shortCodeComfirm.setContentView(R.layout.register_phone_shortcode);
		final EditText edt_shortCode = (EditText) shortCodeComfirm.findViewById(R.id.register_shortCodeEditText);
		Button btn_shortButton = (Button) shortCodeComfirm.findViewById(R.id.register_comfirmShortCode);
		//驗證
		btn_shortButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String phoneFromNet = phoneFromServer.substring(3);
				String phoneFromPhone = phone.substring(1);
				String shortCodeFromUser = edt_shortCode.getText().toString();
				if (phoneFromNet.equals(phoneFromPhone) && shortCodeFromUser.equals(shortCode)) {
					Intent go_register = new Intent(RegisterPhoneCheck.this, RegisterActivity.class);
					Bundle bundle = new Bundle();
					bundle.putString("phoneNumber", phone);
					go_register.putExtras(bundle);
					startActivity(go_register);
					overridePendingTransition(R.anim.detail_in, R.anim.storelist_leave);
					shortCodeComfirm.dismiss();
				}
				else {
				    Toast.makeText(getApplicationContext(), getResources().getString(R.string.Register_shortCodeError), Toast.LENGTH_SHORT).show();
				}
			}
		});
		shortCodeComfirm.show();
	}

	class GetShortCode extends AsyncTask<String, Integer, String> {
        String phoneNumberOnCellphone;
        int httpResponseCode;
        boolean success = false;
		public GetShortCode(String phone) {
        	phoneNumberOnCellphone = phone;
        }
		@Override
		protected String doInBackground(String... messages) {
			String url = "http://192.241.211.104/~proposal/api/index.php/user/twilio?a=a";
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("phone", messages[0]));
			JSONParser jsonParser = new JSONParser(getApplicationContext());
			JSONObject json = jsonParser.httpRequestRest(url, "GET", params);
			if (json == null) {
				Log.e("JSON Object is null", "when doing background request ShortCode");
				httpResponseCode = jsonParser.getHttpResponseCode();
				success = false;
				return null;
			}
			//Log.e("Register JSON =>", json.toString());
			httpResponseCode = jsonParser.getHttpResponseCode();
			success = true;
			try {
				shortCode = json.getString("Short_Code");
				phoneFromServer = json.getString("Phone");
			}
			catch (JSONException e) {
				e.printStackTrace();
			}
			return null;
		}
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (success) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						//Toast.makeText(getApplicationContext(), "ShortCode="+shortCode+"\nPhone From Net="+phoneFromServer, Toast.LENGTH_LONG).show();
						comfirmShortCodeDialog(phoneNumberOnCellphone);
					}
				});
			}
			else {
				boolean errorOccur = false;
				if (httpResponseCode == HttpStatus.SC_BAD_REQUEST
						|| httpResponseCode == HttpStatus.SC_CONFLICT
						|| httpResponseCode == HttpStatus.SC_INTERNAL_SERVER_ERROR
						|| httpResponseCode == HttpStatus.SC_UNAUTHORIZED
						|| httpResponseCode == 900) {
					errorOccur = true;
				}
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (httpResponseCode == 900) {
							//連線逾時
							Toast.makeText(getApplicationContext(), getResources().getString(R.string.ConnectionTimeOut), Toast.LENGTH_SHORT).show();
							return;
						}
						if (httpResponseCode == HttpStatus.SC_BAD_REQUEST) {
					        Toast.makeText(getApplicationContext(), getResources().getString(R.string.getShrotCode_httpResponse_400), Toast.LENGTH_SHORT).show();
						    return;
						}
						if (httpResponseCode == HttpStatus.SC_CONFLICT) {
							Toast.makeText(getApplicationContext(), getResources().getString(R.string.Register_phoneDoubled), Toast.LENGTH_SHORT).show();
							return;
						}
						if (httpResponseCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
							Toast.makeText(getApplicationContext(), getResources().getString(R.string.httpResponse_500), Toast.LENGTH_SHORT).show();
						    return;
						}
						if (httpResponseCode == HttpStatus.SC_UNAUTHORIZED) {
							//授權失敗，每個都有
							Toast.makeText(getApplicationContext(), getResources().getString(R.string.login_httpResponse_401), Toast.LENGTH_SHORT).show();
							return;
						}
					}
				});
				if (makeButtonDisable != null && errorOccur) {
					if (!makeButtonDisable.isCancelled()
						    && makeButtonDisable.getStatus().equals(AsyncTask.Status.RUNNING)) {
						makeButtonDisable.cancel(true);
						btn_ComfirmPhone.setEnabled(true);    //make button enable
						sPreferences = getSharedPreferences("foodbook_pref", MODE_PRIVATE);
						sPreferences.edit().putBoolean("isSuspended", false).commit();
						sPreferences.edit().remove("smsCurrentTime").commit();
					}
				}
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
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
	protected void onPause() {
		super.onPause();
		if (makeButtonDisable != null) {
			if (!makeButtonDisable.isCancelled()
				    && makeButtonDisable.getStatus().equals(AsyncTask.Status.RUNNING)) {
				makeButtonDisable.cancel(true);
			}
		}
		sPreferences = getSharedPreferences("foodbook_pref", MODE_PRIVATE);
		sPreferences.edit().putBoolean("isSuspended", suspended).commit();
	}

	@Override
	protected void onResume() {
		super.onResume();
		checkButtonStatus();
		NetworkState networkState = new NetworkState(this);
		boolean isOnline = networkState.checkInternet();
		if (!isOnline) {
			new AlertDialog.Builder(RegisterPhoneCheck.this)
			.setTitle(getResources().getString(R.string.splash_alertNetErrorTitle))
			.setIcon(R.drawable.ic_launcher)
			.setMessage(getResources().getString(R.string.splash_alertNetErrorMes))
			.setPositiveButton(getResources().getString(R.string.alertDialogOkay), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent intent = new Intent(RegisterPhoneCheck.this, splash.class);
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

	class MakeButtonDisable extends AsyncTask<Long,Integer,Long> {
        long programStartTime;
		public MakeButtonDisable(long _programStartTime) {
        	programStartTime = _programStartTime;
        }
		@Override
		protected Long doInBackground(Long... params) {
			long currentTime = 300 * 1000000;
			while (programStartTime <= currentTime) {
				programStartTime++;
				if (programStartTime%1000000 == 0)
					publishProgress((int)programStartTime/3000000);
			}
			return null;
		}
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			final String txt_head = getResources().getString(R.string.shortCode_freezeTime);
			final int processInt = 100 - values[0];
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					btn_ComfirmPhone.setText(txt_head+processInt);
				}
			});
		}
		@Override
		protected void onPostExecute(Long result) {
			super.onPostExecute(result);
			sPreferences = getSharedPreferences("foodbook_pref", MODE_PRIVATE);
			sPreferences.edit().remove("smsCurrentTime").commit();
			suspended = false;
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					btn_ComfirmPhone.setEnabled(true);
					btn_ComfirmPhone.setText(getResources().getString(R.string.Register_phoneCheckButton));
				}
			});
		}
		@SuppressLint("NewApi")
		@Override
		protected void onCancelled(Long result) {
			if(Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
				super.onCancelled(result);
				sPreferences = getSharedPreferences("foodbook_pref", MODE_PRIVATE);
				sPreferences.edit().putLong("smsCurrentTime", programStartTime).commit();
			}
			else {
				super.onCancelled();
				sPreferences = getSharedPreferences("foodbook_pref", MODE_PRIVATE);
				sPreferences.edit().putLong("smsCurrentTime", programStartTime).commit();
			}
		}
	}
}
