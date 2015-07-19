package com.foodietrip.android.library;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkState {
	Context context;
    public NetworkState(Context _context) {
    	context = _context;
    }
    
	/*檢查網路狀態Method*/
	public boolean checkInternet(){
		boolean result = false;
		ConnectivityManager connect_manager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netinfo = connect_manager.getActiveNetworkInfo();    //網路狀態
		if (netinfo == null || !netinfo.isConnected())
			result = false;
		else{
			if (!netinfo.isAvailable())
				result = false;
			else
				result = true;
		}
		return result;
	}
	
	public boolean checkWifiState() {
		ConnectivityManager connectManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifi_networkInfo = connectManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (wifi_networkInfo.isConnected()) {
			return true;
		}
		return false;
	}
}
