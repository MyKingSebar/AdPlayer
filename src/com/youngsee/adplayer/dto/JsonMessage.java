package com.youngsee.adplayer.dto;

import com.alibaba.fastjson.annotation.JSONField;

public class JsonMessage {
	private String encrypt;
	private String msgSignature;
	private String timeStamp;
	private String nonce;
	private String token;
	
	@JSONField(name = "encrypt")
	public String getEncrypt() {
		return encrypt;
	}
	
	@JSONField(name = "encrypt")
	public void setEncrypt(String encrypt) {
		this.encrypt = encrypt;
	}
	
	@JSONField(name = "msgSignature")
	public String getMsgSignature() {
		return msgSignature;
	}
	
	@JSONField(name = "msgSignature")
	public void setMsgSignature(String msgSignature) {
		this.msgSignature = msgSignature;
	}
	
	@JSONField(name = "timeStamp")
	public String getTimeStamp() {
		return timeStamp;
	}
	
	@JSONField(name = "timeStamp")
	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}
	
	@JSONField(name = "nonce")
	public String getNonce() {
		return nonce;
	}
	
	@JSONField(name = "nonce")
	public void setNonce(String nonce) {
		this.nonce = nonce;
	}
	
	@JSONField(name = "token")
	public String getToken() {
		return token;
	}
	
	@JSONField(name = "token")
	public void setToken(String token) {
		this.token = token;
	}
}
