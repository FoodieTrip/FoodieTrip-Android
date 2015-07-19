package com.foodietrip.android.library;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;
import org.json.JSONObject;

import com.foodietrip.android.R;

import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.util.Log;

public class JSONParser {
    InputStream is  = null;
    JSONObject jObj = null;
    String json = "";
    private String hostUrl = "http://192.241.211.104/~proposal/api/index.php/store";
    public int httpRequestCode = 0;
    int timeOutConnection = 1000 * 15;
    int timeOutSocket = 1000 * 25;
    Context context;
    NetworkState networkState;
    String nuclearLogin, nuclearPw;
    //constructor
    public JSONParser(Context _context){
    	context = _context;
    	networkState = new NetworkState(_context);
    	nuclearLogin = _context.getResources().getString(R.string.nuclear_login);
    	nuclearPw = _context.getResources().getString(R.string.nuclear_pw);
    }

    //function get json from URL
    //by making HTTP POST or GET method
    public JSONObject makeHttpRequest(String action,String method,List<NameValuePair> params){
    	boolean isOnline = networkState.checkInternet();
    	if (!isOnline) return null;
		//Making Http Request
    	AndroidHttpClient httpClient = null;
    	try{
    		if (method.equals("POST")){
    			hostUrl += action;
    			//defaultHttpClient
    			//Auth uses
    			URL urlObj = new URL(hostUrl);    //URL Object
    			HttpHost httpHost = new HttpHost(urlObj.getHost(), urlObj.getPort(), urlObj.getProtocol());
    			AuthScope scope = new AuthScope(urlObj.getHost(), urlObj.getPort());
    			UsernamePasswordCredentials creds = new UsernamePasswordCredentials(nuclearLogin, nuclearPw);
    			CredentialsProvider cProvider = new BasicCredentialsProvider();
    			cProvider.setCredentials(scope, creds);
    			HttpContext credContext = new BasicHttpContext();
    			credContext.setAttribute(ClientContext.CREDS_PROVIDER, cProvider);
    			httpClient = AndroidHttpClient.newInstance("test user agant");
    			//Auth End
    			//TimeOutSetting
    			HttpParams httpParams = new BasicHttpParams();
    			HttpConnectionParams.setConnectionTimeout(httpParams, timeOutConnection);
    			HttpConnectionParams.setSoTimeout(httpParams, timeOutSocket);
    			//TimeOut End
    			//DefaultHttpClient HttpClient = new DefaultHttpClient();
    			//hostUrl += "&action=" +action;    //網址放後面版本
    			HttpPost httppost = new HttpPost(hostUrl);
    			UrlEncodedFormEntity mesag = new UrlEncodedFormEntity(params,"UTF-8");
    			mesag.setContentEncoding(HTTP.UTF_8);    //強制編碼成UTF-8
    			httppost.setEntity(mesag);
    			httppost.setParams(httpParams);
    			HttpResponse httpResponse = httpClient.execute(httpHost, httppost, credContext);    //加密
    			//檢查Http Response
    			if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
    				httpRequestCode = httpResponse.getStatusLine().getStatusCode();
    				Log.e("JSON", "Http Response is unsuccessful.");
    				Log.e("Http Request", ""+httpResponse.getStatusLine().getStatusCode());
    	    		Log.e("Parse Url is =>", hostUrl);
    				return null;
    			}
    			httpRequestCode = httpResponse.getStatusLine().getStatusCode();
    			HttpEntity httpEntity = httpResponse.getEntity();
    			is = httpEntity.getContent();
    		}
    		else if (method.equals("GET")){
    			//defaultHttpClient
    			hostUrl += action;
    			//DefaultHttpClient HttpClient = new DefaultHttpClient();
    			//hostUrl += "&action=" +action;    //網址放後面版本
    			String paramString = URLEncodedUtils.format(params, "utf-8");
    			if (paramString.trim().length() > 0)
    				hostUrl += "?a=a&" + paramString;    //&本來是?
    			//Auth uses
    			URL urlObj = new URL(hostUrl);    //URL Object
    			HttpHost httpHost = new HttpHost(urlObj.getHost(), urlObj.getPort(), urlObj.getProtocol());
    			AuthScope scope = new AuthScope(urlObj.getHost(), urlObj.getPort());
    			UsernamePasswordCredentials creds = new UsernamePasswordCredentials(nuclearLogin, nuclearPw);
    			CredentialsProvider cProvider = new BasicCredentialsProvider();
    			cProvider.setCredentials(scope, creds);
    			HttpContext credContext = new BasicHttpContext();
    			credContext.setAttribute(ClientContext.CREDS_PROVIDER, cProvider);
    			httpClient = AndroidHttpClient.newInstance("test user agant");
    			//Auth End
    			//TimeOutSetting
    			HttpParams httpParams = new BasicHttpParams();
    			HttpConnectionParams.setConnectionTimeout(httpParams, timeOutConnection);
    			HttpConnectionParams.setSoTimeout(httpParams, timeOutSocket);
    			//TimeOut End
    			HttpGet httpGet = new HttpGet(hostUrl);
    			httpGet.setParams(httpParams);
    			HttpResponse httpResponse = httpClient.execute(httpHost, httpGet, credContext);
    			//檢查Http Response
    			if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
    				httpRequestCode = httpResponse.getStatusLine().getStatusCode();
    				Log.e("JSON", "Http Response is unsuccessful.");
    				Log.e("Http Request", ""+httpResponse.getStatusLine().getStatusCode());
    	    		Log.e("Parse Url is =>", hostUrl);
    			}
    			httpRequestCode = httpResponse.getStatusLine().getStatusCode();
    			HttpEntity httpEntity = httpResponse.getEntity();
    			is = httpEntity.getContent();
    		}
    		//Log.e("Connection is okay, Parse Url is =>", hostUrl);
    	}
    	catch (SocketTimeoutException e) {
    		httpRequestCode = 900;
    		e.printStackTrace();
    	}
    	catch (ConnectTimeoutException e) {
    		httpRequestCode = 900;
    		e.printStackTrace();
		}
    	catch (UnsupportedEncodingException e){
    		e.printStackTrace();
    	}
    	catch (ClientProtocolException e){
    		e.printStackTrace();
    	}
    	catch (IOException e){
    		//File no found exception
    		httpRequestCode = 901;
    		JSONObject fileNotFound = null;
    		Log.e("JSON Data is completely null!!", "JSONPaser");
    		e.printStackTrace();
    		return fileNotFound;
    	}
    	if (httpRequestCode != HttpStatus.SC_OK)
    		return null;
    	try{
    		BufferedReader reader = new BufferedReader(new InputStreamReader(is,"utf-8"),8);
    		StringBuilder sb = new StringBuilder();
    		String line = null;
    		while ((line = reader.readLine()) != null){
    			sb.append(line + "\n");
    		}
    		is.close();
    		json = sb.toString();
    		//Log.e("Raw Data =>", json);
    	}
    	catch (Exception e){
    		Log.e("Buffer Error","Error Converting Result"+e.toString());
    	}
    	//try parse a string to a JSON object
    	try{
    		jObj = new JSONObject(json);
    	}
    	catch(JSONException e) {
    		Log.e("JSON Parser","Error parsing data" +e.toString());
    	}
    	//return JSON String
    	if (httpClient != null) httpClient.close();
    	return jObj;
    }

    public JSONObject MemberRequest(String url,String method,List<NameValuePair> params){
    	boolean isOnline = networkState.checkInternet();
    	if (!isOnline) return null;
 		//Making Http Request
    	AndroidHttpClient httpClient = null;
     	try{
     	    //check request method
     		if (method.equals("POST")){
     			//Auth uses
    			URL urlObj = new URL(url);    //URL Object
    			HttpHost httpHost = new HttpHost(urlObj.getHost(), urlObj.getPort(), urlObj.getProtocol());
    			AuthScope scope = new AuthScope(urlObj.getHost(), urlObj.getPort());
    			UsernamePasswordCredentials creds = new UsernamePasswordCredentials(nuclearLogin, nuclearPw);
    			CredentialsProvider cProvider = new BasicCredentialsProvider();
    			cProvider.setCredentials(scope, creds);
    			HttpContext credContext = new BasicHttpContext();
    			credContext.setAttribute(ClientContext.CREDS_PROVIDER, cProvider);
    			httpClient = AndroidHttpClient.newInstance("test user agant");
    			//Auth End
    			//TimeOutSetting
    			HttpParams httpParams = new BasicHttpParams();
    			HttpConnectionParams.setConnectionTimeout(httpParams, timeOutConnection);
    			HttpConnectionParams.setSoTimeout(httpParams, timeOutSocket);
    			//TimeOut End
     			HttpPost httppost = new HttpPost(url);
     			UrlEncodedFormEntity mesag = new UrlEncodedFormEntity(params,"UTF-8");
     			mesag.setContentEncoding(HTTP.UTF_8);    //強制編碼成UTF-8
     			httppost.setEntity(mesag);
     			httppost.setParams(httpParams);
     			HttpResponse httpResponse = httpClient.execute(httpHost,httppost,credContext);
     			//檢查Http Response
    			if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
    				httpRequestCode = httpResponse.getStatusLine().getStatusCode();
    			httpRequestCode = httpResponse.getStatusLine().getStatusCode();
     			HttpEntity httpEntity = httpResponse.getEntity();
     			is = httpEntity.getContent();
     		}
        }
     	catch (SocketTimeoutException e) {
     		httpRequestCode = 900;
     		e.printStackTrace();
     	}
     	catch (ConnectTimeoutException e) {
     		httpRequestCode = 900;
     		e.printStackTrace();
		}
       	catch (UnsupportedEncodingException e){
    		e.printStackTrace();
    	}
    	catch (ClientProtocolException e){
    		e.printStackTrace();
    	}
    	catch (IOException e){
    		e.printStackTrace();
    	}
     	if (httpRequestCode != HttpStatus.SC_OK)
    		return null;
    	try{
    		BufferedReader reader = new BufferedReader(new InputStreamReader(is,"utf-8"),8);
    		StringBuilder sb = new StringBuilder();
    		String line = null;
    		while ((line = reader.readLine()) != null){
    			sb.append(line + "\n");
    		}
    		is.close();
    		json = sb.toString();
    	}
    	catch (Exception e){
    		Log.e("Buffer Error","Error Converting Result"+e.toString());
    	}

    	//try parse a string to a JSON object
    	try{
    		jObj = new JSONObject(json);
    	}
    	catch(JSONException e) {
    		Log.e("JSON Parser","Error parsing data" +e.toString());
    	}
    	//return JSON String
    	if (httpClient != null) httpClient.close();
    	return jObj;
    }

    //For Test
    //function get json from URL
    public JSONObject httpRequestRest(String url, String method, List<NameValuePair> params){
    	boolean isOnline = networkState.checkInternet();
    	if (!isOnline) return null;
		//Making Http Request
    	AndroidHttpClient httpClient = null;
    	try{
    		if (method.equals("GET")){
    			//defaultHttpClient
    			//DefaultHttpClient HttpClient = new DefaultHttpClient();
    			//hostUrl += "&action=" +action;    //網址放後面版本
    			String paramString = URLEncodedUtils.format(params, "utf-8");
    			url += "&" + paramString;    //&本來是?
    			//Auth uses
    			URL urlObj = new URL(url);    //URL Object
    			HttpHost httpHost = new HttpHost(urlObj.getHost(), urlObj.getPort(), urlObj.getProtocol());
    			AuthScope scope = new AuthScope(urlObj.getHost(), urlObj.getPort());
    			UsernamePasswordCredentials creds = new UsernamePasswordCredentials(nuclearLogin, nuclearPw);
    			CredentialsProvider cProvider = new BasicCredentialsProvider();
    			cProvider.setCredentials(scope, creds);
    			HttpContext credContext = new BasicHttpContext();
    			credContext.setAttribute(ClientContext.CREDS_PROVIDER, cProvider);
    			httpClient = AndroidHttpClient.newInstance("test user agant");
    			//Auth End
    			//TimeOutSetting
    			HttpParams httpParams = new BasicHttpParams();
    			HttpConnectionParams.setConnectionTimeout(httpParams, timeOutConnection);
    			HttpConnectionParams.setSoTimeout(httpParams, timeOutSocket);
    			//TimeOut End
    			HttpGet httpGet = new HttpGet(url);
    			httpGet.setParams(httpParams);
    			HttpResponse httpResponse = httpClient.execute(httpHost,httpGet,credContext);
    			//檢查Http Response
    			if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
    				httpRequestCode = httpResponse.getStatusLine().getStatusCode();
    				Log.e("JSON", "Http Response is unsuccessful.");
    				Log.e("Http Request", ""+httpResponse.getStatusLine().getStatusCode());
    				Log.e("Http Rest Request Url=>", url);
    			}
    			httpRequestCode = httpResponse.getStatusLine().getStatusCode();
    			HttpEntity httpEntity = httpResponse.getEntity();
    			is = httpEntity.getContent();
    		}
    		else if (method.equals("POST")){
    			//defaultHttpClient
    			//DefaultHttpClient HttpClient = new DefaultHttpClient();
    			//hostUrl += "&action=" +action;    //網址放後面版本
    			//Auth uses
    			URL urlObj = new URL(url);    //URL Object
    			HttpHost httpHost = new HttpHost(urlObj.getHost(), urlObj.getPort(), urlObj.getProtocol());
    			AuthScope scope = new AuthScope(urlObj.getHost(), urlObj.getPort());
    			UsernamePasswordCredentials creds = new UsernamePasswordCredentials(nuclearLogin, nuclearPw);
    			CredentialsProvider cProvider = new BasicCredentialsProvider();
    			cProvider.setCredentials(scope, creds);
    			HttpContext credContext = new BasicHttpContext();
    			credContext.setAttribute(ClientContext.CREDS_PROVIDER, cProvider);
    			httpClient = AndroidHttpClient.newInstance("test user agant");
    			//Auth End
    			//TimeOutSetting
    			HttpParams httpParams = new BasicHttpParams();
    			HttpConnectionParams.setConnectionTimeout(httpParams, timeOutConnection);
    			HttpConnectionParams.setSoTimeout(httpParams, timeOutSocket);
    			//TimeOut End
    			HttpPost httppost = new HttpPost(url);
    			UrlEncodedFormEntity mesag = new UrlEncodedFormEntity(params,"UTF-8");
    			mesag.setContentEncoding(HTTP.UTF_8);    //強制編碼成UTF-8
    			httppost.setEntity(mesag);
    			httppost.setParams(httpParams);
    			HttpResponse httpResponse = httpClient.execute(httpHost,httppost,credContext);
    			//檢查Http Response
    			if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
    				Log.e("JSON", "Http Response is unsuccessful.");
    				Log.e("Http Request", ""+httpResponse.getStatusLine().getStatusCode());
    				Log.e("Http Rest Request Url=>", url);
    				httpRequestCode = httpResponse.getStatusLine().getStatusCode();
    				return null;
    			}
    			httpRequestCode = httpResponse.getStatusLine().getStatusCode();
    			HttpEntity httpEntity = httpResponse.getEntity();
    			is = httpEntity.getContent();
    		}
    	}
    	catch (SocketTimeoutException e) {
     		httpRequestCode = 900;
     		e.printStackTrace();
     	}
     	catch (ConnectTimeoutException e) {
     		httpRequestCode = 900;
     		e.printStackTrace();
		}
    	catch (UnsupportedEncodingException e){
    		e.printStackTrace();
    	}
    	catch (ClientProtocolException e){
    		e.printStackTrace();
    	}
    	catch (IOException e){
    		//File no found exception
    		httpRequestCode = 901;
    		Log.e("JSON", "IO Exception");
    		JSONObject fileNotFound = null;
    		e.printStackTrace();
    		return fileNotFound;
    	}
    	if (httpRequestCode != HttpStatus.SC_OK)
    		return null;
    	try{
    		BufferedReader reader = new BufferedReader(new InputStreamReader(is,"utf-8"),8);
    		StringBuilder sb = new StringBuilder();
    		String line = null;
    		while ((line = reader.readLine()) != null){
    			sb.append(line + "\n");
    		}
    		is.close();
    		json = sb.toString();
    	}
    	catch (Exception e){
    		Log.e("Buffer Error","Error Converting Result"+e.toString());
    	}
    	//try parse a string to a JSON object
    	try{
    		jObj = new JSONObject(json);
    	}
    	catch(JSONException e) {
    		Log.e("JSON Parser","Error parsing data" +e.toString());
    	}
    	//return JSON String
    	if (httpClient != null) httpClient.close();
    	return jObj;
    }

    //To get HTTP Request Code
    public int getHttpResponseCode() {
    	return httpRequestCode;
    }

}
