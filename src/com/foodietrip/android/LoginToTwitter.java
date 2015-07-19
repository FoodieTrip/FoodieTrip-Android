package com.foodietrip.android;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.foodietrip.android.library.NetworkState;
import com.foodietrip.android.library.TwitterHelper;

public class LoginToTwitter extends SherlockActivity {
    TwitterHelper twitterHelper;
    ActionBar actionBar;
    //Constants
	static String TWITTER_CONSUMER_KEY;
	static String TWITTER_CONSUMER_SECRET;
	//Preference Constants
	static String PREFERENCE_NAME;
	static String PRIF_KEY_OAUTH_TOKEN;
	static String PRIF_KEY_OAUTH_SERECT;
	static String PRIF_KEY_TWITTER_LOGIN;
	static String TWITTER_CALLBACK_URL = "oauth://t4jsample";
	//Twitter oauth urls
	static String URL_TWITTER_AUTH;
	static String URL_TWITTER_OAUTH_VERIFIER;
	static String URL_TWITTER_OAUTH_TOKEN;
	//Twitter Use
	private static Twitter twitter;
	private static RequestToken requestToken;
	//SharedPreference
	SharedPreferences sprfSetting;
	boolean isTheFirstRun = true;
	WebView webView;
	String urlString;
	SherlockActivity activity;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_PROGRESS);
		setContentView(R.layout.twitter_view);
		getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);
		actionBar = getSupportActionBar();
		actionBar.setIcon(R.drawable.ic_action_web_site);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		twitterHelper = new TwitterHelper(getApplicationContext());
    	sprfSetting = PreferenceManager.getDefaultSharedPreferences(this);
    	isTheFirstRun = sprfSetting.getBoolean("isTheFirstRun", true);
    	TWITTER_CONSUMER_KEY = getResources().getString(R.string.twitter_consumer_key);
    	TWITTER_CONSUMER_SECRET = getResources().getString(R.string.twitter_consumer_secret);
    	PREFERENCE_NAME = getResources().getString(R.string.preference_name);
    	PRIF_KEY_OAUTH_TOKEN = getResources().getString(R.string.pref_key_oauth_token);
    	PRIF_KEY_OAUTH_SERECT = getResources().getString(R.string.pref_key_oauth_secret);
    	PRIF_KEY_TWITTER_LOGIN = getResources().getString(R.string.pref_key_twitter_login);
    	URL_TWITTER_OAUTH_VERIFIER = getResources().getString(R.string.url_twitter_oauth_verifier);
    	URL_TWITTER_OAUTH_TOKEN = getResources().getString(R.string.url_twitter_oauth_token);
    	webView = (WebView) findViewById(R.id.twitter_WebView);
	    activity = this;
	    if (!isTwitterLoggedInAlready() && isTheFirstRun)
	    	loginToTwitter();
	}
	
	//Login To Twitter
	@SuppressLint("SetJavaScriptEnabled")
	private void loginToTwitter() {
		sprfSetting.edit().putBoolean("isTheFirstRun", false).commit();
		final boolean isLogged = isTwitterLoggedInAlready();
		webView.setVisibility(View.VISIBLE);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setBuiltInZoomControls(true);
		new Thread(new Runnable() {
			@Override
			public void run() {
				if (!isLogged) {
					try {
					    ConfigurationBuilder builder = new ConfigurationBuilder();
					    builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
					    builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
					    Configuration configuration = builder.build();
					    TwitterFactory factory = new TwitterFactory(configuration);
					    twitter = factory.getInstance();
					    requestToken = twitter.getOAuthRequestToken(TWITTER_CALLBACK_URL);
					    urlString = requestToken.getAuthenticationURL();
                        webViewStart(urlString);
					}
					catch (TwitterException e) {
						//e.printStackTrace();
						//Log.e("Twitter Login Error",">" +e.getMessage());
					}
				}
				else {
					finish();
				}
			}
		}).start();
	}
	
	private void webViewStart(String url) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
			    webView.setWebChromeClient(webChromeClient);
			    webView.setWebViewClient(webViewClient);
			    webView.loadUrl(urlString);
			}
		});
	}
	
	private WebViewClient webViewClient = new WebViewClient() {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
		    if (url.contains(getResources().getString(R.string.twitter_callback_url))) {
		    	final String fUrl = url;
		    	new Thread(new Runnable() {
					@Override
					public void run() {
						Uri uri = Uri.parse(fUrl);
					    //oAuth Verifier
						String verifier = uri.getQueryParameter(URL_TWITTER_OAUTH_VERIFIER);
						try {
							//Get Access Token
						    AccessToken accessToken = twitter.getOAuthAccessToken(requestToken,verifier);
							//SharedPreferences
						    sprfSetting = PreferenceManager.getDefaultSharedPreferences(LoginToTwitter.this);
							Editor e = sprfSetting.edit();
							//得到了Access Token 和 Secret 後存入 SharedPreference
							e.putString(PRIF_KEY_OAUTH_TOKEN, accessToken.getToken());
							e.putString(PRIF_KEY_OAUTH_SERECT, accessToken.getTokenSecret());
							//登入boolean = true
							e.putBoolean(PRIF_KEY_TWITTER_LOGIN, true);
							e.putBoolean("prefTwitter", true);
							e.commit();    //存檔
							sprfSetting.edit().remove("isTheFirstRun").commit();
							Intent list_view = new Intent(LoginToTwitter.this,ItemList.class);
							list_view.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							toast(getResources().getString(R.string.Twitter_logined));
							startActivity(list_view);
						    finish();
						    overridePendingTransition(R.anim.finish_in, R.anim.finish_out);
						}
						catch (TwitterException e) {
						    e.printStackTrace();
						    Log.e("TwitterLogin", e.getErrorMessage());
						}
					}
				}).start();
				//Log.e("Twitter oAuth Token", ">" +accessToken.getToken());
		        return true;
		    }
		    return false;
		}
	};
	
	private WebChromeClient webChromeClient = new WebChromeClient() {
		@Override
		public void onProgressChanged(WebView view, int newProgress) {
			String nowLoading = getResources().getString(R.string.Twitter_nowLoading);
			activity.setTitle(nowLoading +"..." +newProgress +"%");
			activity.setProgress(newProgress * 100);
			if (newProgress == 100) {
				activity.setTitle(getResources().getString(R.string.loginToTwitter));
			}
		}
	};

	private void toast(final String mes) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), mes, Toast.LENGTH_SHORT).show();
			}
		});
	}

	private boolean isTwitterLoggedInAlready() {
		return twitterHelper.isTwitterLoggedInAlready();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			sprfSetting.edit().putBoolean("prefTwitter", false).commit();
			finish();
			this.overridePendingTransition(R.anim.finish_in, R.anim.finish_out);
			break;
		default:
			super.onOptionsItemSelected(item);
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
					Intent intent = new Intent(LoginToTwitter.this, splash.class);
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
