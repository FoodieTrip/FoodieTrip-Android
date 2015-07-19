package com.foodietrip.android;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class AddItem extends SherlockFragment {
	Bundle catagory;
	ArrayList<HashMap<String, String>> bigCatagory,smallCatagory;
	ArrayList<String> smallTags;
	EditText input_Address,input_Township, input_County;
	TextView address_sample,txt_warningTags,bigTagValueCurrrent;
	LinearLayout bigCataView;
	TableLayout smallCataView;
	String input_country = null , current = "", smallTagsString, warningTagsString;
	int sample_loaded = 0, bigTagCatagoryValue;
	int[] bigCataChoosed;
	SharedPreferences addItemPre;
	Animation pushEffect;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		if (savedInstanceState != null)
			smallTags = savedInstanceState.getStringArrayList("smallSelected");
	}
	@SuppressWarnings("unchecked")
	@Override
	public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceBundle) {
		View view = inflater.inflate(R.layout.add_item, container,false);
		catagory = getActivity().getIntent().getExtras();
		if (catagory != null) {
			bigCatagory = (ArrayList<HashMap<String,String>>) catagory.getSerializable("BigCatagory");
			smallCatagory = (ArrayList<HashMap<String,String>>) catagory.getSerializable("SmallCatagory");
		}
		addItemPre = getActivity().getSharedPreferences("addItem_tmp", 0);
		//從介面取得物件
		input_Address = (EditText)view.findViewById(R.id.editText_address);
		input_Township = (EditText)view.findViewById(R.id.editText_township);
		input_County = (EditText)view.findViewById(R.id.editText_county);
		address_sample = (TextView)view.findViewById(R.id.addItem_address_sample);
		txt_warningTags = (TextView)view.findViewById(R.id.addItem_warning_tags);
		warningTagsString = getActivity().getResources().getString(R.string.tagsTips);
		txt_warningTags.setText(warningTagsString);
        bigCataView = (LinearLayout)view.findViewById(R.id.additem_BigTagsLayout);
        smallCataView = (TableLayout)view.findViewById(R.id.additem_SmallTagsLayout);
        bigTagValueCurrrent = (TextView)view.findViewById(R.id.addItem_current_Bigtag);
        smallTags = new ArrayList<String>();
        bigCataChoosed = new int[bigCatagory.size()+1];
        for (int i=0;i<bigCatagory.size()+1;i++)
        	bigCataChoosed[i] = 0;
        //設定延遲幾秒再讀取Bundle
        if (sample_loaded == 0) {
        	Handler myhandler = new Handler();
  	  		myhandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					current = getActivity().getString(R.string.ItemP1_currentCountry);
					String txt_AddressSample = addItemPre.getString("sampleAdd1", "");
					String txt_Township = addItemPre.getString("sampleAdd2","");
					String txt_Address = addItemPre.getString("sampleAdd3","");
					String txt_Country = addItemPre.getString("sampleAdd4","");
					addItemPre = getActivity().getSharedPreferences("addItem_tmp", 0);
					Editor inputWorker = addItemPre.edit();
					inputWorker.putString("sTownship", txt_Township);
					inputWorker.putString("sLocation", txt_Address);
					inputWorker.commit();
					if (txt_AddressSample != null && !txt_AddressSample.equals(""))
						address_sample.setText(txt_AddressSample);
					else
						address_sample.setText(getActivity().getString(R.string.ItemTab_dataGetError));
					if (txt_Township != null && !txt_Township.equals(""))
						input_Township.setText(txt_Township);
					if (txt_Address != null && !txt_Address.equals(""))
						input_Address.setText(txt_Address);
					if (txt_Country != null && !txt_Country.equals(""))
						current = txt_Country;
					else
						current = getActivity().getString(R.string.ItemP1_plzChooseCountry);
					input_County.setText(current);
				}
			}, 700);
        	sample_loaded = 1;
        }
        else {
			address_sample.setText(addItemPre.getString("sampleAdd1", getActivity().getString(R.string.ItemTab_dataGetError)));
        }
        prepareBigCata();
        pushEffect = AnimationUtils.loadAnimation(getActivity(), R.anim.image_effect);
        return view;
	}

	/*訊息置入小幫手*/
	public void informationGeter() {
		addItemPre = getActivity().getSharedPreferences("addItem_tmp", 0);
		Editor inputWorker = addItemPre.edit();
		inputWorker.putString("mcID", String.valueOf(bigTagCatagoryValue));
		inputWorker.putString("scID", smallTagsString);
		inputWorker.commit();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.forceinput, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_forceinput:
			informationGeter();
			Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.allChangesAreSaved), Toast.LENGTH_SHORT).show();
			//Toast.makeText(getActivity(), "test", Toast.LENGTH_SHORT).show();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void prepareBigCata() {
		final RadioButton[] radioButton = new RadioButton[bigCatagory.size()];
		RadioGroup radioGroup = new RadioGroup(getActivity().getApplicationContext());
		radioGroup.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
		radioGroup.setOrientation(RadioGroup.HORIZONTAL);
		for (int i=0 ; i<bigCatagory.size(); i++) {
			final int position = i;
			radioButton[i] = new RadioButton(getActivity().getApplicationContext());
			radioButton[i].setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
			radioButton[i].setText(bigCatagory.get(i).get("Name"));
			radioButton[i].setTextColor(Color.BLACK);
			radioButton[i].setTextSize(24);
			radioButton[i].setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if (isChecked) {
						bigTagCatagoryValue = Integer.parseInt(bigCatagory.get(position).get("mcID"));
						prepareSmallCata();
						if (bigCataChoosed[bigTagCatagoryValue] == 0 && smallTags.size() >= 1)
							txt_warningTags.setVisibility(View.VISIBLE);
						else
							txt_warningTags.setVisibility(View.GONE);
						//smallTags.clear();
						//Toast.makeText(getActivity().getApplicationContext(), "Check id = " +bigTagCatagoryValue, Toast.LENGTH_SHORT).show();
					}
				}
			});
			radioButton[i].setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					radioButton[position].startAnimation(pushEffect);
				}
			});
			radioButton[i].setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					radioButton[position].startAnimation(pushEffect);
					String basic_string = getActivity().getResources().getString(R.string.Tag_select_current);
					String selected_value = bigCatagory.get(position).get("Name");
					addItemPre = getActivity().getSharedPreferences("addItem_tmp", 0);
					Editor inputWorker = addItemPre.edit();
					inputWorker.putString("mcID", String.valueOf(bigTagCatagoryValue));
					inputWorker.commit();
					bigTagValueCurrrent.setText(basic_string+" "+selected_value);
					return true;
				}
			});
			radioGroup.addView(radioButton[i]);
		}
		bigCataView.addView(radioGroup);
	}

	private void prepareSmallCata() {
		if (smallCataView != null) smallCataView.removeAllViews();
		int matchTimes = 0;
		boolean firstTime = true;
		TableRow tableRow = null;
		final ToggleButton[] toggleButton = new ToggleButton[smallCatagory.size()];
		for (int i= 0 ; i < smallCatagory.size() ; i++) {
			final int position = i;
			int mcID = Integer.valueOf(smallCatagory.get(i).get("mcID"));
			final int mcID_now = mcID;
			if (matchTimes == 3 || firstTime) {
				if(!firstTime) smallCataView.addView(tableRow, new TableLayout.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
				tableRow = new TableRow(getActivity().getApplicationContext());
				tableRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
			    firstTime = false;
			    matchTimes = 0;
			}
			if (mcID == bigTagCatagoryValue) {
				toggleButton[i] = new ToggleButton(getActivity().getApplicationContext());
				toggleButton[i].setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
				String text = smallCatagory.get(i).get("Name");
				String scIDnumber = smallCatagory.get(i).get("scID");
				toggleButton[i].setText(text);
				toggleButton[i].setTextOn(text);
				toggleButton[i].setTextOff(text);
				toggleButton[i].setTextSize(19);
				toggleButton[i].setTextColor(Color.BLACK);
				toggleButton[i].setBackgroundColor(Color.TRANSPARENT);
				toggleButton[i].setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						String checkedvaule = smallCatagory.get(position).get("scID");
						boolean isRepeat = false;
						if (isChecked) {
							for (String tag:smallTags)
								if (checkedvaule.equals(tag)) isRepeat = true;
                            if (!isRepeat) {
                            	smallTags.add(checkedvaule);
                            	toggleButton[position].setBackgroundResource(R.drawable.detail_corner);
                            	bigCataChoosed[mcID_now]++;
                            }
						}
						else{
                            smallTags.remove(checkedvaule);
                            toggleButton[position].setBackgroundColor(Color.TRANSPARENT);
                            bigCataChoosed[mcID_now]--;
						}
						smallTagsString = getSmallString();
						addItemPre = getActivity().getSharedPreferences("addItem_tmp", 0);
						Editor inputWorker = addItemPre.edit();
						inputWorker.putString("scID", smallTagsString);
						inputWorker.commit();
					    //Toast.makeText(getActivity().getApplicationContext(), smallTagsString, Toast.LENGTH_SHORT).show();
					}
				});
				toggleButton[i].setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						toggleButton[position].startAnimation(pushEffect);
					}
				});
				for (int j=0 ; j<smallTags.size() ; j++) {
					String tempHasChoose = smallTags.get(j);
					if (scIDnumber.equals(tempHasChoose)) {
						toggleButton[i].setChecked(true);
						toggleButton[i].setBackgroundResource(R.drawable.detail_corner);
					}
				}
				tableRow.addView(toggleButton[i]);
				matchTimes++;
		    }
		}
		smallCataView.addView(tableRow, new TableLayout.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
	}

    private String getSmallString() {
    	String smallTagString;
    	if (smallTags.size() == 0) {
    		smallTagString = "";
    		return smallTagString;
    	}
    	ArrayList<Integer> arrayOfInt = new ArrayList<Integer>();
    	for (Object str : smallTags) {
    		arrayOfInt.add(Integer.parseInt((String)str));
    	}
    	Collections.sort(arrayOfInt);
    	smallTagString = arrayOfInt.toString();
    	smallTagString = smallTagString.substring(1, smallTagString.length() - 1);
    	return smallTagString;
    }

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putStringArrayList("smallSelected", smallTags);
	}
}
