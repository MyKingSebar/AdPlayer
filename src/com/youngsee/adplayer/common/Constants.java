package com.youngsee.adplayer.common;

public class Constants {

	public static final String UDISK_NAME_PREFIX = "usbhost";
	
	public static final String APPDIRNAME = "AdPlayer";
	
	public static final String VIDEODIRNAME = "Video";
	public static final String IMAGEDIRNAME = "Image";
	public static final String TEXTDIRNAME = "TEXT";
	
	public static final String STANDBYDIRNAME = "Standby";
	
	public static final String STANDBYFILENAME_LANDSCAPE = "StandbyLandscape.jpg";
	public static final String STANDBYFILENAME_PORTRAIT = "StandbyPortrait.jpg";
	
	public static final int AREATYPE_MULTIMEDIA = 1;

	public static final int MEDIATYPE_VIDEO = 1;
	public static final int MEDIATYPE_IMAGE = 2;
	public static final int MEDIATYPE_TEXT = 3;
	
	public static final int MEDIAMODE_NONE = 0;
	public static final int MEDIAMODE_LEFTTORIGHT = 1;
	public static final int MEDIAMODE_RIGHTTOLEFT = 2;
	public static final int MEDIAMODE_TOPTOBOTTOM = 3;
	public static final int MEDIAMODE_BOTTOMTOTOP = 4;
	public static final int MEDIAMODE_LEFTTOPTORIGHTBOTTOM = 5;
	public static final int MEDIAMODE_RIGHTTOPTOLEFTBOTTON = 6;
	public static final int MEDIAMODE_INSIDETOOUTSIDE = 7;
	public static final int MEDIAMODE_OUTSIDETOINSIDE = 8;
	public static final int MEDIAMODE_RANDOM = 9;

	public static final int SERVERTYPE_IADS = 0x1001;
	public static final int SERVERTYPE_SMS = 0x1002;
	public static final int SERVERTYPE_AMPS = 0x1003;
	
	public static final int SERVICETYPE_IADS_BASE = 0x6000;
	public static final int SERVICETYPE_IADS_SERVERINFO = SERVICETYPE_IADS_BASE + 1;
	
	public static final int SERVICETYPE_SMS_BASE = 0x7000;
	public static final int SERVICETYPE_SMS_CHARGE = SERVICETYPE_SMS_BASE + 1;
	
	public static final int SERVICETYPE_AMPS_BASE = 0x8000;
	public static final int SERVICETYPE_AMPS_SYSINFO = SERVICETYPE_AMPS_BASE + 1;
	public static final int SERVICETYPE_AMPS_HEARTBEAT = SERVICETYPE_AMPS_BASE + 2;
	public static final int SERVICETYPE_AMPS_CMDRESULT = SERVICETYPE_AMPS_BASE + 3;
	public static final int SERVICETYPE_AMPS_PGMLIST = SERVICETYPE_AMPS_BASE + 4;
	
	public static final int DEFAULT_NONCE_STRLEN = 6;
	
	public static final String DEFAULT_CHARSET = "UTF-8";

}
