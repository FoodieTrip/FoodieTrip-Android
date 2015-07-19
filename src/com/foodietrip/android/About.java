package com.foodietrip.android;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;

import com.actionbarsherlock.view.MenuItem;
import com.foodietrip.android.library.NetworkState;
import com.foodietrip.android.library.ShakeListener;
import com.foodietrip.android.library.ShakeListener.OnShakeListener;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class About extends SherlockActivity{
	ActionBar actionbar;
	TextView txt_lan,txt_lng,txt_version;
	ImageView logo_img;
	boolean stage2 = false;
	int i = 0;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);    //�NLayout �]�w��About
		//���o��������
		logo_img = (ImageView)findViewById(R.id.imageView_logo);
		logo_img.setOnClickListener(listener);
		TextView sThanks = (TextView)findViewById(R.id.sThanks);
		txt_version = (TextView) findViewById(R.id.textView_version);
		String thanks = "JakeWharton\n[ActionBarSherlock] [ViewPagerIndicator]\n"
				+"Jeremy Feinstein\n[SlidingMenu]\n"
				+"Kevin Gaudin\n[ACRA]\n"
				+"biboune&edisonw\n[DateTimePicker]\n"
				+"chrisbanes\n[ActionBar-PullToRefresh]\n"
				+"Yusuke Yamamoto\n[Twitter4J]\n"
				+"ssyu \n[programming.im]\n"
				+"Arnaud Vallat\n[Android-ScrollBarPanel]\n"
				+"Jeff Gilfelt\n[Android Action Bar Style Generator]\n"
				+"Justin Schultz\n[android-lightbox]\n"
				+"Ms.Hsu [Likert scale]";
		sThanks.setText(thanks);
		try {
			String verstionString = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
			txt_version.setText(verstionString);
		}
		catch (NameNotFoundException e) {}
		//ActionBar
		actionbar = getSupportActionBar();
		actionbar.setDisplayHomeAsUpEnabled(true);
		actionbar.setHomeButtonEnabled(true);
		ShakeListener shakeListener = new ShakeListener(this);
		shakeListener.setOnShakeListener(new OnShakeListener() {
			@Override
			public void onShake() {
				if (stage2) {
					onVibrator();
					stage2 = false;
					new AlertDialog.Builder(About.this)
					.setTitle(getResources().getString(R.string.splash_alertNetErrorTitle))
					.setMessage(getResources().getString(R.string.we_love_yaya))
					.setPositiveButton(getResources().getString(R.string.itemList_alertExitAppOkay), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Toast.makeText(getApplicationContext(), getResources().getString(R.string.we_love_yaya_okay), Toast.LENGTH_SHORT).show();
						}
					})
					.setNegativeButton(getResources().getString(R.string.CrashTest), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Toast.makeText(getApplicationContext(), getResources().getString(R.string.we_love_yaya_QQ), Toast.LENGTH_SHORT).show();
							onVibrator();
							throw new RuntimeException("ACRA test");
						}
					})
					.show();
				}
			}
		});
	}

	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
		case android.R.id.home:
			finish();
			this.overridePendingTransition(R.anim.finish_in, R.anim.finish_out);
			break;
		default:
			super.onOptionsItemSelected(item);
		}
		return true;
	}

	private ImageView.OnClickListener listener = new ImageView.OnClickListener(){
		@Override
		public void onClick(View v) {
			Animation effect = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.image_effect);
			logo_img.startAnimation(effect);
			if (i == 10){
				logo_img.setImageDrawable(getResources().getDrawable(R.drawable.yaya));
			    i = i + 1;
			}
			else if(i > 10){
			}
			else {
				i = i + 1;
				if (i >= 6) {
					int times = -(i - 10) + 1;
					Toast.makeText(getApplicationContext(), ""+times, Toast.LENGTH_SHORT).show();
					stage2 = true;
				}
			}
		}
	};

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.finish_in, R.anim.finish_out);
	}

	private void onVibrator() {
		Vibrator vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
		if (vibrator == null) {
			Vibrator localVibrator = (Vibrator) this.getApplicationContext().getSystemService("vibrator");
		    vibrator = localVibrator;
		}
		vibrator.vibrate(100L);
	}

	@Override
	protected void onResume() {
		super.onResume();
		NetworkState networkState = new NetworkState(this);
		boolean isOnline = networkState.checkInternet();
		if (!isOnline) {
			new AlertDialog.Builder(About.this)
			.setTitle(getResources().getString(R.string.splash_alertNetErrorTitle))
			.setIcon(R.drawable.ic_launcher)
			.setMessage(getResources().getString(R.string.splash_alertNetErrorMes))
			.setPositiveButton(getResources().getString(R.string.alertDialogOkay), new DialogInterface.OnClickListener() {
				@SuppressLint("InlinedApi")
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent intent = new Intent(About.this, splash.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					if(Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1)
					    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
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
