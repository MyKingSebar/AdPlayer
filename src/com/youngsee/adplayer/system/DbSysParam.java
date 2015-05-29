package com.youngsee.adplayer.system;

public class DbSysParam {
	public String deviceid;
	public String devicemodel;
	public String softwareversion;
	public String kernelversion;
	public String terminalgroup;
	public String terminalname;
	public int chargereportperiod;
	public int heartbeatperiod;
	public String iadstoken;
	public String smstoken;
	public String ampstoken;
	public String aeskey;
	public String rsakey;
	public String pgmid;
	public String pgmsha1;
	public String pgmjsondata;
	public String ftphost;
	public int ftpport;
	public String ftpusername;
	public String ftppassword;
	public String iadshost;
	public int iadsport;
	public String smshost;
	public int smsport;
	public String ampshost;
	public int ampsport;
	
	public DbSysParam() {
		deviceid = null;
        devicemodel = null;
        softwareversion = null;
        kernelversion = null;
        terminalgroup = null;
        terminalname = null;
        chargereportperiod = -1;
        heartbeatperiod = -1;
        iadstoken = null;
        smstoken = null;
        ampstoken = null;
        aeskey = null;
        rsakey = null;
        pgmid = null;
        pgmsha1 = null;
        pgmjsondata = null;
        ftphost = null;
        ftpport = -1;
        ftpusername = null;
        ftppassword = null;
        iadshost = null;
        iadsport = -1;
        smshost = null;
        smsport = -1;
        ampshost = null;
        ampsport  = -1;
	}
}
