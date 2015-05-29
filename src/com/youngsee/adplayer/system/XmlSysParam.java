package com.youngsee.adplayer.system;

public class XmlSysParam {
	public String deviceid;
	public String devicemodel;
	public int chargereportperiod;
	public int heartbeatperiod;
	public String aeskey;
	public String rsakey;
	public String iadshost;
	public int iadsport;
	
	public XmlSysParam() {
		deviceid = null;
		devicemodel = null;
		chargereportperiod = -1;
		heartbeatperiod = -1;
		aeskey = null;
		rsakey = null;
		iadshost = null;
		iadsport = -1;
	}
}
