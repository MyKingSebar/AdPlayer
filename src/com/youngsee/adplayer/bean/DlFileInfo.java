package com.youngsee.adplayer.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class DlFileInfo {
	private String filename;
	private long totalSize;
	private long currentSize;
	
	@JSONField(name = "filename")
	public String getFilename() {
		return filename;
	}
	
	@JSONField(name = "filename")
	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	@JSONField(name = "totalsize")
	public long getTotalSize() {
		return totalSize;
	}
	
	@JSONField(name = "totalsize")
	public void setTotalSize(long totalSize) {
		this.totalSize = totalSize;
	}
	
	@JSONField(name = "currentsize")
	public long getCurrentSize() {
		return currentSize;
	}
	
	@JSONField(name = "currentsize")
	public void setCurrentSize(long currentSize) {
		this.currentSize = currentSize;
	}
}
