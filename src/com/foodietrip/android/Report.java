package com.foodietrip.android;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.foodietrip.android.library.NetworkState;
import com.foodietrip.android.library.ReportTask;

public class Report extends SherlockActivity {
	ActionBar actionBar;
	RadioGroup radioGroup;
	RadioButton rad_0,rad_1,rad_2,rad_3,rad_4;
	TextView advice_textView;
	EditText advice_editText,report_note;
	boolean haveAdvise = false;
	String sName,sID,uID,originalValue;
	double sLatitude = 0.0, sLongitude = 0.0;
	int reportSwitch = 100;
	Animation pushEffect;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.report);
		actionBar = getSupportActionBar();
		actionBar.setIcon(R.drawable.ic_action_error);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		Intent intent = this.getIntent();
		Bundle bundle = intent.getExtras();
		sName = bundle.getString("sName");
		sID = bundle.getString("sID");
		uID = bundle.getString("uID");
		sLatitude = bundle.getDouble("sLatitude");
		sLongitude = bundle.getDouble("sLongitude");
		radioGroup = (RadioGroup) findViewById(R.id.report_radioGroup);
		rad_0 = (RadioButton) findViewById(R.id.reportRadio1);
		rad_1 = (RadioButton) findViewById(R.id.reportRadio2);
		rad_2 = (RadioButton) findViewById(R.id.reportRadio3);
		rad_3 = (RadioButton) findViewById(R.id.reportRadio4);
		rad_4 = (RadioButton) findViewById(R.id.reportRadio5);
		advice_textView = (TextView) findViewById(R.id.report_textView);
		advice_editText = (EditText) findViewById(R.id.report_editText);
		report_note = (EditText) findViewById(R.id.report_note);
		radioGroup.setOnCheckedChangeListener(radio_listener);
		rad_0.setOnClickListener(radio_animation);
		rad_1.setOnClickListener(radio_animation);
		rad_2.setOnClickListener(radio_animation);
		rad_3.setOnClickListener(radio_animation);
		rad_4.setOnClickListener(radio_animation);
		pushEffect = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.image_effect);
		//Toast.makeText(getApplicationContext(), sName+"\n"+sLatitude+"\n"+sLongitude+"\n"+sID+", "+uID, Toast.LENGTH_SHORT).show();
	}
	
	private RadioButton.OnClickListener radio_animation = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.reportRadio1:
				rad_0.startAnimation(pushEffect);
				break;
			case R.id.reportRadio2:
				rad_1.startAnimation(pushEffect);
				break;
			case R.id.reportRadio3:
				rad_2.startAnimation(pushEffect);
				break;
			case R.id.reportRadio4:
				rad_3.startAnimation(pushEffect);
				break;
			case R.id.reportRadio5:
				rad_4.startAnimation(pushEffect);
				break;
			}
		}
	};
	
	private RadioGroup.OnCheckedChangeListener radio_listener = new RadioGroup.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			switch (checkedId) {
			case R.id.reportRadio1:
				haveAdvise = true;
                setAdviceUI(true);
				advice_editText.setHint(R.string.report_advice_name);
				reportSwitch = 0;    //店家名稱錯誤
				originalValue = sName;
				break;
			case R.id.reportRadio2:
				haveAdvise = false;
                setAdviceUI(false);
                reportSwitch = 1;    //店家重複
                originalValue = sName;
				break;
			case R.id.reportRadio3:
				haveAdvise = true;
				setAdviceUI(true);
				advice_editText.setHint(R.string.report_advice_address);
				reportSwitch = 2;    //店家已經不再此處
				originalValue = sName +", 經度= " +sLatitude +", 緯度=" +sLongitude;
				break;
			case R.id.reportRadio4:
				haveAdvise = false;
				setAdviceUI(false);
				reportSwitch = 3;    //店家已經不再營業
				originalValue = sName;
				break;
			case R.id.reportRadio5:
				haveAdvise = false;
				setAdviceUI(true);
				advice_editText.setHint(R.string.report_advice_notExsistReason);
				reportSwitch = 4;    //店家不存在
				originalValue = sName;
				break;
			}
		}
	};
	
	private void setAdviceUI(boolean advices) {
		if (advices) {
			advice_textView.setVisibility(View.VISIBLE);
			advice_editText.setVisibility(View.VISIBLE);
		}
		else {
			advice_textView.setVisibility(View.GONE);
			advice_editText.setVisibility(View.GONE);
			advice_editText.setHint("");
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.report, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			this.overridePendingTransition(R.anim.finish_in, R.anim.finish_out);
			break;
		case R.id.report_send:
			//send the item
			if (reportSwitch != 100) {
				new AlertDialog.Builder(Report.this)
				.setTitle(getResources().getString(R.string.alertComfirmTitle))
				.setMessage(getResources().getString(R.string.Report_error_message))
				.setIcon(R.drawable.ic_action_warning)
				.setPositiveButton(getResources().getString(R.string.alertDialogOkay), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//send action
						sendReportTask();
					}
				})
				.setNegativeButton(getResources().getString(R.string.alertDialogCancel), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {}
				})
				.show();
			}
			else {
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.Report_error), Toast.LENGTH_SHORT).show();
			}
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
					Intent intent = new Intent(Report.this, splash.class);
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
	
	private void sendReportTask() {
		String adviceString = advice_editText.getText().toString();
		String noteString = report_note.getText().toString();
		ReportTask reportTask = new ReportTask(getApplicationContext(), reportSwitch, uID, sID);
		reportTask.addValue(originalValue, adviceString, noteString);
		reportTask.execute();
		Toast.makeText(getApplicationContext(), getResources().getString(R.string.Report_error_toast), Toast.LENGTH_SHORT).show();
	    finish();
	}
	
}
