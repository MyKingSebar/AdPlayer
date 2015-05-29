package com.youngsee.adplayer.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class CmdResult {
	private String deviceId;
	private int type;
	private String id;
	private int result;
	private String param;
	
	@JSONField(name = "deviceid")
	public String getDeviceId() {
		return deviceId;
	}
	
	@JSONField(name = "deviceid")
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	
	@JSONField(name = "type")
	public int getType() {
		return type;
	}
	
	@JSONField(name = "type")
	public void setType(int type) {
		this.type = type;
	}
	
	@JSONField(name = "id")
	public String getId() {
		return id;
	}
	
	@JSONField(name = "id")
	public void setId(String id) {
		this.id = id;
	}
	
	@JSONField(name = "result")
	public int getResult() {
		return result;
	}
	
	@JSONField(name = "result")
	public void setResult(int result) {
		this.result = result;
	}
	
	@JSONField(name = "param")
	public String getParam() {
		return param;
	}
	
	@JSONField(name = "param")
	public void setParam(String param) {
		this.param = param;
	}
}
