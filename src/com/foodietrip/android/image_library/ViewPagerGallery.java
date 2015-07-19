package com.foodietrip.android.image_library;

import java.util.ArrayList;
import java.util.HashMap;

import com.foodietrip.android.R;
import com.foodietrip.android.library.ImageViewHelper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ViewPagerGallery extends PagerAdapter {
    Context context;
    ArrayList<HashMap<String, String>> storeImage;
    ImageLoader imageLoader;
    DisplayMetrics dm;
	public ViewPagerGallery(Context _context, ArrayList<HashMap<String, String>> _storeImage, DisplayMetrics _dm) {
    	context = _context;
    	storeImage = _storeImage;
    	imageLoader = new ImageLoader(_context);
    	dm = _dm;
    }

	@Override
	public int getCount() {
		return storeImage.size();
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == ((LinearLayout) object);
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		LinearLayout linearLayout = new LinearLayout(context);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		linearLayout.setLayoutParams(layoutParams);
		linearLayout.setGravity(Gravity.CENTER);
		linearLayout.setOrientation(LinearLayout.VERTICAL);
		ImageView imageView = new ImageView(context);
		imageView.setAdjustViewBounds(true);
		imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
		String currentUrl = storeImage.get(position).get("bigSizeUrl");
		TextView textview = new TextView(context);
		textview.setText(storeImage.get(position).get("message"));
		textview.setTextSize(20);
		textview.setLayoutParams(layoutParams);
		textview.setTextColor(Color.WHITE);
		ProgressBar progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleLarge);
		String iID = storeImage.get(position).get("photoId");
		String sID = storeImage.get(position).get("storeId");
		DownloadImageFromUrl imageDownloader = new DownloadImageFromUrl(imageView, progressBar, iID, sID);
		imageDownloader.execute(currentUrl);
		linearLayout.addView(imageView);
		linearLayout.addView(progressBar);
		linearLayout.addView(textview);
		((ViewPager) container).addView(linearLayout, 0);
		return linearLayout;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		((ViewPager) container).removeView((LinearLayout) object);
	}
	
	class DownloadImageFromUrl extends AsyncTask<String, Void, Bitmap> {
		ImageView imageView;
		ProgressBar progressBar;
		String iID,sID;
        public DownloadImageFromUrl(ImageView currentImageView, ProgressBar _progressBar, String _iID, String _sID) {
        	imageView = currentImageView;
        	progressBar = _progressBar;
        	iID = _iID;
        	sID = _sID;
        }
        
		@Override
		protected void onPreExecute() {
			imageView.setVisibility(View.GONE);
		}

		@Override
		protected Bitmap doInBackground(String... urls) {
			Bitmap bitmap = imageLoader.getBitmap(urls[0]);
			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
            if (imageView != null) {
            	imageView.setImageBitmap(bitmap);
            	new ImageViewHelper(context, dm, imageView, bitmap);
            	Animation fade_animation = AnimationUtils.loadAnimation(context, R.anim.fadein);
            	imageView.setVisibility(View.VISIBLE);
            	imageView.setAnimation(fade_animation);
            	progressBar.setVisibility(View.GONE);
            }
		}
		
	}
	
}
