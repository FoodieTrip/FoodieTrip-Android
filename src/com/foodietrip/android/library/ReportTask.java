package com.foodietrip.android.library;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class ReportTask extends AsyncTask<String, Integer, Void> {
	Context context;
	String originalValue = "", adviceValue = "", note = "", uID = "", sID = "";
	int reportSwitch, httpReponseCode = 0;
	JSONParser jsonParser;
	public ReportTask(Context _context, int _reportSwitch, String _uID, String _sID) {
		context = _context;
		reportSwitch = _reportSwitch;
		uID = _uID;
		sID = _sID;
		jsonParser = new JSONParser(_context);
	}
	public void addValue(String _originalValue, String _adviceValue, String _note) {
		originalValue = _originalValue;
		adviceValue = _adviceValue;
		note = _note;	
	}
	public void addPhotoUrl(String _originalPhotoUrl) {
		originalValue = _originalPhotoUrl;
	}
	public void addMessage(String _originalMessage) {
		originalValue = _originalMessage;
	}
	@Override
	protected Void doInBackground(String... message) {
		String url = "http://192.241.211.104/~proposal/api/index.php/report/submit";
		String reportItemString = String.valueOf(reportSwitch);
		String adviceItemString = adviceValue;    //建議值
		String noteString = note;    //其他建議
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("uID", uID));
		params.add(new BasicNameValuePair("id", sID));
		params.add(new BasicNameValuePair("report_item", reportItemString));
		params.add(new BasicNameValuePair("origin_thing", originalValue));
		params.add(new BasicNameValuePair("correct_thing", adviceItemString));
		params.add(new BasicNameValuePair("remark", noteString));
		JSONObject json = jsonParser.httpRequestRest(url, "POST", params);
		if (json == null) {
			Log.e("json is null => ", "Report");
			httpReponseCode = jsonParser.getHttpResponseCode();
			return null;
		}
		httpReponseCode = jsonParser.getHttpResponseCode();
		//Log.e("Report json Message =>", json.toString());
		return null;
	}
	
	public int getHttpResponseCode() {
		return httpReponseCode;
	}
}
