package com.youngsee.adplayer.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class HeartBeatInfoResp {
	private int cmdType;
	private String cmdId;
	private String cmdParam;
	private int status;

	@JSONField(name = "cmdtype")
	public int getCmdType() {
		return cmdType;
	}

	@JSONField(name = "cmdtype")
	public void setCmdType(int cmdType) {
		this.cmdType = cmdType;
	}

	@JSONField(name = "cmdid")
	public String getCmdId() {
		return cmdId;
	}

	@JSONField(name = "cmdid")
	public void setCmdId(String cmdId) {
		this.cmdId = cmdId;
	}

	@JSONField(name = "cmdparam")
	public String getCmdParam() {
		return cmdParam;
	}

	@JSONField(name = "cmdparam")
	public void setCmdParam(String cmdParam) {
		this.cmdParam = cmdParam;
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