package com.youngsee.adplayer.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class TokenInfo {
	private String accessToken;
	private String tokenType;
	private long expiresIn;
	private String scope;
	
	@JSONField(name = "access_token")
	public String getAccessToken() {
		return accessToken;
	}
	
	@JSONField(name = "access_token")
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	
	@JSONField(name = "token_type")
	public String getTokenType() {
		return tokenType;
	}
	
	@JSONField(name = "token_type")
	public void setTokenType(String tokenType) {
		this.tokenType = tokenType;
	}
	
	@JSONField(name = "expires_in")
	public long getExpiresIn() {
		return expiresIn;
	}
	
	@JSONField(name = "expires_in")
	public void setExpiresIn(long expiresIn) {
		this.expiresIn = expiresIn;
	}
	
	@JSONField(name = "scope")
	public String getScope() {
		return scope;
	}
	
	@JSONField(name = "scope")
	public void setScope(String scope) {
		this.scope = scope;
	}
}
