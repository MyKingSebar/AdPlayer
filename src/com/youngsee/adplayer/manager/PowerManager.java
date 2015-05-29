package com.youngsee.adplayer.manager;

import com.youngsee.adplayer.util.Logger;


public class PowerManager {
	private Logger mLogger = new Logger();
	
	public static final int STATUS_ONLINE = 1;
	public static final int STATUS_STANDBY = 2;
	
	private int mStatus = STATUS_ONLINE;
	
	private PowerManager() {
		mStatus = STATUS_ONLINE;
	}

	private static class PowerHolder {
        static final PowerManager INSTANCE = new PowerManager();
    }

	public static PowerManager getInstance() {
		return PowerHolder.INSTANCE;
	}
	
	public int getStatus() {
		return mStatus;
	}
}
