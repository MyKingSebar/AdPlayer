package com.youngsee.adplayer.provider;

import android.net.Uri;

public class DbConstants {

	public static final String AUTHORITY = "com.youngsee.adplayer.provider";
	
	public static final String DATABASE_NAME = "adplayer.db";
	
	public static final int DATABASE_VERSION = 1;
	
	public static final String NOTSET = "Not set";
	
	public static final String TABLE_SYSPARAM = "sysparam";
	public static final String TABLE_CHARGEINFO = "chargeinfo";
	
	public static final String _ID = "_id";

	public static final String SPT_DEVICEID = "deviceid";
	public static final String SPT_DEVICEMODEL = "devicemodel";
	public static final String SPT_SOFTWAREVERSION = "softwareversion";
	public static final String SPT_KERNELVERSION = "kernelversion";
	public static final String SPT_SCREENWIDTH = "screenwidth";
	public static final String SPT_SCREENHEIGHT = "screenheight";
	public static final String SPT_TERMINALGROUP = "terminalgroup";
	public static final String SPT_TERMINALNAME = "terminalname";
	public static final String SPT_CHARGEREPORTPERIOD = "chargereportperiod";
	public static final String SPT_HEARTBEATPERIOD = "heartbeatperiod";
	public static final String SPT_IADSTOKEN = "iadstoken";
	public static final String SPT_SMSTOKEN = "smstoken";
	public static final String SPT_AMPSTOKEN = "ampstoken";
	public static final String SPT_AESKEY = "aeskey";
	public static final String SPT_RSAKEY = "rsakey";
	public static final String SPT_PGMID = "pgmid";
	public static final String SPT_PGMSHA1 = "pgmsha1";
	public static final String SPT_PGMJSONDATA = "pgmjsondata";
	public static final String SPT_FTPHOST = "ftphost";
	public static final String SPT_FTPPORT = "ftpport";
	public static final String SPT_FTPUSERNAME = "ftpusername";
	public static final String SPT_FTPPASSWORD = "ftppassword";
	public static final String SPT_IADSHOST = "iadshost";
	public static final String SPT_IADSPORT = "iadsport";
	public static final String SPT_SMSHOST = "smshost";
	public static final String SPT_SMSPORT = "smsport";
	public static final String SPT_AMPSHOST = "ampshost";
	public static final String SPT_AMPSPORT = "ampsport";

	public static final String CIT_PUBLISHID = "publishid";
	public static final String CIT_PLAYDATE = "playdate";
	public static final String CIT_TOTALPLAYTIMES = "totalplaytimes";
	public static final String CIT_CURRENTPLAYTIMES = "currentplaytimes";

	public static final Uri CONTENTURI_SYSPARAM = Uri.parse("content://"
			+ AUTHORITY + "/sysparam");
	public static final Uri CONTENTURI_CHARGEINFO = Uri.parse("content://"
			+ AUTHORITY + "/chargeinfo");

	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/"
			+ AUTHORITY + ".type";
	public static final String CONTENT_TYPE_ITME = "vnd.android.cursor.item/"
			+ AUTHORITY + ".item";
}
