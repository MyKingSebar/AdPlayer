package com.youngsee.adplayer.system;

public class AmpsInfo {

	public String host;
	public int port;

	public AmpsInfo() {
		host = null;
		port = -1;
	}

	public AmpsInfo(AmpsInfo info) {
		host = (info.host != null) ? new String(info.host) : null;
		port = info.port;
	}

}
