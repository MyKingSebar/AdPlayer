package com.youngsee.adplayer.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class SysInfoResp {
	private String terminalGroup;
	private String terminalName;
	private int chargeReportPeriod;
	private int heartbeatPeriod;
	private FtpParam ftpParam;
	
	@JSONField(name = "terminalgroup")
	public String getTerminalGroup() {
		return terminalGroup;
	}
	
	@JSONField(name = "terminalgroup")
	public void setTerminalGroup(String terminalGroup) {
		this.terminalGroup = terminalGroup;
	}
	
	@JSONField(name = "terminalname")
	public String getTerminalName() {
		return terminalName;
	}
	
	@JSONField(name = "terminalname")
	public void setTerminalName(String terminalName) {
		this.terminalName = terminalName;
	}
	
	@JSONField(name = "chargereportperiod")
	public int getChargeReportPeriod() {
		return chargeReportPeriod;
	}
	
	@JSONField(name = "chargereportperiod")
	public void setChargeReportPeriod(int chargeReportPeriod) {
		this.chargeReportPeriod = chargeReportPeriod;
	}
	
	@JSONField(name = "heartbeatperiod")
	public int getHeartbeatPeriod() {
		return heartbeatPeriod;
	}
	
	@JSONField(name = "heartbeatperiod")
	public void setHeartbeatPeriod(int heartbeatPeriod) {
		this.heartbeatPeriod = heartbeatPeriod;
	}
	
	@JSONField(name = "ftpparam")
	public FtpParam getFtpParam() {
		return ftpParam;
	}
	
	@JSONField(name = "ftpparam")
	public void setFtpParam(FtpParam ftpParam) {
		this.ftpParam = ftpParam;
	}
}
