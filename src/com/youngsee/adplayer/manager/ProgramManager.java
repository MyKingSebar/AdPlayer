package com.youngsee.adplayer.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.text.TextUtils;

import com.youngsee.adplayer.activity.AdMainActivity;
import com.youngsee.adplayer.bean.Area;
import com.youngsee.adplayer.bean.Media;
import com.youngsee.adplayer.bean.PgmBill;
import com.youngsee.adplayer.bean.PgmListInfoResp;
import com.youngsee.adplayer.bean.PgmPlayBill;
import com.youngsee.adplayer.bean.Template;
import com.youngsee.adplayer.common.Constants;
import com.youngsee.adplayer.ftp.FtpFileInfo;
import com.youngsee.adplayer.pgm.AreaRef;
import com.youngsee.adplayer.pgm.MediaRef;
import com.youngsee.adplayer.pgm.PgmBillRef;
import com.youngsee.adplayer.pgm.PlayBillRef;
import com.youngsee.adplayer.util.DbHelper;
import com.youngsee.adplayer.util.FileUtils;
import com.youngsee.adplayer.util.FtpHelper;
import com.youngsee.adplayer.util.JsonHelper;
import com.youngsee.adplayer.util.Logger;
import com.youngsee.adplayer.util.Sha1Util;
import com.youngsee.adplayer.util.TimeUtil;

public class ProgramManager {

	private Logger mLogger = new Logger();
	
	private final long ONEDAYMILLIS = 24 * 60 * 60 * 1000;
	
	private final int STATUS_IDLE = 1;
	private final int STATUS_PLAYINGSTANDBYPGM = 2;
	private final int STATUS_PLAYINGNORMALPGM = 3;
	private final int STATUS_PLAYINGADDITIONALPGM = 4;

	private final long DEFAULT_THREAD_PERIOD = 200;
	
	private static ProgramManager INSTANCE = null;

	private MyThread mThread = null;
	
	private List<PgmBillRef> mPgmBillRefLst = null;
	private CurrentPgmInfo mCurrentPgmInfo = new CurrentPgmInfo();
	private boolean mIsPgmListChanged = false;
	private Object mPgmLock = new Object();
	
	private AdMainActivity mAdMainActivity = null;

	private int mStatus = STATUS_IDLE;
	
	private long mPgmTodayEndMillis = -1;
	
	private class CurrentPgmInfo {
		int index = -1;
		PgmBillRef pgmbill = null;
		boolean finished = false;
	}

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

		mPgmTodayEndMillis = TimeUtil.getCurrentDateMillis() + ONEDAYMILLIS;

		mPgmBillRefLst = getPgmBillRef(pgminfo);

		downloadPgmMaterials(mPgmBillRefLst);
	}
	
	private int getActualMetric(int value, int pgmvalue, int screenvalue) {
		if (value < 0) {
			mLogger.i("Value is less than zore.");
			return -1;
		}
		if (pgmvalue < 0) {
			mLogger.i("Program value is less than zore.");
			return -1;
		}
		if (screenvalue < 0) {
			mLogger.i("Screen value is less than zore.");
			return -1;
		}

		return screenvalue * value / pgmvalue;
	}
	
	private boolean isMediaTypeValid(int type) {
		switch (type) {
		case Constants.MEDIATYPE_VIDEO:
		case Constants.MEDIATYPE_IMAGE:
		case Constants.MEDIATYPE_TEXT:
			return true;
		default:
			return false;
		}
	}
	
	private String getMediaLocalPath(int type) {
		if (!isMediaTypeValid(type)) {
			mLogger.i("Invalid medie type, type = " + type + ".");
			return null;
		}

		StringBuilder sb = new StringBuilder();
		sb.append(FileUtils.getHardDiskPath());
		sb.append(File.separator);
		sb.append(Constants.APPDIRNAME);
		sb.append(File.separator);

		switch (type) {
		case Constants.MEDIATYPE_VIDEO:
			sb.append(Constants.VIDEODIRNAME);
			break;
		case Constants.MEDIATYPE_IMAGE:
			sb.append(Constants.IMAGEDIRNAME);
			break;
		case Constants.MEDIATYPE_TEXT:
			sb.append(Constants.TEXTDIRNAME);
			break;
		}

		return sb.toString();
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

		long currentmillis = System.currentTimeMillis();

		int screenwidth = SysParamManager.getInstance().getScreenWidth();
		int screenheight = SysParamManager.getInstance().getScreenHeight();

		List<PgmBillRef> pgmbillreflst = new ArrayList<PgmBillRef>();
		for (PgmBill pgmbill : pgmbilllst) {
			PgmBillRef pgmbillref = new PgmBillRef();
			pgmbillref.publishid = pgmbill.getPublishId();

			Template template = pgmbill.getTemplate();
			if (template == null) {
				mLogger.i("Template info of pgmbill is null.");
			} else {
				int width = template.getWidth();
				int height = template.getHeight();

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
						arearef.x = getActualMetric(area.getX(), width, screenwidth);
						arearef.y = getActualMetric(area.getY(), height, screenheight);
						arearef.w = getActualMetric(area.getW(), width, screenwidth);
						arearef.h = getActualMetric(area.getH(), height, screenheight);

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
								mediaref.mode = Constants.MEDIAMODE_NONE; // For temporary
								mediaref.duration = media.getDuration();
								mediaref.fontname = "默认"; // For temporary
								mediaref.fontsize = 25; // For temporary
								mediaref.fontcolor = "0xFFFFFFFF"; // For temporary
								mediaref.name = media.getName();
								mediaref.remotepath = media.getPath();
								mediaref.localpath = getMediaLocalPath(mediaref.type)
										+ File.separator
										+ FileUtils.getFilename(mediaref.remotepath);
								mediaref.sha1 = media.getSha1();
								mediaref.playtimes = 0;

								mediareflst.add(mediaref);
							}

							arearef.medias = mediareflst;
						}

						arearef.finished = false;

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
				pgmbillref.todayplaytimes =
						DbHelper.getInstance().getPgmCurrentPlayTimes(pgmbillref.publishid,
								TimeUtil.getCurrentDate());
				pgmbillref.todaytotalplaytimes = 0;

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
						playbillref.endtimemillis = TimeUtil.getDateMillis(playenddate) + ONEDAYMILLIS;
					}

					playbillref.playtimes = playbill.getPlayTimes();

					// Record the play times info of today for every program bill.
					if ((currentmillis >= playbillref.begintimemillis)
							&& (currentmillis < playbillref.endtimemillis)) {
						pgmbillref.todaytotalplaytimes = playbillref.playtimes;
					}

					playbillreflst.add(playbillref);
				}

				pgmbillref.playbills = playbillreflst;
			}

			pgmbillreflst.add(pgmbillref);
		}

		return pgmbillreflst;
	}
	
	private FtpFileInfo getDownloadInfo(MediaRef media) {
		if (FileUtils.isExist(media.localpath)
				&& media.sha1.equals(Sha1Util.getFileSignature(media.localpath))) {
			mLogger.i("Media has been existing. No need to download.");
			return null;
		}
		
		FtpFileInfo info = new FtpFileInfo();
		info.setRemotePath(media.remotepath);
		info.setLocalPath(FileUtils.getFileAbsolutePath(media.localpath));
		info.setSignature(media.sha1);

		if (FtpHelper.getInstance().isDownloading(info)) {
			mLogger.i("Media is being downloaded. Skip...");
			return null;
		}
		
		return info;
	}

	private void downloadPgmMaterials(List<PgmBillRef> pgmbillreflst) {
		if (pgmbillreflst == null) {
			mLogger.i("Program bill list is null.");
			return;
		}
		if (pgmbillreflst.isEmpty()) {
			mLogger.i("No program bill in the list.");
			return;
		}

		LinkedList<FtpFileInfo> currvideodownloadlst = new LinkedList<FtpFileInfo>();
        LinkedList<FtpFileInfo> currimagedownloadlst = new LinkedList<FtpFileInfo>();
        LinkedList<FtpFileInfo> currtextdownloadlst = new LinkedList<FtpFileInfo>();
        LinkedList<FtpFileInfo> otherdownloadlst = new LinkedList<FtpFileInfo>();

        FtpFileInfo downloadfile;
        for (PgmBillRef pgmbillref : pgmbillreflst) {
        	if (pgmbillref.areas == null) {
        		mLogger.i("No area info in the program bill.");
    			continue;
    		}

        	if (isPgmValid(pgmbillref)) {
        		for (AreaRef area : pgmbillref.areas) {
        			if (area.medias == null) {
        				mLogger.i("No media info in the area.");
            			continue;
        			}

        			for (MediaRef media : area.medias) {
        				if (TextUtils.isEmpty(media.localpath)) {
        					mLogger.i("Media local path is empty.");
        					continue;
        				} else if (TextUtils.isEmpty(media.sha1)) {
        					mLogger.i("Media sha1 is empty.");
        					continue;
        				}

        				downloadfile = getDownloadInfo(media);
        				if (downloadfile != null) {
					        switch (media.type) {
					        case Constants.MEDIATYPE_VIDEO:
						        if (!currvideodownloadlst.contains(downloadfile)) {
						        	currvideodownloadlst.addLast(downloadfile);
						        }

						        break;
					        case Constants.MEDIATYPE_IMAGE:
					        	if (!currimagedownloadlst.contains(downloadfile)) {
					        		currimagedownloadlst.addLast(downloadfile);
						        }

					        	break;
					        case Constants.MEDIATYPE_TEXT:
					        	if (!currtextdownloadlst.contains(downloadfile)) {
					        		currtextdownloadlst.addLast(downloadfile);
						        }

					        	break;
					        }
        				}
        			}
        		}
        	} else {
        		for (AreaRef area : pgmbillref.areas) {
        			if (area.medias == null) {
        				mLogger.i("No media info in the area.");
            			continue;
        			}

        			for (MediaRef media : area.medias) {
        				if (TextUtils.isEmpty(media.localpath)) {
        					mLogger.i("Media local path is empty.");
        					continue;
        				} else if (TextUtils.isEmpty(media.sha1)) {
        					mLogger.i("Media sha1 is empty.");
        					continue;
        				}

        				downloadfile = getDownloadInfo(media);
        				if (downloadfile != null) {
	        				if (!otherdownloadlst.contains(downloadfile)) {
	        					otherdownloadlst.addLast(downloadfile);
					        }
        				}
        			}
        		}
        	}
        }

        int videolstsize = currvideodownloadlst.size();
        int imagelstsize = currimagedownloadlst.size();
        int textlstsize = currtextdownloadlst.size();
        int otherlstsize = otherdownloadlst.size();
        if ((videolstsize == 0) && (imagelstsize == 0) && (textlstsize == 0) && (otherlstsize == 0)) {
        	mLogger.i("No program materials need to download.");
        	return;
        }

        LinkedList<FtpFileInfo> totaldownloadlst = new LinkedList<FtpFileInfo>();
        if (textlstsize != 0) {
        	totaldownloadlst.addAll(currtextdownloadlst);
        }
        if (imagelstsize != 0) {
        	totaldownloadlst.addAll(currimagedownloadlst);
        }
        if (videolstsize != 0) {
        	totaldownloadlst.addAll(currvideodownloadlst);
        }
        if (otherlstsize != 0) {
        	totaldownloadlst.addAll(otherdownloadlst);
        }

        FtpHelper.getInstance().startDownloadPgmMaterials(totaldownloadlst, null);
	}

	private boolean isPgmTimeValid(PgmBillRef pgmbillref) {
		if (pgmbillref == null) {
			mLogger.i("Program bill is null.");
			return false;
		}
		if (pgmbillref.playbills == null) {
			mLogger.i("Play bill list of program bill is null.");
			return false;
		}

		long currentmillis = System.currentTimeMillis();

		for (PlayBillRef playbillref : pgmbillref.playbills) {
			if ((currentmillis >= playbillref.begintimemillis)
					&& (currentmillis < playbillref.endtimemillis)) {
				return true;
			}
		}

		return false;
	}

	private boolean isPgmValid(PgmBillRef pgmbillref) {
		if (!isPgmTimeValid(pgmbillref)) {
			mLogger.i("Program time is invalid.");
			return false;
		}
		
		if (pgmbillref.todayplaytimes < pgmbillref.todaytotalplaytimes) {
			return true;
		}

		return false;
	}
	
	private PgmBillRef findNextNormalPgm() {
		if (mPgmBillRefLst == null) {
			mLogger.i("Program bill list is null.");
			return null;
		}

		int size = mPgmBillRefLst.size();
		if (size == 0) {
			mLogger.i("There is no program bill in the list.");
			return null;
		}

		PgmBillRef pgmbillref;
		int count = size;
		while (count-- != 0) {
			mCurrentPgmInfo.index++;
			if (mCurrentPgmInfo.index >= size) {
				mCurrentPgmInfo.index = 0;
			}

			pgmbillref = mPgmBillRefLst.get(mCurrentPgmInfo.index);
			if (isPgmValid(pgmbillref)) {
				return pgmbillref;
			}
		}

		return null;
	}
	
	private PgmBillRef findNextAdditionalPgm() {
		if (mPgmBillRefLst == null) {
			mLogger.i("Program bill list is null.");
			return null;
		}

		int size = mPgmBillRefLst.size();
		if (size == 0) {
			mLogger.i("There is no program bill in the list.");
			return null;
		}

		mCurrentPgmInfo.index++;
		if (mCurrentPgmInfo.index >= size) {
			mCurrentPgmInfo.index = 0;
		}

		return mPgmBillRefLst.get(mCurrentPgmInfo.index);
	}

	private void resetCurrentPgmInfo() {
		mCurrentPgmInfo.index = -1;
		mCurrentPgmInfo.pgmbill = null;
		mCurrentPgmInfo.finished = false;
	}
	
	private void resetAreaInfo(List<AreaRef> arealst) {
		if (arealst == null) {
			mLogger.i("Area list is null.");
			return;
		}

		for (AreaRef area : arealst) {
			area.finished = false;
		}
	}

	private void playStandbyPgm() {
		mAdMainActivity.showStandbyPgm();
		mStatus = STATUS_PLAYINGSTANDBYPGM;
	}

	private void playNextPgm() {
		mCurrentPgmInfo.pgmbill = findNextNormalPgm();
		if (mCurrentPgmInfo.pgmbill == null) {
			resetCurrentPgmInfo();

			mCurrentPgmInfo.pgmbill = findNextAdditionalPgm();
			if (mCurrentPgmInfo.pgmbill == null) {
				resetCurrentPgmInfo();
				playStandbyPgm();
			} else {
				resetAreaInfo(mCurrentPgmInfo.pgmbill.areas);
				mAdMainActivity.showPgm(mCurrentPgmInfo.pgmbill);
				mStatus = STATUS_PLAYINGADDITIONALPGM;
			}
		} else {
			resetAreaInfo(mCurrentPgmInfo.pgmbill.areas);
			mAdMainActivity.showPgm(mCurrentPgmInfo.pgmbill);
			mStatus = STATUS_PLAYINGNORMALPGM;
		}
	}

	private void playNextAdditionalPgm() {
		mCurrentPgmInfo.pgmbill = findNextAdditionalPgm();
		if (mCurrentPgmInfo.pgmbill == null) {
			resetCurrentPgmInfo();
			playStandbyPgm();
		} else {
			resetAreaInfo(mCurrentPgmInfo.pgmbill.areas);
			mAdMainActivity.showPgm(mCurrentPgmInfo.pgmbill);
			mStatus = STATUS_PLAYINGADDITIONALPGM;
		}
	}
	
	private void updatePlayTimesInfo(List<PgmBillRef> pgmbillreflst) {
		if (pgmbillreflst == null) {
			mLogger.i("Program bill list is null.");
			return;
		} else if (pgmbillreflst.isEmpty()) {
			mLogger.i("No program bill in the list.");
			return;
		}

		long currentmillis = System.currentTimeMillis();

		for (PgmBillRef pgmbill : pgmbillreflst) {
			if (pgmbill.playbills == null) {
				mLogger.i("Play bill list of program bill is null.");
				continue;
			} else if (pgmbill.playbills.isEmpty()) {
				mLogger.i("No play bill in the list.");
				continue;
			}

			pgmbill.todayplaytimes =
					DbHelper.getInstance().getPgmCurrentPlayTimes(pgmbill.publishid,
							TimeUtil.getCurrentDate());
			pgmbill.todaytotalplaytimes = 0;

			for (PlayBillRef playbill : pgmbill.playbills) {
				if ((currentmillis >= playbill.begintimemillis)
						&& (currentmillis < playbill.endtimemillis)) {
					pgmbill.todaytotalplaytimes = playbill.playtimes;

					break;
				}
			}
		}
	}
	
	private boolean hasValidPgm() {
		if (mPgmBillRefLst == null) {
			mLogger.i("Program bill list is null.");
			return false;
		} else if (mPgmBillRefLst.isEmpty()) {
			mLogger.i("No program bill in the list.");
			return false;
		}

		for (PgmBillRef pgmbill : mPgmBillRefLst) {
			if (pgmbill.playbills == null) {
				mLogger.i("Play bill list of program bill is null.");
				continue;
			} else if (pgmbill.playbills.isEmpty()) {
				mLogger.i("No play bill in the list.");
				continue;
			}

			if (isPgmValid(pgmbill)) {
				return true;
			}
		}
		
		return false;
	}

	private final class MyThread extends Thread {
		@Override
		public void run() {
			mLogger.i("A new ProgramManager thread is started. Thread id is " + getId() + ".");

			while (!isInterrupted()) {
				try {
					switch (mStatus) {
					case STATUS_IDLE:
						synchronized (mPgmLock) {
							if ((mPgmBillRefLst == null) || mPgmBillRefLst.isEmpty()) {
								playStandbyPgm();
							} else {
								playNextPgm();
							}
						}

						break;
					case STATUS_PLAYINGSTANDBYPGM:
						synchronized (mPgmLock) {
							if (mIsPgmListChanged) {
								mIsPgmListChanged = false;

								if ((mPgmBillRefLst != null) && !mPgmBillRefLst.isEmpty()) {
									playNextPgm();
								}
							}
						}

						break;
					case STATUS_PLAYINGNORMALPGM:
						synchronized (mPgmLock) {
							if ((mPgmBillRefLst == null) || mPgmBillRefLst.isEmpty()) {
								mIsPgmListChanged = false;

								playStandbyPgm();
							} else {
								if (mIsPgmListChanged) {
									mIsPgmListChanged = false;

									playNextPgm();
								} else if ((System.currentTimeMillis() >= mPgmTodayEndMillis)) {
									updatePlayTimesInfo(mPgmBillRefLst);
									mPgmTodayEndMillis =
											TimeUtil.getCurrentDateMillis() + ONEDAYMILLIS;

									playNextPgm();
								} else if (!isPgmTimeValid(mCurrentPgmInfo.pgmbill)) {
									playNextPgm();
								} else if (mCurrentPgmInfo.finished) {
									mCurrentPgmInfo.finished = false;

									playNextPgm();
								}
							}
						}

						break;
					case STATUS_PLAYINGADDITIONALPGM:
						synchronized (mPgmLock) {
							if ((mPgmBillRefLst == null) || mPgmBillRefLst.isEmpty()) {
								mIsPgmListChanged = false;

								playStandbyPgm();
							} else {
								if (mIsPgmListChanged) {
									mIsPgmListChanged = false;

									playNextPgm();
								} else if ((System.currentTimeMillis() >= mPgmTodayEndMillis)) {
									updatePlayTimesInfo(mPgmBillRefLst);
									mPgmTodayEndMillis =
											TimeUtil.getCurrentDateMillis() + ONEDAYMILLIS;

									playNextPgm();
								} else if (hasValidPgm()) {
									playNextPgm();
								} else if (mCurrentPgmInfo.finished) {
									mCurrentPgmInfo.finished = false;

									playNextAdditionalPgm();
								}
							}
						}

						break;
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
			mPgmTodayEndMillis = TimeUtil.getCurrentDateMillis() + ONEDAYMILLIS;

			mPgmBillRefLst = getPgmBillRef(pgminfo);
			resetCurrentPgmInfo();
			mIsPgmListChanged = true;

			downloadPgmMaterials(mPgmBillRefLst);
		}
	}

	public void informPgmAreaFinished(String publishid, int areaindex) {
		synchronized (mPgmLock) {
			if (TextUtils.isEmpty(publishid)) {
				mLogger.i("Publish id is empty.");
				return;
			} else if ((areaindex < 0) || (areaindex >= mCurrentPgmInfo.pgmbill.areas.size())) {
				mLogger.i("Invalid area index, areaindex = " + areaindex + ".");
				return;
			}

			if (mCurrentPgmInfo.pgmbill == null) {
				mLogger.i("Current program bill is null.");
				return;
			} else if (TextUtils.isEmpty(mCurrentPgmInfo.pgmbill.publishid)) {
				mLogger.i("Publish id of current program bill is empty.");
				return;
			} else if (mCurrentPgmInfo.pgmbill.areas == null) {
				mLogger.i("Area list of current program bill is null.");
				return;
			}
			
			if (!publishid.equals(mCurrentPgmInfo.pgmbill.publishid)) {
				mLogger.i("Publish id doesn't equal the one of current program bill, skip...");
				return;
			}

			mCurrentPgmInfo.pgmbill.areas.get(areaindex).finished = true;

			// If there is any area which doesn't finish playing, skip.
			for (AreaRef area : mCurrentPgmInfo.pgmbill.areas) {
				if (!area.finished) {
					return;
				}
			}

			// Reset area info.
			for (AreaRef area : mCurrentPgmInfo.pgmbill.areas) {
				area.finished = false;
			}
			mCurrentPgmInfo.finished = true;

			// Update play times info of program and save it to database.
			mCurrentPgmInfo.pgmbill.todayplaytimes++;
			DbHelper.getInstance().updateChargeInfo(
					mCurrentPgmInfo.pgmbill.publishid,
					TimeUtil.getCurrentDate(),
					mCurrentPgmInfo.pgmbill.todaytotalplaytimes,
					mCurrentPgmInfo.pgmbill.todayplaytimes);
		}
	}

}
