package com.youngsee.adplayer.util;

import com.youngsee.adplayer.bean.HeartBeatInfoResp;
import com.youngsee.adplayer.manager.WebManager;

public class WebCmdHelper {
	private static Logger mLogger = new Logger();
	
	private final int CMDRESULT_SUCCESS = 0;
	private final int CMDRESULT_FAILURE = 1;
	
	private WebCmdHelper() {
	}
	
	private static class WeCmdHolder {
        static final WebCmdHelper INSTANCE = new WebCmdHelper();
    }

	public static WebCmdHelper getInstance() {
		return WeCmdHolder.INSTANCE;
	}
	
	private void downloadPgmList(String host, String deviceid, int cmdtype, String cmdid) {
		int result;
		if (WebManager.getInstance().downloadPgmList(host, deviceid)) {
			result = CMDRESULT_SUCCESS;
		} else {
			result = CMDRESULT_FAILURE;
		}
		reportCmdResult(host, deviceid, cmdtype, cmdid, result, null);
	}
	
	private void reportCmdResult(String host, String deviceid,
			int cmdtype, String cmdid, int cmdresult, String param) {
		WebManager.getInstance().reportCmdResult(host, deviceid, cmdtype, cmdid,
				cmdresult, param);
	}
	
	public void handleCmd(String host, String deviceid, HeartBeatInfoResp data) {
		if (data == null) {
			mLogger.e("Command data is null");
			return;
		}
		
		int cmdtype = data.getCmdType();
		String cmdid = data.getCmdId();
		
		switch (cmdtype) {
		case CmdType.CMDTYPE_SETNAME:
			mLogger.i("Set terminal name...");
			break;
		case CmdType.CMDTYPE_DOWNPGMLIST:
			mLogger.i("Download program list...");
			downloadPgmList(host, deviceid, cmdtype, cmdid);
			break;
		default:
			mLogger.i("Command type is invalid, cmdtype = " + cmdtype + ".");
		}
	}
	
	private class CmdType {
		public static final int CMDTYPE_SETNAME          = 0x03;
		public static final int CMDTYPE_DOWNPGMLIST      = 0x1F;
	}
}
