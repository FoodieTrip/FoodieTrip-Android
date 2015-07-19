package com.foodietrip.android.library;

import com.facebook.Session;

import android.content.Context;

public class FacebookHelper {
 
	Context context;
    public FacebookHelper(Context _context) {
    	context = _context;
    }
	
    public boolean isFacebookLogined() {
    	Session session = Session.getActiveSession();
    	if (session != null && session.isOpened()) {
    		//Toast.makeText(context, "Facebook Loggin!", Toast.LENGTH_SHORT).show();
    		return true;
    	}
    	else {
    		//Toast.makeText(context, "Facebook Logout", Toast.LENGTH_SHORT).show();
    		return false;
    	}
    }
    
    public void logoutFromFacebook() {
    	Session session = Session.getActiveSession();
    	if (session != null && session.isOpened()) 
    	    session.closeAndClearTokenInformation();
    }
    
    
}
