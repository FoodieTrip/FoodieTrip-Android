package com.foodietrip.android;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class EditItem_page2 extends SherlockFragment implements TimePickerDialog.OnTimeSetListener {
	SharedPreferences editItemPre;
	ArrayList<String> restDayArray;
	List<String> org_restDayArray;
	int is24Hours = 0, start_hr_temp = -1, start_min_temp = -1, end_hr_temp = -1, end_min_temp = -1;;
	CheckBox cb_w1, cb_w2, cb_w3, cb_w4, cb_w5, cb_w6, cb_w7, cb_holiday, cb_is24Hour, cb_noRestDays;
	TextView txt_Start, txt_Close;
	private Button btn_set_start, btn_set_close;
	String start_backup,end_backup;
	EditText edt_Memo;
	String startTime, endTime, start, end, restDaysOrg;
	boolean hasRestDayValue = false, setStartTime;
	Animation pushEffect;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		if (savedInstanceState != null)
			restDayArray = savedInstanceState.getStringArrayList("RestDayHasSeleted");
	}
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceBundle) {
		View view = inflater.inflate(R.layout.edit_item_page2, container, false);
		editItemPre = getActivity().getSharedPreferences("editItem_tmp", 0);
		restDayArray = new ArrayList<String>();
		// 從介面取得物件
		txt_Start = (TextView) view.findViewById(R.id.textView_store_start2);
		txt_Close = (TextView) view.findViewById(R.id.textView_store_end2);
		btn_set_start = (Button) view.findViewById(R.id.button_add_start_time2);
		btn_set_close = (Button) view.findViewById(R.id.button_add_close_time2);
		edt_Memo = (EditText) view.findViewById(R.id.editText_sMemo2);
		// 從Bundle取得資訊
		is24Hours = editItemPre.getInt("is24Hours", 0);
		start = editItemPre.getString("startTime", "");
		end = editItemPre.getString("closeTime", "");
		restDaysOrg = editItemPre.getString("RestDay_ID_org", "");
		if (!restDaysOrg.equals("")) {
			hasRestDayValue = true;
			org_restDayArray = Arrays.asList(restDaysOrg.split("\\s*,\\s*"));
			restDayArray.addAll(org_restDayArray);
		}
		start_backup = start;
		end_backup = end;
		if (!start.equals("") || !end.equals("")) {
			txt_Start.setText(start);
			txt_Close.setText(end);
			StringTokenizer start_original = new StringTokenizer(start, ":");
			start_hr_temp = Integer.valueOf(start_original.nextToken());
			start_min_temp = Integer.valueOf(start_original.nextToken());
			StringTokenizer end_original = new StringTokenizer(end, ":");
			end_hr_temp = Integer.valueOf(end_original.nextToken());
			end_min_temp = Integer.valueOf(end_original.nextToken());
		}
		cb_is24Hour = (CheckBox) view.findViewById(R.id.is24hr_editItem);
		if (is24Hours == 1) {
			cb_is24Hour.setChecked(true);
			btn_set_start.setClickable(false);
			btn_set_close.setClickable(false);
			btn_set_start.setBackgroundColor(Color.parseColor("#808080"));
			btn_set_close.setBackgroundColor(Color.parseColor("#808080"));
		}
		edt_Memo.setText(editItemPre.getString("sMemo", ""));
		// CheckBox
		cb_w1 = (CheckBox) view.findViewById(R.id.checkBox_w1_editItem);
		cb_w2 = (CheckBox) view.findViewById(R.id.checkBox_w2_editItem);
		cb_w3 = (CheckBox) view.findViewById(R.id.checkBox_w3_editItem);
		cb_w4 = (CheckBox) view.findViewById(R.id.checkBox_w4_editItem);
		cb_w5 = (CheckBox) view.findViewById(R.id.checkBox_w5_editItem);
		cb_w6 = (CheckBox) view.findViewById(R.id.checkBox_w6_editItem);
		cb_w7 = (CheckBox) view.findViewById(R.id.checkBox_w7_editItem);
		cb_noRestDays = (CheckBox) view.findViewById(R.id.checkBox_noRest_editItem);
		cb_holiday = (CheckBox) view.findViewById(R.id.checkBox_holiday_editItem);
		setCheckBoxState();
		// 設定監聽
		cb_is24Hour.setOnCheckedChangeListener(hourListener);
		btn_set_close.setOnClickListener(btn_listener);
		btn_set_start.setOnClickListener(btn_listener);
		cb_w1.setOnCheckedChangeListener(cb_listener);
		cb_w2.setOnCheckedChangeListener(cb_listener);
		cb_w3.setOnCheckedChangeListener(cb_listener);
		cb_w4.setOnCheckedChangeListener(cb_listener);
		cb_w5.setOnCheckedChangeListener(cb_listener);
		cb_w6.setOnCheckedChangeListener(cb_listener);
		cb_w7.setOnCheckedChangeListener(cb_listener);
		cb_noRestDays.setOnCheckedChangeListener(cb_listener);
		cb_holiday.setOnCheckedChangeListener(cb_listener);
		setCheckBoxAnimation();
		pushEffect = AnimationUtils.loadAnimation(getActivity(), R.anim.image_effect);
		return view;
	}

	private void setCheckBoxAnimation() {
		cb_is24Hour.setOnClickListener(animationListener);
		cb_w1.setOnClickListener(animationListener);
		cb_w2.setOnClickListener(animationListener);
		cb_w3.setOnClickListener(animationListener);
		cb_w4.setOnClickListener(animationListener);
		cb_w5.setOnClickListener(animationListener);
		cb_w6.setOnClickListener(animationListener);
		cb_w7.setOnClickListener(animationListener);
		cb_noRestDays.setOnClickListener(animationListener);
		cb_holiday.setOnClickListener(animationListener);
	}

	private CheckBox.OnClickListener animationListener = new CheckBox.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.is24hr_editItem:
				cb_is24Hour.startAnimation(pushEffect);
				break;
            case R.id.checkBox_w1_editItem:
				cb_w1.startAnimation(pushEffect);
				break;
            case R.id.checkBox_w2_editItem:
				cb_w2.startAnimation(pushEffect);
				break;
            case R.id.checkBox_w3_editItem:
				cb_w3.startAnimation(pushEffect);
				break;
            case R.id.checkBox_w4_editItem:
				cb_w4.startAnimation(pushEffect);
				break;
            case R.id.checkBox_w5_editItem:
				cb_w5.startAnimation(pushEffect);
				break;
            case R.id.checkBox_w6_editItem:
				cb_w6.startAnimation(pushEffect);
				break;
            case R.id.checkBox_w7_editItem:
				cb_w7.startAnimation(pushEffect);
				break;
            case R.id.checkBox_holiday_editItem:
				cb_holiday.startAnimation(pushEffect);
				break;
            case R.id.checkBox_noRest_editItem:
				cb_noRestDays.startAnimation(pushEffect);
				break;
			}
		}
	};

	private void setCheckBoxState() {
		if (hasRestDayValue) {
			for (Object str : restDayArray) {
				int str_int = Integer.parseInt((String) str);
				switch (str_int) {
				case 0:
					cb_noRestDays.setChecked(true);
					setCheckBoxDisable();
					break;
				case 1:
					cb_w1.setChecked(true);
					break;
				case 2:
					cb_w2.setChecked(true);
					break;
				case 3:
					cb_w3.setChecked(true);
					break;
				case 4:
					cb_w4.setChecked(true);
					break;
				case 5:
					cb_w5.setChecked(true);
					break;
				case 6:
					cb_w6.setChecked(true);
					break;
				case 7:
					cb_w7.setChecked(true);
					break;
				case 8:
					cb_holiday.setChecked(true);
					break;
				}
			}
		}
	}

	private CheckBox.OnCheckedChangeListener hourListener = new CheckBox.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			editItemPre = getActivity().getSharedPreferences("editItem_tmp", 0);
			Editor editWriter = editItemPre.edit();
			if (cb_is24Hour.isChecked()) {
				is24Hours = 1;
				txt_Start.setText("00:00");
				txt_Close.setText("24:00");
				editWriter.putString("startTime", "00:00");
				editWriter.putString("closeTime", "24:00");
				btn_set_start.setClickable(false);
				btn_set_close.setClickable(false);
				btn_set_start.setBackgroundColor(Color.parseColor("#808080"));
				btn_set_close.setBackgroundColor(Color.parseColor("#808080"));
			} else {
				is24Hours = 0;
				if (start_backup.equals("") || end_backup.equals("")) {
					final String start_word = getActivity().getResources().getString(R.string.Item_startTime);
					final String end_word = getActivity().getResources().getString(R.string.Item_endTime);
					txt_Start.setText(start_word);
					txt_Close.setText(end_word);
				}
				txt_Start.setText(start_backup);
				txt_Close.setText(end_backup);
				editWriter.putString("startTime", start);
				editWriter.putString("closeTime", end);
				btn_set_start.setClickable(true);
				btn_set_close.setClickable(true);
				btn_set_start.setBackgroundResource(android.R.drawable.btn_default);
				btn_set_close.setBackgroundResource(android.R.drawable.btn_default);
			}
			editWriter.putInt("is24Hours", is24Hours);
			editWriter.commit();
		}
	};

	private CheckBox.OnCheckedChangeListener cb_listener = new CheckBox.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			String rest_day = ""; // 總
	        switch (buttonView.getId()) {
			case R.id.checkBox_w1_editItem:
				if (isChecked)
					restDayArray.add("1");
				else
					restDayArray.remove("1");
				break;
			case R.id.checkBox_w2_editItem:
				if (isChecked)
					restDayArray.add("2");
				else
					restDayArray.remove("2");
				break;
			case R.id.checkBox_w3_editItem:
				if (isChecked)
					restDayArray.add("3");
				else
					restDayArray.remove("3");
				break;
			case R.id.checkBox_w4_editItem:
				if (isChecked)
					restDayArray.add("4");
				else
					restDayArray.remove("4");
				break;
			case R.id.checkBox_w5_editItem:
				if (isChecked)
					restDayArray.add("5");
				else
					restDayArray.remove("5");
				break;
			case R.id.checkBox_w6_editItem:
				if (isChecked)
					restDayArray.add("6");
				else
					restDayArray.remove("6");
				break;
			case R.id.checkBox_w7_editItem:
				if (isChecked)
					restDayArray.add("7");
				else
					restDayArray.remove("7");
			    break;
			case R.id.checkBox_holiday_editItem:
				if (isChecked)
					restDayArray.add("8");
				else
					restDayArray.remove("8");
				break;
			case R.id.checkBox_noRest_editItem:
				if (isChecked) {
					setCheckBoxDisable();
					restDayArray.add("0");
				}
				else {
					restDayArray.remove("0");
					setCheckBoxEnable();
				}
				break;
			}
			rest_day = sortRestDays();
			editItemPre = getActivity().getSharedPreferences("editItem_tmp", 0);
			Editor editWriter = editItemPre.edit();
			editWriter.putString("RestDay_ID_org", rest_day);
			editWriter.commit();
		}
	};

	private String sortRestDays() {
		String sortedRestDays;
		if (restDayArray.size() == 0) {
			sortedRestDays = "";
			return sortedRestDays;
		}
		ArrayList<Integer> arrayOfInt = new ArrayList<Integer>();
		for (Object str : restDayArray) {
			arrayOfInt.add(Integer.parseInt((String) str));
		}
		Collections.sort(arrayOfInt);
		sortedRestDays = arrayOfInt.toString();
		sortedRestDays = sortedRestDays.substring(1, sortedRestDays.length() - 1);
		//Toast.makeText(getActivity(), sortedRestDays, Toast.LENGTH_SHORT).show();
		return sortedRestDays;
	}

	private void setCheckBoxDisable() {
		restDayArray.clear();
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
			timePickerDialog = TimePickerDialog.newInstance(EditItem_page2.this, 0, 0, false);
			switch (v.getId()) {
			case R.id.button_add_start_time2:
				setStartTime = true;
				if (start_hr_temp != -1 && start_min_temp != -1)
					timePickerDialog = TimePickerDialog.newInstance(EditItem_page2.this, start_hr_temp, start_min_temp, false);
				timePickerDialog.show(getActivity().getSupportFragmentManager(), "start time picker");
				break;
			case R.id.button_add_close_time2:
				setStartTime = false;
				if (end_hr_temp != -1 && end_min_temp != -1)
					timePickerDialog = TimePickerDialog.newInstance(EditItem_page2.this, end_hr_temp, end_min_temp, false);
				timePickerDialog.show(getActivity().getSupportFragmentManager(), "end time picker");
				break;
			}
		}
	};

	// 更新TextView的時間用的
	private void updateDisplay(int hours, int minute) {
		String times = pad(hours) + ":" + pad(minute);
		editItemPre = getActivity().getSharedPreferences("editItem_tmp", 0);
		Editor editWriter = editItemPre.edit();
		if (setStartTime) {
			txt_Start.setText(times);
			start_backup = times;
			editWriter.putString("startTime", times);
		}
		else {
			txt_Close.setText(times);
			end_backup = times;
			editWriter.putString("closeTime", times);
		}
		editWriter.commit();
	}

	// 時間補零用的
	private String pad(int c) {
		if (c >= 10) {
			return String.valueOf(c);
		} else {
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
			//Toast.makeText(getActivity(), "Hello", Toast.LENGTH_SHORT).show();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putStringArrayList("RestDayHasSeleted", restDayArray);
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
