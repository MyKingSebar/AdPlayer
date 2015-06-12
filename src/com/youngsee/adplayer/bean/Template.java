package com.youngsee.adplayer.bean;

import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;

public class Template {
	private int width;
	private int height;
	private List<Area> areas;
	
	@JSONField(name = "width")
	public int getWidth() {
		return width;
	}
	
	@JSONField(name = "width")
	public void setWidth(int width) {
		this.width = width;
	}
	
	@JSONField(name = "height")
	public int getHeight() {
		return height;
	}
	
	@JSONField(name = "height")
	public void setHeight(int height) {
		this.height = height;
	}
	
	@JSONField(name = "area")
	public List<Area> getAreas() {
		return areas;
	}
	
	@JSONField(name = "area")
	public void setAreas(List<Area> areas) {
		this.areas = areas;
	}
}
