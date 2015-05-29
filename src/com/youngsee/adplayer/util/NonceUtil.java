package com.youngsee.adplayer.util;

public class NonceUtil {
	private static String mBaseStr = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

	private static int getRandom(int count) {
		return (int) Math.round(Math.random() * (count));
	}
	
	public static String getRandomString(int length) {
		StringBuffer sb = new StringBuffer();
		int len = mBaseStr.length();
		for (int i = 0; i < length; i++) {
			sb.append(mBaseStr.charAt(getRandom(len - 1)));
		}
		return sb.toString();
	}
}
