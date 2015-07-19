package com.foodietrip.android.library;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.content.Context;

public class UserFunctions {
	private JSONParser jsonparser;
	private static String loginURL = "http://192.241.211.104/~proposal/api/index.php/user";
	Context context;
	//Contructor
	public UserFunctions(Context _context) {
		context = _context;
		jsonparser = new JSONParser(_context);
	}
	//Function make Login Request
	//@param email,@param password
	public JSONObject loginUser(String phone,String password) {
		// Buliding Parameters
		String url = loginURL +"/signin";
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("phone", phone));
		params.add(new BasicNameValuePair("password", password));
		JSONObject json = jsonparser.MemberRequest(url, "POST", params);
		//return json
		//Log.e("JSON login = ", json.toString());
		return json;
	}
	//Function make Login Request
	public JSONObject registerUser(String nick_name,String phone,String password, int gender) {
		//Buliding Parameters
		String url = loginURL +"/create";
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("nickname", nick_name));
		params.add(new BasicNameValuePair("phone", phone));
		params.add(new BasicNameValuePair("password", password));
		params.add(new BasicNameValuePair("gender", String.valueOf(gender)));
		JSONObject json = jsonparser.MemberRequest(url , "POST", params);
		//return json
		//Log.e("JSON register = ", json.toString());
		return json;
	}

	public String getUserName(Context context) {
		DatabaseHandler db = new DatabaseHandler(context);
		HashMap<String, String> user = db.getUserDetails();
		String name = user.get("name");
		return name;
	}

	public String getUserEmail(Context context) {
		DatabaseHandler db = new DatabaseHandler(context);
		HashMap<String, String> user = db.getUserDetails();
		String email = user.get("email");
		return email;
	}

	public int getUserUid(Context context) {
		DatabaseHandler db = new DatabaseHandler(context);
		HashMap<String, String> user = db.getUserDetails();
		int uID = Integer.parseInt(user.get("uID"));
		return uID;
	}

	public String getUserPhone(Context context) {
		DatabaseHandler db = new DatabaseHandler(context);
		HashMap<String, String> user = db.getUserDetails();
		String phone = user.get("phone");
		phone = phone.replace("+886", "0");
		return phone;
	}

	//Function get Login status
	public boolean isUserLoggedIn(Context context) {
		DatabaseHandler db = new DatabaseHandler(context);
		int count = db.getRowCount();
		//Log.e("get row count = ", "" +count);
		if (count > 0) {
			//使用者已經登入
			return true;
		}
		return false;
	}
    //Function to logout user
	public boolean logoutUser(Context context) {
		DatabaseHandler db = new DatabaseHandler(context);
		db.resetTable();
		return true;
	}

	public int getResponseCode() {
		int httpResponseCode = jsonparser.getHttpResponseCode();
		return httpResponseCode;
	}

}