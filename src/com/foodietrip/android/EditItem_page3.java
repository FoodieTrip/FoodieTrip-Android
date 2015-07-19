package com.foodietrip.android;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

public class EditItem_page3 extends SherlockFragment {
	EditText edt_email,edt_web,edt_price,edt_phone;
	CheckBox cb_togo,cb_toDliver;
	int sw_togo,sw_todliver;
  	SharedPreferences editItemPre;
  	Animation pushEffect;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}
	public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceBundle) {
  		View view = inflater.inflate(R.layout.edit_item_page3, container,false);
  		editItemPre = getActivity().getSharedPreferences("editItem_tmp", 0);
  		Editor editWriter = editItemPre.edit();
		//取得畫面元件
		edt_email = (EditText)view.findViewById(R.id.editText_email_editItem);
		edt_web = (EditText)view.findViewById(R.id.editText_store_web_editItem);
		edt_price = (EditText)view.findViewById(R.id.editText_price_editItem);
		edt_phone = (EditText)view.findViewById(R.id.editItem_phone);
		//Checkbox
		cb_toDliver = (CheckBox)view.findViewById(R.id.checkBox_CanDelivery_editItem);
		cb_togo = (CheckBox)view.findViewById(R.id.checkBox_CanToGo_editItem);
		//從Bundle取得物件
  		edt_phone.setText(editItemPre.getString("sPhone", ""));
  		edt_price.setText(editItemPre.getString("sMinCharge", ""));
		edt_email.setText(editItemPre.getString("sEmail", ""));
		edt_web.setText(editItemPre.getString("sURL", ""));
		sw_togo = editItemPre.getInt("sCanToGo", 0);
		sw_todliver = editItemPre.getInt("sCanDelivery", 0);
		//Log.d("sw_togo = ", ""+sw_togo);
		//Log.d("sw_todiliver = ", ""+sw_todliver);
		if (sw_togo == 1) {
			cb_togo.setChecked(true);
			editWriter.putInt("sCanToGo", 1);
			editWriter.commit();
		}
		if (sw_todliver == 1) {
			cb_toDliver.setChecked(true);
			editWriter.putInt("sCanDelivery", 1);
			editWriter.commit();
		}
		cb_toDliver.setOnCheckedChangeListener(cb_listener);
		cb_togo.setOnCheckedChangeListener(cb_listener);
		cb_toDliver.setOnClickListener(animationListener);
		cb_togo.setOnClickListener(animationListener);
		pushEffect = AnimationUtils.loadAnimation(getActivity(), R.anim.image_effect);
	    return view;
	}
	
	private CheckBox.OnClickListener animationListener = new CheckBox.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.checkBox_CanDelivery_editItem:
				cb_toDliver.startAnimation(pushEffect);
				break;
			case R.id.checkBox_CanToGo_editItem:
				cb_togo.startAnimation(pushEffect);
				break;
			}
		}
	};
	
	private CheckBox.OnCheckedChangeListener cb_listener = new CheckBox.OnCheckedChangeListener(){
		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			int sCanDelivery, sCanToGo;
			if (cb_toDliver.isChecked()) 
				sCanDelivery = 1;
			else 
				sCanDelivery = 0;
	        if (cb_togo.isChecked())
	        	sCanToGo = 1;
	        else 
	        	sCanToGo = 0;
	  		editItemPre = getActivity().getSharedPreferences("editItem_tmp", 0);
	  		Editor editWriter = editItemPre.edit();
	  		editWriter.putInt("sCanDelivery", sCanDelivery);
	  		editWriter.putInt("sCanToGo", sCanToGo);
	  		editWriter.commit();
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
}
