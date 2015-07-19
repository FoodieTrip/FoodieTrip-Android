package com.foodietrip.android.image_library;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import android.graphics.Bitmap;


public class MemoryCache {
    //private static final String TAG = "MemoryCache";
    //Last Argument true for LRU ordering
    private Map<String,Bitmap> cache = 
    		Collections.synchronizedMap(new LinkedHashMap<String, Bitmap>(10, 1.5f, true)); 
    private long size = 0;    //Current allocate size
    private long limit = 1000000;    //max memory in bytes
    public MemoryCache() {
    	//use 25 percents of available heap size
    	setLimit(Runtime.getRuntime().maxMemory()/4);
    }
    
    public void setLimit(long newLimit) {
    	limit = newLimit;
    	//Log.i(TAG, "Memory Cache will use up to: " +limit/1024./1024.+"MB");
    }
    
    public Bitmap get(String id) {
    	try {
    		if (!cache.containsKey(id)) return null;
    		//NullPointerException sometimes happens here
    		return cache.get(id);
    	}
    	catch (NullPointerException e) {
    		e.printStackTrace();
    		return null;
    	}
    }
    
    public void put(String id,Bitmap bitmap) {
    	try {
    		if (cache.containsKey(id))
    			size -= getSizeInBytes(cache.get(id));
    		cache.put(id, bitmap);
    		size += getSizeInBytes(bitmap);
    		checkSize();
    	}
    	catch (Throwable t) {
    		t.printStackTrace();
    	}
    }
    
    private void checkSize() {
    	//Log.i(TAG, "cache size = " +size +"length = " +cache.size());
    	if (size > limit) {
    		//least recently accessed item will be the first one integrated
    		Iterator<Entry<String,Bitmap>> iter = cache.entrySet().iterator();
    		while (iter.hasNext()) {
				Entry<String, Bitmap> entry = iter.next();
				size -= getSizeInBytes(entry.getValue());
				iter.remove();
				if (size <=limit) break;
			}
    		//Log.i(TAG, "Clean cache. New size :" +cache.size());
    	}
    }
    
    public void clear() {
    	try {
    		//NullPointerException sometimes happen here
    		cache.clear();
    		size = 0;
    	}
    	catch (NullPointerException e) {
    		e.printStackTrace();
    	}
    }
    
    long getSizeInBytes(Bitmap bitmap) {
    	if (bitmap == null)
    		return 0;
    	return bitmap.getRowBytes() * bitmap.getHeight();
    }
}
