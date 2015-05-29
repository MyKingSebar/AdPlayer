/*
 * Copyright (C) 2013 poster PCE YoungSee Inc.
 * All Rights Reserved Proprietary and Confidential.
 * 
 * @author LiLiang-Ping
 */

package com.youngsee.adplayer.ftp;

public class FtpFileInfo {

    private String mRemotePath = null;
    private String mLocalPath = null;
    private String mSignature = null;
    private int mRetryTimes = 0;
    
    public String getRemotePath() {
        return mRemotePath;
    }
    
    public void setRemotePath(String remotePath) {
    	mRemotePath = remotePath;
    }
    
    public String getLocalPath() {
        return mLocalPath;
    }
    
    public void setLocalPath(String localPath) {
        mLocalPath = localPath;
    }
    
    public String getSignature() {
        return mSignature;
    }
    
    public void setSignature(String signature) {
    	mSignature = signature;
    }

    public void setRetyTimes(int nTimes) {
        mRetryTimes = nTimes;
    }
    
    public void addTimes() {
        mRetryTimes++;
    }

    public boolean isReachMaxRetryTimes(int nMaxTimes) {
        return (mRetryTimes >= nMaxTimes);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FtpFileInfo) {
            FtpFileInfo fileInfo = (FtpFileInfo) obj;
            
            if (fileInfo.mRemotePath != null && fileInfo.mLocalPath != null &&
            		fileInfo.mSignature != null) {
                return fileInfo.mRemotePath.equals(mRemotePath) &&
                		fileInfo.mLocalPath.equals(mLocalPath) &&
                		fileInfo.mSignature.equals(mSignature);
            }
        }

        return false;
    }
}
