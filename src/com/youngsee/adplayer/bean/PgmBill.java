package com.youngsee.adplayer.bean;

import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;

public class PgmBill {
	private String publishId;
	private Template template;
	private List<PgmPlayBill> playBills;
	
	@JSONField(name = "publishid")
	public String getPublishId() {
		return publishId;
	}
	
	@JSONField(name = "publishid")
	public void setPublishId(String publishId) {
		this.publishId = publishId;
	}
	
	@JSONField(name = "template")
	public Template getTemplate() {
		return template;
	}
	
	@JSONField(name = "template")
	public void setTemplate(Template template) {
		this.template = template;
	}
	
	@JSONField(name = "playbill")
	public List<PgmPlayBill> getPlayBills() {
		return playBills;
	}
	
	@JSONField(name = "playbill")
	public void setPlayBills(List<PgmPlayBill> playBills) {
		this.playBills = playBills;
	}
}
