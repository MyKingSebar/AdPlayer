package com.youngsee.adplayer.activity;

import com.youngsee.adplayer.R;
import com.youngsee.adplayer.manager.ChargeManager;
import com.youngsee.adplayer.manager.ProgramManager;
import com.youngsee.adplayer.manager.SysParamManager;
import com.youngsee.adplayer.manager.WebManager;
import com.youngsee.adplayer.util.Logger;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.view.KeyEvent;
import android.widget.FrameLayout;
import android.app.Activity;
import android.content.Context;

public class AdMainActivity extends Activity {
	
	private final String TAG = "AdMainActivity";

	private Logger mLogger = new Logger();
	
	private final int EVENT_BASE = 0x9000;
	private final int EVENT_SHOWSTANDBYPGM = EVENT_BASE + 1;

	private PowerManager.WakeLock mWakeLock = null;

	private WebManager mWebManager = null;
	private ChargeManager mChargeManager = null;
	private ProgramManager mProgramManager = null;

	private FrameLayout mFrameLayout= null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad_main);
        
        mFrameLayout = (FrameLayout)findViewById(R.id.activity_ad_lyt);
        
        PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(
        		PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.FULL_WAKE_LOCK, TAG);

        SysParamManager.getInstance().init();

        mWebManager = WebManager.getInstance();
        mChargeManager = ChargeManager.getInstance();
        mProgramManager = ProgramManager.getInstance();
        mProgramManager.setAdMainActivity(this);
    }
    
    @Override
	protected void onResume() {
		super.onResume();
		
		if (mWakeLock != null) {
        	mWakeLock.acquire();
        }
    }
    
    @Override
	protected void onPause() {
    	if (mWakeLock != null) {
        	mWakeLock.release();
        }

    	super.onPause();
    }
    
    @Override
	protected void onDestroy() {
    	if (mWebManager != null) {
    		mWebManager.destroy();
    	}
    	if (mChargeManager != null) {
    		mChargeManager.destroy();
    	}
    	if (mProgramManager != null) {
    		mProgramManager.destroy();
    	}

    	super.onDestroy();
    }
    
    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
            return true;
            
        case KeyEvent.KEYCODE_MENU:
            return true;
            
        case KeyEvent.KEYCODE_PAGE_UP:
            return true;
            
        case KeyEvent.KEYCODE_PAGE_DOWN:
            return true;
            
        case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
            return true;
            
        case KeyEvent.KEYCODE_MEDIA_STOP:
            return true;
        }
        
        return super.onKeyDown(keyCode, event);
    }
    
    private void doShowStandbyPgm() {
    	mFrameLayout.setBackground(getResources().getDrawable(R.drawable.standby));
    }

    public void showStandbyPgm() {
    	Message msg = mHandler.obtainMessage();
		msg.what = EVENT_SHOWSTANDBYPGM;
		msg.sendToTarget();
    }

    private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case EVENT_SHOWSTANDBYPGM:
				doShowStandbyPgm();
				
				break;
            default:
                break;
            }

            super.handleMessage(msg);
		}
    };
}
