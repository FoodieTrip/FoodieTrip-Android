package com.foodietrip.android;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class EditItem extends SherlockFragment {
	Bundle catagory;
	ArrayList<HashMap<String, String>> bigCatagory,smallCatagory;
	ArrayList<String> smallTags;
	LinearLayout bigCataView;
	List<String> org_SmallTagList;
	TableLayout smallCataView;
	EditText edt_township,edt_location,edt_County;
	TextView txt_name,txt_originalTags,txt_warning,txt_currentBigTagState;
	String country_now,current_name,smallTagsString, org_BigTags, org_SmallTags, org_SmallTags_number, status_original;
	String[] current_country;
	int switch_loading = 0, bigTagCatagoryValue;
	int[] bigCataChoosed;
  	SharedPreferences editItemPre;
  	boolean smallHaveLoaded = false, isFirstTime = true;
  	Animation pushEffect;
  	RadioGroup radioGroup;
  	RadioButton store_status0,store_status1,store_status2,store_status3;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		if (savedInstanceState != null)
			smallTags = savedInstanceState.getStringArrayList("smallSelected");
	}
  	@SuppressWarnings("unchecked")
	public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceBundle) {
  		View view = inflater.inflate(R.layout.edit_item, container,false);
  		catagory = getActivity().getIntent().getExtras();
  		if (catagory != null) {
			bigCatagory = (ArrayList<HashMap<String,String>>) catagory.getSerializable("BigCatagory");
			smallCatagory = (ArrayList<HashMap<String,String>>) catagory.getSerializable("SmallCatagory");
  		}
  		editItemPre = getActivity().getSharedPreferences("editItem_tmp", 0);
  		txt_name = (TextView)view.findViewById(R.id.textView_name_editItem);
  		txt_originalTags = (TextView)view.findViewById(R.id.original_tags);
  		edt_County = (EditText)view.findViewById(R.id.editText_county_editItem);
  		edt_township = (EditText)view.findViewById(R.id.editText_township_editItem);
  		edt_location = (EditText)view.findViewById(R.id.editText_address_editItem);
  		txt_warning = (TextView)view.findViewById(R.id.editItem_Warning);
  		txt_currentBigTagState = (TextView)view.findViewById(R.id.editItem_current_Bigtag);
  		radioGroup = (RadioGroup)view.findViewById(R.id.storestatus_radiogroup);
  		store_status0 = (RadioButton)view.findViewById(R.id.storestatus_0);
  		store_status1 = (RadioButton)view.findViewById(R.id.storestatus_1);
  		store_status2 = (RadioButton)view.findViewById(R.id.storestatus_2);
  		store_status3 = (RadioButton)view.findViewById(R.id.storestatus_3);
  		store_status0.setOnClickListener(storeChangerAnimator);
  		store_status1.setOnClickListener(storeChangerAnimator);
  		store_status2.setOnClickListener(storeChangerAnimator);
  		store_status3.setOnClickListener(storeChangerAnimator);
  		radioGroup.setOnCheckedChangeListener(storeStatusChanger);
  		String warnString = getActivity().getResources().getString(R.string.tagsTips);
  		txt_warning.setText(warnString);
  		org_SmallTags_number = editItemPre.getString("scIDNumber", "");
  		status_original = editItemPre.getString("status", "active");
  		smallTags = new ArrayList<String>();
  		if (!org_SmallTags_number.equals("")) {
  	  		org_SmallTagList = Arrays.asList(org_SmallTags_number.split("\\s*,\\s*"));
  	  		smallTags.addAll(org_SmallTagList);
  		}
        bigCataChoosed = new int[bigCatagory.size()+1];
        for (int i=0;i<bigCatagory.size()+1;i++)
        	bigCataChoosed[i] = 0;
  		//LinearLayout
  		bigCataView = (LinearLayout)view.findViewById(R.id.edititem_BigTagsLayout);
  		smallCataView = (TableLayout)view.findViewById(R.id.edititem_SmallTagsLayout);
  		//editItem_layout1_inner.setOnTouchListener(layout_listener);
  		if (switch_loading == 0) {
  			country_now = getActivity().getString(R.string.editItemP1_currentCountry);
			current_name = editItemPre.getString("sName", "");
	  		getActivity().setTitle(current_name);
	  		txt_name.setText(current_name);
	  		edt_township.setText(editItemPre.getString("sTownship", getActivity().getString(R.string.editItemP1_countryError)));
	  		edt_location.setText(editItemPre.getString("sLocation", getActivity().getString(R.string.editItemP1_countryError)));
	  		country_now = editItemPre.getString("sCountry", getActivity().getString(R.string.editItemP1_countryError));
		    edt_County.setText(country_now);
	  		String original_txtStart = getActivity().getResources().getString(R.string.originalTags);
	  		org_BigTags = editItemPre.getString("mcID", "");    //這是字串
	  		if (org_BigTags.equals("null")) {
	  			org_BigTags = getActivity().getResources().getString(R.string.tagDoesntSet);
	  		}
	  		org_SmallTags = editItemPre.getString("scID", "");
	  		String originalTagsString;
	  		if (!org_SmallTags.equals(""))
	  			originalTagsString = original_txtStart +"[" +org_BigTags +"]: " +org_SmallTags;
	  		else
	  			originalTagsString = original_txtStart +"[" +org_BigTags +"]";
	  		editItemPre.edit().putString("originalTagsString", originalTagsString).commit();
	  		txt_originalTags.setText(originalTagsString);
	  		if (status_original.equals("active")) store_status0.setChecked(true);
	  		else if (status_original.equals("move_away")) store_status1.setChecked(true);
	  		else if (status_original.equals("temp_rest")) store_status2.setChecked(true);
	  		else if (status_original.equals("unavailable")) store_status3.setChecked(true);
  	  		switch_loading = 1;
  		}
  		else {
	  		//從 Bundle 取得資料
	  		current_name = editItemPre.getString("sName", "");
	  		getActivity().setTitle(current_name);
	  		txt_name.setText(current_name);
	  		edt_township.setText(editItemPre.getString("sTownship", ""));
	  		edt_location.setText(editItemPre.getString("sLocation", ""));
	  		txt_originalTags.setText(editItemPre.getString("originalTagsString", ""));
  		}
  		prepareBigCata();
  		pushEffect = AnimationUtils.loadAnimation(getActivity(), R.anim.image_effect);
        return view;
  	}

  	private OnClickListener storeChangerAnimator = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.storestatus_0:
				store_status0.startAnimation(pushEffect);
				break;
            case R.id.storestatus_1:
				store_status1.startAnimation(pushEffect);
				break;
            case R.id.storestatus_2:
				store_status2.startAnimation(pushEffect);
				break;
            case R.id.storestatus_3:
				store_status3.startAnimation(pushEffect);
	            break;
			}
		}
	};

  	private RadioGroup.OnCheckedChangeListener storeStatusChanger = new RadioGroup.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			editItemPre = getActivity().getSharedPreferences("editItem_tmp", 0);
			Editor putWriter = editItemPre.edit();
            switch (checkedId) {
			case R.id.storestatus_0:
				putWriter.putString("status", "active");
				break;
            case R.id.storestatus_1:
            	putWriter.putString("status", "move_away");
				break;
            case R.id.storestatus_2:
            	putWriter.putString("status", "temp_rest");
				break;
            case R.id.storestatus_3:
                putWriter.putString("status", "unavailable");
	            break;
			}
			putWriter.commit();
		}
	};

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.forceinput, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_forceinput:
			Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.allChangesAreSaved), Toast.LENGTH_SHORT).show();
			//Toast.makeText(getActivity(), "Hello", Toast.LENGTH_SHORT).show();
			break;
		default:
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
			String buttonText = bigCatagory.get(i).get("Name");
			radioButton[i] = new RadioButton(getActivity().getApplicationContext());
			radioButton[i].setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
			radioButton[i].setText(buttonText);
			radioButton[i].setId(i);
			radioButton[i].setTextColor(Color.BLACK);
			radioButton[i].setTextSize(24);
			radioButton[i].setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if (isChecked) {
						bigTagCatagoryValue = Integer.parseInt(bigCatagory.get(position).get("mcID"));
						prepareSmallCata();
						if (smallHaveLoaded) {
							if (bigCataChoosed[bigTagCatagoryValue] == 0 && smallTags.size() > 1 && !isFirstTime)
								txt_warning.setVisibility(View.VISIBLE);
							else
								txt_warning.setVisibility(View.GONE);
							isFirstTime = false;
						}
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
					String basic_String = getActivity().getResources().getString(R.string.Tag_select_current);
					String selected_tag = bigCatagory.get(position).get("Name");
					editItemPre = getActivity().getSharedPreferences("editItem_tmp", 0);
					Editor putWriter = editItemPre.edit();
					putWriter.putString("mcID", String.valueOf(bigTagCatagoryValue));
					putWriter.commit();
					txt_currentBigTagState.setText(basic_String +" " +selected_tag);
					return true;
				}
			});
			radioGroup.addView(radioButton[i]);
			if (buttonText.equals(org_BigTags)) {
				int buttonId = radioButton[i].getId();
				radioGroup.check(buttonId);
				editItemPre = getActivity().getSharedPreferences("editItem_tmp", 0);
				Editor putWriter = editItemPre.edit();
				putWriter.putString("mcID", String.valueOf(bigCatagory.get(position).get("mcID")));
				putWriter.commit();
			}
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
						editItemPre = getActivity().getSharedPreferences("editItem_tmp", 0);
						Editor putWriter = editItemPre.edit();
						putWriter.putString("scID", smallTagsString);
						putWriter.commit();
					//Toast.makeText(getActivity().getApplicationContext(), smallTags.toString(), Toast.LENGTH_SHORT).show();
					}
				});
				toggleButton[i].setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						toggleButton[position].startAnimation(pushEffect);
					}
				});
				for (int j=0; j<smallTags.size() ; j++) {
					String tempSmallTag = smallTags.get(j);
					if (scIDnumber.equals(tempSmallTag)) {
						toggleButton[i].setChecked(true);
						toggleButton[i].setBackgroundResource(R.drawable.detail_corner);
					}
				}
				tableRow.addView(toggleButton[i]);
				matchTimes++;
		    }
		}
		smallCataView.addView(tableRow, new TableLayout.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        smallHaveLoaded = true;
	}

    private String getSmallString() {
    	String smallTagString;
    	if (smallTags.size() == 0 || smallTags.get(0).equals("")) {
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
