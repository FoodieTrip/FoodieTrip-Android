package com.foodietrip.android;

import org.apache.http.HttpStatus;
import org.json.JSONObject;

import com.actionbarsherlock.app.ActionBar;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.foodietrip.android.library.*;

public class RegisterActivity extends SherlockActivity {
	ActionBar actionBar;
	EditText edt_password,edt_nickName,edt_password_again;
	TextView txt_error, txt_phoneNumber;
	String phoneNumber;
	private ProgressDialog pDialog;
	boolean userHasRegistered = false;
	int gender = 3;
	Animation pushEffect;
	//JSON Node 名稱
	private static String KEY_NICKNAME = "uNickName";
	private static String KEY_EMAIL = "uEmail";
	private static String KEY_UID = "uID";
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);    //將Layout 設定成Register那頁
		//從介面取得元件
		txt_phoneNumber = (TextView) findViewById(R.id.register_phoneNumberLocked);
		edt_password = (EditText)findViewById(R.id.register_password);
		edt_password_again = (EditText)findViewById(R.id.register_password_again);
		edt_nickName = (EditText)findViewById(R.id.register_nickname);
		txt_error = (TextView)findViewById(R.id.register_error);
		//actionBar
		actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		Intent intent = this.getIntent();
		Bundle bundle = intent.getExtras();
		phoneNumber = bundle.getString("phoneNumber");
		txt_phoneNumber.setText(phoneNumber);
		pushEffect = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.image_effect);
	}

	class doTheRegisterWork extends AsyncTask<String, String, String> {
        //在開始讀取之前，顯示Progress Dialog(對話方塊)
		int httpResponseCode;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(RegisterActivity.this);
			pDialog.setMessage(getResources().getString(R.string.register_registing));
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}
		@Override
		protected String doInBackground(String... params) {
			String nickName = edt_nickName.getText().toString();
			String phoneAccount = phoneNumber;  //帳號
			String password = edt_password.getText().toString();
			UserFunctions userFunctions = new UserFunctions(getApplicationContext());
			JSONObject json = userFunctions.registerUser(nickName, phoneAccount, password, gender);
			if (json == null) {
				Log.e("JSON Object is null", "when doing background task Register Activity");
				userHasRegistered = false;
				httpResponseCode = userFunctions.getResponseCode();
			    return null;
			}
			//Log.e("Register Json = ", json.toString());
			//檢查註冊時的Response
			try {
				userHasRegistered = true;
				httpResponseCode = userFunctions.getResponseCode();
				//使用者成功註冊了
				//用SQLite儲存所有的使用者資料
				DatabaseHandler db = new DatabaseHandler(getApplicationContext());
				//JSONObject json_user = json.getJSONObject("User");
				//清除所有資料庫裡面的舊資料(進行登入動作？)
				userFunctions.logoutUser(getApplicationContext());
				db.addUser(json.getString(KEY_NICKNAME), json.getString(KEY_EMAIL), json.getString(KEY_UID), json.getString("uPhone"));
				//開啟登入後視窗
				Intent go_Item_list = new Intent(RegisterActivity.this,ItemList.class);
				//開啟之前清除其他所有View
				go_Item_list.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(go_Item_list);
				overridePendingTransition(R.anim.finish_in, R.anim.finish_out);
				finish();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
		//完成背景作業後，防止對話框跳出來
		protected void onPostExecute(String file_url) {
			//忽略掉對話框
			pDialog.dismiss();
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (userHasRegistered) {
						txt_error.setText("");
					}
					else {
						if (httpResponseCode == 900) {
							//連線逾時
							Toast.makeText(getApplicationContext(), getResources().getString(R.string.ConnectionTimeOut), Toast.LENGTH_SHORT).show();
							return;
						}
						if (httpResponseCode == HttpStatus.SC_CONFLICT) {
							Toast.makeText(getApplicationContext(), getResources().getString(R.string.register_httpResponse_409), Toast.LENGTH_SHORT).show();
						    return;
						}
						if (httpResponseCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
							Toast.makeText(getApplicationContext(), getResources().getString(R.string.httpResponse_500), Toast.LENGTH_SHORT).show();
						    return;
						}
						new AlertDialog.Builder(RegisterActivity.this)
						.setTitle("")
						.setIcon(R.drawable.ic_action_warning)
						.setMessage(getResources().getString(R.string.register_errorOccured))
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
		getSupportMenuInflater().inflate(R.menu.register, menu);
		return true;
	}
	/*ActionBar*/
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			this.overridePendingTransition(R.anim.finish_in, R.anim.finish_out);
			break;
		case R.id.register_menuButton: {
			new AlertDialog.Builder(RegisterActivity.this)
			.setTitle(getResources().getString(R.string.alertComfirmTitle))
			.setIcon(R.drawable.ic_launcher)
			.setMessage(getResources().getString(R.string.register_alertComfirmMes))
			.setPositiveButton(getResources().getString(R.string.alertDialogOkay), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String password1 = edt_password.getText().toString();
					String password2 = edt_password_again.getText().toString();
					if (edt_password.getText().toString().equals("")||
							edt_password_again.getText().toString().equals("")||
							edt_nickName.getText().toString().equals("")) {
						Toast.makeText(RegisterActivity.this, getResources().getString(R.string.register_toastEmailFormatOrEmpty), Toast.LENGTH_SHORT).show();
					}
					else if (password1.trim().length() <= 6) {
						Toast.makeText(getApplicationContext(), getResources().getString(R.string.password_lengthFixed), Toast.LENGTH_SHORT).show();
					}
					else {
						//檢查密碼和重複輸入的密碼是否相同
						if (password1.equals(password2)) {
							new doTheRegisterWork().execute();
						}
						else {
							Toast.makeText(RegisterActivity.this, getResources().getString(R.string.register_passwordNotSame), Toast.LENGTH_SHORT).show();
						}
					}
				}
			})
			.setNegativeButton(getResources().getString(R.string.alertDialogCancel), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			})
			.show();
			break;
		}
		case R.id.register_cancelButton:{
			new AlertDialog.Builder(RegisterActivity.this)
			.setTitle(getResources().getString(R.string.alertComfirmTitle))
			.setIcon(R.drawable.ic_launcher)
			.setMessage(getResources().getString(R.string.register_exitRegisterPageMes))
			.setPositiveButton(getResources().getString(R.string.alertDialogOkay), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
					RegisterActivity.this.overridePendingTransition(R.anim.finish_in, R.anim.finish_out);
				}
			})
			.setNegativeButton(getResources().getString(R.string.alertDialogCancel), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			})
			.show();
			break;
		}
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

	/*To fix the IllegalArgumentException*/
	@Override
	protected void onSaveInstanceState(Bundle outState){
		super.onSaveInstanceState(outState);
		outState.putString("phoneNumber", phoneNumber);
		try{
			pDialog.dismiss();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		if (savedInstanceState != null) {
			phoneNumber = savedInstanceState.getString("phoneNumber");
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
					Intent intent = new Intent(RegisterActivity.this, splash.class);
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
