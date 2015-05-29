package com.youngsee.adplayer.bean;

import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;

public class ChargeInfo {
	private String deviceId;
	private List<ChargeBill> chargeBills;
	
	@JSONField(name = "deviceid")
	public String getDeviceId() {
		return deviceId;
	}
	
	@JSONField(name = "deviceid")
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	
	@JSONField(name = "chargebill")
	public List<ChargeBill> getChargeBills() {
		return chargeBills;
	}
	
	@JSONField(name = "chargebill")
	public void setChargeBills(List<ChargeBill> chargeBills) {
		this.chargeBills = chargeBills;
	}
}
