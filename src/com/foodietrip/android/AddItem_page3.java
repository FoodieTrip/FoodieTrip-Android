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
import android.widget.Toast;

public class AddItem_page3 extends SherlockFragment {
	CheckBox cb_togo,cb_toDliver;
	SharedPreferences addItemPre;
	Animation pushEffect;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}
	public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceBundle) {
		View view = inflater.inflate(R.layout.add_item_page3, container,false);
		addItemPre = getActivity().getSharedPreferences("addItem_tmp", 0);
		//Checkbox
		cb_toDliver = (CheckBox)view.findViewById(R.id.checkBox_CanDelivery);
		cb_togo = (CheckBox)view.findViewById(R.id.checkBox_CanToGo);
		//≥]©w∫ ≈•
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
			case R.id.checkBox_CanDelivery:
				cb_toDliver.startAnimation(pushEffect);
				break;
            case R.id.checkBox_CanToGo:
				cb_togo.startAnimation(pushEffect);
				break;
			}
		}
	};
	
	private CheckBox.OnCheckedChangeListener cb_listener = new CheckBox.OnCheckedChangeListener(){
		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			addItemPre = getActivity().getSharedPreferences("addItem_tmp", 0);
			Editor inputWorker = addItemPre.edit();
			int sCanDelivery, sCanToGo;
			if (cb_toDliver.isChecked()) 
				sCanDelivery = 1;
			else 
				sCanDelivery = 0;
	        if (cb_togo.isChecked())
	        	sCanToGo = 1;
	        else 
	        	sCanToGo = 0;
	        inputWorker.putInt("sCanDelivery", sCanDelivery);
	        inputWorker.putInt("sCanToGo", sCanToGo);
	        inputWorker.commit();
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
			break;
		}
		return super.onOptionsItemSelected(item);
	}

}
