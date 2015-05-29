package com.youngsee.adplayer.system;

public class DbChargeInfo {
	public String publishid;
	public String playdate;
	public int totalplaytimes;
	public int currentplaytimes;
	
	public DbChargeInfo() {
		publishid = null;
		playdate = null;
		totalplaytimes = -1;
		currentplaytimes = -1;
	}
	
	public DbChargeInfo(String publishid, String playdate, int totalplaytimes, int currentplaytimes) {
		this.publishid = publishid;
		this.playdate = playdate;
		this.totalplaytimes = totalplaytimes;
		this.currentplaytimes = currentplaytimes;
	}
}
