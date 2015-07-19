package com.foodietrip.android.library;

import com.foodietrip.android.R;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TutorialViewPager extends PagerAdapter {
	Context context;
	int[] tutorialImages = {R.drawable.tutorial_1, R.drawable.tutorial_2, R.drawable.tutorial_3, R.drawable.tutorial_4, R.drawable.tutorial_5};
    int[] tutorialTexts = {R.string.tutorial_page1, R.string.tutorial_page2, R.string.tutorial_page3, R.string.tutorial_page4, R.string.tutorial_page5};
	int finalPosition;
	public TutorialViewPager(Context _context) {
    	context = _context;
    	finalPosition = tutorialImages.length + 1;
    }
    
	@Override
	public int getCount() {
		return tutorialImages.length + 1;
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
		if (position < tutorialImages.length) {
			ImageView imageView = new ImageView(context);
			imageView.setAdjustViewBounds(true);
			imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
			imageView.setImageResource(tutorialImages[position]);
			TextView textView = new TextView(context);
			textView.setText(tutorialTexts[position]);
			textView.setLayoutParams(layoutParams);
			textView.setTextColor(Color.WHITE);
			textView.setGravity(Gravity.CENTER_HORIZONTAL);
			textView.setPadding(0, 16, 0, 0);
			textView.setTextSize(19);
			linearLayout.addView(imageView);
			linearLayout.addView(textView);
		}
		else {
			TextView txt_finish = new TextView(context);
			txt_finish.setText(R.string.tutorial_finish);
			txt_finish.setLayoutParams(layoutParams);
			txt_finish.setTextColor(Color.WHITE);
			txt_finish.setGravity(Gravity.CENTER_HORIZONTAL);
			txt_finish.setTextSize(24);
			linearLayout.addView(txt_finish);
		}
		((ViewPager) container).addView(linearLayout, 0);
		return linearLayout;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		((ViewPager) container).removeView((LinearLayout) object);
	}
	
}
