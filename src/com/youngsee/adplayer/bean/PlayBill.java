package com.youngsee.adplayer.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class PlayBill {
	private String playDate;
	private int playTimes;
	
	@JSONField(name = "playdate")
	public String getPlayDate() {
		return playDate;
	}
	
	@JSONField(name = "playdate")
	public void setPlayDate(String playDate) {
		this.playDate = playDate;
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
