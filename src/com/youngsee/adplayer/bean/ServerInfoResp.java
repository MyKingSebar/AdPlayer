package com.youngsee.adplayer.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class ServerInfoResp {
	private String smsHost;
	private String ampsHost;
	private int status;
	
	@JSONField(name = "smshost")
	public String getSmsHost() {
		return smsHost;
	}
	
	@JSONField(name = "smshost")
	public void setSmsHost(String smsHost) {
		this.smsHost = smsHost;
	}
	
	@JSONField(name = "ampshost")
	public String getAmpsHost() {
		return ampsHost;
	}
	
	@JSONField(name = "ampshost")
	public void setAmpsHost(String ampsHost) {
		this.ampsHost = ampsHost;
	}
	
	@JSONField(name = "status")
	public int getStatus() {
		return status;
	}
	
	@JSONField(name = "status")
	public void setStatus(int status) {
		this.status = status;
	}
}
