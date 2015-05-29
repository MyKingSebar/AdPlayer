package com.youngsee.adplayer.system;

public class DevInfo {

	/** Device id */
	public String id;
	/** Device model */
	public String model;
	/** Software version */
	public String softwareversion;
	/** Kernel version */
	public String kernelversion;
	/** Terminal group */
	public String terminalgroup;
	/** Terminal name */
	public String terminalname;
	/** Period of charge report (minute) */
	public int chargereportperiod;
	/** Period of heartbeat (second) */
	public int heartbeatperiod;
	/** Token of address server */
	public String iadstoken;
	/** Token of sms server */
	public String smstoken;
	/** Token of amps server */
	public String ampstoken;
	/** AES key */
	public String aeskey;
	/** RSA key */
	public String rsakey;
	/** Program id */
	public String pgmid;
	/** Program sha1 */
	public String pgmsha1;
	/** Program data */
	public String pgmdata;

	public DevInfo() {
		id = null;
		model = null;
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
		pgmdata = null;
	}

	public DevInfo(DevInfo info) {
		id = (info.id != null) ? new String(info.id) : null;
		model = (info.model != null) ? new String(info.model) : null;
		softwareversion = (info.softwareversion != null) ? new String(info.softwareversion) : null;
		kernelversion = (info.kernelversion != null) ? new String(info.kernelversion) : null;
		terminalgroup = (info.terminalgroup != null) ? new String(info.terminalgroup) : null;
		terminalname = (info.terminalname != null) ? new String(info.terminalname) : null;
		chargereportperiod = info.chargereportperiod;
		heartbeatperiod = info.heartbeatperiod;
		iadstoken = (info.iadstoken != null) ? new String(info.iadstoken) : null;
		smstoken = (info.smstoken != null) ? new String(info.smstoken) : null;
		ampstoken = (info.ampstoken != null) ? new String(info.ampstoken) : null;
		aeskey = (info.aeskey != null) ? new String(info.aeskey) : null;
		rsakey = (info.rsakey != null) ? new String(info.rsakey) : null;
		pgmid = (info.pgmid != null) ? new String(info.pgmid) : null;
		pgmsha1 = (info.pgmsha1 != null) ? new String(info.pgmsha1) : null;
		pgmdata = (info.pgmdata != null) ? new String(info.pgmdata) : null;
	}

}
