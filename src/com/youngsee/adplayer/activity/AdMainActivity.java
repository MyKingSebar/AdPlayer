package com.youngsee.adplayer.activity;

import java.util.ArrayList;
import java.util.List;

import com.youngsee.adplayer.AdApplication;
import com.youngsee.adplayer.R;
import com.youngsee.adplayer.common.Constants;
import com.youngsee.adplayer.manager.ChargeManager;
import com.youngsee.adplayer.manager.ProgramManager;
import com.youngsee.adplayer.manager.SysParamManager;
import com.youngsee.adplayer.manager.WebManager;
import com.youngsee.adplayer.pgm.AreaRef;
import com.youngsee.adplayer.pgm.PgmBillRef;
import com.youngsee.adplayer.util.Logger;
import com.youngsee.adplayer.view.AdMultiMediaView;
import com.youngsee.adplayer.view.AdView;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.widget.FrameLayout;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;

public class AdMainActivity extends Activity {
	
	private final String TAG = "AdMainActivity";

	private Logger mLogger = new Logger();
	
	private final int EVENT_BASE = 0x9000;
	private final int EVENT_SHOWSTANDBYPGM = EVENT_BASE + 0;
	private final int EVENT_SHOWPGM = EVENT_BASE + 1;

	private PowerManager.WakeLock mWakeLock = null;

	private WebManager mWebManager = null;
	private ChargeManager mChargeManager = null;
	private ProgramManager mProgramManager = null;

	private FrameLayout mFrameLayout= null;

	private List<AdViewInfo> mViewInfoLst = null;

	private class AdViewInfo {
		public int id;
		public int type;
		public int x;
		public int y;
		public int w;
		public int h;
		public AdView view;
		
		public AdViewInfo(int id, int type, int x, int y, int w, int h, AdView view) {
			this.id = id;
			this.type = type;
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
			this.view = view;
		}
	}

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
		
		if (mViewInfoLst != null) {
    		for (AdViewInfo info : mViewInfoLst) {
    			info.view.onResume();
    		}
    	}
    }
    
    @Override
	protected void onPause() {
    	cleanupMsg();

    	if (mViewInfoLst != null) {
    		for (AdViewInfo info : mViewInfoLst) {
    			info.view.onPause();
    		}
    	}

    	if (mWakeLock != null) {
        	mWakeLock.release();
        }

    	super.onPause();
    }
    
    @Override
	protected void onDestroy() {
    	cleanupMsg();

    	if (mViewInfoLst != null) {
    		for (AdViewInfo info : mViewInfoLst) {
    			info.view.onDestroy();
    		}
    	}

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
    
    private void cleanupMsg() {
    	mHandler.removeMessages(EVENT_SHOWSTANDBYPGM);
    	mHandler.removeMessages(EVENT_SHOWPGM);
    }

    private void cleanupLayout() {
    	mFrameLayout.setBackground(null);
    	
    	if (mViewInfoLst != null) {
	    	for (AdViewInfo info : mViewInfoLst) {
	    		info.view.onDestroy();
	    	}
	    	mFrameLayout.removeAllViews();
	    	mViewInfoLst.clear();
	    	mViewInfoLst = null;
    	}
    }

    private void doShowStandbyPgm() {
    	cleanupLayout();

    	mLogger.i("Show Standby program.");

    	mFrameLayout.setBackground(new BitmapDrawable(getResources(), 
    			AdApplication.getInstance().getStandbyImg()));
    }

    private boolean compareAreas(PgmBillRef pgmbill) {
    	if (mViewInfoLst == null) {
    		mLogger.i("View info list is null.");
    		return false;
    	} else if (mViewInfoLst.size() != pgmbill.areas.size()) {
    		mLogger.i("Area size is different.");
    		return false;
    	}

    	for (AreaRef area : pgmbill.areas) {
    		boolean found = false;
    		for (AdViewInfo info : mViewInfoLst) {
    			if ((area.type == info.type)
    					&& (area.x == info.x)
    					&& (area.y == info.y)
    					&& (area.w == info.w)
    					&& (area.h == info.h)) {
    				found = true;
    				break;
    			}
    		}

    		if (!found) {
    			return false;
    		}
    	}

    	return true;
    }

    private void createAreas(PgmBillRef pgmbill) {
    	cleanupLayout();

    	int viewsize = pgmbill.areas.size();
    	mLogger.i("Create views, size = " + viewsize + ".");

    	if (mViewInfoLst == null) {
    		mViewInfoLst = new ArrayList<AdViewInfo>();
    	}

    	AdViewInfo viewinfo;
    	AdView adview;
    	AreaRef area;
    	for (int i = 0; i < viewsize; i++) {
    		viewinfo = null;
    		adview = null;
    		area = pgmbill.areas.get(i);
    		switch (area.type) {
    		case Constants.AREATYPE_MULTIMEDIA:
    			adview = new AdMultiMediaView(this);

    			break;
    		default:
    			mLogger.i("Invalid area type, area.type = " + area.type + ".");
    			continue;
    		}

    		if (adview != null) {
    			adview.setPublishId(pgmbill.publishid);
    			adview.setAreaIndex(i);
    			adview.setMediaList(area.medias);

				adview.setX(area.x);
				adview.setY(area.y);
				mFrameLayout.addView(adview, area.w, area.h);
				
				adview.start();

				viewinfo = new AdViewInfo(area.id, area.type, area.x, area.y, area.w, area.h, adview);
				mViewInfoLst.add(viewinfo);
    		}
    	}
    }

    private void stopViews() {
    	if (mViewInfoLst != null) {
	    	for (AdViewInfo info : mViewInfoLst) {
	    		info.view.stop();
	    	}
    	}
    }

    private void updateAreas(PgmBillRef pgmbill) {
    	stopViews();

    	int size = pgmbill.areas.size();
    	mLogger.i("Update views...");

    	AdViewInfo viewinfo;
    	AreaRef area;
    	for (int i = 0; i < size; i++) {
    		area = pgmbill.areas.get(i);
    		for (int j = 0; j < size; j++) {
    			viewinfo = mViewInfoLst.get(j);
    			if ((area.type == viewinfo.type)
    					&& (area.x == viewinfo.x)
    					&& (area.y == viewinfo.y)
    					&& (area.w == viewinfo.w)
    					&& (area.h == viewinfo.h)) {
    				viewinfo.view.setPublishId(pgmbill.publishid);
    				viewinfo.view.setAreaIndex(j);
    				viewinfo.view.setMediaList(area.medias);

    				viewinfo.view.start();

    				break;
    			}
    		}
    	}
    }

    private void doShowPgm(PgmBillRef pgmbill) {
    	if (pgmbill == null) {
    		mLogger.i("Program bill is null.");
    		return;
    	} else if (TextUtils.isEmpty(pgmbill.publishid)) {
    		mLogger.i("Publish id of program bill is empty.");
    		return;
    	} else if (pgmbill.areas == null) {
    		mLogger.i("Area list of program bill is null.");
    		return;
    	} else if (pgmbill.areas.isEmpty()) {
    		mLogger.i("No area info in the program bill.");
    		return;
    	}

    	if (!compareAreas(pgmbill)) {
    		AdApplication.getInstance().clearMemoryCache();

    		createAreas(pgmbill);
    	} else {
    		updateAreas(pgmbill);
    	}
    }

    public void showStandbyPgm() {
    	Message msg = mHandler.obtainMessage();
		msg.what = EVENT_SHOWSTANDBYPGM;
		msg.sendToTarget();
    }

    public void showPgm(PgmBillRef pgmbill) {
    	Message msg = mHandler.obtainMessage();
		msg.what = EVENT_SHOWPGM;
		msg.obj = pgmbill;
		msg.sendToTarget();
    }

    private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case EVENT_SHOWSTANDBYPGM:
				doShowStandbyPgm();

				break;
			case EVENT_SHOWPGM:
				doShowPgm((PgmBillRef)msg.obj);

				break;
            default:
            	mLogger.i("Unknown event, msg.what = " + msg.what + ".");
                break;
            }

            super.handleMessage(msg);
		}
    };
}
