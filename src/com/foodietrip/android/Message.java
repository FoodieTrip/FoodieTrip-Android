package com.foodietrip.android;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RatingBar.OnRatingBarChangeListener;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.foodietrip.android.library.JSONParser;
import com.foodietrip.android.library.LazyAdapter;
import com.foodietrip.android.library.NetworkState;
import com.foodietrip.android.library.ReportTask;
import com.foodietrip.android.library.UserFunctions;

public class Message extends SherlockActivity{
	ListView listView;
	Button btn_Add;
	EditText userMessage;
	TextView noAnyMessage,avgRank, userRatingTime, userAlreadyMessage;
	RatingBar avgRantingBar, userRatingBar;
	ActionBar actionBar;
	ArrayList<HashMap<String, String>> all_message,user_message;
	LinearLayout rankLayout,userLayout;
	JSONArray all_message_raw,user_message_raw;
	UserFunctions userFunctions;
	LazyAdapter lazyAdapter;
	View horiLine;
	double distence;
	private ProgressDialog pDialog;
	private ProgressBar progressBar;
	float userRationFloat;
	int userID;
	String sid,avgFloat;
	boolean isUserLogged = false, isSuccessed = false, haveMessage = false, userHaveMessage = false;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.message);
		actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		userFunctions = new UserFunctions(getApplicationContext());
		isUserLogged = userFunctions.isUserLoggedIn(getApplicationContext());
		//從Intent取得Item資訊
		Intent i = this.getIntent();
		Bundle bundle = i.getExtras();
		sid = bundle.getString("sID");
		distence = bundle.getDouble("distence");
		//Get Widget from UI
		listView = (ListView) findViewById(R.id.message_listview);
		btn_Add = (Button) findViewById(R.id.addAMessage);
		progressBar = (ProgressBar) findViewById(R.id.message_loading);
		avgRank = (TextView) findViewById(R.id.message_avgRank);
		avgRantingBar = (RatingBar) findViewById(R.id.message_avgRankBar);
		noAnyMessage = (TextView) findViewById(R.id.message_noAnyMessage);
		rankLayout = (LinearLayout) findViewById(R.id.message_ranklayout);
		userLayout = (LinearLayout) findViewById(R.id.myself_meslayout);
		userAlreadyMessage = (TextView) findViewById(R.id.message_myselfMessage);
		userRatingTime = (TextView) findViewById(R.id.message_myselfTime);
		userRatingBar = (RatingBar) findViewById(R.id.message_myselfRating);
		horiLine = (View) findViewById(R.id.message_horizontalline);
		if (isUserLogged) {
			userID = userFunctions.getUserUid(getApplicationContext());
			btn_Add.setVisibility(View.VISIBLE);
			if (distence >= 500.0) btn_Add.setEnabled(false);
		}
		else
			userID = 0;
		btn_Add.setOnClickListener(buttonListener);
		listView.setOnItemLongClickListener(long_listener);
		if (savedInstanceState == null) {
			all_message = new ArrayList<HashMap<String,String>>();
			user_message = new ArrayList<HashMap<String,String>>();
			GetAllMessage getAllMessage = new GetAllMessage(progressBar, listView, noAnyMessage, avgRank, avgRantingBar);
			getAllMessage.execute();
		}
		listView.setOverScrollMode(ListView.OVER_SCROLL_ALWAYS);
		listView.setSmoothScrollbarEnabled(true);
	}

	private ListView.OnItemLongClickListener long_listener = new ListView.OnItemLongClickListener() {
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View v,
				int position, long id) {
			reportInvalidMessage(position);
			return true;
		}
	};

	private Button.OnClickListener buttonListener = new Button.OnClickListener() {
		@Override
		public void onClick(View v) {
			submitMessage();
		}
	};

	private void reportInvalidMessage(final int position) {
		final String[] items = new String[] {getResources().getString(R.string.report_invalid_message)};
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, items);
		new AlertDialog.Builder(Message.this)
		.setAdapter(adapter, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String uID = String.valueOf(userID);
				ReportTask reportTask = new ReportTask(getApplicationContext(), 6, uID, sid);
				reportTask.addMessage(all_message.get(position).get("Messages"));
				reportTask.execute();
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.Report_error_toast), Toast.LENGTH_SHORT).show();
			}
		})
		.show();
	}

	private void submitMessage() {
		final Dialog submitMessage = new Dialog(Message.this);
		submitMessage.setTitle(getResources().getString(R.string.add_aMessage));
		submitMessage.setContentView(R.layout.message_add);
		final String rt1 = getResources().getString(R.string.add_aMessage_rt1);
		final String rt2 = getResources().getString(R.string.add_aMessage_rt2);
		final String rt3 = getResources().getString(R.string.add_aMessage_rt3);
		final String rt4 = getResources().getString(R.string.add_aMessage_rt4);
		final String rt5 = getResources().getString(R.string.add_aMessage_rt5);
		final TextView ratingText = (TextView) submitMessage.findViewById(R.id.addMessage_ratingText);
		RatingBar userRating = (RatingBar) submitMessage.findViewById(R.id.addMessage_rating);
		userRating.setStepSize((float) 1.0);
		userRating.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {
			@Override
			public void onRatingChanged(RatingBar ratingBar, float rating,
					boolean fromUser) {
				userRationFloat = rating;
				ratingText.setVisibility(View.VISIBLE);
				if (rating > 0.0 && rating <= 1.0) ratingText.setText(rt1);
				if (rating > 1.0 && rating <= 2.0) ratingText.setText(rt2);
				if (rating > 2.0 && rating <= 3.0) ratingText.setText(rt3);
				if (rating > 3.0 && rating <= 4.0) ratingText.setText(rt4);
				if (rating > 4.0) ratingText.setText(rt5);
			}
		});
		userMessage = (EditText) submitMessage.findViewById(R.id.addMessage_edittext);
		Button btn_cancel = (Button) submitMessage.findViewById(R.id.AddMessage_cancel);
		Button btn_okay = (Button) submitMessage.findViewById(R.id.AddMessage_okay);
		btn_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				submitMessage.cancel();
			}
		});
		btn_okay.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (userRationFloat > 0.0) {
					new SendUserMessage().execute();
					submitMessage.dismiss();
				}
				else
					Toast.makeText(getApplicationContext(), getResources().getString(R.string.add_aMessage_warning), Toast.LENGTH_SHORT).show();
			}
		});
		submitMessage.show();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			this.overridePendingTransition(R.anim.finish_in, R.anim.finish_out);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	class SendUserMessage extends AsyncTask<String, Intent, String> {
		int httpResponseCode;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(Message.this);
			pDialog.setMessage(getResources().getString(R.string.add_aMessage_sending));
			pDialog.setCancelable(false);
			pDialog.setIndeterminate(false);
			pDialog.show();
		}
		@Override
		protected String doInBackground(String... string) {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			String userMessageString = userMessage.getText().toString();
			String userIDString = String.valueOf(userID);
			//String userMessageTime = getCurrentTime();
			String userRatingString = String.valueOf(userRationFloat);
			//params.add(new BasicNameValuePair("action", "rating"));
			params.add(new BasicNameValuePair("uID", userIDString));
			params.add(new BasicNameValuePair("id", sid));
			params.add(new BasicNameValuePair("comment", userMessageString));
			params.add(new BasicNameValuePair("rating", userRatingString));
			JSONParser jsonParser = new JSONParser(getApplicationContext());
			JSONObject json = jsonParser.makeHttpRequest("/rating" ,"POST", params);
			if (json == null) {
				Log.e("JSON Object is null", "when doing background task Message");
				isSuccessed = false;
				httpResponseCode = jsonParser.getHttpResponseCode();
			    return null;
			}
			//Log.e("Message JSON: ", json.toString());
			isSuccessed = true;
			httpResponseCode = jsonParser.getHttpResponseCode();
			return null;
		}
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			pDialog.dismiss();
			if (isSuccessed) {
				runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(getApplicationContext(), getResources().getString(R.string.add_aMessage_success), Toast.LENGTH_SHORT).show();
					}
				});
			    isSuccessed = false;
			    refreshMessage();
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
	}

	class GetAllMessage extends AsyncTask<String, Integer, Void> {
        ProgressBar pBar;
        ListView listView;
        TextView noItemTxt,avgTxt;
        RatingBar avgBar;
        boolean thereHaveItems = false, userAlreadyHasMessage = false, success;
        int httpResponseCode;
		public GetAllMessage (ProgressBar _pBar, ListView _listView, TextView _noItemTxt, TextView _avg, RatingBar _avgBar) {
        	pBar = _pBar;
        	listView = _listView;
        	noItemTxt = _noItemTxt;
        	avgTxt = _avg;
        	avgBar = _avgBar;
        }
		@Override
		protected void onPreExecute() {
			listView.setVisibility(View.GONE);
			super.onPreExecute();
		}
		@Override
		protected Void doInBackground(String... uID) {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			//params.add(new BasicNameValuePair("action", "get_rating_list"));
			params.add(new BasicNameValuePair("id", sid));
			params.add(new BasicNameValuePair("uID", String.valueOf(userID)));
			JSONParser jsonParser = new JSONParser(getApplicationContext());
			JSONObject json = jsonParser.makeHttpRequest("/rating_list","GET", params);
			if (json == null) {
				Log.e("JSON Object is null", "when doing background task Message");
				thereHaveItems = false;
				success = false;
				httpResponseCode = jsonParser.getHttpResponseCode();
			    return null;
			}
			//Log.e("get all message json:", json.toString());
			success = true;
			httpResponseCode = jsonParser.getHttpResponseCode();
			try {
				avgFloat = json.getString("Store_avg_rating");
				all_message_raw = json.getJSONArray("Rating_List");    //Temporarory
				user_message_raw = json.getJSONArray("Users_rating");
				for (int i=0 ; i<all_message_raw.length() ; i++) {
				   	JSONObject jObject = all_message_raw.getJSONObject(i);
				   	all_message.add(ListAdapter(jObject , 0));
				}
				if (user_message_raw.length() > 0) {
				    JSONObject currentUser = user_message_raw.getJSONObject(0);
					user_message.add(ListAdapter(currentUser, 1));
					userAlreadyHasMessage = true;
				}
				thereHaveItems = true;
				haveMessage = thereHaveItems;
				userHaveMessage = userAlreadyHasMessage;
			}
			catch (JSONException e) {
				e.printStackTrace();
			}
			return null;
		}
		@Override
		protected void onPostExecute(Void result) {
			pBar.setVisibility(View.GONE);
			if (success) {
				if (thereHaveItems) {
					lazyAdapter = new LazyAdapter(Message.this, all_message);
					listView.setAdapter(lazyAdapter);
					listView.setVisibility(View.VISIBLE);
					avgTxt.setText(DecimalHelper(avgFloat));
					float rating = Float.parseFloat(avgFloat);
					avgRantingBar.setRating(rating);
					rankLayout.setVisibility(View.VISIBLE);
					horiLine.setVisibility(View.VISIBLE);
					if (userAlreadyHasMessage) {
						setMyMessage();
						btn_Add.setEnabled(false);
					}
					else {
						if (distence < 500.0) btn_Add.setEnabled(true);
					}
				}
			}
			else {
				if (httpResponseCode == 900) {
					//連線逾時
					Toast.makeText(getApplicationContext(), getResources().getString(R.string.ConnectionTimeOut), Toast.LENGTH_SHORT).show();
					return;
				}
				if (httpResponseCode == HttpStatus.SC_NOT_FOUND) {
					if (!thereHaveItems) {
						if (userAlreadyHasMessage) {
							setMyMessage();
						    btn_Add.setEnabled(false);
						    horiLine.setVisibility(View.VISIBLE);
						}
						else {
							noItemTxt.setVisibility(View.VISIBLE);
							if (distence < 500.0) btn_Add.setEnabled(true);
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
			super.onPostExecute(result);
		}
	}

	//Adapter Worker
	private HashMap<String, String> ListAdapter(JSONObject jsonObject,int toggle) {
		HashMap<String, String> maps = new HashMap<String, String>();
		String uID = "",comment = "",rating = "",timestamp = "";
		try {
			if (toggle != 1)
				uID = jsonObject.getString("uNickName");
			comment = jsonObject.getString("comment");
			rating = jsonObject.getString("rating");
			timestamp = jsonObject.getString("rating_time");
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
		maps.put("Author", uID);
		maps.put("Messages", comment);
		maps.put("Rating", rating);
		maps.put("Times", timestamp);
		return maps;
	}

	public void refreshMessage() {
		listView.setVisibility(View.GONE);
		progressBar.setVisibility(View.VISIBLE);
		noAnyMessage.setVisibility(View.GONE);
		userLayout.setVisibility(View.GONE);
		all_message = new ArrayList<HashMap<String,String>>();
		user_message = new ArrayList<HashMap<String,String>>();
		lazyAdapter = new LazyAdapter(Message.this, all_message);
		lazyAdapter.notifyDataSetChanged();
		GetAllMessage getAllMessage = new GetAllMessage(progressBar, listView, noAnyMessage, avgRank, avgRantingBar);
		getAllMessage.execute();
	}

	private String DecimalHelper(String ratingAvg) {
		double ratingDouble = Double.valueOf(ratingAvg);
		String formatedRatingAvg = new DecimalFormat("#.0").format(ratingDouble*1.0);
		return formatedRatingAvg;
	}

	public void setMyMessage() {
		String time = user_message.get(0).get("Times");
		String message = user_message.get(0).get("Messages");
		String rating = user_message.get(0).get("Rating");
		float ratingFloatUser = Float.parseFloat(rating);
		if (message.trim().length() > 0) {
			userAlreadyMessage.setVisibility(View.VISIBLE);
		    userAlreadyMessage.setText(message);
		}
        userRatingTime.setText(time);
        userRatingBar.setRating(ratingFloatUser);
        userLayout.setVisibility(View.VISIBLE);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		try {
			ArrayList<HashMap<String, String>> adapterTemp = lazyAdapter.getAdapterData();
			outState.putSerializable("allMessage", adapterTemp);
		}
		catch (NullPointerException e) {
			outState = null;
			return;
		}
		outState.putSerializable("userMessage", user_message);
		outState.putString("avgFloat", avgFloat);
		outState.putBoolean("haveMessage", haveMessage);
		outState.putBoolean("userHaveMessage", userHaveMessage);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		if (savedInstanceState != null) {
			all_message = (ArrayList<HashMap<String, String>>) savedInstanceState.getSerializable("allMessage");
			user_message = (ArrayList<HashMap<String, String>>) savedInstanceState.getSerializable("userMessage");
		    haveMessage = savedInstanceState.getBoolean("userHaveMessage", false);
		    userHaveMessage = savedInstanceState.getBoolean("userHaveMessage", false);
			avgFloat = savedInstanceState.getString("avgFloat");
		    setMessageUI();
		}
	}

	private void setMessageUI() {
		progressBar.setVisibility(View.GONE);
		if (haveMessage) {
			lazyAdapter = new LazyAdapter(Message.this, all_message);
			listView.setAdapter(lazyAdapter);
			listView.setVisibility(View.VISIBLE);
			avgRank.setText(DecimalHelper(avgFloat));
			float rating = Float.parseFloat(avgFloat);
			avgRantingBar.setRating(rating);
			rankLayout.setVisibility(View.VISIBLE);
			horiLine.setVisibility(View.VISIBLE);
			if (userHaveMessage) {
				setMyMessage();
				btn_Add.setEnabled(false);
			}
			else
				if (distence < 500.0) btn_Add.setEnabled(true);
		}
		else {
			if (userHaveMessage) {
				setMyMessage();
			    btn_Add.setEnabled(false);
			    horiLine.setVisibility(View.VISIBLE);
			}
			else {
				noAnyMessage.setVisibility(View.VISIBLE);
				if (distence < 500.0) btn_Add.setEnabled(true);
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
					Intent intent = new Intent(Message.this, splash.class);
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
