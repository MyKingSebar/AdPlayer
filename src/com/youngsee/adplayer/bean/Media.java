package com.youngsee.adplayer.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class Media {
	private int id;
	private int type;
	private int duration;
	private String name;
	private String path;
	private String sha1;
	
	@JSONField(name = "id")
	public int getId() {
		return id;
	}
	
	@JSONField(name = "id")
	public void setId(int id) {
		this.id = id;
	}
	
	@JSONField(name = "type")
	public int getType() {
		return type;
	}
	
	@JSONField(name = "type")
	public void setType(int type) {
		this.type = type;
	}
	
	@JSONField(name = "duration")
	public int getDuration() {
		return duration;
	}
	
	@JSONField(name = "duration")
	public void setDuration(int duration) {
		this.duration = duration;
	}
	
	@JSONField(name = "name")
	public String getName() {
		return name;
	}
	
	@JSONField(name = "name")
	public void setName(String name) {
		this.name = name;
	}
	
	@JSONField(name = "path")
	public String getPath() {
		return path;
	}
	
	@JSONField(name = "path")
	public void setPath(String path) {
		this.path = path;
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
