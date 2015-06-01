package com.youngsee.adplayer.manager;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import com.youngsee.adplayer.AdApplication;
import com.youngsee.adplayer.bean.ChargeBill;
import com.youngsee.adplayer.bean.ChargeInfo;
import com.youngsee.adplayer.bean.ChargeInfoResp;
import com.youngsee.adplayer.bean.PlayBill;
import com.youngsee.adplayer.bean.TokenInfo;
import com.youngsee.adplayer.common.Constants;
import com.youngsee.adplayer.common.ServerResponse;
import com.youngsee.adplayer.system.DbChargeInfo;
import com.youngsee.adplayer.system.SmsInfo;
import com.youngsee.adplayer.util.DbHelper;
import com.youngsee.adplayer.util.HttpHelper;
import com.youngsee.adplayer.util.JsonHelper;
import com.youngsee.adplayer.util.Logger;
import com.youngsee.adplayer.util.MessageHelper;
import com.youngsee.adplayer.util.NonceUtil;
import com.youngsee.adplayer.util.TimestampUtil;

public class ChargeManager {
	private Logger mLogger = new Logger();
	
	private final long DEFAULT_THREAD_PERIOD = 1000;
	private final int DEFAULT_CHARGEREPORT_PERIOD = 3 * 60 * 1000;
	
	private final int CHARGEINFOREPORT_SUCCESS = 0;
	private final int CHARGEINFOREPORT_FAILURE = 1;
	
	private static ChargeManager INSTANCE = null;
	
	private MyThread mThread = null;
	
	private class PlayInfo {
		public String playdate;
		public int playtimes;
	}

	private class PublishInfo {
		public String publishid;
		public List<PlayInfo> playlst;
	}
	
	private ChargeManager() {
		mThread = new MyThread();
		mThread.start();
	}
	
	public static ChargeManager getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new ChargeManager();
		}
		return INSTANCE;
	}
	
	public void destroy() {
		if (mThread != null) {
			mThread.interrupt();
			mThread = null;
		}
	}
	
	private boolean isServiceTypeValid(int type) {
		switch (type) {
		case Constants.SERVICETYPE_SMS_CHARGE:
			return true;
		default:
			return false;
		}
	}
	
	private String requestServerToken(String host, String deviceid) {
		if (host == null) {
			mLogger.e("Host is null.");
			return null;
		}
		if (deviceid == null) {
			mLogger.i("Device id is null.");
			return null;
		}

		String token = null;

		TokenInfo tokeninfo = HttpHelper.getSeverToken(Constants.SERVERTYPE_SMS, host, deviceid);
		if (tokeninfo == null) {
			mLogger.i("Token info from SMS is null, host = " + host + ".");
		} else {
			token = new String(tokeninfo.getAccessToken());
			SysParamManager.getInstance().setSmsToken(token);
		}
		
		return token;
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

		String token = SysParamManager.getInstance().getSmsToken();
		if (token == null) {
			mLogger.i("There is no SMS access token.");
			mLogger.i("Try to get token from SMS, servicetype = " + servicetype + "...");

			token = requestServerToken(host, deviceid);
			if (token == null) {
				mLogger.i("Token from SMS is null.");
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
				mLogger.i("SMS response is null, servicetype = " + servicetype + ".");
				return null;
			}
			
			if (resp.code == HttpURLConnection.HTTP_UNAUTHORIZED) {
				mLogger.i("Token is invalid. Try to get token from SMS, servicetype = " + servicetype + "...");

				token = requestServerToken(host, deviceid);
				if (token == null) {
					mLogger.i("Token from SMS is null.");
					return null;
				}

				resp = HttpHelper.postServerData(servicetype, host, token, encryptmsg);
				if (resp == null) {
					mLogger.i("SMS response is null, servicetype = " + servicetype + ".");
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
				mLogger.i("Response code of SMS is " + resp.code + ", servicetype = " + servicetype + ".");
			}
		}
		
		return null;
	}

	private List<PublishInfo> getPublishId(DbChargeInfo[] chargeinfolst) {
		if (chargeinfolst == null) {
			mLogger.e("Charge info from database is null.");
			return null;
		}
		if (chargeinfolst.length == 0) {
			mLogger.e("No charge info in list.");
			return null;
		}

		List<PublishInfo> publishinfolst = new ArrayList<PublishInfo>();

		for (DbChargeInfo chargeinfo : chargeinfolst) {
			boolean found = false;

			for (PublishInfo publishinfo : publishinfolst) {
				if (chargeinfo.publishid.equals(publishinfo.publishid)) {
					PlayInfo playinfo = new PlayInfo();
					playinfo.playdate = chargeinfo.playdate;
					playinfo.playtimes = chargeinfo.currentplaytimes;
					publishinfo.playlst.add(playinfo);
					
					found = true;
					break;
				}
			}
			
			if (!found) {
				PublishInfo publishinfo = new PublishInfo();
				publishinfo.publishid = chargeinfo.publishid;

				PlayInfo playinfo = new PlayInfo();
				playinfo.playdate = chargeinfo.playdate;
				playinfo.playtimes = chargeinfo.currentplaytimes;
				publishinfo.playlst.add(playinfo);
				
				publishinfolst.add(publishinfo);
			}
		}
		
		return publishinfolst;
	}
	
	private void doChargeReport(String host, String deviceid) {
		if (host == null) {
			mLogger.e("Host is null.");
			return;
		}
		if (deviceid == null) {
			mLogger.e("Device id is null.");
			return;
		}
		
		DbChargeInfo[] dbchargeinfo = DbHelper.getInstance().getChargeInfo();
		if (dbchargeinfo == null) {
			mLogger.i("Charge info from database is null. No need to be reported.");
			return;
		}
		
		List<PublishInfo> publishinfolst = getPublishId(dbchargeinfo);
		if (publishinfolst == null) {
			mLogger.i("Publish info list is null.");
			return;
		}
		
		ChargeInfo reqdata = new ChargeInfo();
		reqdata.setDeviceId(deviceid);
		
		List<ChargeBill> chargebills = new ArrayList<ChargeBill>();
		
		for (PublishInfo publishinfo : publishinfolst) {
			ChargeBill chargebill = new ChargeBill();
			chargebill.setPublishId(publishinfo.publishid);

			if (publishinfo.playlst != null) {
				List<PlayBill> playbills = new ArrayList<PlayBill>();
				
				for (PlayInfo playinfo : publishinfo.playlst) {
					PlayBill playbill = new PlayBill();
					playbill.setPlayDate(playinfo.playdate);
					playbill.setPlayTimes(playinfo.playtimes);
					
					playbills.add(playbill);
				}
				
				if (playbills.size() > 0) {
					chargebill.setPlayBills(playbills);
				}
			} else {
				mLogger.i("Play list is null, publishinfo.publishid = " + publishinfo.publishid + ".");
			}
		}
		
		if (chargebills.size() > 0) {
			reqdata.setChargeBills(chargebills);
		}
		
		String reqstr = JsonHelper.jsonObjectToString(reqdata);
		if (reqstr == null) {
			mLogger.e("Request string is null.");
			return;
		}
		
		String respstr = requestServerData(Constants.SERVICETYPE_SMS_CHARGE, host,
				deviceid, reqstr);
		if (respstr == null) {
			mLogger.i("Response string of charge info is null.");
			return;
		}
		
		ChargeInfoResp respdata = JsonHelper.getObject(respstr, ChargeInfoResp.class);
		if (respdata == null) {
			mLogger.i("Response data of charge info is null.");
			return;
		}

		int status = respdata.getStatus();
		if (status == CHARGEINFOREPORT_SUCCESS) {
			DbHelper.getInstance().deleteChargeInfo(dbchargeinfo);
		} else {
			mLogger.i("Failed to report charge info, status = " + status + ".");
		}
	}
	
	private final class MyThread extends Thread {
		@Override
		public void run() {
			mLogger.i("A new ChargeManager thread is started. Thread id is " + getId() + ".");
			
			String deviceid;
			SmsInfo smsinfo;
			long crperiod;

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
					
					smsinfo = SysParamManager.getInstance().getSmsInfo();
					if ((smsinfo == null) || (smsinfo.host == null)) {
						mLogger.i("No SMS info or SMS host is null.");
						mLogger.i("Wait for SMS info from IADS...");
					} else {
						doChargeReport(smsinfo.host, deviceid);
						
						crperiod = SysParamManager.getInstance().getChargeReportPeriod();
						if (crperiod >= 1) {
							Thread.sleep(crperiod * 60 * 1000);
						} else {
							mLogger.i("Charge report period is invalid, crperiod = " + crperiod + ".");
							mLogger.i("Use default charge report period...");
							Thread.sleep(DEFAULT_CHARGEREPORT_PERIOD);
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
}
