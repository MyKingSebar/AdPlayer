package com.youngsee.adplayer.util;

import com.youngsee.adplayer.dto.JsonMessage;
import com.youngsee.adplayer.wxcrypt.AesException;
import com.youngsee.adplayer.wxcrypt.WXBizMsgCrypt;

public class MessageHelper {

	private static Logger mLogger = new Logger();
	
	public static String encryptMsg(String aeskey, String deviceid, String timestamp,
			String nonce, String token, String data) {
		if (aeskey == null) {
			mLogger.e("Aes key is null.");
			return null;
		}
		if (deviceid == null) {
			mLogger.e("Device id is null.");
			return null;
		}
		if (timestamp == null) {
			mLogger.e("Timestamp is null.");
			return null;
		}
		if (nonce == null) {
			mLogger.e("Nonce is null.");
			return null;
		}
		if (token == null) {
			mLogger.e("Token is null.");
			return null;
		}
		if (data == null) {
			mLogger.e("Data is null.");
			return null;
		}
		
		String encryptmsg = null;
		try {
			WXBizMsgCrypt pc = new WXBizMsgCrypt(token, aeskey, deviceid);
			encryptmsg = pc.encryptMsg(data, timestamp, nonce);
		} catch (AesException e) {
			e.printStackTrace();
		}
		
		return encryptmsg;
	}

	public static String decryptMsg(String aeskey, String deviceid, String serverdata) {
		if (aeskey == null) {
			mLogger.e("Aes key is null.");
			return null;
		}
		if (deviceid == null) {
			mLogger.e("Device id is null.");
			return null;
		}
		if (serverdata == null) {
			mLogger.e("Server data is null.");
			return null;
		}

		JsonMessage jsonmsg = JsonHelper.getObject(serverdata, JsonMessage.class);
		if (jsonmsg == null) {
			mLogger.e("Json parse error: jsonmsg is null.");
			return null;
		}

		String decryptmsg = null;
		try {
			WXBizMsgCrypt pc = new WXBizMsgCrypt(jsonmsg.getToken(), aeskey, deviceid);
			decryptmsg = pc.decryptMsg(jsonmsg.getMsgSignature(), jsonmsg.getTimeStamp(),
					jsonmsg.getNonce(), jsonmsg.getEncrypt());
		} catch (AesException e) {
			e.printStackTrace();
		}
		
		return decryptmsg;
	}

}
