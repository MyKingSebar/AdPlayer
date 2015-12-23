package com.youngsee.adplayer.manager;

import java.net.HttpURLConnection;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.youngsee.adplayer.AdApplication;
import com.youngsee.adplayer.bean.CmdResult;
import com.youngsee.adplayer.bean.CmdResultResp;
import com.youngsee.adplayer.bean.DlFileInfo;
import com.youngsee.adplayer.bean.HeartBeatInfo;
import com.youngsee.adplayer.bean.HeartBeatInfoResp;
import com.youngsee.adplayer.bean.PgmInfo;
import com.youngsee.adplayer.bean.PgmListInfo;
import com.youngsee.adplayer.bean.PgmListInfoResp;
import com.youngsee.adplayer.bean.ServerInfo;
import com.youngsee.adplayer.bean.ServerInfoResp;
import com.youngsee.adplayer.bean.SysInfo;
import com.youngsee.adplayer.bean.SysInfoResp;
import com.youngsee.adplayer.bean.TokenInfo;
import com.youngsee.adplayer.bean.UlFileInfo;
import com.youngsee.adplayer.common.Constants;
import com.youngsee.adplayer.common.ServerResponse;
import com.youngsee.adplayer.system.IadsInfo;
import com.youngsee.adplayer.system.AmpsInfo;
import com.youngsee.adplayer.util.FtpHelper;
import com.youngsee.adplayer.util.HttpHelper;
import com.youngsee.adplayer.util.JsonHelper;
import com.youngsee.adplayer.util.Logger;
import com.youngsee.adplayer.util.MessageHelper;
import com.youngsee.adplayer.util.NonceUtil;
import com.youngsee.adplayer.util.Sha1Util;
import com.youngsee.adplayer.util.SysInfoHelper;
import com.youngsee.adplayer.util.TimestampUtil;
import com.youngsee.adplayer.util.WebCmdHelper;

public class WebManager {

	private final int EVENT_BASE = 0x9000;
	private final int EVENT_HEARTBEAT = EVENT_BASE + 1;
	
	private final long DEFAULT_THREAD_PERIOD = 1000;
	private final long DEFAULT_HEARTBEAT_PERIOD = 3 * 1000;

	private Logger mLogger = new Logger();
	
	private static WebManager INSTANCE = null;
	
	private HandlerThread mHandlerThread = null;
	private MyHandler mHandler = null;
	
	private MyThread mThread = null;
	
	private boolean mHasGotSysInfo = false;

	public static WebManager getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new WebManager();
		}
		return INSTANCE;
	}
	
	private WebManager() {
		mHandlerThread = new HandlerThread("webmgr_hthd");
		mHandlerThread.start();
		mHandler = new MyHandler(mHandlerThread.getLooper());
		
		mThread = new MyThread();
		mThread.start();
		mHasGotSysInfo = false;
	}

	public void destroy() {
		if (mThread != null) {
			mThread.interrupt();
			mThread = null;
		}
		
		if (mHandler != null) {
			mHandler.removeMessages(EVENT_HEARTBEAT);
			mHandler = null;
		}

		if (mHandlerThread != null) {
			mHandlerThread.getLooper().quit();
			mHandlerThread = null;
		}
	}
	
	private boolean isServerTypeValid(int type) {
		switch (type) {
		case Constants.SERVERTYPE_IADS:
		case Constants.SERVERTYPE_AMPS:
			return true;
		default:
			return false;
		}
	}

	private boolean isServiceTypeValid(int type) {
		switch (type) {
		case Constants.SERVICETYPE_IADS_SERVERINFO:
		case Constants.SERVICETYPE_AMPS_SYSINFO:
		case Constants.SERVICETYPE_AMPS_HEARTBEAT:
		case Constants.SERVICETYPE_AMPS_CMDRESULT:
		case Constants.SERVICETYPE_AMPS_PGMLIST:
			return true;
		default:
			return false;
		}
	}

	private String requestServerToken(int type, String host, String deviceid) {
		if (!isServerTypeValid(type)) {
			mLogger.e("Service type is invalid, type = " + type + ".");
			return null;
		}
		if (host == null) {
			mLogger.e("Host is null.");
			return null;
		}
		if (deviceid == null) {
			mLogger.i("Device id is null.");
			return null;
		}

		String token = null;

		TokenInfo tokeninfo = HttpHelper.getSeverToken(type, host, deviceid);
		if (tokeninfo == null) {
			mLogger.i("Token info from server is null, type = " + type + ", host = " + host + ".");
		} else {
			token = new String(tokeninfo.getAccessToken());
			if (type == Constants.SERVERTYPE_IADS) {
				SysParamManager.getInstance().setIadsToken(token);
			} else if (type == Constants.SERVERTYPE_AMPS) {
				SysParamManager.getInstance().setAmpsToken(token);
			}
		}
		
		return token;
	}
	
	private String getTokenByServiceType(int type) {
		switch (type) {
		case Constants.SERVICETYPE_IADS_SERVERINFO:
			return SysParamManager.getInstance().getIadsToken();
		case Constants.SERVICETYPE_AMPS_SYSINFO:
		case Constants.SERVICETYPE_AMPS_HEARTBEAT:
		case Constants.SERVICETYPE_AMPS_CMDRESULT:
		case Constants.SERVICETYPE_AMPS_PGMLIST:
			return SysParamManager.getInstance().getAmpsToken();
		default:
			return null;
		}
	}
	
	private int getServerTypeByServiceType(int type) {
		switch (type) {
		case Constants.SERVICETYPE_IADS_SERVERINFO:
			return Constants.SERVERTYPE_IADS;
		case Constants.SERVICETYPE_AMPS_SYSINFO:
		case Constants.SERVICETYPE_AMPS_HEARTBEAT:
		case Constants.SERVICETYPE_AMPS_CMDRESULT:
		case Constants.SERVICETYPE_AMPS_PGMLIST:
			return Constants.SERVERTYPE_AMPS;
		default:
			return -1;
		}
	}

	private String requestServerData(int servicetype, String host, String deviceid, String reqstr) {
		if (!isServiceTypeValid(servicetype)) {
			mLogger.e("Service type is invalid, servicetype = " + servicetype + ".");
			return null;
		}
		if (host == null) {
			mLogger.e("Host is null.");
			return null;
		}
		if (deviceid == null) {
			mLogger.e("Device id is null.");
			return null;
		}
		if (reqstr == null) {
			mLogger.e("Request data is null.");
			return null;
		}

		String token = getTokenByServiceType(servicetype);
		if (token == null) {
			mLogger.i("There is no access token.");
			mLogger.i("Try to get token from server(servicetype=" + servicetype + ")...");

			int servertype = getServerTypeByServiceType(servicetype);
			if (servertype == -1) {
				mLogger.i("Server type is invalid(-1).");
				return null;
			}
			
			token = requestServerToken(servertype, host, deviceid);
			if (token == null) {
				mLogger.i("Token from server(servertype=" + servertype + ") is null.");
				return null;
			}
		}

		if (token != null) {
			String aeskey = SysParamManager.getInstance().getAesKey();
			if (aeskey == null) {
				mLogger.i("Aes key is null.");
				return null;
			}
			
			String encryptmsg = MessageHelper.encryptMsg(aeskey, deviceid, TimestampUtil.getTimestamp(),
					NonceUtil.getRandomString(Constants.DEFAULT_NONCE_STRLEN), token, reqstr);
			if (encryptmsg == null) {
				mLogger.i("Encrypted message is null.");
				return null;
			}
			
			ServerResponse resp = HttpHelper.postServerData(servicetype, host, token, encryptmsg);
			if (resp == null) {
				mLogger.i("Server(servicetype=" + servicetype + ") response is null.");
				return null;
			}
			
			if (resp.code == HttpURLConnection.HTTP_UNAUTHORIZED) {
				mLogger.i("Token is invalid. Try to get token from server(servicetype=" + servicetype + ")...");
				
				int servertype = getServerTypeByServiceType(servicetype);
				if (servertype == -1) {
					mLogger.i("Server type is invalid(-1).");
					return null;
				}
				
				token = requestServerToken(servertype, host, deviceid);
				if (token == null) {
					mLogger.i("Token from server(servertype=" + servertype + ") is null.");
					return null;
				}

				resp = HttpHelper.postServerData(servicetype, host, token, encryptmsg);
				if (resp == null) {
					mLogger.i("Server(servicetype=" + servicetype + ") response is null.");
					return null;
				}
			}
			if (resp.code == HttpURLConnection.HTTP_OK) {
				String decryptmsg = MessageHelper.decryptMsg(aeskey, deviceid, resp.data);
				if (decryptmsg == null) {
					mLogger.i("Decrypted message is null.");
					return null;
				}

				return decryptmsg;
			} else {
				mLogger.i("Response code of server(servicetype=" + servicetype + ") is "
						+ resp.code + ".");
			}
		}

		return null;
	}

	private void requestServerInfo(String host, String deviceid) {
		if (host == null) {
			mLogger.e("Host is null.");
			return;
		}
		if (deviceid == null) {
			mLogger.e("Device id is null.");
			return;
		}

		ServerInfo reqdata = new ServerInfo();
		reqdata.setDeviceId(deviceid);

		String reqstr = JsonHelper.jsonObjectToString(reqdata);
		if (reqstr == null) {
			mLogger.e("Request string is null.");
			return;
		}
		
		String respstr = requestServerData(Constants.SERVICETYPE_IADS_SERVERINFO, host,
				deviceid, reqstr);
		if (respstr == null) {
			mLogger.i("Response string of server info is null.");
			return;
		}
		
		ServerInfoResp respdata = JsonHelper.getObject(respstr, ServerInfoResp.class);
		if (respdata == null) {
			mLogger.i("Response data of server info is null.");
			return;
		}
		
		SysParamManager.getInstance().setServerInfo(respdata);
	}
	
	private void getSysInfoFromServer(String host, String deviceid) {
		if (host == null) {
			mLogger.e("Host is null.");
			return;
		}
		if (deviceid == null) {
			mLogger.e("Device id is null.");
			return;
		}
		
		SysInfo reqdata = new SysInfo();
		reqdata.setDeviceId(deviceid);
		reqdata.setDeviceModel(SysParamManager.getInstance().getDeviceModel());
		reqdata.setSoftwareVersion(SysParamManager.getInstance().getSoftwareVersion());
		reqdata.setKernelVersion(SysParamManager.getInstance().getKernelVersion());
		reqdata.setHeartbeatPeriod(SysParamManager.getInstance().getHeartbeatPeriod());

		String reqstr = JsonHelper.jsonObjectToString(reqdata);
		if (reqstr == null) {
			mLogger.e("Request string is null.");
			return;
		}

		String respstr = requestServerData(Constants.SERVICETYPE_AMPS_SYSINFO, host,
				deviceid, reqstr);
		if (respstr == null) {
			mLogger.i("Response string of system info is null.");
			return;
		}
		
		SysInfoResp respdata = JsonHelper.getObject(respstr, SysInfoResp.class);
		if (respdata == null) {
			mLogger.i("Response data of system info is null.");
			return;
		}
		
		SysParamManager.getInstance().setSysInfo(respdata);
		
		mHasGotSysInfo = true;
	}
	
	private void doHeartBeat(String host, String deviceid) {
		if (host == null) {
			mLogger.e("Host is null.");
			return;
		}
		if (deviceid == null) {
			mLogger.e("Device id is null.");
			return;
		}
		
		HeartBeatInfo reqdata = new HeartBeatInfo();
		reqdata.setDeviceId(deviceid);
		reqdata.setIp(SysInfoHelper.getLocalIp());
		reqdata.setTime(SysInfoHelper.getCurrentTime());
		reqdata.setStatus(PowerManager.getInstance().getStatus());
		reqdata.setDisk(SysInfoHelper.getDiskInfo());
		reqdata.setCpu(SysInfoHelper.getCpuUsage());
		reqdata.setMemory(SysInfoHelper.getMemoryUsage());

		String pgmid = SysParamManager.getInstance().getPgmId();
		if (pgmid != null) {
			PgmInfo pgminfo = new PgmInfo();
			pgminfo.setId(pgmid);
			reqdata.setPgmInfo(pgminfo);
		}

		if (FtpHelper.getInstance().ftpDownloadIsWorking()) {
			DlFileInfo dlfileinfo = new DlFileInfo();
			dlfileinfo.setFilename(FtpHelper.getInstance().getDownloadFileName());
			dlfileinfo.setTotalSize(FtpHelper.getInstance().getDownloadFileSize());
			dlfileinfo.setCurrentSize(FtpHelper.getInstance().getDownloadFileCurrentSize());
			reqdata.setDlFileInfo(dlfileinfo);
		} else {
			reqdata.setDlFileInfo(null);
		}

		if (FtpHelper.getInstance().ftpUploadIsWorking()) {
			UlFileInfo ulfileinfo = new UlFileInfo();
			ulfileinfo.setFilename(FtpHelper.getInstance().getUploadFileName());
			ulfileinfo.setTotalSize(-1);
			ulfileinfo.setCurrentSize(-1);
			reqdata.setUlFileInfo(ulfileinfo);
		} else {
			reqdata.setUlFileInfo(null);
		}

		String reqstr = JsonHelper.jsonObjectToString(reqdata);
		if (reqstr == null) {
			mLogger.e("Request string is null.");
			return;
		}

		String respstr = requestServerData(Constants.SERVICETYPE_AMPS_HEARTBEAT, host,
				deviceid, reqstr);
		if (respstr == null) {
			mLogger.i("Response string of heartbeat is null.");
			return;
		}

		HeartBeatInfoResp respdata = JsonHelper.getObject(respstr, HeartBeatInfoResp.class);
		if (respdata == null) {
			mLogger.i("Response data of heartbeat is null.");
			return;
		}

		WebCmdHelper.getInstance().handleCmd(host, deviceid, respdata);
	}

	public boolean downloadPgmList(String host, String deviceid) {
		if (host == null) {
			mLogger.e("Host is null.");
			return false;
		}
		if (deviceid == null) {
			mLogger.e("Device id is null.");
			return false;
		}

		PgmListInfo reqdata = new PgmListInfo();
		reqdata.setDeviceId(deviceid);

		String reqstr = JsonHelper.jsonObjectToString(reqdata);
		if (reqstr == null) {
			mLogger.e("Request string is null.");
			return false;
		}

		String respstr = requestServerData(Constants.SERVICETYPE_AMPS_PGMLIST, host,
				deviceid, reqstr);
		if (respstr == null) {
			mLogger.i("Response string of program list info is null.");
			return false;
		}
		mLogger.i("Downloaded program list:");
		mLogger.i(respstr);

		String respstrsha1 = Sha1Util.getSignature(respstr);

		String currentpgmsha1 = SysParamManager.getInstance().getPgmSha1();
		if ((currentpgmsha1 == null) || !currentpgmsha1.equals(respstrsha1)) {
			PgmListInfoResp respdata = JsonHelper.getObject(respstr, PgmListInfoResp.class);
			if (respdata == null) {
				mLogger.i("Response data of program list info is null.");
				return false;
			}

			SysParamManager.getInstance().setPgmInfo(respdata.getPgmId(), respstrsha1, respstr);
			
			ProgramManager.getInstance().updatePgmList(respdata);
		} else {
			mLogger.i("Current program sha1 equals the one of downloaded program, just ignore...");
		}

		return true;
	}
	
	public void reportCmdResult(String host, String deviceid, int cmdtype, String cmdid,
			int cmdresult, String param) {
		if (host == null) {
			mLogger.e("Host is null.");
			return;
		}
		if (deviceid == null) {
			mLogger.e("Device id is null.");
			return;
		}
		if (cmdtype < 0) {
			mLogger.e("Command type is invalid, cmdtype = " + cmdtype + ".");
			return;
		}
		if (cmdid == null) {
			mLogger.e("Command id is null.");
			return;
		}
		if (cmdresult < 0) {
			mLogger.e("Command result is invalid, cmdresult = " + cmdresult + ".");
			return;
		}

		CmdResult reqdata = new CmdResult();
		reqdata.setDeviceId(deviceid);
		reqdata.setType(cmdtype);
		reqdata.setId(cmdid);
		reqdata.setResult(cmdresult);
		reqdata.setParam(param);
		
		String reqstr = JsonHelper.jsonObjectToString(reqdata);
		if (reqstr == null) {
			mLogger.e("Request string is null.");
			return;
		}
		
		String respstr = requestServerData(Constants.SERVICETYPE_AMPS_CMDRESULT, host,
				deviceid, reqstr);
		if (respstr == null) {
			mLogger.i("Response string of command result is null.");
			return;
		}
		
		CmdResultResp respdata = JsonHelper.getObject(respstr, CmdResultResp.class);
		if (respdata == null) {
			mLogger.i("Response data of command result is null.");
			return;
		}

		mLogger.i("Report status of command result (type=" + cmdtype + ",id=" + cmdid
				+ ") is " + respdata.getStatus() + ".");
	}
	
	private final class MyThread extends Thread {
		@Override
		public void run() {
			mLogger.i("A new WebManager thread is started. Thread id is " + getId() + ".");
			
			String deviceid;
			IadsInfo iadsinfo;
			AmpsInfo ampsinfo;
			int hbperiod;

			while (!isInterrupted()) {
				try {
					if (!AdApplication.getInstance().isNetworkConnected()) {
						mLogger.i("Network is down, please check it.");
						Thread.sleep(DEFAULT_THREAD_PERIOD);
						continue;
					}
					
					deviceid = SysParamManager.getInstance().getDeviceId();
					if (deviceid == null) {
						mLogger.i("Device id is null.");
						Thread.sleep(DEFAULT_THREAD_PERIOD);
						continue;
					}

					ampsinfo = SysParamManager.getInstance().getAmpsInfo();
					if ((ampsinfo == null) || (ampsinfo.host == null)) {
						mLogger.i("No AMPS info or AMPS host is null.");
						mLogger.i("Tried to request info from IADS...");
						iadsinfo = SysParamManager.getInstance().getIadsInfo();
						if ((iadsinfo != null) && (iadsinfo.host != null)) {
							requestServerInfo(iadsinfo.host, deviceid);
							ampsinfo = SysParamManager.getInstance().getAmpsInfo();
						} else {
							mLogger.i("There is no IADS info or IADS host is null, please check it.");
							Thread.sleep(DEFAULT_THREAD_PERIOD);
							continue;
						}
					}

					if ((ampsinfo != null) && (ampsinfo.host != null)) {
						if (!mHasGotSysInfo) {
							getSysInfoFromServer(ampsinfo.host, deviceid);
						}
						
						if (mHasGotSysInfo) {
							doHeartBeat(ampsinfo.host, deviceid);

							hbperiod = SysParamManager.getInstance().getHeartbeatPeriod();
							if (hbperiod >= 1) {
								Thread.sleep(hbperiod * 1000);
							} else {
								mLogger.i("Heartbeat period is invalid, hbperiod = " + hbperiod + ".");
							    mLogger.i("Use default heartbeat period...");
								Thread.sleep(DEFAULT_HEARTBEAT_PERIOD);
							}
						} else {
							mLogger.i("System info is not got yet, try it again.");
							Thread.sleep(DEFAULT_THREAD_PERIOD);
						}
						continue;
					}

					Thread.sleep(DEFAULT_THREAD_PERIOD);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private class MyHandler extends Handler {
		public MyHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
            case EVENT_HEARTBEAT:
            	
            	break;
            default:
            	
                break;
            }
            super.handleMessage(msg);
		}
	}

}
