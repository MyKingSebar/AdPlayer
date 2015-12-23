package com.youngsee.adplayer.util;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import com.youngsee.adplayer.AdApplication;
import com.youngsee.adplayer.bean.FtpParam;
import com.youngsee.adplayer.bean.ServerInfoResp;
import com.youngsee.adplayer.bean.SysInfoResp;
import com.youngsee.adplayer.common.Constants;
import com.youngsee.adplayer.provider.DbConstants;
import com.youngsee.adplayer.system.DbChargeInfo;
import com.youngsee.adplayer.system.DbSysParam;
import com.youngsee.adplayer.system.XmlSysParam;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;

public class DbHelper {
	private static Logger mLogger = new Logger();

	private static final int DEFAULT_SYSPARAM_DBID = 1;
	
	private ContentResolver mContentResolver;
	
	private DbHelper() {
		mContentResolver = AdApplication.getInstance().getContentResolver();
	}
	
	private static class DbHolder {
        static final DbHelper INSTANCE = new DbHelper();
    }

	public static DbHelper getInstance() {
		return DbHolder.INSTANCE;
	}
	
	public DbSysParam getSysParam() {
		DbSysParam param = null;
		Cursor c = mContentResolver.query(DbConstants.CONTENTURI_SYSPARAM, null, null, null, null);
		
		if (c.moveToFirst()) {
			param = new DbSysParam();
			param.deviceid = c.getString(c.getColumnIndex(DbConstants.SPT_DEVICEID));
			param.devicemodel = c.getString(c.getColumnIndex(DbConstants.SPT_DEVICEMODEL));
			param.softwareversion = c.getString(c.getColumnIndex(DbConstants.SPT_SOFTWAREVERSION));
			param.kernelversion = c.getString(c.getColumnIndex(DbConstants.SPT_KERNELVERSION));
			param.screenwidth = c.getInt(c.getColumnIndex(DbConstants.SPT_SCREENWIDTH));
			param.screenheight = c.getInt(c.getColumnIndex(DbConstants.SPT_SCREENHEIGHT));
			param.terminalgroup = c.getString(c.getColumnIndex(DbConstants.SPT_TERMINALGROUP));
			param.terminalname = c.getString(c.getColumnIndex(DbConstants.SPT_TERMINALNAME));
			param.chargereportperiod = c.getInt(c.getColumnIndex(DbConstants.SPT_CHARGEREPORTPERIOD));
			param.heartbeatperiod = c.getInt(c.getColumnIndex(DbConstants.SPT_HEARTBEATPERIOD));
			param.iadstoken = c.getString(c.getColumnIndex(DbConstants.SPT_IADSTOKEN));
			param.smstoken = c.getString(c.getColumnIndex(DbConstants.SPT_SMSTOKEN));
			param.ampstoken = c.getString(c.getColumnIndex(DbConstants.SPT_AMPSTOKEN));
			param.aeskey = c.getString(c.getColumnIndex(DbConstants.SPT_AESKEY));
			param.rsakey = c.getString(c.getColumnIndex(DbConstants.SPT_RSAKEY));
			param.pgmid = c.getString(c.getColumnIndex(DbConstants.SPT_PGMID));
			param.pgmsha1 = c.getString(c.getColumnIndex(DbConstants.SPT_PGMSHA1));
			byte[] pgmjsondata = c.getBlob(c.getColumnIndex(DbConstants.SPT_PGMJSONDATA));
			if (pgmjsondata != null) {
				try {
					param.pgmjsondata = new String(pgmjsondata, Constants.DEFAULT_CHARSET);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
			param.ftphost = c.getString(c.getColumnIndex(DbConstants.SPT_FTPHOST));
			param.ftpport = c.getInt(c.getColumnIndex(DbConstants.SPT_FTPPORT));
			param.ftpusername = c.getString(c.getColumnIndex(DbConstants.SPT_FTPUSERNAME));
			param.ftppassword = c.getString(c.getColumnIndex(DbConstants.SPT_FTPPASSWORD));
			param.iadshost = c.getString(c.getColumnIndex(DbConstants.SPT_IADSHOST));
			param.iadsport = c.getInt(c.getColumnIndex(DbConstants.SPT_IADSPORT));
			param.smshost = c.getString(c.getColumnIndex(DbConstants.SPT_SMSHOST));
			param.smsport = c.getInt(c.getColumnIndex(DbConstants.SPT_SMSPORT));
			param.ampshost = c.getString(c.getColumnIndex(DbConstants.SPT_AMPSHOST));
			param.ampsport = c.getInt(c.getColumnIndex(DbConstants.SPT_AMPSPORT));
		} else {
			mLogger.i("No record can be found in the system parameter table.");
		}

		c.close();
		
		return param;
	}
	
	public void setSysParam(boolean isinitial, XmlSysParam xmlsysparam, String softwareversion,
			String kernelversion, int screenwidth, int screenheight) {
		ContentValues cv = new ContentValues();
		if (xmlsysparam != null) {
			if (xmlsysparam.deviceid != null) {
				cv.put(DbConstants.SPT_DEVICEID, xmlsysparam.deviceid);
			}
			if (xmlsysparam.devicemodel != null) {
				cv.put(DbConstants.SPT_DEVICEMODEL, xmlsysparam.devicemodel);
			}
			if (xmlsysparam.chargereportperiod != -1) {
				cv.put(DbConstants.SPT_CHARGEREPORTPERIOD, xmlsysparam.chargereportperiod);
			}
			if (xmlsysparam.heartbeatperiod != -1) {
				cv.put(DbConstants.SPT_HEARTBEATPERIOD, xmlsysparam.heartbeatperiod);
			}
			if (xmlsysparam.aeskey != null) {
				cv.put(DbConstants.SPT_AESKEY, xmlsysparam.aeskey);
			}
			if (xmlsysparam.rsakey != null) {
				cv.put(DbConstants.SPT_RSAKEY, xmlsysparam.rsakey);
			}
			if (xmlsysparam.iadshost != null) {
				cv.put(DbConstants.SPT_IADSHOST, xmlsysparam.iadshost);
			}
			if (xmlsysparam.iadsport != -1) {
				cv.put(DbConstants.SPT_IADSPORT, xmlsysparam.iadsport);
			}
		}
		if (softwareversion != null) {
			cv.put(DbConstants.SPT_SOFTWAREVERSION, softwareversion);
		}
		if (kernelversion != null) {
			cv.put(DbConstants.SPT_KERNELVERSION, kernelversion);
		}
		if (screenwidth != -1) {
			cv.put(DbConstants.SPT_SCREENWIDTH, screenwidth);
		}
		if (screenheight != -1) {
			cv.put(DbConstants.SPT_SCREENHEIGHT, screenheight);
		}
		
		if (cv.size() > 0) {
			if (isinitial) {
				mContentResolver.insert(DbConstants.CONTENTURI_SYSPARAM, cv);
			} else {
				mContentResolver.update(ContentUris.withAppendedId(
						DbConstants.CONTENTURI_SYSPARAM, DEFAULT_SYSPARAM_DBID), cv, null, null);
			}
		} else {
			mLogger.i("No content value (System parameter) need to be inserted or updated.");
		}
	}
	
	public void updateSoftwareVersion(String version) {
		if (version == null) {
			mLogger.i("Software version is null.");
			return;
		}

		ContentValues cv = new ContentValues();
		cv.put(DbConstants.SPT_SOFTWAREVERSION, version);
		mContentResolver.update(ContentUris.withAppendedId(
				DbConstants.CONTENTURI_SYSPARAM, DEFAULT_SYSPARAM_DBID), cv, null, null);
	}
	
	public void updateKernelVersion(String version) {
		if (version == null) {
			mLogger.i("Kernel version is null.");
			return;
		}

		ContentValues cv = new ContentValues();
		cv.put(DbConstants.SPT_KERNELVERSION, version);
		mContentResolver.update(ContentUris.withAppendedId(
				DbConstants.CONTENTURI_SYSPARAM, DEFAULT_SYSPARAM_DBID), cv, null, null);
	}
	
	public void updateScreenWidth(int width) {
		if (width < 0) {
			mLogger.i("Screen width is less than zore.");
			return;
		}

		ContentValues cv = new ContentValues();
		cv.put(DbConstants.SPT_SCREENWIDTH, width);
		mContentResolver.update(ContentUris.withAppendedId(
				DbConstants.CONTENTURI_SYSPARAM, DEFAULT_SYSPARAM_DBID), cv, null, null);
	}
	
	public void updateScreenHeight(int height) {
		if (height < 0) {
			mLogger.i("Screen height is less than zore.");
			return;
		}

		ContentValues cv = new ContentValues();
		cv.put(DbConstants.SPT_SCREENHEIGHT, height);
		mContentResolver.update(ContentUris.withAppendedId(
				DbConstants.CONTENTURI_SYSPARAM, DEFAULT_SYSPARAM_DBID), cv, null, null);
	}
	
	public void updateIadsToken(String token) {
		if (token == null) {
			mLogger.i("Token is null.");
			return;
		}

		ContentValues cv = new ContentValues();
		cv.put(DbConstants.SPT_IADSTOKEN, token);
		mContentResolver.update(ContentUris.withAppendedId(
				DbConstants.CONTENTURI_SYSPARAM, DEFAULT_SYSPARAM_DBID), cv, null, null);
	}
	
	public void updateSmsToken(String token) {
		if (token == null) {
			mLogger.i("Token is null.");
			return;
		}

		ContentValues cv = new ContentValues();
		cv.put(DbConstants.SPT_SMSTOKEN, token);
		mContentResolver.update(ContentUris.withAppendedId(
				DbConstants.CONTENTURI_SYSPARAM, DEFAULT_SYSPARAM_DBID), cv, null, null);
	}
	
	public void updateAmpsToken(String token) {
		if (token == null) {
			mLogger.i("Token is null.");
			return;
		}

		ContentValues cv = new ContentValues();
		cv.put(DbConstants.SPT_AMPSTOKEN, token);
		mContentResolver.update(ContentUris.withAppendedId(
				DbConstants.CONTENTURI_SYSPARAM, DEFAULT_SYSPARAM_DBID), cv, null, null);
	}
	
	public void updateServerInfo(ServerInfoResp info) {
		if (info == null) {
			mLogger.i("Server info response is null.");
			return;
		}

		ContentValues cv = new ContentValues();

		String smshost = info.getSmsHost();
		if (smshost != null) {
			cv.put(DbConstants.SPT_SMSHOST, smshost);
		}
		
		String ampshost = info.getAmpsHost();
		if (ampshost != null) {
			cv.put(DbConstants.SPT_AMPSHOST, ampshost);
		}
		
		if (cv.size() > 0) {
			mContentResolver.update(ContentUris.withAppendedId(
					DbConstants.CONTENTURI_SYSPARAM, DEFAULT_SYSPARAM_DBID), cv, null, null);
		} else {
			mLogger.i("No content value (Server info) need to be updated.");
		}
	}
	
	public void updateSysInfo(SysInfoResp info) {
		if (info == null) {
			mLogger.i("System info response is null.");
			return;
		}

		ContentValues cv = new ContentValues();

		String terminalgroup = info.getTerminalGroup();
		if (terminalgroup != null) {
			cv.put(DbConstants.SPT_TERMINALGROUP, terminalgroup);
		}
		
		String terminalname = info.getTerminalName();
		if (terminalname != null) {
			cv.put(DbConstants.SPT_TERMINALNAME, terminalname);
		}
		
		int chargereportperiod = info.getChargeReportPeriod();
		if (chargereportperiod != -1) {
			cv.put(DbConstants.SPT_CHARGEREPORTPERIOD, chargereportperiod);
		}
		
		int heartbeatperiod = info.getHeartbeatPeriod();
		if (heartbeatperiod != 1) {
			cv.put(DbConstants.SPT_HEARTBEATPERIOD, heartbeatperiod);
		}
		
		FtpParam ftpparam = info.getFtpParam();
		if (ftpparam != null) {
			String host = ftpparam.getHost();
			if (host != null) {
				cv.put(DbConstants.SPT_FTPHOST, host);
			}
			int port = ftpparam.getPort();
			if (port != -1) {
				cv.put(DbConstants.SPT_FTPPORT, port);
			}
			String username = ftpparam.getUsername();
			if (username != null) {
				cv.put(DbConstants.SPT_FTPUSERNAME, username);
			}
			String password = ftpparam.getPassword();
			if (password != null) {
				cv.put(DbConstants.SPT_FTPPASSWORD, password);
			}
		}
		
		if (cv.size() > 0) {
			mContentResolver.update(ContentUris.withAppendedId(
					DbConstants.CONTENTURI_SYSPARAM, DEFAULT_SYSPARAM_DBID), cv, null, null);
		} else {
			mLogger.i("No content value (System info) need to be updated.");
		}
	}

	public void updatePgmInfo(String id, String sha1, String data) {
		if (id == null) {
			mLogger.i("Program id is null.");
			return;
		}
		if (sha1 == null) {
			mLogger.i("Program sha1 is null.");
			return;
		}
		if (data == null) {
			mLogger.i("Program data is null.");
			return;
		}
		
		byte[] pgmjsondata = null;
		try {
			pgmjsondata = data.getBytes(Constants.DEFAULT_CHARSET);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		if (pgmjsondata == null) {
			mLogger.i("Program bytes data is null.");
			return;
		}

		ContentValues cv = new ContentValues();
		cv.put(DbConstants.SPT_PGMID, id);
		cv.put(DbConstants.SPT_PGMSHA1, sha1);
		cv.put(DbConstants.SPT_PGMJSONDATA, pgmjsondata);
		mContentResolver.update(ContentUris.withAppendedId(
				DbConstants.CONTENTURI_SYSPARAM, DEFAULT_SYSPARAM_DBID), cv, null, null);
	}

	public DbChargeInfo[] getChargeInfo() {
		Cursor c = mContentResolver.query(DbConstants.CONTENTURI_CHARGEINFO, null, null, null, null);
		
		List<DbChargeInfo> infoLst = null;
		if (c.moveToFirst()) {
			infoLst = new ArrayList<DbChargeInfo>();
			String publishid, playdate;
			int totalplaytimes, currentplaytimes;
			while (!c.isAfterLast()) {
				publishid = c.getString(c.getColumnIndex(DbConstants.CIT_PUBLISHID));
				playdate = c.getString(c.getColumnIndex(DbConstants.CIT_PLAYDATE));
				totalplaytimes = c.getInt(c.getColumnIndex(DbConstants.CIT_TOTALPLAYTIMES));
				currentplaytimes = c.getInt(c.getColumnIndex(DbConstants.CIT_CURRENTPLAYTIMES));
				
				if (!publishid.equals(DbConstants.NOTSET)
						&& !playdate.equals(DbConstants.NOTSET)
						&& (totalplaytimes != -1)
						&& (currentplaytimes != -1)) {
					DbChargeInfo info = new DbChargeInfo(publishid, playdate,
							totalplaytimes, currentplaytimes);
					infoLst.add(info);
				}
				
				c.moveToNext();
			}
		} else {
			mLogger.i("No charge info can be found in the database.");
		}
		
		c.close();

		if (infoLst != null) {
			int size = infoLst.size();
			if (size > 0) {
				DbChargeInfo[] chargeinfo = new DbChargeInfo[size];
				return (DbChargeInfo[])infoLst.toArray(chargeinfo);
			} else {
				mLogger.i("Size of charge info is " + size + ".");
			}
		}

		return null;
	}
	
	public void updateChargeInfo(String publishid, String playdate, int totalplaytimes,
			int currentplaytimes) {
		if (publishid == null) {
			mLogger.i("Publish id is null.");
			return;
		} else if (playdate == null) {
			mLogger.i("Play date is null.");
			return;
		} else if (totalplaytimes < 0) {
			mLogger.i("Total play times is invalid, totalplaytimes = " + totalplaytimes + ".");
			return;
		} else if (currentplaytimes < 1) {
			mLogger.i("Current play times is invalid, currentplaytimes = " + currentplaytimes + ".");
			return;
		}

		StringBuilder sb = new StringBuilder();
		sb.append(DbConstants.CIT_PUBLISHID).append("='").append(publishid).append("'");
		sb.append(" AND ");
		sb.append(DbConstants.CIT_PLAYDATE).append("='").append(playdate).append("'");
		
		String where = sb.toString();
		Cursor c = mContentResolver.query(DbConstants.CONTENTURI_CHARGEINFO, null, where, null, null);
		if (c.moveToFirst()) {
			int dbtotalplaytimes = c.getInt(c.getColumnIndex(DbConstants.CIT_TOTALPLAYTIMES));
			if (totalplaytimes != dbtotalplaytimes) {
				mLogger.i("Invalid total play times, totalplaytimes = "
						+ totalplaytimes + " dbtotalplaytimes = " + dbtotalplaytimes + ".");
			}

			long id = c.getLong(c.getColumnIndex(DbConstants._ID));
			
			ContentValues cv = new ContentValues();
			cv.put(DbConstants.CIT_CURRENTPLAYTIMES, currentplaytimes);

			mContentResolver.update(ContentUris.withAppendedId(
					DbConstants.CONTENTURI_CHARGEINFO, id), cv, null, null);
		} else {
			ContentValues cv = new ContentValues();
			cv.put(DbConstants.CIT_PUBLISHID, publishid);
			cv.put(DbConstants.CIT_PLAYDATE, playdate);
			cv.put(DbConstants.CIT_TOTALPLAYTIMES, totalplaytimes);
			cv.put(DbConstants.CIT_CURRENTPLAYTIMES, currentplaytimes);
			
			mContentResolver.insert(DbConstants.CONTENTURI_CHARGEINFO, cv);
		}
		
		c.close();
	}
	
	public void deleteChargeInfo(DbChargeInfo[] infolst) {
		if (infolst == null) {
			mLogger.i("Charge info is null.");
			return;
		}
		
		long currentdatemillis = TimeUtil.getCurrentDateMillis();
		for (DbChargeInfo info : infolst) {
			long datemillis = TimeUtil.getDateMillis(info.playdate);
			if (datemillis < currentdatemillis) {
				StringBuilder sb = new StringBuilder();
				sb.append(DbConstants.CIT_PUBLISHID).append("='").append(info.publishid).append("'");
				sb.append(" AND ");
				sb.append(DbConstants.CIT_PLAYDATE).append("='").append(info.playdate).append("'");
				
				mContentResolver.delete(DbConstants.CONTENTURI_CHARGEINFO, sb.toString(), null);
			}
		}
	}
	
	public int getPgmCurrentPlayTimes(String publishid, String playdate) {
		if (publishid == null) {
			mLogger.i("Publish id is null.");
			return 0;
		} else if (playdate == null) {
			mLogger.i("Play date is null.");
			return 0;
		}

		int currentplaytimes = 0;

		StringBuilder sb = new StringBuilder();
		sb.append(DbConstants.CIT_PUBLISHID).append("='").append(publishid).append("'");
		sb.append(" AND ");
		sb.append(DbConstants.CIT_PLAYDATE).append("='").append(playdate).append("'");

		String where = sb.toString();
		Cursor c = mContentResolver.query(DbConstants.CONTENTURI_CHARGEINFO, null, where, null, null);
		if (c.moveToFirst()) {
			currentplaytimes = c.getInt(c.getColumnIndex(DbConstants.CIT_CURRENTPLAYTIMES));
		}
		
		c.close();
		
		return currentplaytimes;
	}

}
