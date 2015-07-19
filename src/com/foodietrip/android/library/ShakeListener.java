package com.foodietrip.android.library;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class ShakeListener implements SensorEventListener{
    //��n�̳t�ר�F�o�ӭȫᲣ�ͧ@��
	private static final int SPEED_SHRESHOLD = 3500;
	//�⦸�˴����ɶ����j
	private static final int UPDATE_INTERVAL_TIME = 90;
	//�W���˴��ɶ�
	private long lastUpdateTime;
	private SensorManager sensorManager;
	private Sensor sensor;
	private Context context;    //�ݭn��Context
	private OnShakeListener onShakeListener;
	//�P�����y��(�W���ϥΪ�)
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
			sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);    //Ū���[�t�׷P����
		if (sensor != null) 
			sensorManager.registerListener(this,sensor, SensorManager.SENSOR_DELAY_GAME);    //SENSOR_DELAY_GAME(?)
	}
	
	//�����
	public void stop() {
		sensorManager.unregisterListener(this);
	}
	
	//�o�q����(?)
    public interface OnShakeListener {
    	public void onShake();
    }
    
    public void setOnShakeListener(OnShakeListener listener) {
    	onShakeListener = listener;
    }
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {}
	
    //�[�t�׷P�����ƾ��ܤ�
	@Override
	public void onSensorChanged(SensorEvent event) {
		long currentUpdateTime = System.currentTimeMillis();    //�{�b�˴��ɶ�
		long timeInterval = currentUpdateTime - lastUpdateTime;    //�ˬd���⦸�ɶ��t
		//�p�G�٨S�춡�j�ɶ��A�N����return
		if (timeInterval < UPDATE_INTERVAL_TIME) return;
		lastUpdateTime = currentUpdateTime;
		//����y��
		float x = event.values[0];
		float y = event.values[1];
		float z = event.values[2];
		//�y���ܤƭȡA���ʵ{��
		float deltaX = x - lastX;
		float deltaY = y - lastY;
		float deltaZ = z - lastZ;
		//�N������o���ƭȦs�J����last����
		lastX = x;
		lastY = y;
		lastZ = z;
		double speed = Math.sqrt(deltaX*deltaX + deltaY*deltaY + deltaZ*deltaZ)/timeInterval * 10000;
		//�t�צ���
		if (speed >= SPEED_SHRESHOLD) onShakeListener.onShake();
	}
	
}
