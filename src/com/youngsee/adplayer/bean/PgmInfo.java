package com.youngsee.adplayer.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class PgmInfo {
	private String id;
	private String sha1;
	
	@JSONField(name = "id")
	public String getId() {
		return id;
	}
	
	@JSONField(name = "id")
	public void setId(String id) {
		this.id = id;
	}
	
	@JSONField(name = "sha1")
	public String getSha1() {
		return sha1;
	}
	
	@JSONField(name = "sha1")
	public void setSha1(String sha1) {
		this.sha1 = sha1;
	}
}
