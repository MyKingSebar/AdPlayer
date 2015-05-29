package com.youngsee.adplayer.bean;

import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;

public class ChargeBill {
	private String publishId;
	private List<PlayBill> playBills;
	
	@JSONField(name = "publishid")
	public String getPublishId() {
		return publishId;
	}
	
	@JSONField(name = "publishid")
	public void setPublishId(String publishId) {
		this.publishId = publishId;
	}
	
	@JSONField(name = "playbill")
	public List<PlayBill> getPlayBills() {
		return playBills;
	}
	
	@JSONField(name = "playbill")
	public void setPlayBills(List<PlayBill> playBills) {
		this.playBills = playBills;
	}
}
