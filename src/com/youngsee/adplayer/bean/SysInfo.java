package com.youngsee.adplayer.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class SysInfo {
	private String deviceId;
	private String deviceModel;
	private String softwareVersion;
	private String kernelVersion;
	private int heartbeatPeriod;
	
	@JSONField(name = "deviceid")
	public String getDeviceId() {
		return deviceId;
	}
	
	@JSONField(name = "deviceid")
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	
	@JSONField(name = "devicemodel")
	public String getDeviceModel() {
		return deviceModel;
	}
	
	@JSONField(name = "devicemodel")
	public void setDeviceModel(String deviceModel) {
		this.deviceModel = deviceModel;
	}
	
	@JSONField(name = "softwareversion")
	public String getSoftwareVersion() {
		return softwareVersion;
	}
	
	@JSONField(name = "softwareversion")
	public void setSoftwareVersion(String softwareVersion) {
		this.softwareVersion = softwareVersion;
	}
	
	@JSONField(name = "kernelversion")
	public String getKernelVersion() {
		return kernelVersion;
	}
	
	@JSONField(name = "kernelversion")
	public void setKernelVersion(String kernelVersion) {
		this.kernelVersion = kernelVersion;
	}
	
	@JSONField(name = "heartbeatperiod")
	public int getHeartbeatPeriod() {
		return heartbeatPeriod;
	}
	
	@JSONField(name = "heartbeatperiod")
	public void setHeartbeatPeriod(int heartbeatPeriod) {
		this.heartbeatPeriod = heartbeatPeriod;
	}
}
