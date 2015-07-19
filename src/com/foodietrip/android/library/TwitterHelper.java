package com.foodietrip.android.library;

import java.io.File;

import com.foodietrip.android.R;

import twitter4j.GeoLocation;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.media.ImageUpload;
import twitter4j.media.ImageUploadFactory;
import twitter4j.media.MediaProvider;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class TwitterHelper {
	Context context;
    //Constants
	static String TWITTER_CONSUMER_KEY;
	static String TWITTER_CONSUMER_SECRET;
	static String TWITPIC_API_KEY;
	//Preference Constants
	static String PREFERENCE_NAME;
	static String PRIF_KEY_OAUTH_TOKEN;
	static String PRIF_KEY_OAUTH_SERECT;
	static String PRIF_KEY_TWITTER_LOGIN;
	static String TWITTER_CALLBACK_URL;
	//Twitter oauth urls
	static String URL_TWITTER_AUTH;
	static String URL_TWITTER_OAUTH_VERIFIER;
	static String URL_TWITTER_OAUTH_TOKEN;
	AccessToken accessToken;
	//SharedPreference
	SharedPreferences sprfSetting;
	//Constructor
    public TwitterHelper(Context _context) {
    	context = _context;
    	sprfSetting = PreferenceManager.getDefaultSharedPreferences(context);
    	TWITTER_CONSUMER_KEY = context.getResources().getString(R.string.twitter_consumer_key);
    	TWITTER_CONSUMER_SECRET = context.getResources().getString(R.string.twitter_consumer_secret);
    	TWITPIC_API_KEY = context.getResources().getString(R.string.twitPic_api_key);
    	PREFERENCE_NAME = context.getResources().getString(R.string.preference_name);
    	PRIF_KEY_OAUTH_TOKEN = context.getResources().getString(R.string.pref_key_oauth_token);
    	PRIF_KEY_OAUTH_SERECT = context.getResources().getString(R.string.pref_key_oauth_secret);
    	PRIF_KEY_TWITTER_LOGIN = context.getResources().getString(R.string.pref_key_twitter_login);
    	TWITTER_CALLBACK_URL = context.getResources().getString(R.string.url_twitter_auth);
    	URL_TWITTER_OAUTH_VERIFIER = context.getResources().getString(R.string.url_twitter_oauth_verifier);
    	URL_TWITTER_OAUTH_TOKEN = context.getResources().getString(R.string.url_twitter_oauth_token);
    }

	//Login Check
	public boolean isTwitterLoggedInAlready() {
		//從 sharedPreference 取得Twitter登入狀態
		return sprfSetting.getBoolean(PRIF_KEY_TWITTER_LOGIN, false);
	}

	//Logout Twitter
	public void logoutFromTwitter() {
		//將SharedPreference清空
		Editor e = sprfSetting.edit();
		e.remove(PRIF_KEY_OAUTH_TOKEN);
		e.remove(PRIF_KEY_OAUTH_SERECT);
		e.remove(PRIF_KEY_TWITTER_LOGIN);
		e.remove("prefTwitter");
		e.commit();
	}

	//Tweet
	public void makeTweet(Context ctx, String userString, String StoreName,double latitude,double longitude) {
		String iAmAt = context.getResources().getString(R.string.Twitter_iAmAt);
		String message = userString +" ," +iAmAt +StoreName;
		new updateTwitterStatus(ctx,latitude,longitude).execute(message);
	}

	//Tweet
	public void makePhotoTweet(Context ctx,String userString, String StoreName,double latitude,double longitude,File photo) {
		String iAmAt = context.getResources().getString(R.string.Twitter_iAmAt);
		String message = userString +" ," +iAmAt +StoreName;
		new UploadPhoto(ctx, photo, latitude, longitude).execute(message);
	}

	//get New Access Token
	public void getNewAccessToken() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					ConfigurationBuilder builder = new ConfigurationBuilder();
					builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
					builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
					//Access Token
					String access_token = sprfSetting.getString(PRIF_KEY_OAUTH_TOKEN, "");
					//Access Token Secret
					String access_token_serect = sprfSetting.getString(PRIF_KEY_OAUTH_SERECT, "");
					//Get Access Token
					accessToken = new AccessToken(access_token, access_token_serect);
					//Log.e("Twitter oAuth Token", ">" +accessToken.getToken());
				}
				catch(Exception e) {
					e.printStackTrace();
					Log.e("Twitter Login Error",">" +e.getMessage());
				}
			}
		}).start();
	}

	//Tweet Action
	public class updateTwitterStatus extends AsyncTask<String, String, String> {
		private Context context;
		ProgressDialog pDialog;
		double latitude,longitude;
		public updateTwitterStatus(Context _context,double _latitude,double _longitude) {
			context = _context;
			latitude = _latitude;
			longitude = _longitude;
			pDialog = new ProgressDialog(context);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog.setMessage(context.getResources().getString(R.string.Twitter_upload));
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}
		@Override
		protected String doInBackground(String... params) {
			//Log.d("Tweet Text ", ">" +params[0]);
			String status = params[0] +"  via #FoodBookBeta";
			StatusUpdate statusUpdate = new StatusUpdate(status);
			GeoLocation geoLocation = new GeoLocation(latitude, longitude);
			statusUpdate.setLocation(geoLocation);
			try {
				ConfigurationBuilder builder = new ConfigurationBuilder();
				builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
				builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
				//Access Token
				String access_token = sprfSetting.getString(PRIF_KEY_OAUTH_TOKEN, "");
				//Access Token Secret
				String access_token_serect = sprfSetting.getString(PRIF_KEY_OAUTH_SERECT, "");
				AccessToken accessToken = new AccessToken(access_token, access_token_serect);
				Twitter twitter = new TwitterFactory(builder.build()).getInstance(accessToken);
				//更新動態
				twitter.updateStatus(statusUpdate);
				//Log.d("Status", ">" +response.getText());
			}
			catch (TwitterException e) {
				//更新動態失敗
				Log.e("Twitter Update Error", ">" +e.getMessage());
			}
			return null;
		}
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			pDialog.dismiss();
			Toast.makeText(context, context.getResources().getString(R.string.Twitter_upload_success), Toast.LENGTH_SHORT).show();
		}
	}

	public class UploadPhoto extends AsyncTask<String, Integer, String> {
		Context context;
		ProgressDialog pDialog;
		File photo;
		double latitude,longitude;
		public UploadPhoto(Context _ctx,File _photo,double _latitude,double _longitude) {
			context = _ctx;
			photo = _photo;
			latitude = _latitude;
			longitude = _longitude;
			pDialog = new ProgressDialog(context);
		}
		@Override
		protected String doInBackground(String... params) {
			//Log.d("Tweet Text ", ">" +params[0]);
			try {
				//Access Token
				String access_token = sprfSetting.getString(PRIF_KEY_OAUTH_TOKEN, "");
				//Access Token Secret
				String access_token_serect = sprfSetting.getString(PRIF_KEY_OAUTH_SERECT, "");
				ConfigurationBuilder bulider = new ConfigurationBuilder();
				bulider.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
				bulider.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
				bulider.setMediaProviderAPIKey(TWITPIC_API_KEY);
				bulider.setOAuthAccessToken(access_token);
				bulider.setOAuthAccessTokenSecret(access_token_serect);
				Configuration config = bulider.build();
				ImageUpload imageUpload = new ImageUploadFactory(config).getInstance(MediaProvider.TWITPIC);
			    String url = imageUpload.upload(photo);
				String status = params[0] +url +"  via #FoodieTrip";
				StatusUpdate statusUpdate = new StatusUpdate(status);
				GeoLocation geoLocation = new GeoLocation(latitude, longitude);
				statusUpdate.setLocation(geoLocation);
			    Twitter twitter = new TwitterFactory(config).getInstance();
			    twitter.updateStatus(statusUpdate);
			    //Log.d("Status", ">" +response.getText());
			}
			catch (TwitterException e) {
				e.printStackTrace();
				Log.e("Twitter Update Error", ">" +e.getMessage());
			}
			return null;
		}
		@Override
		protected void onPostExecute(String result) {
			Toast.makeText(context, context.getResources().getString(R.string.Twitter_upload_success), Toast.LENGTH_SHORT).show();
			super.onPostExecute(result);
		}
	}

}
