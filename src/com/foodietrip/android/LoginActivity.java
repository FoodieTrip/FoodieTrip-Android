package com.foodietrip.android;

import org.apache.http.HttpStatus;
import org.json.JSONObject;

import com.actionbarsherlock.app.ActionBar;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

import com.actionbarsherlock.app.SherlockActivity;
import com.foodietrip.android.library.*;

public class LoginActivity extends SherlockActivity {
	ActionBar actionBar;
	EditText edt_account,edt_password;
	Button btnToRegister, btn_forgetPw;
	TextView txt_error;
	CheckBox cb_showPw;
	private ProgressDialog pDialog;
	//JSON Node �W��
	private static String KEY_NICKNAME = "uNickName";
	private static String KEY_EMAIL = "uEmail";
	private static String KEY_UID = "uID";
	boolean userHasLogined = false;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);    //�NLayout �]�w��Login����
		//ActionBar
		actionBar = getSupportActionBar();
		actionBar.setIcon(R.drawable.ic_action_person);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		//�Ѥ������o����
		edt_account = (EditText)findViewById(R.id.login_account);
		edt_password = (EditText)findViewById(R.id.login_password);
		btnToRegister = (Button)findViewById(R.id.btn_toRegister);
		btn_forgetPw = (Button) findViewById(R.id.btn_forgetPassword);
		txt_error = (TextView)findViewById(R.id.login_error);
		cb_showPw = (CheckBox) findViewById(R.id.login_showPassword);
		//�]�w��ť
		btnToRegister.setOnClickListener(listener);
		btn_forgetPw.setOnClickListener(listener);
		cb_showPw.setOnClickListener(listener);
		btn_forgetPw.setVisibility(View.GONE);
		cb_showPw.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked)
					edt_password.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
				else
					edt_password.setInputType(129);
			}
		});
		edt_password.setOnEditorActionListener(done_listener);
	}

	private OnEditorActionListener done_listener = new OnEditorActionListener() {
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if (actionId == EditorInfo.IME_ACTION_DONE) {
				if(edt_account.getText().toString().equals("") ||
						edt_password.getText().toString().equals("")) {
					Toast.makeText(getApplicationContext(), getResources().getString(R.string.login_plzCheckEmailFormat), Toast.LENGTH_SHORT).show();
				}
				else {
					new doTheLoginWork().execute();
				}
			}
			return false;
		}
	};

	private Button.OnClickListener listener = new Button.OnClickListener(){
		@Override
		public void onClick(View v) {
			Animation pushEffect = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.image_effect);
			switch (v.getId()) {
			case R.id.btn_toRegister:
				btnToRegister.startAnimation(pushEffect);
				Intent go_register = new Intent(LoginActivity.this,RegisterPhoneCheck.class);
				startActivity(go_register);
				overridePendingTransition(R.anim.detail_in, R.anim.storelist_leave);
				break;
			case R.id.btn_forgetPassword:
				btn_forgetPw.startAnimation(pushEffect);
				new AlertDialog.Builder(LoginActivity.this)
				.setTitle(getResources().getString(R.string.mapdragger_alertCautionTitle))
				.setMessage(getResources().getString(R.string.forgot_sendMessage))
				.setIcon(R.drawable.ic_action_warning)
				.setNegativeButton(getResources().getString(R.string.alertDialogCancel), new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {}
				})
				.setPositiveButton(getResources().getString(R.string.forgot_toCalling), new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Uri uri = Uri.parse("tel:+14807252617");
						Intent goCall = new Intent(Intent.ACTION_DIAL, uri);
						startActivity(goCall);
					}
				})
				.show();
				break;
			case R.id.login_showPassword:
				cb_showPw.startAnimation(pushEffect);
				break;
			}
		}
	};

	class doTheLoginWork extends AsyncTask<String, String, String> {
		int stateCode = 0;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(LoginActivity.this);
			pDialog.setMessage(getResources().getString(R.string.login_nowLogining));
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}

		@Override
		protected String doInBackground(String... params) {
			String phone = edt_account.getText().toString();
			String password = edt_password.getText().toString();
			UserFunctions userFunctions = new UserFunctions(getApplicationContext());
			JSONObject json = userFunctions.loginUser(phone, password);
			if (json == null) {
				Log.e("JSON Object is null", "when doing background task LoginTask");
				userHasLogined = false;
				stateCode = userFunctions.getResponseCode();
			    return null;
			}
			//Log.e("json  user detail = ", json.toString());    //Log uses
			//�ˬd�n�J��Response
			try {
				stateCode = userFunctions.getResponseCode();
				userHasLogined = true;
				//�ϥΪ̦��\�n�J�F
				//SQLite �x�s�ϥΪ̪��ԲӸ��
				DatabaseHandler db = new DatabaseHandler(getApplicationContext());
				//JSONObject json_user = json.getJSONObject("User");
				//Log.e("json user data = ", json_user.toString());
				//�M����Ʈw���e���Ҧ����
				userFunctions.logoutUser(getApplicationContext());
				db.addUser(json.getString(KEY_NICKNAME), json.getString(KEY_EMAIL), json.getString(KEY_UID), json.getString("uPhone"));
				//�}�ҵn�J�����
				Intent go_item_list = new Intent(LoginActivity.this,ItemList.class);
				//�b�}�ҵn�J�������e���M������L��View
				go_item_list.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(go_item_list);
				overridePendingTransition(R.anim.finish_in, R.anim.finish_out);
				finish();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			return null;
		}
		//�����I���@�~��A�N��ܮ�����
		protected void onPostExecute(String file_url) {
			//��������ܮ�
			pDialog.dismiss();
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					//Toast.makeText(getApplicationContext(), "Response Code =>" +stateCode, Toast.LENGTH_LONG).show();
					if (userHasLogined) {
						txt_error.setText("");
						UserFunctions userFunctions = new UserFunctions(getApplicationContext());
						String nick_name = userFunctions.getUserName(getApplicationContext());
						Toast.makeText(getApplicationContext(), getResources().getString(R.string.login_welcomeMessage1) +nick_name +getResources().getString(R.string.login_welcomeMessage2),Toast.LENGTH_SHORT).show();
						//int uid = userFunctions.getUserUid(getApplicationContext());
						//String useremail = userFunctions.getUserEmail(getApplicationContext());
						//String userPhone = userFunctions.getUserPhone(getApplicationContext());
						//Toast.makeText(getApplicationContext(), userPhone, Toast.LENGTH_SHORT).show();
					}
					else {
						if (stateCode == 900) {
							//�s�u�O��
							Toast.makeText(getApplicationContext(), getResources().getString(R.string.ConnectionTimeOut), Toast.LENGTH_SHORT).show();
							return;
						}
						if (stateCode == HttpStatus.SC_UNAUTHORIZED) {
							//���v���ѡA�C�ӳ���
							Toast.makeText(getApplicationContext(), getResources().getString(R.string.login_httpResponse_401), Toast.LENGTH_SHORT).show();
							return;
						}
						if (stateCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
							Toast.makeText(getApplicationContext(), getResources().getString(R.string.httpResponse_500), Toast.LENGTH_SHORT).show();
						    return;
						}
						if (stateCode == HttpStatus.SC_NOT_FOUND) {
							Toast.makeText(getApplicationContext(), getResources().getString(R.string.login_httpResponse_404), Toast.LENGTH_SHORT).show();
						}
						new AlertDialog.Builder(LoginActivity.this)
						.setTitle("")
						.setIcon(R.drawable.ic_action_warning)
						.setMessage(getResources().getString(R.string.login_errorMessage))
						.setPositiveButton(getResources().getString(R.string.alertDialogOkay), new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {}
					    })
					    .show();
					}
				}
			});
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	/*ActionBar*/
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent go_back = new Intent(LoginActivity.this,ItemList.class);
			go_back.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(go_back);
			overridePendingTransition(R.anim.finish_in, R.anim.finish_out);
			finish();
			break;
		case R.id.login_menuButton:
			if(edt_account.getText().toString().equals("") ||
					edt_password.getText().toString().equals("")) {
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.login_plzCheckEmailFormat), Toast.LENGTH_SHORT).show();
			}
			else {
				new doTheLoginWork().execute();
			}
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent go_back = new Intent(LoginActivity.this,ItemList.class);
			startActivity(go_back);
			overridePendingTransition(R.anim.finish_in, R.anim.finish_out);
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	/*To fix the IllegalArgumentException*/
	@Override
	protected void onSaveInstanceState(Bundle outState){
		super.onSaveInstanceState(outState);
		try{
			pDialog.dismiss();
		}
		catch(Exception e){
			e.printStackTrace();
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
					Intent intent = new Intent(LoginActivity.this, splash.class);
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
