package com.youngsee.adplayer.bean;

import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;

public class Area {
	private int id;
	private int type;
	private int x;
	private int y;
	private int w;
	private int h;
	private List<Media> medias;
	
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
	
	@JSONField(name = "x")
	public int getX() {
		return x;
	}
	
	@JSONField(name = "x")
	public void setX(int x) {
		this.x = x;
	}
	
	@JSONField(name = "y")
	public int getY() {
		return y;
	}
	
	@JSONField(name = "y")
	public void setY(int y) {
		this.y = y;
	}
	
	@JSONField(name = "w")
	public int getW() {
		return w;
	}
	
	@JSONField(name = "w")
	public void setW(int w) {
		this.w = w;
	}
	
	@JSONField(name = "h")
	public int getH() {
		return h;
	}
	
	@JSONField(name = "h")
	public void setH(int h) {
		this.h = h;
	}
	
	@JSONField(name = "media")
	public List<Media> getMedias() {
		return medias;
	}
	
	@JSONField(name = "media")
	public void setMedias(List<Media> medias) {
		this.medias = medias;
	}
}
