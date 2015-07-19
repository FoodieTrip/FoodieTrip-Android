package com.foodietrip.android.library;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import com.actionbarsherlock.app.SherlockActivity;
import com.foodietrip.android.R;
import com.foodietrip.android.image_library.ImageLoader;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

public class AllItemAdapter extends BaseAdapter{
	private SherlockActivity activity;
	private ArrayList<HashMap<String, String>> data;
	private static LayoutInflater inflater = null;
	Context context;
	ImageLoader imageLoader;
	String status1,status2,status3;
	public AllItemAdapter(SherlockActivity _activity, ArrayList<HashMap<String,String>> _data,Context _context) {
    	activity = _activity;
    	data = _data;
    	context = _context;
    	inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader = new ImageLoader(_context);
        status1 = context.getResources().getString(R.string.store_status_code1);    //move_away
        status2 = context.getResources().getString(R.string.store_status_code2);    //rest
        status3 = context.getResources().getString(R.string.store_status_code3);    //Unavailable
    }
	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (convertView == null)
			view = inflater.inflate(R.layout.list_item, null);
		TextView sID = (TextView) view.findViewById(R.id.listViewRow_sid);
		final ImageView storeImage = (ImageView) view.findViewById(R.id.listViewRow_imageView);
		storeImage.setLayoutParams(new android.widget.LinearLayout.LayoutParams(getPixels(96), getPixels(96)));
		storeImage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Animation effect = AnimationUtils.loadAnimation(context, R.anim.image_effect);
				storeImage.startAnimation(effect);
			}
		});
		ImageView storeOpen = (ImageView) view.findViewById(R.id.listViewRow_open);
		ImageView storeClose = (ImageView) view.findViewById(R.id.listViewRow_closed);
		ImageView storeNotYet = (ImageView) view.findViewById(R.id.listViewRow_NotYet);
		storeOpen.setVisibility(View.GONE);
		storeClose.setVisibility(View.GONE);
		storeNotYet.setVisibility(View.GONE);
		TextView sName = (TextView) view.findViewById(R.id.listViewRow_sName);
		//TextView status = (TextView) view.findViewById(R.id.listViewRow_status);
		RatingBar storeRating = (RatingBar) view.findViewById(R.id.listViewRow_rating);
		TextView bigTag = (TextView) view.findViewById(R.id.listViewRow_bigTag);
		TextView smallTags = (TextView) view.findViewById(R.id.listViewRow_smallTag);
		TextView sDistance = (TextView) view.findViewById(R.id.listViewRow_distance);
		ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.listViewRow_progressBar);
		progressBar.setLayoutParams(new android.widget.LinearLayout.LayoutParams(getPixels(96), getPixels(96)));
		HashMap<String, String> storeHashMap = new HashMap<String, String>();
		storeHashMap = data.get(position);
		sID.setText(storeHashMap.get("sID"));
		sName.setText(storeHashMap.get("sName"));
		sName.setSelected(true);    //make marquee works
		bigTag.setText(storeHashMap.get("bigTags"));
		smallTags.setText(storeHashMap.get("smallTags"));
		sDistance.setText(storeHashMap.get("distance"));
		String ratingAvg = storeHashMap.get("Store_avg_rating");
		float ratingAvgFloat = Float.parseFloat(ratingAvg);
		String imageUrl = storeHashMap.get("Latest_image_path");
		storeRating.setRating(ratingAvgFloat);
		DownloadImageFromUrlList imageDownloader = new DownloadImageFromUrlList(storeImage, progressBar);
		imageDownloader.execute(imageUrl);
		String startTime = storeHashMap.get("startTime");
		String closeTime = storeHashMap.get("closeTime");
		if (!storeOpen.isShown() || !storeClose.isShown() || !storeNotYet.isShown()) {
			TimeWorker timeWorker = new TimeWorker(startTime, closeTime, storeOpen, storeClose, storeNotYet);
			timeWorker.execute();
		}
		return view;
	}

  	//GetDpiToPixels
  	public int getPixels(int dipValue) {
  		Resources resources = context.getResources();
  		int px = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, resources.getDisplayMetrics());
  		return px;
  	}

  	public ArrayList<HashMap<String, String>> getDataItems() {
  		return data;
  	}

  	class TimeWorker extends AsyncTask<String, Void, Integer> {
  		ImageView open, close, notYet;
  		String startTime,closeTime;
  		int STATE_OPEN = 0;
  		int STATE_CLOSE = 1;
  		int STATE_NOTYET = 2;
  		String formatedTime;
        public TimeWorker(String _openTime, String _endTime, ImageView _open, ImageView _close, ImageView _notYet) {
        	open = _open;
        	close = _close;
        	notYet = _notYet;
        	startTime = _openTime;
        	closeTime = _endTime;
        }
		@SuppressLint("SimpleDateFormat")
		@Override
		protected void onPreExecute() {
      		open.setVisibility(View.GONE);
      		close.setVisibility(View.GONE);
      		notYet.setVisibility(View.GONE);
    	    Calendar calendar = Calendar.getInstance();
    		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
    		formatedTime = timeFormat.format(calendar.getTime());
			super.onPreExecute();
		}
		@SuppressLint("SimpleDateFormat")
		@Override
		protected Integer doInBackground(String... params) {
			int mode = STATE_NOTYET;
            //String is24HoursAvaliable = data.get(position).get("is24Hours");
            if (startTime.trim().length() > 0 && closeTime.trim().length() > 0) {
            	try {
            		String closeTimeCompareString = closeTime;
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                    Date startDate = timeFormat.parse(startTime);
                    if (closeTime.equals("0:00")||closeTime.equals("00:00"))
                    	closeTimeCompareString = "24:00";
                    Date closeDate = timeFormat.parse(closeTimeCompareString);
                    Date nowDate = timeFormat.parse(formatedTime);
                    int NowToStart = nowDate.compareTo(startDate);
                    int NowToClose = nowDate.compareTo(closeDate);
                    if (NowToStart > 0 && NowToClose < 0)
                    	mode = STATE_OPEN;
                    else
                    	mode = STATE_CLOSE;
            	}
            	catch (ParseException e) {
					e.printStackTrace();
				}
            }
        	else {
        		mode = STATE_NOTYET;
        	}
            /*if (is24HoursAvaliable.equals("1"))
            	open.setVisibility(View.VISIBLE);
            else {
            }*/
			return mode;
		}
		@Override
		protected void onPostExecute(Integer result) {
			switch (result) {
			case 0:
				open.setVisibility(View.VISIBLE);
				break;
			case 1:
				close.setVisibility(View.VISIBLE);
				break;
			case 2:
				notYet.setVisibility(View.VISIBLE);
				break;
			}
			super.onPostExecute(result);
		}
  	}

	class DownloadImageFromUrlList extends AsyncTask<String, Void, Bitmap> {
        ProgressBar progressBar;
        ImageView imageView;
        final int stub_id = R.drawable.cat;
        public DownloadImageFromUrlList(ImageView _imageView,ProgressBar _processBar) {
        	progressBar = _processBar;
        	imageView = _imageView;
        }

		@Override
		protected Bitmap doInBackground(String... urls) {
			Bitmap bitmap = null;
			if (!urls[0].equals("") && urls[0].trim().length() > 0)
				bitmap = imageLoader.getBitmap(urls[0]);
			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if (imageView != null) {
				if (bitmap != null)
				    imageView.setImageBitmap(bitmap);
				else
					imageView.setImageResource(stub_id);
				Animation fade_animation = AnimationUtils.loadAnimation(context, R.anim.fadein);
				progressBar.setVisibility(View.GONE);
				imageView.setVisibility(View.VISIBLE);
				imageView.setAnimation(fade_animation);
			}
		}

	}
}
