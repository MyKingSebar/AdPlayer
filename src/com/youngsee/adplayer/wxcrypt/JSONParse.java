package com.youngsee.adplayer.wxcrypt;

import com.youngsee.adplayer.dto.JsonMessage;
import com.youngsee.adplayer.util.JsonHelper;

public class JSONParse {
	public static String generate(String encrypt, String signature, String timestamp,
			String nonce, String token) {
		JsonMessage jsonmsg = new JsonMessage();
		jsonmsg.setEncrypt(encrypt);
		jsonmsg.setMsgSignature(signature);
		jsonmsg.setTimeStamp(timestamp);
		jsonmsg.setNonce(nonce);
		jsonmsg.setToken(token);

		return JsonHelper.jsonObjectToString(jsonmsg);
	}
}
