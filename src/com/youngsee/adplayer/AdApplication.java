package com.youngsee.adplayer;

import com.youngsee.adplayer.util.Logger;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class AdApplication extends Application {

	private static Logger                   sLogger                        = new Logger();
    private static AdApplication            INSTANCE                       = null;

	@Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
    }

	public static AdApplication getInstance() {
        return INSTANCE;
    }

	public boolean isNetworkConnected() {
        ConnectivityManager connectivity =
        		(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isAvailable()) {
                return info.isConnected();
            }
        }
        return false;
    }
	
	public boolean isForbidToDownload() {
		// TBD
		return false;
	}
	
	public boolean releaseMaterialSpace(long size) {
		// TBD
		return true;
	}

}
