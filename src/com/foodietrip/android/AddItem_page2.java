package com.foodietrip.android;

import java.util.ArrayList;
import java.util.Collections;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

public class AddItem_page2 extends SherlockFragment implements TimePickerDialog.OnTimeSetListener {
	SharedPreferences addItemPre;
	ArrayList<String> restDaysArray;
	TextView txt_Start, txt_Close;
	CheckBox cb_w1,cb_w2,cb_w3,cb_w4,cb_w5,cb_w6,cb_w7,cb_holiday,is24Hr,cb_noRest;
	int is24Hours = 0, start_hr_temp = -1, start_min_temp = -1, end_hr_temp = -1, end_min_temp = -1;
	private Button btn_set_start,btn_set_close;
	String startTime,endTime,start,end,start_backup,end_backup;
	boolean isFirstTime = true, setStartTime;
	Animation pushEffect;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		if (savedInstanceState != null)
			restDaysArray = savedInstanceState.getStringArrayList("RestDayHasSeleted");
	}
	public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceBundle) {
		View view = inflater.inflate(R.layout.add_item_page2, container,false);
		//從介面取得物件
		txt_Start = (TextView)view.findViewById(R.id.textView_store_start);
		txt_Close = (TextView)view.findViewById(R.id.textView_store_end);
		btn_set_start = (Button)view.findViewById(R.id.button_add_start_time);
		btn_set_close = (Button)view.findViewById(R.id.button_add_close_time);
		addItemPre = getActivity().getSharedPreferences("addItem_tmp", 0);
		restDaysArray = new ArrayList<String>();
		if (isFirstTime) {
			addItemPre.edit().putString("startTime", "").commit();
			addItemPre.edit().putString("closeTime", "").commit();
			isFirstTime = false;
		}
		start = addItemPre.getString("startTime","");
		end = addItemPre.getString("closeTime","");
		start_backup = start;    //使用者取消24小時營業
		end_backup = end;
		if (!start.equals("") || !end.equals("")){
			txt_Start.setText(start);
			txt_Close.setText(end);
		}
		//CheckBox
		is24Hr = (CheckBox)view.findViewById(R.id.is24hr_addItem);
		cb_w1 = (CheckBox)view.findViewById(R.id.checkBox_w1);
		cb_w2 = (CheckBox)view.findViewById(R.id.checkBox_w2);
		cb_w3 = (CheckBox)view.findViewById(R.id.checkBox_w3);
		cb_w4 = (CheckBox)view.findViewById(R.id.checkBox_w4);
		cb_w5 = (CheckBox)view.findViewById(R.id.checkBox_w5);
		cb_w6 = (CheckBox)view.findViewById(R.id.checkBox_w6);
		cb_w7 = (CheckBox)view.findViewById(R.id.checkBox_w7);
		cb_holiday = (CheckBox)view.findViewById(R.id.checkBox_holiday);
		cb_noRest = (CheckBox) view.findViewById(R.id.checkBox_noRestDay);
		//設定監聽
		is24Hr.setOnCheckedChangeListener(hours_listener);
		btn_set_close.setOnClickListener(btn_listener);
		btn_set_start.setOnClickListener(btn_listener);
		cb_w1.setOnCheckedChangeListener(cb_listener);
		cb_w2.setOnCheckedChangeListener(cb_listener);
		cb_w3.setOnCheckedChangeListener(cb_listener);
		cb_w4.setOnCheckedChangeListener(cb_listener);
		cb_w5.setOnCheckedChangeListener(cb_listener);
		cb_w6.setOnCheckedChangeListener(cb_listener);
		cb_w7.setOnCheckedChangeListener(cb_listener);
		cb_holiday.setOnCheckedChangeListener(cb_listener);
		cb_noRest.setOnCheckedChangeListener(cb_listener);
		setCheckBoxAnimation();
		pushEffect = AnimationUtils.loadAnimation(getActivity(), R.anim.image_effect);
		return view;
	}

	private void setCheckBoxAnimation() {
		is24Hr.setOnClickListener(animation_listener);
		cb_w1.setOnClickListener(animation_listener);
		cb_w2.setOnClickListener(animation_listener);
		cb_w3.setOnClickListener(animation_listener);
		cb_w4.setOnClickListener(animation_listener);
		cb_w5.setOnClickListener(animation_listener);
		cb_w6.setOnClickListener(animation_listener);
		cb_w7.setOnClickListener(animation_listener);
		cb_holiday.setOnClickListener(animation_listener);
		cb_noRest.setOnClickListener(animation_listener);
	}

	private CheckBox.OnClickListener animation_listener = new CheckBox.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.is24hr_addItem:
				is24Hr.startAnimation(pushEffect);
				break;
			case R.id.checkBox_w1:
				cb_w1.startAnimation(pushEffect);
				break;
			case R.id.checkBox_w2:
				cb_w2.startAnimation(pushEffect);
				break;
			case R.id.checkBox_w3:
				cb_w3.startAnimation(pushEffect);
				break;
			case R.id.checkBox_w4:
				cb_w4.startAnimation(pushEffect);
				break;
			case R.id.checkBox_w5:
				cb_w5.startAnimation(pushEffect);
				break;
			case R.id.checkBox_w6:
				cb_w6.startAnimation(pushEffect);
				break;
			case R.id.checkBox_w7:
				cb_w7.startAnimation(pushEffect);
				break;
			case R.id.checkBox_noRestDay:
				cb_noRest.startAnimation(pushEffect);
				break;
			case R.id.checkBox_holiday:
				cb_holiday.startAnimation(pushEffect);
				break;
			}
		}
	};

	private CheckBox.OnCheckedChangeListener hours_listener = new CheckBox.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			addItemPre = getActivity().getSharedPreferences("addItem_tmp", 0);
			Editor inputWorker = addItemPre.edit();
			if (is24Hr.isChecked()) {
				is24Hours = 1;
				txt_Start.setText("00:00");
				txt_Close.setText("24:00");
				inputWorker.putString("startTime", "00:00");
				inputWorker.putString("closeTime", "24:00");
				btn_set_start.setClickable(false);
				btn_set_close.setClickable(false);
				btn_set_start.setBackgroundColor(Color.parseColor("#808080"));
				btn_set_close.setBackgroundColor(Color.parseColor("#808080"));
			}
			else {
				is24Hours = 0;
				if (start_backup.equals("") || end_backup.equals("")) {
					final String start_word = getActivity().getResources().getString(R.string.Item_startTime);
					final String end_word = getActivity().getResources().getString(R.string.Item_endTime);
					txt_Start.setText(start_word);
					txt_Close.setText(end_word);
				}
				txt_Start.setText(start_backup);
				txt_Close.setText(end_backup);
				inputWorker.putString("startTime", start);
				inputWorker.putString("closeTime", end);
				btn_set_start.setClickable(true);
				btn_set_close.setClickable(true);
				btn_set_start.setBackgroundResource(android.R.drawable.btn_default);
				btn_set_close.setBackgroundResource(android.R.drawable.btn_default);
			}
			inputWorker.putInt("is24Hours", is24Hours);
			inputWorker.commit();
		}
	};

	private CheckBox.OnCheckedChangeListener cb_listener = new CheckBox.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			String rest_day=""; //總
            switch (buttonView.getId()) {
			case R.id.checkBox_w1:
				if (isChecked)
					restDaysArray.add("1");
				else
					restDaysArray.remove("1");
				break;
			case R.id.checkBox_w2:
				if (isChecked)
					restDaysArray.add("2");
				else
					restDaysArray.remove("2");
				break;
			case R.id.checkBox_w3:
				if (isChecked)
					restDaysArray.add("3");
				else
					restDaysArray.remove("3");
				break;
			case R.id.checkBox_w4:
				if (isChecked)
					restDaysArray.add("4");
				else
					restDaysArray.remove("4");
				break;
			case R.id.checkBox_w5:
				if (isChecked)
					restDaysArray.add("5");
				else
					restDaysArray.remove("5");
				break;
			case R.id.checkBox_w6:
				if (isChecked)
					restDaysArray.add("6");
				else
					restDaysArray.remove("6");
				break;
			case R.id.checkBox_w7:
				if (isChecked)
					restDaysArray.add("7");
				else
					restDaysArray.remove("7");
				break;
			case R.id.checkBox_holiday:
				if (isChecked)
					restDaysArray.add("8");
				else
					restDaysArray.remove("8");
				break;
			case R.id.checkBox_noRestDay:
				if (isChecked) {
					setCheckBoxDisable();
					restDaysArray.add("0");
				}
				else {
					restDaysArray.remove("0");
					setCheckBoxEnable();
				}
				break;
			}
			rest_day = sortRestDays();
			addItemPre = getActivity().getSharedPreferences("addItem_tmp", 0);
			Editor inputWorker = addItemPre.edit();
			inputWorker.putString("RestDay", rest_day);
			inputWorker.commit();
		}
	};

	private String sortRestDays() {
		String sortedRestDays;
		if (restDaysArray.size() == 0) {
			sortedRestDays = "";
			return sortedRestDays;
		}
		ArrayList<Integer> arrayOfInt = new ArrayList<Integer>();
		for (Object str : restDaysArray) {
			arrayOfInt.add(Integer.parseInt((String) str));
		}
		Collections.sort(arrayOfInt);
		sortedRestDays = arrayOfInt.toString();
		sortedRestDays = sortedRestDays.substring(1, sortedRestDays.length() - 1);
		//Toast.makeText(getActivity(), sortedRestDays, Toast.LENGTH_SHORT).show();
		return sortedRestDays;
	}

	private void setCheckBoxDisable() {
		restDaysArray.clear();    //清除休息日內容
		cb_w1.setEnabled(false);
		cb_w1.setChecked(false);
		cb_w2.setEnabled(false);
		cb_w2.setChecked(false);
		cb_w3.setEnabled(false);
		cb_w3.setChecked(false);
		cb_w4.setEnabled(false);
		cb_w4.setChecked(false);
		cb_w5.setEnabled(false);
		cb_w5.setChecked(false);
		cb_w6.setEnabled(false);
		cb_w6.setChecked(false);
		cb_w7.setEnabled(false);
		cb_w7.setChecked(false);
		cb_holiday.setEnabled(false);
		cb_holiday.setChecked(false);
	}

	private void setCheckBoxEnable() {
		cb_w1.setEnabled(true);
		cb_w2.setEnabled(true);
		cb_w3.setEnabled(true);
		cb_w4.setEnabled(true);
		cb_w5.setEnabled(true);
		cb_w6.setEnabled(true);
		cb_w7.setEnabled(true);
		cb_holiday.setEnabled(true);
	}

	private Button.OnClickListener btn_listener = new Button.OnClickListener() {
		@Override
		public void onClick(View v) {
			TimePickerDialog timePickerDialog;
			timePickerDialog = TimePickerDialog.newInstance(AddItem_page2.this, 0, 0, false);
			switch (v.getId()) {
		    case R.id.button_add_start_time:
		    	setStartTime = true;
		    	if (start_hr_temp != -1 && start_min_temp != -1)
					timePickerDialog = TimePickerDialog.newInstance(AddItem_page2.this, start_hr_temp, start_min_temp, false);
		    	timePickerDialog.show(getActivity().getSupportFragmentManager(), "start time picker");
			    break;
            case R.id.button_add_close_time:
            	setStartTime = false;
            	if(end_hr_temp != -1 && end_min_temp != -1)
    				timePickerDialog = TimePickerDialog.newInstance(AddItem_page2.this, end_hr_temp, end_min_temp, false);
            	timePickerDialog.show(getActivity().getSupportFragmentManager(), "close time picker");
			    break;
		    }
		}
	};

	//更新TextView的時間用的
	private void updateDisplay(int hours, int minute) {
		String times = pad(hours) +":" +pad(minute);
		addItemPre = getActivity().getSharedPreferences("addItem_tmp", 0);
		Editor inputWorker = addItemPre.edit();
		if (setStartTime){
			txt_Start.setText(times);
			start_backup = times;
			inputWorker.putString("startTime", times);
		}
		else {
			txt_Close.setText(times);
			end_backup = times;
			inputWorker.putString("closeTime", times);
		}
		inputWorker.commit();
	}

	//時間補零用的
	private String pad(int c) {
		if (c >= 10) {
			return String.valueOf(c);
		}
		else {
			return "0" + String.valueOf(c);
		}
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
			Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.allChangesAreSaved), Toast.LENGTH_SHORT).show();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putStringArrayList("RestDayHasSeleted", restDaysArray);
	}

	@Override
	public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
		if (setStartTime) {
			start_hr_temp = hourOfDay;
			start_min_temp = minute;
		}
		else {
			end_hr_temp = hourOfDay;
			end_min_temp = minute;
		}
		updateDisplay(hourOfDay, minute);
	}

}
