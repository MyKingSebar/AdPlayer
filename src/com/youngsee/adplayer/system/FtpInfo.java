package com.youngsee.adplayer.system;

public class FtpInfo {

	public String host;
	public int port;
	public String username;
	public String password;

	public FtpInfo() {
		host = null;
		port = -1;
		username = null;
		password = null;
	}

	public FtpInfo(FtpInfo info) {
		host = (info.host != null) ? new String(info.host) : null;
		port = info.port;
		username = (info.username != null) ? new String(info.username) : null;
		password = (info.password != null) ? new String(info.password) : null;
	}

}
