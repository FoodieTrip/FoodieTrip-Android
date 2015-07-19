package com.foodietrip.android.image_library;

import java.io.File;

import android.content.Context;

public class FileCache {
    private File cacheDir;
    public FileCache(Context context) {
    	//Find the dir to save cached images
    	if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
    		cacheDir = new File(android.os.Environment.getExternalStorageDirectory(), "foodietrip_cache");
    	else
    		cacheDir = context.getCacheDir();
    	if (!cacheDir.exists()) cacheDir.mkdirs();
    }

    public File getFile(String url) {
    	//Identity images by hashcode. Not a good way should be modify
    	String fileName = String.valueOf(url.hashCode());
    	//Or use String fileName = URLEncoder.encode(url);
    	File file = new File(cacheDir, fileName);
    	return file;
    }

    public void clear() {
    	File[] files = cacheDir.listFiles();
    	if (files == null)
    		return;
    	else {
    		for(File file:files) {
    			file.delete();
    		}
    	}
    }

}
