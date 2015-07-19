package com.foodietrip.android;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.facebook.Session;
import com.facebook.Session.StatusCallback;
import com.facebook.SessionState;
import com.foodietrip.android.image_library.ImageLoader;
import com.foodietrip.android.library.FacebookHelper;
import com.foodietrip.android.library.NetworkState;
import com.foodietrip.android.library.TwitterHelper;
import com.foodietrip.android.library.UserFunctions;

public class Setting extends SherlockPreferenceActivity {
	static final String PRIF_KEY_TWITTER_LOGIN = "isTwitterLogedIn";
    ActionBar actionBar;
    TwitterHelper twitterHelper;
    FacebookHelper facebookHelper;
    CheckBoxPreference twitterUse,facebookUse;
    Preference clearCache,seeTutorialAgain;
    ImageLoader imageLoader;
    UserFunctions userFunctions;
    SharedPreferences sprfSetting;
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		actionBar = getSupportActionBar();
		actionBar.setIcon(R.drawable.ic_action_settings);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		getListView().setBackgroundColor(Color.TRANSPARENT);
		int api_version = Build.VERSION.SDK_INT;    //API版本
		String android_version = Build.VERSION.RELEASE;    //Android版本
		//Log.e("android_version Check:", "API:" +api_version +" ,release:" +android_version);
		if(api_version < Build.VERSION_CODES.HONEYCOMB && android_version.matches("(1|2)\\..+")) {
			addPreferencesFromResource(R.xml.setting);
			twitterUse = (CheckBoxPreference) findPreference("prefTwitter");
			facebookUse = (CheckBoxPreference) findPreference("prefFacebook");
			clearCache = (Preference) findPreference("prefClearCache");
			clearCache.setOnPreferenceClickListener(clearCacheAction);
			seeTutorialAgain = (Preference) findPreference("prefTutorial");
			seeTutorialAgain.setOnPreferenceClickListener(seeTutorial);
			sprfSetting = PreferenceManager.getDefaultSharedPreferences(this);
		    twitterHelper = new TwitterHelper(getApplicationContext());
		    facebookHelper = new FacebookHelper(getApplicationContext());
		    imageLoader = new ImageLoader(this);
		    userFunctions = new UserFunctions(getApplicationContext());
		    boolean isUserLogged = userFunctions.isUserLoggedIn(getApplicationContext());
		    if(!isUserLogged) {
		    	twitterUse.setEnabled(false);
		    	facebookUse.setEnabled(false);
		    }
		    boolean isTwitterLogged = twitterHelper.isTwitterLoggedInAlready();
		    if (isTwitterLogged)
		    	twitterUse.setDefaultValue(true);
		    else
				twitterUse.setDefaultValue(false);
		    boolean isFacebookLogged = facebookHelper.isFacebookLogined();
		    if (isFacebookLogged)
		    	facebookUse.setDefaultValue(true);
		    else
		    	facebookUse.setDefaultValue(false);
		}
		else
		    getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingFragment()).commit();
	}
	
	public static class SettingFragment extends PreferenceFragment {
		TwitterHelper twitterHelper;
		FacebookHelper facebookHelper;
		UserFunctions userFunctions;
		SwitchPreference twitterUse,facebookUse;
		Preference clearCache,seeTutorialAgain;
		ImageLoader imageLoader;
		SharedPreferences sprfSetting;
		boolean isFirstTime = true;
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.setting);
			twitterUse = (SwitchPreference) findPreference("prefTwitter");
			facebookUse = (SwitchPreference) findPreference("prefFacebook");
			clearCache = (Preference) findPreference("prefClearCache");
			clearCache.setOnPreferenceClickListener(clearCacheAction);
			seeTutorialAgain = (Preference) findPreference("prefTutorial");
			seeTutorialAgain.setOnPreferenceClickListener(seeTutorial);
			sprfSetting = PreferenceManager.getDefaultSharedPreferences(getActivity());
		    twitterHelper = new TwitterHelper(getActivity());
		    facebookHelper = new FacebookHelper(getActivity());
		    imageLoader = new ImageLoader(getActivity());
		    boolean isTwitterLogged = twitterHelper.isTwitterLoggedInAlready();
		    if (isTwitterLogged)
		    	twitterUse.setDefaultValue(true);
		    else
				twitterUse.setDefaultValue(false);
		    boolean isFacebookLogged = facebookHelper.isFacebookLogined();
		    if (isFacebookLogged)
		    	facebookUse.setDefaultValue(true);
		    else
		    	facebookUse.setDefaultValue(false);
		}
		
		private OnPreferenceClickListener clearCacheAction = new Preference.OnPreferenceClickListener(){
			@Override
			public boolean onPreferenceClick(Preference preference) {
				imageLoader.clearCache();
				Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.prefer_clear_cache_ok), Toast.LENGTH_SHORT).show();
				return false;
			}
		};
		
		private OnPreferenceClickListener seeTutorial = new Preference.OnPreferenceClickListener(){
			@Override
			public boolean onPreferenceClick(Preference preference) {
				sprfSetting.edit().remove("TutorialHasViewed").commit();
				Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.prefer_tutorial_toast), Toast.LENGTH_SHORT).show();
				return false;
			}
		};
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View view = super.onCreateView(inflater, container, savedInstanceState);
			view.setBackgroundColor(Color.TRANSPARENT);
		    userFunctions = new UserFunctions(getActivity());
		    boolean isUserLogged = userFunctions.isUserLoggedIn(getActivity());
		    if(!isUserLogged) {
		    	twitterUse.setEnabled(false);
		    	facebookUse.setEnabled(false);
		    }
			return view;
		}
		
		private SharedPreferences.OnSharedPreferenceChangeListener spcListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
					String key) {
                if (key.equals("prefTwitter")) {
                	boolean twitterBoolean = sharedPreferences.getBoolean("prefTwitter", false);
    				if (twitterBoolean)
    					Twitterlogin();
    				else {
    					Twitterloggout();
    					sprfSetting.unregisterOnSharedPreferenceChangeListener(spcListener);
    				}
                }
                if (key.equals("prefFacebook")) {
    				boolean facebookBoolean = sharedPreferences.getBoolean("prefFacebook", false);
    				if (facebookBoolean) 
    					Facebooklogin();
    				else {
    					Facebooklogout();
    					sprfSetting.unregisterOnSharedPreferenceChangeListener(spcListener);
    				}
                }
			}
		};
		
		private void Twitterloggout() {
			new AlertDialog.Builder(getActivity())
			.setTitle(getActivity().getResources().getString(R.string.Twitter_logout_title))
			.setMessage(getActivity().getResources().getString(R.string.Twitter_logout_mes))
			.setNegativeButton(getActivity().getResources().getString(R.string.alertDialogCancel), new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					twitterUse.setChecked(true);
					sprfSetting.registerOnSharedPreferenceChangeListener(spcListener);
				}
			})
			.setPositiveButton(getActivity().getResources().getString(R.string.alertDialogOkay), new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					twitterHelper.logoutFromTwitter();
					Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.Twitter_logouted), Toast.LENGTH_SHORT).show();
				    sprfSetting.registerOnSharedPreferenceChangeListener(spcListener);
				}
			})
			.show();
		}
		
		private void Twitterlogin() {
			Intent goLogin = new Intent(getActivity(), LoginToTwitter.class);
			sprfSetting.edit().remove("isTheFirstRun").commit();
			goLogin.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			startActivity(goLogin);
		}
		
		private void Facebooklogin() {
			Session.openActiveSession(getActivity(), true, new StatusCallback() {
				@Override
				public void call(Session session, SessionState state, Exception exception) {
					if (session.isOpened()) {
						Log.e("Facebook Login", "is success");
					}
				}
			});
		}
		
		private void Facebooklogout() {
			new AlertDialog.Builder(getActivity())
			.setTitle(getActivity().getResources().getString(R.string.Facebook_logout_title))
			.setMessage(getActivity().getResources().getString(R.string.Facebook_logout_mes))
			.setNegativeButton(getActivity().getResources().getString(R.string.alertDialogCancel), new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					facebookUse.setChecked(true);
					sprfSetting.registerOnSharedPreferenceChangeListener(spcListener);
				}
			})
			.setPositiveButton(getActivity().getResources().getString(R.string.alertDialogOkay), new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					facebookHelper.logoutFromFacebook();
					Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.Facebook_logouted), Toast.LENGTH_SHORT).show();
				    sprfSetting.registerOnSharedPreferenceChangeListener(spcListener);
				}
			})
			.show();
		}

		@Override
		public void onStart() {
			sprfSetting.registerOnSharedPreferenceChangeListener(spcListener);
			super.onStart();
		}

		@Override
		public void onStop() {
			sprfSetting.unregisterOnSharedPreferenceChangeListener(spcListener);
			super.onStop();
		}
		
	}
	
    //For Under Honeycomb
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			//setResult(RESULT_CANCELED);
			finish();
			this.overridePendingTransition(R.anim.finish_in, R.anim.finish_out);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private OnPreferenceClickListener clearCacheAction = new Preference.OnPreferenceClickListener(){
		@Override
		public boolean onPreferenceClick(Preference preference) {
			imageLoader.clearCache();
			Toast.makeText(getApplicationContext(), getResources().getString(R.string.prefer_clear_cache_ok), Toast.LENGTH_SHORT).show();
			return false;
		}
	};
	
	private OnPreferenceClickListener seeTutorial = new Preference.OnPreferenceClickListener(){
		@Override
		public boolean onPreferenceClick(Preference preference) {
			sprfSetting.edit().remove("TutorialHasViewed").commit();
			Toast.makeText(getApplicationContext(), getResources().getString(R.string.prefer_tutorial_toast), Toast.LENGTH_SHORT).show();
			return false;
		}
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			//setResult(RESULT_CANCELED);
			finish();
			this.overridePendingTransition(R.anim.finish_in, R.anim.finish_out);
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void twitterLogout() {
		new AlertDialog.Builder(getApplicationContext())
		.setTitle(getResources().getString(R.string.Twitter_logout_title))
		.setMessage(getResources().getString(R.string.Twitter_logout_mes))
		.setNegativeButton(getResources().getString(R.string.alertDialogCancel), new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		})
		.setPositiveButton(getResources().getString(R.string.alertDialogOkay), new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				twitterHelper.logoutFromTwitter();
			}
		})
		.show();
	}
	
	private void twitterLogin() {
		Intent goLogin = new Intent(Setting.this, LoginToTwitter.class);
		sprfSetting.edit().remove("isTheFirstRun").commit();
		goLogin.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(goLogin);
	}
	
	private void Facebooklogin() {
		Session.openActiveSession(this, true, new StatusCallback() {
			@Override
			public void call(Session session, SessionState state, Exception exception) {
				if (session.isOpened()) {
					Log.e("Facebook Login", "is success");
				}
			}
		});
	}
	
	private void Facebooklogout() {
		new AlertDialog.Builder(Setting.this)
		.setTitle(getResources().getString(R.string.Facebook_logout_title))
		.setMessage(getResources().getString(R.string.Facebook_logout_mes))
		.setNegativeButton(getResources().getString(R.string.alertDialogCancel), new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				facebookUse.setChecked(true);
			}
		})
		.setPositiveButton(getResources().getString(R.string.alertDialogOkay), new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				facebookHelper.logoutFromFacebook();
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.Facebook_logouted), Toast.LENGTH_SHORT).show();
			}
		})
		.show();
	}
	
	@Override
	protected void onStart() {
		int api_version = Build.VERSION.SDK_INT;    //API版本
		String android_version = Build.VERSION.RELEASE;    //Android版本
		if(api_version < Build.VERSION_CODES.HONEYCOMB && android_version.matches("(1|2)\\..+"))
			sprfSetting.registerOnSharedPreferenceChangeListener(spcListener);
		super.onStop();
	}
	
	@Override
	protected void onStop() {
		int api_version = Build.VERSION.SDK_INT;    //API版本
		String android_version = Build.VERSION.RELEASE;    //Android版本
		if(api_version < Build.VERSION_CODES.HONEYCOMB && android_version.matches("(1|2)\\..+"))
			sprfSetting.unregisterOnSharedPreferenceChangeListener(spcListener);
		super.onStop();
	}

	private SharedPreferences.OnSharedPreferenceChangeListener spcListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
				String key) {
			if (key.equals("prefTwitter")) {
				boolean twitterBoolean = sharedPreferences.getBoolean("prefTwitter", false);
				if (twitterBoolean) 
					twitterLogin();
				else 
					twitterLogout();
			}
			if (key.equals("prefFacebook")) {
				boolean facebookBoolean = sharedPreferences.getBoolean("prefFacebook", false);
				if (facebookBoolean) 
					Facebooklogin();
				else 
					Facebooklogout();
			}
		}
	};
	
	@Override
	public void onActivityResult(int requestCode, int resultCode,
			Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
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
					Intent intent = new Intent(Setting.this, splash.class);
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
