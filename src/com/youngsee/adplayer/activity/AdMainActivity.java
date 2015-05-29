package com.youngsee.adplayer.activity;

import com.youngsee.adplayer.R;
import com.youngsee.adplayer.manager.ChargeManager;
import com.youngsee.adplayer.manager.SysParamManager;
import com.youngsee.adplayer.manager.WebManager;
import com.youngsee.adplayer.util.Logger;

import android.os.Bundle;
import android.app.Activity;

public class AdMainActivity extends Activity {

	private Logger mLogger = new Logger();

	private WebManager mWebManager = null;
	private ChargeManager mChargeManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad_main);
        
        SysParamManager.getInstance().init();

        mWebManager = WebManager.getInstance();
        
        mChargeManager = ChargeManager.getInstance();
    }
    
    @Override
	protected void onDestroy() {
    	if (mWebManager != null) {
    		mWebManager.destroy();
    	}
    	if (mChargeManager != null) {
    		mChargeManager.destroy();
    	}
    	
    	super.onDestroy();
    }

}
