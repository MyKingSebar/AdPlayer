package com.youngsee.adplayer.bean;

import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;

public class Template {
	private List<Area> areas;
	
	@JSONField(name = "area")
	public List<Area> getAreas() {
		return areas;
	}
	
	@JSONField(name = "area")
	public void setAreas(List<Area> areas) {
		this.areas = areas;
	}
}
