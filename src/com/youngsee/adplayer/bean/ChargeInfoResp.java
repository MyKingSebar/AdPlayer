package com.youngsee.adplayer.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class ChargeInfoResp {
	private int status;
	
	@JSONField(name = "status")
	public int getStatus() {
		return status;
	}
	
	@JSONField(name = "status")
	public void setStatus(int status) {
		this.status = status;
	}
}
