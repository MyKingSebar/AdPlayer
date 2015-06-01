package com.youngsee.adplayer.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class PgmInfo {
	private String id;
	
	@JSONField(name = "id")
	public String getId() {
		return id;
	}
	
	@JSONField(name = "id")
	public void setId(String id) {
		this.id = id;
	}
}
