package com.youngsee.adplayer.manager;

import java.util.ArrayList;
import java.util.List;

import com.youngsee.adplayer.activity.AdMainActivity;
import com.youngsee.adplayer.bean.Area;
import com.youngsee.adplayer.bean.Media;
import com.youngsee.adplayer.bean.PgmBill;
import com.youngsee.adplayer.bean.PgmListInfoResp;
import com.youngsee.adplayer.bean.PgmPlayBill;
import com.youngsee.adplayer.bean.Template;
import com.youngsee.adplayer.pgm.AreaRef;
import com.youngsee.adplayer.pgm.MediaRef;
import com.youngsee.adplayer.pgm.PgmBillRef;
import com.youngsee.adplayer.pgm.PlayBillRef;
import com.youngsee.adplayer.util.JsonHelper;
import com.youngsee.adplayer.util.Logger;
import com.youngsee.adplayer.util.TimeUtil;

public class ProgramManager {

	private Logger mLogger = new Logger();
	
	private final int STATUS_IDLE = 1;
	private final int STATUS_SHOWSTANDBYPGM = 2;

	private final long DEFAULT_THREAD_PERIOD = 1000;
	
	private static ProgramManager INSTANCE = null;

	private MyThread mThread = null;
	
	private List<PgmBillRef> mPgmBillRefLst = null;
	private int mPgmIndex = 0;
	private boolean mIsPgmListChanged = false;
	private Object mPgmLock = new Object();
	
	private AdMainActivity mAdMainActivity = null;
	private int mAdMainActivityStatus = STATUS_IDLE;

	private ProgramManager() {
		loadLocalPgmInfo();

		mThread = new MyThread();
		mThread.start();
	}
	
	public static ProgramManager getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new ProgramManager();
		}
		return INSTANCE;
	}
	
	public void destroy() {
		if (mThread != null) {
			mThread.interrupt();
			mThread = null;
		}
		mAdMainActivity = null;
	}
	
	public void setAdMainActivity(AdMainActivity activity) {
		mAdMainActivity = activity;
	}
	
	private void loadLocalPgmInfo() {
		String pgmjsondata = SysParamManager.getInstance().getPgmJsonData();
		if (pgmjsondata == null) {
			mLogger.i("Local program bill is null.");
			return;
		}
		mLogger.i("Local program bill:");
		mLogger.i(pgmjsondata);

		PgmListInfoResp pgminfo = JsonHelper.getObject(pgmjsondata, PgmListInfoResp.class);
		if (pgminfo == null) {
			mLogger.i("Faided to parse local program bill.");
			return;
		}

		mPgmBillRefLst = getPgmBillRef(pgminfo);
	}
	
	private List<PgmBillRef> getPgmBillRef(PgmListInfoResp pgminfo) {
		if (pgminfo == null) {
			mLogger.i("Program info is null.");
			return null;
		}

		List<PgmBill> pgmbilllst = pgminfo.getPgmBills();
		if (pgmbilllst == null) {
			mLogger.i("Program bill of pgminfo is null.");
			return null;
		}

		if (pgmbilllst.size() == 0) {
			mLogger.i("No program bill in pgminfo.");
			return null;
		}
		
		List<PgmBillRef> pgmbillreflst = new ArrayList<PgmBillRef>();
		for (PgmBill pgmbill : pgmbilllst) {
			PgmBillRef pgmbillref = new PgmBillRef();
			pgmbillref.publishid = pgmbill.getPublishId();
			
			Template template = pgmbill.getTemplate();
			if (template == null) {
				mLogger.i("Template info of pgmbill is null.");
			} else {
				List<Area> arealst = template.getAreas();
				if (arealst == null) {
					mLogger.i("Area info of template is null.");
				} else if (arealst.size() == 0) {
					mLogger.i("No area in template.");
				} else {
					List<AreaRef> areareflst = new ArrayList<AreaRef>();
					for (Area area : arealst) {
						AreaRef arearef = new AreaRef();
						arearef.id = area.getId();
						arearef.type = area.getType();
						arearef.x = area.getX();
						arearef.y = area.getY();
						arearef.w = area.getW();
						arearef.h = area.getH();
						
						List<Media> medialst = area.getMedias();
						if (medialst == null) {
							mLogger.i("Media info of area is null.");
						} else if (medialst.size() == 0) {
							mLogger.i("No media in area.");
						} else {
							List<MediaRef> mediareflst = new ArrayList<MediaRef>();
							for (Media media : medialst) {
								MediaRef mediaref = new MediaRef();
								mediaref.id = media.getId();
								mediaref.type = media.getType();
								mediaref.duration = media.getDuration();
								mediaref.name = media.getName();
								mediaref.remotepath = media.getPath();
								mediaref.sha1 = media.getSha1();
								mediaref.playtimes = 0;
								
								mediareflst.add(mediaref);
							}
							
							arearef.medias = mediareflst;
						}
						
						areareflst.add(arearef);
					}
					
					pgmbillref.areas = areareflst;
				}
			}
			
			List<PgmPlayBill> playbilllst = pgmbill.getPlayBills();
			if (playbilllst == null) {
				mLogger.i("Play bill info of pgmbill is null.");
			} else if (playbilllst.size() == 0) {
				mLogger.i("No play bill info in pgmbill.");
			} else {
				List<PlayBillRef> playbillreflst = new ArrayList<PlayBillRef>();
				for (PgmPlayBill playbill : playbilllst) {
					PlayBillRef playbillref = new PlayBillRef();
					
					String playbegindate = playbill.getPlayBeginDate();
					if (playbegindate == null) {
						mLogger.i("Play begin date of playbill is null.");
					} else {
						playbillref.begintimemillis = TimeUtil.getDateMillis(playbegindate);
					}
					
					String playenddate = playbill.getPlayEndDate();
					if (playenddate == null) {
						mLogger.i("Play end date of playbill is null.");
					} else {
						playbillref.endtimemillis = TimeUtil.getDateMillis(playenddate);
					}
					
					playbillref.totalplaytimes = playbill.getPlayTimes();
					playbillref.currentplaytimes = 0;
					
					playbillreflst.add(playbillref);
				}
				
				pgmbillref.playbills = playbillreflst;
			}

			pgmbillreflst.add(pgmbillref);
		}
		
		return pgmbillreflst;
	}
	
	private final class MyThread extends Thread {
		@Override
		public void run() {
			mLogger.i("A new ProgramManager thread is started. Thread id is " + getId() + ".");

			while (!isInterrupted()) {
				try {
					synchronized (mPgmLock) {
						if ((mPgmBillRefLst == null) || (mPgmBillRefLst.size() == 0)) {
							if ((mAdMainActivity != null)
									&& (mAdMainActivityStatus != STATUS_SHOWSTANDBYPGM)) {
								mAdMainActivity.showStandbyPgm();
								mAdMainActivityStatus = STATUS_SHOWSTANDBYPGM;
							}
						} else {
							if (mIsPgmListChanged) {
								mLogger.i("Program list is changed...");
							}
						}
					}

					Thread.sleep(DEFAULT_THREAD_PERIOD);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void updatePgmList(PgmListInfoResp pgminfo) {
		if (pgminfo == null) {
			mLogger.i("Program info is null.");
			return;
		}
		
		synchronized (mPgmLock) {
			mPgmBillRefLst = getPgmBillRef(pgminfo);
			mIsPgmListChanged = true;
		}
	}

}
