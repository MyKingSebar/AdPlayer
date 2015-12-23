package com.youngsee.adplayer.bean;

import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;

public class PgmListInfoResp {
	private String pgmId;
	private List<PgmBill> pgmBills;
	private int status;

	@JSONField(name = "pgmid")
	public String getPgmId() {
		return pgmId;
	}

	@JSONField(name = "pgmid")
	public void setPgmId(String pgmId) {
		this.pgmId = pgmId;
	}

	@JSONField(name = "pgmbill")
	public List<PgmBill> getPgmBills() {
		return pgmBills;
	}

	@JSONField(name = "pgmbill")
	public void setPgmBills(List<PgmBill> pgmBills) {
		this.pgmBills = pgmBills;
	}

	@JSONField(name = "status")
	public int getStatus() {
		return status;
	}

	@JSONField(name = "status")
	public void setStatus(int status) {
		this.status = status;
	}
}
