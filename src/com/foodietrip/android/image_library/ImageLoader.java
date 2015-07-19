package com.foodietrip.android.image_library;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.net.HttpURLConnection;

import com.foodietrip.android.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ImageLoader {
    MemoryCache memoryCache = new MemoryCache();
    FileCache fileCache;
    ExecutorService executorService;
    public ImageLoader(Context context) {
    	fileCache = new FileCache(context);
    	executorService = Executors.newFixedThreadPool(5);
    }
    
    final int stub_id = R.drawable.ic_menu_goto;    //「沒有圖片」的圖片
    
    public void DisplayImage (String url) {
    	Bitmap bitmap = memoryCache.get(url);
    	if (bitmap == null) queuePhoto(url);
    }
    
    private void queuePhoto(String url) {
    	PhotoToLoad pLoad = new PhotoToLoad(url);
    	executorService.submit(new PhotoLoader(pLoad));
    }
    
    public Bitmap getBitmap(String url) {
    	Bitmap bmp_cache = memoryCache.get(url);
    	if(bmp_cache != null)
    		return bmp_cache;
    	File file = fileCache.getFile(url);
    	//From Disk cache
    	Bitmap bmp_diskCache = decodeFile(file);
    	if (bmp_diskCache != null) {
    		memoryCache.put(url, bmp_diskCache);
    		return bmp_diskCache;
    	}
    	//From Internet URL
    	try {
    		Bitmap bitmap = null;
    		URL imageUrl = new URL(url);
    		HttpURLConnection connection = (HttpURLConnection) imageUrl.openConnection();
    		connection.setConnectTimeout(30000);
    		connection.setReadTimeout(30000);
    		connection.setInstanceFollowRedirects(true);
    		InputStream inputStream = connection.getInputStream();
    		OutputStream outputStream = new FileOutputStream(file);
    		Utils.CopyStream(inputStream, outputStream);
    		outputStream.close();
    		bitmap = decodeFile(file);
    		memoryCache.put(url, bitmap);
    		return bitmap;
    	}
    	catch (Throwable t) {
    		t.printStackTrace();
    		if (t instanceof OutOfMemoryError)
    			memoryCache.clear();
    		return null;
    	}
    }
    
    //decodes image and scale it to reduce memory consumption
    private Bitmap decodeFile(File file) {
    	try {
    		//decode Image File
    		BitmapFactory.Options options = new BitmapFactory.Options();
    		options.inJustDecodeBounds = true;
    		BitmapFactory.decodeStream(new FileInputStream(file), null, options);
    		//find the correct scale value. It should be power of 2
    		/*final int REQUIRE_SIZE = 70;
    		int width_tmp = options.outWidth, height_tmp = options.outHeight;
    		int scale = 1;
    		while (true) {
				if (width_tmp/2 < REQUIRE_SIZE || height_tmp/2 < REQUIRE_SIZE)
                    break;
				width_tmp /= 2;
				height_tmp /=2;
				scale *= 2;
			}*/
    		//decode with inSampleSize
    		BitmapFactory.Options options2 = new BitmapFactory.Options();
    		options2.inPurgeable = true;    //如果記憶體不足可以先行回收
    		options2.inInputShareable = true;
    		options2.inPreferredConfig = Bitmap.Config.RGB_565;
    		return BitmapFactory.decodeStream(new FileInputStream(file), null, options2);
    	}
    	catch (FileNotFoundException e) {}
    	return null;
    }
    
    //Task for the queue
    private class PhotoToLoad {
    	public String url;
    	public PhotoToLoad(String _url) {
    		url = _url;
    	}
    }
    
    class PhotoLoader implements Runnable {
        PhotoToLoad photoToLoad;
        PhotoLoader(PhotoToLoad _photoToLoad) {
			this.photoToLoad = _photoToLoad;
		}
        
		@Override
		public void run() {
			Bitmap bmp = getBitmap(photoToLoad.url);
			memoryCache.put(photoToLoad.url, bmp);
		}
    }
    
    public void clearCache() {
    	memoryCache.clear();
    	fileCache.clear();
    }
}
