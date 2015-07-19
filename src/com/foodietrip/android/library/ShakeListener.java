package com.foodietrip.android.library;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class ShakeListener implements SensorEventListener{
    //當搖晃速度到達這個值後產生作用
	private static final int SPEED_SHRESHOLD = 3500;
	//兩次檢測的時間間隔
	private static final int UPDATE_INTERVAL_TIME = 90;
	//上次檢測時間
	private long lastUpdateTime;
	private SensorManager sensorManager;
	private Sensor sensor;
	private Context context;    //需要傳Context
	private OnShakeListener onShakeListener;
	//感應器座標(上次使用的)
	private float lastX;
	private float lastY;
	private float lastZ;
	
	//Constructor
	public ShakeListener(Context context) {
		this.context = context;
		start();
	}
	
	public void start() {
		sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		if (sensorManager != null)
			sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);    //讀取加速度感應器
		if (sensor != null) 
			sensorManager.registerListener(this,sensor, SensorManager.SENSOR_DELAY_GAME);    //SENSOR_DELAY_GAME(?)
	}
	
	//停止偵測
	public void stop() {
		sensorManager.unregisterListener(this);
	}
	
	//這段不懂(?)
    public interface OnShakeListener {
    	public void onShake();
    }
    
    public void setOnShakeListener(OnShakeListener listener) {
    	onShakeListener = listener;
    }
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {}
	
    //加速度感應器數據變化
	@Override
	public void onSensorChanged(SensorEvent event) {
		long currentUpdateTime = System.currentTimeMillis();    //現在檢測時間
		long timeInterval = currentUpdateTime - lastUpdateTime;    //檢查的兩次時間差
		//如果還沒到間隔時間，就直接return
		if (timeInterval < UPDATE_INTERVAL_TIME) return;
		lastUpdateTime = currentUpdateTime;
		//獲取座標
		float x = event.values[0];
		float y = event.values[1];
		float z = event.values[2];
		//座標變化值，移動程度
		float deltaX = x - lastX;
		float deltaY = y - lastY;
		float deltaZ = z - lastZ;
		//將此次獲得的數值存入成為last的值
		lastX = x;
		lastY = y;
		lastZ = z;
		double speed = Math.sqrt(deltaX*deltaX + deltaY*deltaY + deltaZ*deltaZ)/timeInterval * 10000;
		//速度有到
		if (speed >= SPEED_SHRESHOLD) onShakeListener.onShake();
	}
	
}
