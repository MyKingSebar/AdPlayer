package com.youngsee.adplayer.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class FtpParam {
	private String host;
	private int port;
	private String username;
	private String password;
	
	@JSONField(name = "host")
	public String getHost() {
		return host;
	}
	
	@JSONField(name = "host")
	public void setHost(String host) {
		this.host = host;
	}
	
	@JSONField(name = "port")
	public int getPort() {
		return port;
	}
	
	@JSONField(name = "port")
	public void setPort(int port) {
		this.port = port;
	}
	
	@JSONField(name = "username")
	public String getUsername() {
		return username;
	}
	
	@JSONField(name = "username")
	public void setUsername(String username) {
		this.username = username;
	}
	
	@JSONField(name = "password")
	public String getPassword() {
		return password;
	}
	
	@JSONField(name = "password")
	public void setPassword(String password) {
		this.password = password;
	}
}
