package com.foodietrip.android.library;

import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {
    //���� static �ܼ�
	//��Ʈw������
	private static final int DATABASE_VERSION = 1;
	//��Ʈw���W��
	private static final String DATABASE_NAME = "android_api";
	//�n�J���W��
	private static final String TABLE_LOGIN = "login";
	//�n�J������W��
	private static final String KEY_ID = "id";
	private static final String KEY_NAME = "nick_name";
	private static final String KEY_EMAIL = "email";
	private static final String KEY_UID = "uID";
	private static final String KEY_PHONE = "phone";
	
	public DatabaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
    //�إߪ��
	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_LOGIN_TABLE = "CREATE TABLE " +TABLE_LOGIN +"("
				+KEY_ID +" INTEGER PRIMARY KEY,"
				+KEY_NAME +" TEXT,"
				+KEY_UID +" TEXT,"
				+KEY_EMAIL +" TEXT UNIQUE,"
				+KEY_PHONE +" TEXT" +")";
		db.execSQL(CREATE_LOGIN_TABLE);
	}
    //��s���
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//�p�G���w�s�b���ܡA�N�ª����R��
		db.execSQL("DROP TABLE IF EXISTS " +TABLE_LOGIN);
		//�A�إߤ@�Ӫ��
		onCreate(db);
	}
	//�x�sUSER���ԲӸ�ƨ���̭�
	public void addUser(String name,String email,String uid, String phone) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(KEY_NAME, name);
		values.put(KEY_EMAIL, email);
		values.put(KEY_UID, uid);
		values.put(KEY_PHONE, phone);
		//values.put(KEY_CREATE_AT, created_at);
		//Log.e("DB Handler value", values.toString());
		//��J�C����
		db.insert(TABLE_LOGIN, null, values);
		db.close();
	}
	//�q���̭����o�U�Ȫ����
	public HashMap<String, String> getUserDetails() {
		HashMap<String, String> user = new HashMap<String, String>();
		String selectQuery = "SELECT * FROM " +TABLE_LOGIN;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		//����Ĥ@�C
		cursor.moveToFirst();
		if (cursor.getCount() > 0) {
			user.put("name", cursor.getString(1));
			user.put("uID", cursor.getString(2));
			user.put("email", cursor.getString(3));
			user.put("phone", cursor.getString(4));
		}
		cursor.close();
		db.close();
		//return user
		return user;
	}
	//���o�ϥΪ̵n�J���A�Areturn true if rows are there in table
	public int getRowCount() {
		String countQuery = "SELECT * FROM " +TABLE_LOGIN;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		int rowCount = cursor.getCount();
		//Log.e("DB Handeler Count = ", ""+rowCount);
		db.close();
		cursor.close();
		//return row count
		return rowCount;
	}
	//���s�إߪ��A�]�N�O>>�R���Ҧ������M��A���s�s�y�X��
	public void resetTable() {
		SQLiteDatabase db = this.getWritableDatabase();
		//�R���Ҧ��C
		db.delete(TABLE_LOGIN, null, null);
		db.close();
	}
}
