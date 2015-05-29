package com.youngsee.adplayer.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AdDbHelper extends SQLiteOpenHelper {

	private Context mContext;

	public AdDbHelper(Context context) {
		super(context, DbConstants.DATABASE_NAME, null, DbConstants.DATABASE_VERSION);
		mContext = context;
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		createSysParamTable(db);
		createChargeInfoTable(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	private void createSysParamTable(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS " + DbConstants.TABLE_SYSPARAM + "("
				+ DbConstants._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ DbConstants.SPT_DEVICEID + " TEXT DEFAULT NULL,"
				+ DbConstants.SPT_DEVICEMODEL + " TEXT DEFAULT NULL,"
				+ DbConstants.SPT_SOFTWAREVERSION + " TEXT DEFAULT NULL,"
				+ DbConstants.SPT_KERNELVERSION + " TEXT DEFAULT NULL,"
				+ DbConstants.SPT_TERMINALGROUP + " TEXT DEFAULT NULL,"
				+ DbConstants.SPT_TERMINALNAME + " TEXT DEFAULT NULL,"
				+ DbConstants.SPT_CHARGEREPORTPERIOD + " INTEGER DEFAULT '-1',"
				+ DbConstants.SPT_HEARTBEATPERIOD + " INTEGER DEFAULT '-1',"
				+ DbConstants.SPT_IADSTOKEN + " TEXT DEFAULT NULL,"
				+ DbConstants.SPT_SMSTOKEN + " TEXT DEFAULT NULL,"
				+ DbConstants.SPT_AMPSTOKEN + " TEXT DEFAULT NULL,"
				+ DbConstants.SPT_AESKEY + " TEXT DEFAULT NULL,"
				+ DbConstants.SPT_RSAKEY + " TEXT DEFAULT NULL,"
				+ DbConstants.SPT_PGMID + " TEXT DEFAULT NULL,"
				+ DbConstants.SPT_PGMSHA1 + " TEXT DEFAULT NULL,"
				+ DbConstants.SPT_PGMJSONDATA + " BLOB DEFAULT NULL,"
				+ DbConstants.SPT_FTPHOST + " TEXT DEFAULT NULL,"
				+ DbConstants.SPT_FTPPORT + " INTEGER DEFAULT '-1',"
				+ DbConstants.SPT_FTPUSERNAME + " TEXT DEFAULT NULL,"
				+ DbConstants.SPT_FTPPASSWORD + " TEXT DEFAULT NULL,"
				+ DbConstants.SPT_IADSHOST + " TEXT DEFAULT NULL,"
				+ DbConstants.SPT_IADSPORT + " INTEGER DEFAULT '-1',"
				+ DbConstants.SPT_SMSHOST + " TEXT DEFAULT NULL,"
				+ DbConstants.SPT_SMSPORT + " INTEGER DEFAULT '-1',"
				+ DbConstants.SPT_AMPSHOST + " TEXT DEFAULT NULL,"
				+ DbConstants.SPT_AMPSPORT + " INTEGER DEFAULT '-1');");
	}

	private void createChargeInfoTable(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS " + DbConstants.TABLE_CHARGEINFO + "("
				+ DbConstants._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ DbConstants.CIT_PUBLISHID + " TEXT NOT NULL,"
				+ DbConstants.CIT_PLAYDATE + " TEXT NOT NULL,"
				+ DbConstants.CIT_TOTALPLAYTIMES + " INTEGER NOT NULL,"
				+ DbConstants.CIT_CURRENTPLAYTIMES + " INTEGER NOT NULL);");
	}

}
