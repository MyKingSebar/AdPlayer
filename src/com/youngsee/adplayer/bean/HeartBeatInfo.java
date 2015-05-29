package com.youngsee.adplayer.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class HeartBeatInfo {
	private String deviceId;
	private String ip;
	private String time;
	private int status;
	private String disk;
	private String cpu;
	private String memory;
	private PgmInfo pgmInfo;
	private DlFileInfo dlFileInfo;
	private UlFileInfo ulFileInfo;
	
	@JSONField(name = "deviceid")
	public String getDeviceId() {
		return deviceId;
	}
	
	@JSONField(name = "deviceid")
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	
	@JSONField(name = "ip")
	public String getIp() {
		return ip;
	}
	
	@JSONField(name = "ip")
	public void setIp(String ip) {
		this.ip = ip;
	}
	
	@JSONField(name = "time")
	public String getTime() {
		return time;
	}
	
	@JSONField(name = "time")
	public void setTime(String time) {
		this.time = time;
	}
	
	@JSONField(name = "status")
	public int getStatus() {
		return status;
	}
	
	@JSONField(name = "status")
	public void setStatus(int status) {
		this.status = status;
	}
	
	@JSONField(name = "disk")
	public String getDisk() {
		return disk;
	}
	
	@JSONField(name = "disk")
	public void setDisk(String disk) {
		this.disk = disk;
	}
	
	@JSONField(name = "cpu")
	public String getCpu() {
		return cpu;
	}
	
	@JSONField(name = "cpu")
	public void setCpu(String cpu) {
		this.cpu = cpu;
	}
	
	@JSONField(name = "memory")
	public String getMemory() {
		return memory;
	}
	
	@JSONField(name = "memory")
	public void setMemory(String memory) {
		this.memory = memory;
	}
	
	@JSONField(name = "pgminfo")
	public PgmInfo getPgmInfo() {
		return pgmInfo;
	}
	
	@JSONField(name = "pgminfo")
	public void setPgmInfo(PgmInfo pgmInfo) {
		this.pgmInfo = pgmInfo;
	}
	
	@JSONField(name = "dlfileinfo")
	public DlFileInfo getDlFileInfo() {
		return dlFileInfo;
	}
	
	@JSONField(name = "dlfileinfo")
	public void setDlFileInfo(DlFileInfo dlFileInfo) {
		this.dlFileInfo = dlFileInfo;
	}
	
	@JSONField(name = "ulfileinfo")
	public UlFileInfo getUlFileInfo() {
		return ulFileInfo;
	}
	
	@JSONField(name = "ulfileinfo")
	public void setUlFileInfo(UlFileInfo ulFileInfo) {
		this.ulFileInfo = ulFileInfo;
	}
}
