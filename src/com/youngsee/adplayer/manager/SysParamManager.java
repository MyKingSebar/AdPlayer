package com.youngsee.adplayer.manager;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.youngsee.adplayer.bean.FtpParam;
import com.youngsee.adplayer.bean.ServerInfoResp;
import com.youngsee.adplayer.bean.SysInfoResp;
import com.youngsee.adplayer.system.DbSysParam;
import com.youngsee.adplayer.system.DevInfo;
import com.youngsee.adplayer.system.IadsInfo;
import com.youngsee.adplayer.system.AmpsInfo;
import com.youngsee.adplayer.system.FtpInfo;
import com.youngsee.adplayer.system.SmsInfo;
import com.youngsee.adplayer.system.SysParam;
import com.youngsee.adplayer.system.XmlSysParam;
import com.youngsee.adplayer.util.DbHelper;
import com.youngsee.adplayer.util.Logger;
import com.youngsee.adplayer.util.SysInfoHelper;
import com.youngsee.adplayer.util.XmlUtil;

public class SysParamManager {

	private Logger mLogger = new Logger();

	ReadWriteLock mReadWriteLock = new ReentrantReadWriteLock();
	private SysParam mSysParam = new SysParam();

	private SysParamManager() {

	}

	private static class SysParamHolder {
        static final SysParamManager INSTANCE = new SysParamManager();
    }

	public static SysParamManager getInstance() {
		return SysParamHolder.INSTANCE;
	}
	
	public void init() {
		DbSysParam dbsysparam = DbHelper.getInstance().getSysParam();
		if (dbsysparam == null) {
			loadSysParam(true);
		} else {
			mReadWriteLock.writeLock().lock();

			if (mSysParam.devinfo == null) {
				mSysParam.devinfo = new DevInfo();
			}
			mSysParam.devinfo.id = dbsysparam.deviceid;
			mSysParam.devinfo.model = dbsysparam.devicemodel;
			mSysParam.devinfo.softwareversion = dbsysparam.softwareversion;
			mSysParam.devinfo.kernelversion = dbsysparam.kernelversion;
			mSysParam.devinfo.terminalgroup = dbsysparam.terminalgroup;
			mSysParam.devinfo.terminalname = dbsysparam.terminalname;
			mSysParam.devinfo.chargereportperiod = dbsysparam.chargereportperiod;
			mSysParam.devinfo.heartbeatperiod = dbsysparam.heartbeatperiod;
			mSysParam.devinfo.iadstoken = dbsysparam.iadstoken;
			mSysParam.devinfo.smstoken = dbsysparam.smstoken;
			mSysParam.devinfo.ampstoken = dbsysparam.ampstoken;
			mSysParam.devinfo.aeskey = dbsysparam.aeskey;
			mSysParam.devinfo.rsakey = dbsysparam.rsakey;
			mSysParam.devinfo.pgmid = dbsysparam.pgmid;
			mSysParam.devinfo.pgmsha1 = dbsysparam.pgmsha1;
			mSysParam.devinfo.pgmjsondata = dbsysparam.pgmjsondata;
			
			if (mSysParam.iadsinfo == null) {
				mSysParam.iadsinfo = new IadsInfo();
			}
			mSysParam.iadsinfo.host = dbsysparam.iadshost;
			mSysParam.iadsinfo.port = dbsysparam.iadsport;

			if (mSysParam.smsinfo == null) {
				mSysParam.smsinfo = new SmsInfo();
			}
			mSysParam.smsinfo.host = dbsysparam.smshost;
			mSysParam.smsinfo.port = dbsysparam.smsport;

			if (mSysParam.ampsinfo == null) {
				mSysParam.ampsinfo = new AmpsInfo();
			}
			mSysParam.ampsinfo.host = dbsysparam.ampshost;
			mSysParam.ampsinfo.port = dbsysparam.ampsport;

			if (mSysParam.ftpinfo == null) {
				mSysParam.ftpinfo = new FtpInfo();
			}
			mSysParam.ftpinfo.host = dbsysparam.ftphost;
			mSysParam.ftpinfo.port = dbsysparam.ftpport;
			mSysParam.ftpinfo.username = dbsysparam.ftpusername;
			mSysParam.ftpinfo.password = dbsysparam.ftppassword;
			
			/* If the software version from database doesn't equal the one of system,
			 * just use the latter.
			 */
			String syssoftwareversion = SysInfoHelper.getSoftwareVersion();
			if ((syssoftwareversion != null)
					&& (!syssoftwareversion.equals(mSysParam.devinfo.softwareversion))) {
				mSysParam.devinfo.softwareversion = syssoftwareversion;
				DbHelper.getInstance().updateSoftwareVersion(syssoftwareversion);
			} else {
				mLogger.i("System software version is " + syssoftwareversion);
				mLogger.i("Software version from database is " + mSysParam.devinfo.softwareversion);
			}
			
			/* If the kernel version from database doesn't equal the one of system,
			 * just use the latter.
			 */
			String syskernalversion = SysInfoHelper.getKernelVersion();
			if ((syskernalversion != null)
					&& (!syskernalversion.equals(mSysParam.devinfo.kernelversion))) {
				mSysParam.devinfo.kernelversion = syskernalversion;
				DbHelper.getInstance().updateKernelVersion(syskernalversion);
			} else {
				mLogger.i("System kernel version is " + syskernalversion);
				mLogger.i("Kernel version from database is " + mSysParam.devinfo.kernelversion);
			}

			mReadWriteLock.writeLock().unlock();
		}
	}

	private void loadSysParam(boolean isinitial) {
		XmlSysParam xmlsysparam = XmlUtil.getSysParam();
		if (xmlsysparam == null) {
			mLogger.i("System parameter from XML is null ");
			return;
		}
		
		mReadWriteLock.writeLock().lock();

		if (mSysParam.devinfo == null) {
			mSysParam.devinfo = new DevInfo();
		}
		mSysParam.devinfo.id = xmlsysparam.deviceid;
		mSysParam.devinfo.model = xmlsysparam.devicemodel;
		mSysParam.devinfo.chargereportperiod = xmlsysparam.chargereportperiod;
		mSysParam.devinfo.heartbeatperiod = xmlsysparam.heartbeatperiod;
		mSysParam.devinfo.aeskey = xmlsysparam.aeskey;
		mSysParam.devinfo.rsakey = xmlsysparam.rsakey;
		
		String softwareversion = SysInfoHelper.getSoftwareVersion();
		String kernelversion = SysInfoHelper.getKernelVersion();
		mSysParam.devinfo.softwareversion = softwareversion;
		mSysParam.devinfo.kernelversion = kernelversion;
		
		if (mSysParam.iadsinfo == null) {
			mSysParam.iadsinfo = new IadsInfo();
		}
		mSysParam.iadsinfo.host = xmlsysparam.iadshost;
		mSysParam.iadsinfo.port = xmlsysparam.iadsport;

		DbHelper.getInstance().setSysParam(isinitial, xmlsysparam, softwareversion, kernelversion);

		mReadWriteLock.writeLock().unlock();
	}

	public String getDeviceId() {
		String deviceid = null;

		mReadWriteLock.readLock().lock();

		if ((mSysParam.devinfo != null)
				&& (mSysParam.devinfo.id != null)) {
			deviceid = new String(mSysParam.devinfo.id);
		}

		mReadWriteLock.readLock().unlock();

		return deviceid;
	}
	
	public String getDeviceModel() {
		String devicemodel = null;

		mReadWriteLock.readLock().lock();

		if ((mSysParam.devinfo != null)
				&& (mSysParam.devinfo.model != null)) {
			devicemodel = new String(mSysParam.devinfo.model);
		}

		mReadWriteLock.readLock().unlock();

		return devicemodel;
	}
	
	public String getSoftwareVersion() {
		String softwareversion = null;

		mReadWriteLock.readLock().lock();

		if ((mSysParam.devinfo != null)
				&& (mSysParam.devinfo.softwareversion != null)) {
			softwareversion = new String(mSysParam.devinfo.softwareversion);
		}

		mReadWriteLock.readLock().unlock();

		return softwareversion;
	}
	
	public String getKernelVersion() {
		String kernelversion = null;

		mReadWriteLock.readLock().lock();

		if ((mSysParam.devinfo != null)
				&& (mSysParam.devinfo.kernelversion != null)) {
			kernelversion = new String(mSysParam.devinfo.kernelversion);
		}

		mReadWriteLock.readLock().unlock();

		return kernelversion;
	}

	public int getChargeReportPeriod() {
		int crperiod = -1;

		mReadWriteLock.readLock().lock();

		if (mSysParam.devinfo != null) {
			crperiod = mSysParam.devinfo.chargereportperiod;
		}

		mReadWriteLock.readLock().unlock();

		return crperiod;
	}
	
	public int getHeartbeatPeriod() {
		int hbperiod = -1;

		mReadWriteLock.readLock().lock();

		if (mSysParam.devinfo != null) {
			hbperiod = mSysParam.devinfo.heartbeatperiod;
		}

		mReadWriteLock.readLock().unlock();

		return hbperiod;
	}
	
	public String getIadsToken() {
		String token = null;
		
		mReadWriteLock.readLock().lock();

		if ((mSysParam.devinfo != null)
				&& (mSysParam.devinfo.iadstoken != null)) {
			token = new String(mSysParam.devinfo.iadstoken);
		}

		mReadWriteLock.readLock().unlock();

		return token;
	}
	
	public String getSmsToken() {
		String token = null;
		
		mReadWriteLock.readLock().lock();

		if ((mSysParam.devinfo != null)
				&& (mSysParam.devinfo.smstoken != null)) {
			token = new String(mSysParam.devinfo.smstoken);
		}

		mReadWriteLock.readLock().unlock();

		return token;
	}
	
	public String getAmpsToken() {
		String token = null;
		
		mReadWriteLock.readLock().lock();

		if ((mSysParam.devinfo != null)
				&& (mSysParam.devinfo.ampstoken != null)) {
			token = new String(mSysParam.devinfo.ampstoken);
		}

		mReadWriteLock.readLock().unlock();

		return token;
	}
	
	public String getAesKey() {
		String aeskey = null;
		
		mReadWriteLock.readLock().lock();

		if ((mSysParam.devinfo != null)
				&& (mSysParam.devinfo.aeskey != null)) {
			aeskey = new String(mSysParam.devinfo.aeskey);
		}

		mReadWriteLock.readLock().unlock();

		return aeskey;
	}
	
	public String getPgmId() {
		String pgmid = null;
		
		mReadWriteLock.readLock().lock();

		if ((mSysParam.devinfo != null)
				&& (mSysParam.devinfo.pgmid != null)) {
			pgmid = new String(mSysParam.devinfo.pgmid);
		}

		mReadWriteLock.readLock().unlock();

		return pgmid;
	}
	
	public String getPgmSha1() {
		String pgmsha1 = null;
		
		mReadWriteLock.readLock().lock();

		if ((mSysParam.devinfo != null)
				&& (mSysParam.devinfo.pgmsha1 != null)) {
			pgmsha1 = new String(mSysParam.devinfo.pgmsha1);
		}

		mReadWriteLock.readLock().unlock();

		return pgmsha1;
	}

	public String getPgmJsonData() {
		String pgmjsondata = null;
		
		mReadWriteLock.readLock().lock();

		if ((mSysParam.devinfo != null)
				&& (mSysParam.devinfo.pgmjsondata != null)) {
			pgmjsondata = new String(mSysParam.devinfo.pgmjsondata);
		}

		mReadWriteLock.readLock().unlock();

		return pgmjsondata;
	}

	public IadsInfo getIadsInfo() {
		IadsInfo iadsinfo = null;

		mReadWriteLock.readLock().lock();

		if (mSysParam.iadsinfo != null) {
			iadsinfo = new IadsInfo(mSysParam.iadsinfo);
		}

		mReadWriteLock.readLock().unlock();
		
		return iadsinfo;
	}
	
	public SmsInfo getSmsInfo() {
		SmsInfo smsinfo = null;

		mReadWriteLock.readLock().lock();

		if (mSysParam.smsinfo != null) {
			smsinfo = new SmsInfo(mSysParam.smsinfo);
		}

		mReadWriteLock.readLock().unlock();
		
		return smsinfo;
	}

	public AmpsInfo getAmpsInfo() {
		AmpsInfo ampsinfo = null;

		mReadWriteLock.readLock().lock();

		if (mSysParam.ampsinfo != null) {
			ampsinfo = new AmpsInfo(mSysParam.ampsinfo);
		}

		mReadWriteLock.readLock().unlock();
		
		return ampsinfo;
	}

	public FtpInfo getFtpInfo() {
		FtpInfo ftpinfo = null;

		mReadWriteLock.readLock().lock();

		if (mSysParam.ftpinfo != null) {
			ftpinfo = new FtpInfo(mSysParam.ftpinfo);
		}

		mReadWriteLock.readLock().unlock();
		
		return ftpinfo;
	}
	
	public void setIadsToken(String token) {
		if (token == null) {
			mLogger.i("Failed to set IADS token, token is null.");
			return;
		}

		mReadWriteLock.writeLock().lock();

		if (mSysParam.devinfo == null) {
			mSysParam.devinfo = new DevInfo();
		}
		mSysParam.devinfo.iadstoken = token;
		
		DbHelper.getInstance().updateIadsToken(token);

		mReadWriteLock.writeLock().unlock();
	}
	
	public void setSmsToken(String token) {
		if (token == null) {
			mLogger.i("Failed to set SMS token, token is null.");
			return;
		}

		mReadWriteLock.writeLock().lock();

		if (mSysParam.devinfo == null) {
			mSysParam.devinfo = new DevInfo();
		}
		mSysParam.devinfo.smstoken = token;
		
		DbHelper.getInstance().updateSmsToken(token);

		mReadWriteLock.writeLock().unlock();
	}
	
	public void setAmpsToken(String token) {
		if (token == null) {
			mLogger.i("Failed to set AMPS token, token is null.");
			return;
		}

		mReadWriteLock.writeLock().lock();

		if (mSysParam.devinfo == null) {
			mSysParam.devinfo = new DevInfo();
		}
		mSysParam.devinfo.ampstoken = token;
		
		DbHelper.getInstance().updateAmpsToken(token);

		mReadWriteLock.writeLock().unlock();
	}
	
	public void setServerInfo(ServerInfoResp info) {
		if (info == null) {
			mLogger.i("Failed to set server info, info is null.");
			return;
		}

		mReadWriteLock.writeLock().lock();

		if (mSysParam.smsinfo == null) {
			mSysParam.smsinfo = new SmsInfo();
		}
		String smshost = info.getSmsHost();
		if (smshost != null) {
			mSysParam.smsinfo.host = smshost;
		}
		
		if (mSysParam.ampsinfo == null) {
			mSysParam.ampsinfo = new AmpsInfo();
		}
		String ampshost = info.getAmpsHost();
		if (ampshost != null) {
			mSysParam.ampsinfo.host = ampshost;
		}

		DbHelper.getInstance().updateServerInfo(info);

		mReadWriteLock.writeLock().unlock();
	}
	
	public void setSysInfo(SysInfoResp info) {
		if (info == null) {
			mLogger.i("Failed to set system info, info is null.");
			return;
		}

		mReadWriteLock.writeLock().lock();

		if (mSysParam.devinfo == null) {
			mSysParam.devinfo = new DevInfo();
		}
		mSysParam.devinfo.terminalgroup = info.getTerminalGroup();
		mSysParam.devinfo.terminalname = info.getTerminalName();

		int chargereportperiod = info.getChargeReportPeriod();
		if (chargereportperiod >= 1) {
			mSysParam.devinfo.chargereportperiod = chargereportperiod;
		} else {
			mLogger.i("Invalid charge report period, chargereportperiod = " + chargereportperiod + ".");
		}

		int heartbeatperiod = info.getHeartbeatPeriod();
		if (heartbeatperiod >= 1) {
			mSysParam.devinfo.heartbeatperiod = heartbeatperiod;
		} else {
			mLogger.i("Invalid heartbeat period, heartbeatperiod = " + heartbeatperiod + ".");
		}

		if (mSysParam.ftpinfo == null) {
			mSysParam.ftpinfo = new FtpInfo();
		}
		FtpParam ftpparam = info.getFtpParam();
		if (ftpparam != null) {
			mSysParam.ftpinfo.host = ftpparam.getHost();

			int port = ftpparam.getPort();
			if (port >= 0) {
				mSysParam.ftpinfo.port = port;
			} else {
				mLogger.i("Invalid FTP port, port = " + port + ".");
			}

			mSysParam.ftpinfo.username = ftpparam.getUsername();
			mSysParam.ftpinfo.password = ftpparam.getPassword();
		}
		
		DbHelper.getInstance().updateSysInfo(info);

		mReadWriteLock.writeLock().unlock();
	}

	public void setPgmInfo(String id, String sha1, String data) {
		if (id == null) {
			mLogger.i("Failed to set program info, id is null.");
			return;
		}
		if (sha1 == null) {
			mLogger.i("Failed to set program info, sha1 is null.");
			return;
		}
		if (data == null) {
			mLogger.i("Failed to set program info, data is null.");
			return;
		}

		mReadWriteLock.writeLock().lock();
		
		if (mSysParam.devinfo == null) {
			mSysParam.devinfo = new DevInfo();
		}
		mSysParam.devinfo.pgmid = id;
		mSysParam.devinfo.pgmsha1 = sha1;
		mSysParam.devinfo.pgmjsondata = data;
		
		DbHelper.getInstance().updatePgmInfo(id, sha1, data);
		
		mReadWriteLock.writeLock().unlock();
	}
}
