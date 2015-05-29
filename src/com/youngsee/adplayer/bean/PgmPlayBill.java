package com.youngsee.adplayer.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class PgmPlayBill {
	private String playBeginDate;
	private String playEndDate;
	private int playTimes;
	
	@JSONField(name = "playbegindate")
	public String getPlayBeginDate() {
		return playBeginDate;
	}
	
	@JSONField(name = "playbegindate")
	public void setPlayBeginDate(String playBeginDate) {
		this.playBeginDate = playBeginDate;
	}
	
	@JSONField(name = "playenddate")
	public String getPlayEndDate() {
		return playEndDate;
	}
	
	@JSONField(name = "playenddate")
	public void setPlayEndDate(String playEndDate) {
		this.playEndDate = playEndDate;
	}
	
	@JSONField(name = "playtimes")
	public int getPlayTimes() {
		return playTimes;
	}
	
	@JSONField(name = "playtimes")
	public void setPlayTimes(int playTimes) {
		this.playTimes = playTimes;
	}
}
