package com.youngsee.adplayer.system;

public class IadsInfo {

	public String host;
	public int port;

	public IadsInfo() {
		host = null;
		port = -1;
	}

	public IadsInfo(IadsInfo info) {
		host = (info.host != null) ? new String(info.host) : null;
		port = info.port;
	}

}
