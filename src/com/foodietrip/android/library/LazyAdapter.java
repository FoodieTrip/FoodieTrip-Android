package com.foodietrip.android.library;

import java.util.ArrayList;
import java.util.HashMap;

import com.actionbarsherlock.app.SherlockActivity;
import com.foodietrip.android.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

public class LazyAdapter extends BaseAdapter{
    private SherlockActivity activity;
    private ArrayList<HashMap<String, String>> data;
    private static LayoutInflater inflater = null;
    
    public LazyAdapter(SherlockActivity _activity, ArrayList<HashMap<String, String>> _data) {
    	activity = _activity;
    	data = _data;
    	inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
			view = inflater.inflate(R.layout.message_row, null);
		TextView mesAuthor = (TextView) view.findViewById(R.id.message_name);
		TextView mesTimes = (TextView) view.findViewById(R.id.message_time);
		TextView mesWords = (TextView) view.findViewById(R.id.message_mes);
		RatingBar mesRating = (RatingBar) view.findViewById(R.id.message_rating);
		HashMap<String, String> message = new HashMap<String, String>();
		message = data.get(position);
		//將數值資料設定於ListView上
		mesAuthor.setText(message.get("Author"));
		mesTimes.setText(message.get("Times"));
		mesWords.setText(message.get("Messages"));
		float rating = Float.parseFloat(message.get("Rating"));
		mesRating.setRating(rating);
		return view;
	}
	
	public ArrayList<HashMap<String, String>> getAdapterData() {
		return data;
	}
	
}
