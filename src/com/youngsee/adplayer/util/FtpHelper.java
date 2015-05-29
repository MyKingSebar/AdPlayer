package com.youngsee.adplayer.util;

import it.sauronsoftware.ftp4j.FTPAbortedException;
import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferException;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPFile;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;
import it.sauronsoftware.ftp4j.FTPListParseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.youngsee.adplayer.AdApplication;
import com.youngsee.adplayer.ftp.FtpFileInfo;
import com.youngsee.adplayer.ftp.FtpOperationInterface;
import com.youngsee.adplayer.manager.SysParamManager;
import com.youngsee.adplayer.system.FtpInfo;
import com.youngsee.adplayer.util.Logger;

public class FtpHelper {

    private Logger                  mLogger                     = new Logger();
    
    // 单个文件最大重试次数
    private final static int        MAX_RETRY_TIMES             = 5;
    
    // 节目素材下载线程(只允许有一个)
    private static DLFileListThread mDLPrgMaterialThreadHandler = null;
    
    // FTP 上传线程集
    private String                  mULFileName                 = "0";
    private Set<ULFileListThread>   mULThreadCollection         = new HashSet<ULFileListThread>();
    
    // FTP 下载线程集
    private Set<DLFileListThread>   mDLThreadCollection         = new HashSet<DLFileListThread>();
    // ///////////////////////////////////////////////////////////////////////////////
    
    private FtpHelper() {
        /*
         * This Class is a single instance mode, and define a private constructor to avoid external use the 'new'
         * keyword to instantiate a objects directly.
         */
    }
    
    private static class FtpHelperHolder {
        static final FtpHelper INSTANCE = new FtpHelper();
    }
    
    public synchronized static FtpHelper getInstance() {
        return FtpHelperHolder.INSTANCE;
    }
    
    /**
     * 批量上传本地文件到FTP指定目录上
     * 
     * @param localFilePaths
     *            本地文件路径列表
     * @param remoteFolderPath
     *            FTP上传目录
     */
    public void uploadFileList(List<FtpFileInfo> localFilePaths, FtpOperationInterface cb) {
        ULFileListThread ulThread = new ULFileListThread(localFilePaths);
        ulThread.setCallback(cb);
        ulThread.start();
        
        synchronized (mULThreadCollection) {
            mULThreadCollection.add(ulThread);
        }
    }
    
    /**
     * 终止所有FTP上传线程
     */
    public void cancelAllUploadThread() {
        synchronized (mULThreadCollection) {  
            for (ULFileListThread thread : mULThreadCollection) {
                thread.cancelUpload();
            }
            mULThreadCollection.clear();
        }
    }
    
    /*
     * Ftp 上传是否正在进行
     */
    public boolean ftpUploadIsWorking() {
        synchronized (mULThreadCollection) {
            return (!mULThreadCollection.isEmpty());
        }
    }
    
    public String getUploadFileName() {
        return mULFileName;
    }
    
    /**
     * FTP上传本地文件到FTP的一个目录下
     * 
     * @param localfile
     *            本地文件
     * @param remoteFolderPath
     *            FTP上传目录
     */
    private final class ULFileListThread extends Thread implements FTPDataTransferListener {
        // FTP服务器
        private FTPClient                          mClient             = null;
        FtpOperationInterface                      callback            = null;
        
        // 待上传队列 (Key: 本地待上传文件位置   Value: 上传后远端存储位置)
        private LinkedList<FtpFileInfo> mWaitForUlQueue = null;
        
        // 正在上传的文件信息
        private FtpFileInfo mFileInfo = null;
        
        private long mCurrentFileSize = 0;
        private long mCurrentULSize   = 0;
        
        public ULFileListThread(List<FtpFileInfo> fileList) {
            mWaitForUlQueue = new LinkedList<FtpFileInfo>(fileList);
        }

        public void setCallback(FtpOperationInterface callback) {
            this.callback = callback;
        }
        
        // 终止线程上传
        public void cancelUpload() {         
            if (mWaitForUlQueue != null) {
                synchronized (mWaitForUlQueue) {
                    mWaitForUlQueue.clear();
                    mWaitForUlQueue = null;
                }
            }
            
            this.interrupt();
            
            if (mClient != null) {
                try {
                    // Aborts the current connection attempt
                    mClient.abortCurrentConnectionAttempt();
                    // Aborts data transfer operation
                    mClient.abortCurrentDataTransfer(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                closeConnection(mClient);
                mClient = null;
            }
        }
        
        @Override
        public void run() {
            while (mWaitForUlQueue != null && !mWaitForUlQueue.isEmpty()) {
                try {
                    if (!AdApplication.getInstance().isNetworkConnected()) {
                        Thread.sleep(1000);
                        continue;
                    }
                    
                    synchronized (mWaitForUlQueue) {
                        mFileInfo = mWaitForUlQueue.removeFirst();
                    }
                    
                    if (!mFileInfo.isReachMaxRetryTimes(MAX_RETRY_TIMES)) {
                        if (!uploadFile(mFileInfo)) {
                            pushFileToRetryQueue(mFileInfo);
                        }
                    }
                } catch (InterruptedException e) {
                    closeConnection(mClient);
                    mClient = null;
                } catch (Exception e) {
                    e.printStackTrace();
                    pushFileToRetryQueue(mFileInfo);
                }
            }
            
            // 移除本上传线程
            synchronized (mULThreadCollection) {
                mULThreadCollection.remove(this);
            }
        }
        
        @Override
        public void started() {
        	String localPath = mFileInfo.getLocalPath();
        	String remotePath = mFileInfo.getRemotePath();
            mLogger.i("FTP 上传" + FileUtils.formatPath4File(localPath) + "已启动...");
            mLogger.i("上传远程文件位置为：" + FileUtils.formatPath4FTP(remotePath));
            if (callback != null) {
                callback.started(localPath, mCurrentFileSize);
            }
        }
        
        @Override
        public void transferred(int length) {
        	mCurrentULSize += length;
            if (callback != null) {
                callback.progress(mFileInfo.getLocalPath(), mCurrentULSize);
            }
            
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            	e.printStackTrace();
            }
        }
        
        @Override
        public void completed() {
        	String localPath = mFileInfo.getLocalPath();
            mLogger.i("FTP 上传" + FileUtils.formatPath4File(localPath) + "已完成.");
            
            if (callback != null) {
                callback.completed(localPath);
            }
        }
        
        @Override
        public void aborted() {
        	String localPath = mFileInfo.getLocalPath();
            mLogger.i("FTP 上传" + FileUtils.formatPath4File(localPath) + "异常终止.");
       
            pushFileToRetryQueue(mFileInfo);
            
            if (callback != null) {
                callback.aborted(localPath);
            }
        }
        
        @Override
        public void failed() {
        	String localPath = mFileInfo.getLocalPath();
            mLogger.i("FTP 上传" + FileUtils.formatPath4File(localPath) + "失败.");
            
            pushFileToRetryQueue(mFileInfo);
            
            if (callback != null) {
                callback.failed(localPath);
            }
        }
        
        private boolean uploadFile(final FtpFileInfo fInfo) throws InterruptedException {
            if (fInfo == null) {
            	mLogger.i("fInfo is NULL.");
                return true;
            }
            
            // 检测FTP连接, 若连接不存在，则尝试FTP建立连接
            while (mClient == null || !mClient.isConnected()) {
                mClient = makeFtpConnection();
                Thread.sleep(1000);
                continue;
            }
            
            String remoteFileName = fInfo.getRemotePath();
            String localFolderPath = fInfo.getLocalPath();
            if (localFolderPath == null || remoteFileName == null) {
            	mLogger.e("local file or remote folder is NULL.");
                closeConnection(mClient);
                return false;
            }

            File lFile = new File(FileUtils.formatPath4File(localFolderPath));
            if (!lFile.exists() || !lFile.isFile()) {
                mLogger.e("upload file is invalid: "+lFile.getAbsolutePath());
                closeConnection(mClient);
                return false;
            }
            
            mCurrentFileSize = FileUtils.getFileLength(localFolderPath);
            mCurrentULSize = 0;

            // 改变远端当前目录
            try {
                mClient.changeDirectory(FileUtils.formatPath4FTP(remoteFileName));
            } catch (IllegalStateException e) {
                e.printStackTrace();
                closeConnection(mClient);
                return false;
            } catch (FTPIllegalReplyException e) {
                e.printStackTrace();
                closeConnection(mClient);
                return false;
            } catch (FTPException e) {
                e.printStackTrace();
                closeConnection(mClient);
                return false;
            } catch (IOException ie) {
            	ie.printStackTrace();
            	closeConnection(mClient);
            	return false;
            }
            
            // 开始上传
            mULFileName = localFolderPath;
            File localfile = new File(FileUtils.formatPath4File(localFolderPath));
            try {
                mClient.upload(localfile, this);
            } catch (IllegalStateException e) {
                e.printStackTrace();
                closeConnection(mClient);
                return false;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                closeConnection(mClient);
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                closeConnection(mClient);
                return false;
            } catch (FTPIllegalReplyException e) {
                e.printStackTrace();
                closeConnection(mClient);
                return false;
            } catch (FTPException e) {
                e.printStackTrace();
                closeConnection(mClient);
                return false;
            } catch (FTPDataTransferException e) {
                e.printStackTrace();
                closeConnection(mClient);
                return false;
            } catch (FTPAbortedException e) {
                e.printStackTrace();
                closeConnection(mClient);
                return false;
            }
            
            // 改回根目录
            try {
                mClient.changeDirectory("/");
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (FTPIllegalReplyException e) {
                e.printStackTrace();
            } catch (FTPException e) {
                e.printStackTrace();
            }
            
            closeConnection(mClient);
            return true;
        }
        
        private void pushFileToRetryQueue(FtpFileInfo fInfo) {
            fInfo.addTimes();
            mWaitForUlQueue.addLast(fInfo);
        }
    }
    
    // //////////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * FTP批量下载文件到本地存储的文件夹,若本地文件夹不存在，则创建
     * 
     * @param remoteFileNameList
     *            FTP服务器上的文件列表
     * @param localFolderPath
     *            本地存储目录
     */
    public Thread downloadFileList(List<FtpFileInfo> remoteFileList, FtpOperationInterface callback) {
        if (remoteFileList == null) {
            mLogger.i("No File to download, remoteFileList = null.");
            return null;
        } else if (AdApplication.getInstance().isForbidToDownload()) {
        	mLogger.i("Download is forbid for now.");
            return null;
        }
        
        DLFileListThread dlThread = new DLFileListThread(remoteFileList);
        dlThread.setCallback(callback);
        dlThread.start();
        
        synchronized (mDLThreadCollection) {
            mDLThreadCollection.add(dlThread);
        }
        
        return dlThread;
    }
    
    public void cancelDownload(Thread dlThread) {
    	if (dlThread != null && dlThread instanceof DLFileListThread) {
    	    final DLFileListThread threadHandler = (DLFileListThread) dlThread;
    		new Thread() {
    			@Override
    	        public void run() {
    			    threadHandler.cancelDownload();
			    	synchronized (mDLThreadCollection) {
			            mDLThreadCollection.remove(threadHandler);
			        }
    			}
    		}.start();
    	}
    }
    
    /**
     * FTP批量下载节目素材到本地存储的文件夹,若本地文件夹不存在，则创建
     * 
     * @param remoteFileNameList
     *            FTP服务器上的文件列表
     * @param localFolderPath
     *            本地存储目录
     */
    public void startDownloadPgmMaterials(List<FtpFileInfo> remoteFileList,
    		FtpOperationInterface callback) {
        if (remoteFileList == null) {
        	mLogger.i("No program Material list to download, remoteFileList = null.");
            return;
        } else if (AdApplication.getInstance().isForbidToDownload()) {
        	mLogger.i("Download is forbid for now.");
            return;
        }
        
        stopDownloadPgmMaterials();
        mDLPrgMaterialThreadHandler = new DLFileListThread(remoteFileList);
        mDLPrgMaterialThreadHandler.setCallback(callback);
        mDLPrgMaterialThreadHandler.start();
    }
    
    public void stopDownloadPgmMaterials() {
        if (mDLPrgMaterialThreadHandler != null) {
            mDLPrgMaterialThreadHandler.cancelDownload();
            mDLPrgMaterialThreadHandler = null;
        }
    }
    
    public void addMaterialsToDlQueue(List<FtpFileInfo> fileList) {
    	if (AdApplication.getInstance().isForbidToDownload() ||
    			mDLPrgMaterialThreadHandler == null) {
    		return;
    	}

    	mDLPrgMaterialThreadHandler.addToDlQueue(fileList);
    }
    
    /**
     * 终止所有FTP下载线程
     */
    public void cancelAllDownloadThread() {
        stopDownloadPgmMaterials();
        synchronized (mDLThreadCollection) {
            for (DLFileListThread thread : mDLThreadCollection) {
                thread.cancelDownload();
            }
            mDLThreadCollection.clear();
        }
    }

    /**
     * Ftp 素材下载是否正在进行
     */
    public boolean ftpDownloadIsWorking() {
        return mDLPrgMaterialThreadHandler != null && mDLPrgMaterialThreadHandler.isRunning();
    }
    
    public String getDownloadFileName() {
    	if (AdApplication.getInstance().isForbidToDownload() ||
    			mDLPrgMaterialThreadHandler == null) {
    		return null;
    	}

    	return mDLPrgMaterialThreadHandler.getDownloadingFileName();
    }
    
    public long getDownloadFileSize() {
    	if (AdApplication.getInstance().isForbidToDownload() ||
    			mDLPrgMaterialThreadHandler == null) {
    		return 0;
    	}

        return mDLPrgMaterialThreadHandler.getFileSize();
    }
    
    public long getDownloadFileCurrentSize() {
    	if (AdApplication.getInstance().isForbidToDownload() ||
    			mDLPrgMaterialThreadHandler == null) {
    		return 0;
    	}

    	return mDLPrgMaterialThreadHandler.getDownloadBytes();
    }
    
    public boolean isDownloading(FtpFileInfo fileInfo) {
        if (mDLPrgMaterialThreadHandler == null) {
            return false;
        }

        return mDLPrgMaterialThreadHandler.isDownLoading(fileInfo);
    }
    
    /**
     * FTP下载文件列表线程
     */
    private final class DLFileListThread extends Thread implements FTPDataTransferListener {
        // FTP参数
        private FTPClient               mClient          = null;
        FtpOperationInterface           mCallback        = null;
        
        // 待下载的文件队列
        private LinkedList<FtpFileInfo> mWaitForDlQueue  = null;
        
        // 正在下载的文件信息
        private FtpFileInfo             mCurrentFileInfo = null;
        private long                    mCurrentFileSize = 0;
        private long                    mCurrentDLSize   = 0;
        
        // 标志
        private boolean                 mManualCancel    = false;
        private boolean                 mIsRun           = true;

        public DLFileListThread(List<FtpFileInfo> fileList) {
            mWaitForDlQueue = new LinkedList<FtpFileInfo>(fileList);
        }

        public void addToDlQueue(List<FtpFileInfo> fileList) {
            if (mWaitForDlQueue == null) {
                mWaitForDlQueue = new LinkedList<FtpFileInfo>();
            }
            
            synchronized (mWaitForDlQueue) {
                mWaitForDlQueue.addAll(fileList);
            }
        }
        
        public void setCallback(FtpOperationInterface callback) {
            mCallback = callback;
        }
        
        public boolean isRunning() {
            return mIsRun;
        }
        
        public boolean isDownLoading(FtpFileInfo toDlFile) {
            if (mCurrentFileInfo != null && mCurrentFileInfo.equals(toDlFile)) {
                return true;
            } else if (mWaitForDlQueue != null) {
                synchronized (mWaitForDlQueue) {
                    return mWaitForDlQueue.contains(toDlFile);
                }
            }

            return false;
        }
        
        public String getDownloadingFileName() {
            if (mCurrentFileInfo == null) {
                return null;
            }

            return mCurrentFileInfo.getRemotePath();
        }
        
        public long getDownloadBytes() {
            return mCurrentDLSize;
        }
        
        public long getFileSize() {
            return mCurrentFileSize;
        }
        
        // 终止线程下载
        public void cancelDownload() {
            mManualCancel = true;

            if (mWaitForDlQueue != null) {
                synchronized (mWaitForDlQueue) {
                    mWaitForDlQueue.clear();
                    mWaitForDlQueue = null;
                }
            }

            if (mClient != null) {
                try {
                    // Aborts data transfer operation
                    mClient.abortCurrentDataTransfer(true);
                    // Aborts the current connection attempt
                    mClient.abortCurrentConnectionAttempt();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                closeConnection(mClient);
                mClient = null;
            }

            interrupt();
        }
        
        @Override
        public void run() {
            mIsRun = true;
            while (mWaitForDlQueue != null && !mWaitForDlQueue.isEmpty()) {
                try {
                    if (!AdApplication.getInstance().isNetworkConnected()) {
                        Thread.sleep(1000);
                        continue;
                    }
                    if (AdApplication.getInstance().isForbidToDownload()) {
                        Thread.sleep(1000);
                        continue;
                    }

                    synchronized (mWaitForDlQueue) {
                        mCurrentFileInfo = mWaitForDlQueue.removeFirst();
                    }

                    if (!mCurrentFileInfo.isReachMaxRetryTimes(MAX_RETRY_TIMES)) {
                        if (!downloadFile(mCurrentFileInfo)) {
                            // 下载失败，等待下一次重试
                            pushFileToRetryQueue(mCurrentFileInfo);
                        }
                    }
                } catch (InterruptedException e) {
                    closeConnection(mClient);
                    mClient = null;
                } catch (Exception e) {
                    e.printStackTrace();
                    // 下载失败，等待下一次重试
                    pushFileToRetryQueue(mCurrentFileInfo);
                }
            }

            if (mDLPrgMaterialThreadHandler != null &&
            		getId() == mDLPrgMaterialThreadHandler.getId()) {
                // 清空素材下载线程
                mDLPrgMaterialThreadHandler = null;
            } else {
                // 移除本线程
                synchronized (mDLThreadCollection) {
                    mDLThreadCollection.remove(this);
                }
            }

            mIsRun = false;
        }
        
        @Override
        public void started() {
            mLogger.i("FTP单线程下载" + FileUtils.formatPath4FTP(mCurrentFileInfo.getRemotePath()) + "已启动...");
            mLogger.i("文件大小：" + mCurrentFileSize);
            mLogger.i("文件存储位置为：" + FileUtils.formatPath4File(mCurrentFileInfo.getLocalPath()));
            if (mCallback != null) {
                mCallback.started(mCurrentFileInfo.getRemotePath(), mCurrentFileSize);
            }
        }
        
        @Override
        public void transferred(int length) {
            mCurrentDLSize += length;
            if (mCallback != null) {
                mCallback.progress(mCurrentFileInfo.getRemotePath(), mCurrentDLSize);
            }
            
            if (AdApplication.getInstance().isForbidToDownload()) {
                if (mClient != null) {
                    try {
                        mClient.abortCurrentDataTransfer(true);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    } catch (FTPIllegalReplyException e1) {
                        e1.printStackTrace();
                    }
                }
            } else {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    if (mClient != null) {
                        try {
                            mClient.abortCurrentDataTransfer(true);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        } catch (FTPIllegalReplyException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        }
        
        @Override
        public void completed() {
            // 若是手动终止下载，产生的错误则不需要通知
            if (mManualCancel) {
            	return;
            }
            
            String remotePath = mCurrentFileInfo.getRemotePath();
            mLogger.i("FTP单线程下载" + FileUtils.formatPath4FTP(remotePath) + "已完成.");
            if (mCallback != null) {
                mCallback.completed(remotePath);
            }
        }
        
        @Override
        public void aborted() {
            // 若是手动终止下载，产生的错误则不需要通知
            if (mManualCancel) {
            	return;
            }
            
            String remotePath = mCurrentFileInfo.getRemotePath();
            mLogger.i("FTP单线程下载" + FileUtils.formatPath4FTP(remotePath) + "异常终止.");
            
            // Push to retry Queue
            pushFileToRetryQueue(mCurrentFileInfo);
            
            if (mCallback != null) {
                mCallback.aborted(remotePath);
            }
        }
        
        @Override
        public void failed() {
            // 若是手动终止下载，产生的错误则不需要通知
            if (mManualCancel) {
            	return;
            }
            
            String remotePath = mCurrentFileInfo.getRemotePath();
            mLogger.i("FTP单线程下载" + FileUtils.formatPath4FTP(remotePath) + "失败.");
            
            // Push to retry Queue
            pushFileToRetryQueue(mCurrentFileInfo);
            
            if (mCallback != null) {
                mCallback.failed(remotePath);
            }
        }
        
        /**
         * FTP下载文件到本地一个文件夹, 若本地文件夹不存在，则创建
         * 
         * @param fInfo
         *            FTP服务器上的文件 (绝对路径， 如 '/1.jpg')
         * @throws InterruptedException 
         */
        public boolean downloadFile(final FtpFileInfo fInfo) throws InterruptedException {
            if (fInfo == null) {
                mLogger.i("fInfo is NULL.");
                return true;
            }
            
            // 初始化变量
            mCurrentFileSize = 0;
            mCurrentDLSize = 0;
            String remoteFileName = fInfo.getRemotePath();
            String localFolderPath = fInfo.getLocalPath();
            StringBuilder sb = new StringBuilder();
            sb.append(FileUtils.formatPath4File(localFolderPath));
            sb.append(File.separator);
            sb.append(FileUtils.getFilename(FileUtils.formatPath4File(remoteFileName)));
            String saveFilePath = sb.toString();

            // 文件是否已存在
            if (FileUtils.isExist(saveFilePath) && fInfo.getSignature() != null &&
                fInfo.getSignature().equals(Sha1Util.getFileSignature(saveFilePath))) {
            	mLogger.i("File is already exist.");
                return true;
            }

            // 检测FTP连接, 若连接不存在，则尝试FTP建立连接
            while (mClient == null || !mClient.isConnected()) {
                mClient = makeFtpConnection();
                Thread.sleep(1000);
            }

            if (remoteFileName == null || localFolderPath == null) {
            	mLogger.e("local file or remote folder is NULL.");
                closeConnection(mClient);
                return false;
            } else if (remoteObjIsExist(mClient, remoteFileName) != FTPFile.TYPE_FILE) {
                // 判断远端文件是否存在
            	mLogger.e("remote file didn't found.");
                closeConnection(mClient);
                return false;
            }
            
            // 获取远程文件大小, 并判断远程文件size的有效性
            long lFileSize = -1;
            if ((lFileSize = getRemoteFileLength(mClient, remoteFileName)) <= 0) {
            	mLogger.e("Remote file size invaild, the size is: " + lFileSize);
                closeConnection(mClient);
                return false;
            }
            mCurrentFileSize = lFileSize;
            
            // 若本地文件夹不存在，则创建
            File localFolder = new File(localFolderPath);
            if (!localFolder.exists()) {
                localFolder.mkdirs();
            }
            
            // 判断磁盘空间是否足够, 若不足则自动删除最旧的文件，以获取最足够的磁盘空间
            if (localFolder.getUsableSpace() < lFileSize) {
            	mLogger.e("No enough space for the file: " + remoteFileName + " (" + lFileSize + ")");
            	mLogger.i("Tried to release some material space...");
                if (AdApplication.getInstance().releaseMaterialSpace(lFileSize)) {
                	mLogger.i("Succeeded to release.");
                } else {
                	mLogger.i("Failed to release.");
                	closeConnection(mClient);
                    return false;
                }
            }

            // 开始下载
            boolean ret = ftpDownload(mClient, remoteFileName, new File(saveFilePath), this);
            
            // 断开连接
            closeConnection(mClient);
            mClient = null;
            return ret;
        }
        
        private void pushFileToRetryQueue(FtpFileInfo fInfo) {
            if (fInfo != null && mWaitForDlQueue != null) {
                fInfo.addTimes();
                mWaitForDlQueue.addLast(fInfo);
            }
        }
    }

    /**
     * 创建FTP连接
     */
    private FTPClient makeFtpConnection() {
    	if (!AdApplication.getInstance().isNetworkConnected()) {
    		mLogger.i("Network is down.");
    		return null;
    	}
        FTPClient client = null;

        FtpInfo ftpinfo = SysParamManager.getInstance().getFtpInfo();
        if (ftpinfo != null) {
        	try {
                client = new FTPClient();
                client.setPassive(true);
                client.connect(ftpinfo.host, ftpinfo.port);
                client.login(ftpinfo.username, ftpinfo.password);
            } catch (Exception e) {
            	e.printStackTrace();

                if (client != null) {
                    closeConnection(client);
                    client = null;
                }
            }
        } else {
        	mLogger.i("No FTP parameter, ftpinfo = null.");
        }

        return client;
    }
    
    /**
     * 关闭FTP连接，关闭时候像服务器发送一条关闭命令
     * 
     * @param client
     *            FTP客户端
     * @return 关闭成功，或者链接已断开，或者链接为null时候返回true，通过两次关闭都失败时候返回false 注意：如果外部用'makeFtpConnection()'创建了FTP连接，则一定要用该方法关闭FTP连接
     */
    private boolean closeConnection(FTPClient client) {
        if (client == null) {
            return true;
        }
        
        if (client.isConnected()) {
            try {
                client.disconnect(true);
            } catch (Exception e) {
            	e.printStackTrace();

                try {
                    client.disconnect(false);
                } catch (Exception e1) {
                    e1.printStackTrace();
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * This method lists the entries of the current working directory parsing the reply to a FTP LIST command.
     * 
     * The response to the LIST command is parsed through the FTPListParser objects registered on the client. The distribution of ftp4j contains some
     * standard parsers already registered on every FTPClient object created. If they don't work in your case (a FTPListParseException is thrown), you
     * can build your own parser implementing the FTPListParser interface and add it to the client by calling its addListParser() method.
     * 
     * Calling this method blocks the current thread until the operation is completed. The operation could be interrupted by another thread calling
     * abortCurrentDataTransfer(). The list() method will break with a FTPAbortedException.
     * 
     * @param fileSpec
     *            A file filter string. Depending on the server implementation, wildcard characters could be accepted.
     * @return The list of the files (and directories) in the current working directory.
     * @throws IllegalStateException
     *             If the client is not connected or not authenticated.
     * @throws IOException
     *             If an I/O error occurs.
     * @throws FTPIllegalReplyException
     *             If the server replies in an illegal way.
     * @throws FTPException
     *             If the operation fails.
     * @throws FTPDataTransferException
     *             If a I/O occurs in the data transfer connection. If you receive this exception the transfer failed, but the main connection with
     *             the remote FTP server is in theory still working.
     * @throws FTPAbortedException
     *             If operation is aborted by another thread.
     * @throws FTPListParseException
     *             If none of the registered parsers can handle the response sent by the server.
     * @see FTPListParser
     * @see FTPClient#addListParser(FTPListParser)
     * @see FTPClient#getListParsers()
     * @see FTPClient#abortCurrentDataTransfer(boolean)
     * @see FTPClient#listNames()
     * @since 1.2
     */
    private int remoteObjIsExist(FTPClient client, String remotePath) {
        try {
            String tmpPath = FileUtils.formatPath4FTP(remotePath);
            FTPFile[] list = client.list(tmpPath);
            if (list.length > 1) {
                return FTPFile.TYPE_DIRECTORY;
            } else if (list.length == 1) {
                FTPFile f = list[0];
                return f.getType();
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (FTPIllegalReplyException e) {
            e.printStackTrace();
        } catch (FTPException e) {
            e.printStackTrace();
        } catch (FTPDataTransferException e) {
            e.printStackTrace();
        } catch (FTPAbortedException e) {
            e.printStackTrace();
        } catch (FTPListParseException e) {
            e.printStackTrace();
        }

        return FTPFile.TYPE_DIRECTORY;
    }
    
    public String[] getRemoteFilesName(String fileSpec) {
    	if (fileSpec == null) {
    		mLogger.i("fileSpec = null.");
    		return null;
    	}

    	String[] retStrArray = null;
		FTPClient ftpClient = null;
		try {
			do {
				ftpClient = makeFtpConnection();
				Thread.sleep(1000);
			} while (ftpClient == null || !ftpClient.isConnected());

			FTPFile[] list = ftpClient.list(FileUtils.formatPath4FTP(fileSpec));
			if (list != null) {
				retStrArray = new String[list.length];
				for (int i = 0; i < list.length; i++) {
					retStrArray[i] = new String(list[i].getName());
				}
			}
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (FTPIllegalReplyException e) {
			e.printStackTrace();
		} catch (FTPException e) {
			e.printStackTrace();
		} catch (FTPDataTransferException e) {
			e.printStackTrace();
		} catch (FTPAbortedException e) {
			e.printStackTrace();
		} catch (FTPListParseException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

        return retStrArray;
    }
    
    /**
     * This method resumes a download operation from the remote server to a local file.
     * 
     * Calling this method blocks the current thread until the operation is completed. The operation could be interrupted by another thread calling
     * abortCurrentDataTransfer(). The method will break with a FTPAbortedException.
     * 
     * @param remoteFileName
     *            The name of the file to download.
     * @param localFile
     *            The local file.
     * @param restartAt
     *            The restart point (number of bytes already downloaded). Use {@link FTPClient#isResumeSupported()} to check if the server supports
     *            resuming of broken data transfers.
     * @param listener
     *            The listener for the operation. Could be null.
     * @param block
     *            The block for the size of the download content. if it is 0, then download all the file content.
     * @throws IllegalStateException
     *             If the client is not connected or not authenticated.
     * @throws FileNotFoundException
     *             If the supplied file cannot be found.
     * @throws IOException
     *             If an I/O error occurs.
     * @throws FTPIllegalReplyException
     *             If the server replies in an illegal way.
     * @throws FTPException
     *             If the operation fails.
     * @throws FTPDataTransferException
     *             If a I/O occurs in the data transfer connection. If you receive this exception the transfer failed, but the main connection with
     *             the remote FTP server is in theory still working.
     * @throws FTPAbortedException
     *             If operation is aborted by another thread.
     * @see FTPClient#abortCurrentDataTransfer(boolean)
     */
    private boolean ftpDownload(FTPClient client, String remoteFileName, File localFile,
    		FTPDataTransferListener listener) {
        boolean ret = false;
        try {
            if (localFile.isFile() && localFile.exists()) {
                // 断点续传
                client.download(FileUtils.formatPath4FTP(remoteFileName), localFile, localFile.length(), listener);
            } else {
                // 全新下载
                client.download(FileUtils.formatPath4FTP(remoteFileName), localFile, listener);
            }
            ret = true;
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (FTPIllegalReplyException e) {
            e.printStackTrace();
        } catch (FTPException e) {
            e.printStackTrace();
        } catch (FTPDataTransferException e) {
            e.printStackTrace();
        } catch (FTPAbortedException e) {
            e.printStackTrace();
        }

        return ret;
    }
    
    /**
     * This method asks and returns a file size in bytes.
     * 
     * @param path
     *            The path to the file.
     * @return The file size in bytes.
     * @throws IllegalStateException
     *             If the client is not connected or not authenticated.
     * @throws IOException
     *             If an I/O error occurs.
     * @throws FTPIllegalReplyException
     *             If the server replies in an illegal way.
     * @throws FTPException
     *             If the operation fails.
     */
    private long getRemoteFileLength(FTPClient client, String remoteFileName) {
		long ret = -1;

        try {
            ret = client.fileSize(remoteFileName);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (FTPIllegalReplyException e) {
            e.printStackTrace();
        } catch (FTPException e) {
            e.printStackTrace();
        }

        return ret;
    }
}
