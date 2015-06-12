package com.youngsee.adplayer.view;

import java.util.LinkedList;
import java.util.List;

import com.youngsee.adplayer.ftp.FtpFileInfo;
import com.youngsee.adplayer.pgm.MediaRef;
import com.youngsee.adplayer.util.FileUtils;
import com.youngsee.adplayer.util.FtpHelper;
import com.youngsee.adplayer.util.Logger;
import com.youngsee.adplayer.util.Sha1Util;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public abstract class AdView extends LinearLayout {

	protected Logger mLogger = new Logger();

	protected String mPublishId = null;
	protected int mAreaIndex = -1;
	protected List<MediaRef> mMediaLst = null;
	
	protected Context mContext = null;

	public AdView(Context context) {
		super(context);
		mContext = context;
	}

	public AdView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

    public AdView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
	}

    public void setPublishId(String publishid) {
    	mPublishId = publishid;
    }

    public void setAreaIndex(int index) {
    	mAreaIndex = index;
    }

    public void setMediaList(List<MediaRef> lst) {
    	mMediaLst = lst;
    }

	public abstract void onPause();
    public abstract void onResume();
    public abstract void onDestroy();

    public abstract void start();
    public abstract void stop();

    protected boolean noMediaExistsOrValid() {
    	if (mMediaLst != null) {
	        for (MediaRef media : mMediaLst) {
	            if (FileUtils.isExist(media.localpath)
	            		&& media.sha1.equals(Sha1Util.getFileSignature(media.localpath))) {              
	            	return false;
	            }
	        }
    	}

        return true;
    }
    
    protected boolean isMediaValid(MediaRef media) {
    	//return media.sha1.equals(Sha1Util.getFileSignature(media.localpath));
    	return true;
    }
    
    protected int getFontColor(String fcstr) {
    	if (TextUtils.isEmpty(fcstr)) {
    		mLogger.i("Font color string is empty.");
    		return Color.WHITE;
    	} else if (!fcstr.startsWith("0x")) {
    		mLogger.i("Invalid font color string, fcstr = " + fcstr + ".");
    		return Color.WHITE;
    	}
    	
    	int fontcolor = Color.WHITE;
    	try {
    		fontcolor = 0xFF000000 | Integer.parseInt(fcstr.substring(2), 16);
    	} catch (NumberFormatException e) {
			e.printStackTrace();
		}
    	
    	return fontcolor;
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
    
    protected void downloadMedia(MediaRef media) {
    	FtpFileInfo downloadfile = getDownloadInfo(media);
    	if (downloadfile != null) {
    		LinkedList<FtpFileInfo> downloadlst = new LinkedList<FtpFileInfo>();
    		downloadlst.add(downloadfile);

    		if (FtpHelper.getInstance().ftpDownloadIsWorking()) {
    			FtpHelper.getInstance().addMaterialsToDlQueue(downloadlst);
    		} else {
    			FtpHelper.getInstance().startDownloadPgmMaterials(downloadlst, null);
    		}
    	}
    }

}
