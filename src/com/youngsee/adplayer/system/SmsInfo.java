package com.youngsee.adplayer.system;

public class SmsInfo {

	public String host;
	public int port;

	public SmsInfo() {
		host = null;
		port = -1;
	}

	public SmsInfo(SmsInfo info) {
		host = (info.host != null) ? new String(info.host) : null;
		port = info.port;
	}

}
