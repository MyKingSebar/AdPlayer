package com.youngsee.adplayer.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class PgmListInfo {
	private String deviceId;
	
	@JSONField(name = "deviceid")
	public String getDeviceId() {
		return deviceId;
	}
	
	@JSONField(name = "deviceid")
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
}
