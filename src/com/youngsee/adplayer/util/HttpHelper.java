package com.youngsee.adplayer.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.youngsee.adplayer.bean.TokenInfo;
import com.youngsee.adplayer.common.Constants;
import com.youngsee.adplayer.common.ServerResponse;

public class HttpHelper {

	private static final int DEFAULT_CONNECT_TIMEOUT = 10 * 1000;
	private static final int DEFAULT_READ_TIMEOUT = 10 * 1000;

	private static final String REQUESTMETHOD_GET = "GET";
	private static final String REQUESTMETHOD_POST = "POST";

	private static final String REQUESTPROPERTY_KEY_ACCEPT = "Accept";
	private static final String REQUESTPROPERTY_KEY_CONTTYPE = "Content-Type";

	private static final String REQUESTPROPERTY_VALUE_APPJSON = "application/json";

	private static final String DEFAULT_CHARSET = "UTF-8";

	private static final String HTTP_PREFIX = "http://";
	private static final String HTTPS_PREFIX = "https://";

	private static final int REQUESTPORT_TOKEN = 8443;
	private static final int REQUESTPORT_DATA = 8888;

	private static final String SERVERLABEL_IADS = "IADS";
	private static final String SERVERLABEL_SMS = "SMS";
	private static final String SERVERLABEL_AMPS = "AMPS";

	private static final String TOKENURL_MIDPART = "/oauth/token?client_id=";
	private static final String TOKENURL_SUFFIX = "&client_secret=secret_1&grant_type=client_credentials";

	private static final String DATAURL_MIDPART_IADS_SERVERINFO = "/services/iadsservice/authorise?access_token=";
	private static final String DATAURL_MIDPART_SMS_CHARGE = "/services/smsservice/charge?access_token=";
	private static final String DATAURL_MIDPART_AMPS_SYSINFO = "/services/ampsservice/sysinfo?access_token=";
	private static final String DATAURL_MIDPART_AMPS_HEARTBEAT = "/services/ampsservice/heartbeat?access_token=";
	private static final String DATAURL_MIDPART_AMPS_CMDRESULT = "/services/ampsservice/cmdresult?access_token=";
	private static final String DATAURL_MIDPART_AMPS_PGMLIST = "/services/ampsservice/pgmlist?access_token=";

	private static Logger sLogger = new Logger();

	private static final HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
    	public boolean verify(String hostname, SSLSession session) {
    		return true;
    	}
    };

	private static void trustAllHosts() {
    	TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
    		public java.security.cert.X509Certificate[] getAcceptedIssuers() {
    			return new java.security.cert.X509Certificate[] {};
    		}

    		public void checkClientTrusted(X509Certificate[] chain,
    				String authType) throws CertificateException {
    		}

    		public void checkServerTrusted(X509Certificate[] chain,
    				String authType) throws CertificateException {
    		}
    	} };

    	try {
    		SSLContext sc = SSLContext.getInstance("TLS");
    		sc.init(null, trustAllCerts, new java.security.SecureRandom());
    		HttpsURLConnection
    				.setDefaultSSLSocketFactory(sc.getSocketFactory());
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
	
	private static boolean isServerTypeValid(int type) {
		switch (type) {
		case Constants.SERVERTYPE_IADS:
		case Constants.SERVERTYPE_SMS:
		case Constants.SERVERTYPE_AMPS:
			return true;
		default:
			return false;
		}
	}
	
	private static String getTokenUrl(int type, String host, String deviceid) {
		if (!isServerTypeValid(type)) {
			sLogger.e("Server type is invalid, type = " + type + ".");
			return null;
		}

		if ((host == null) || (deviceid == null)) {
			sLogger.e("Host or deviceid is null.");
			return null;
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append(HTTPS_PREFIX);
		sb.append(host);
		sb.append(":");
		sb.append(REQUESTPORT_TOKEN);
		sb.append("/");
		switch (type) {
		case Constants.SERVERTYPE_IADS:
			sb.append(SERVERLABEL_IADS);
			break;
		case Constants.SERVERTYPE_SMS:
			sb.append(SERVERLABEL_SMS);
			break;
		case Constants.SERVERTYPE_AMPS:
			sb.append(SERVERLABEL_AMPS);
			break;
		}
		sb.append(TOKENURL_MIDPART);
		sb.append(deviceid);
		sb.append(TOKENURL_SUFFIX);
		
		return sb.toString();
	}

	public static TokenInfo getSeverToken(int type, String host, String deviceid) {
		if (!isServerTypeValid(type)) {
			sLogger.e("Server type is invalid.");
			return null;
		}

		if ((host == null) || (deviceid == null)) {
			sLogger.e("Host or deviceid is null.");
			return null;
		}

		TokenInfo info = null;
		HttpsURLConnection urlConnection = null;
    	BufferedReader in = null;
		try {
			trustAllHosts();
			URL url = new URL(getTokenUrl(type, host, deviceid));
			urlConnection = (HttpsURLConnection)url.openConnection();
			urlConnection.setHostnameVerifier(DO_NOT_VERIFY);
			urlConnection.setConnectTimeout(DEFAULT_CONNECT_TIMEOUT);
			urlConnection.setReadTimeout(DEFAULT_READ_TIMEOUT);
			urlConnection.setDoInput(true);

			int respCode = urlConnection.getResponseCode();
			if (respCode == HttpsURLConnection.HTTP_OK) {
				in = new BufferedReader(new InputStreamReader(
						urlConnection.getInputStream(), DEFAULT_CHARSET));
				StringBuilder sb = new StringBuilder();
				String line;
				while((line = in.readLine()) != null) {
					sb.append(line);
				}
				info = JsonHelper.getObject(sb.toString(), TokenInfo.class);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
        	if (in != null) {
        		try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
        	}
        	
            if (urlConnection != null) {
            	urlConnection.disconnect();
            }
        }
		
		return info;
	}
	
	private static boolean isServiceTypeValid(int servicetype) {
		switch (servicetype) {
		case Constants.SERVICETYPE_IADS_SERVERINFO:
		case Constants.SERVICETYPE_SMS_CHARGE:
		case Constants.SERVICETYPE_AMPS_SYSINFO:
		case Constants.SERVICETYPE_AMPS_HEARTBEAT:
		case Constants.SERVICETYPE_AMPS_CMDRESULT:
		case Constants.SERVICETYPE_AMPS_PGMLIST:
			return true;
		default:
			return false;
		}
	}
	
	private static String getDataUrl(int servicetype, String host, String token) {
		if (!isServiceTypeValid(servicetype)) {
			sLogger.e("Service type is invalid, servicetype = " + servicetype + ".");
			return null;
		}

		if ((host == null) || (token == null)) {
			sLogger.e("Host or token is null.");
			return null;
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append(HTTP_PREFIX);
		sb.append(host);
		sb.append(":");
		sb.append(REQUESTPORT_DATA);
		sb.append("/");
		switch (servicetype) {
		case Constants.SERVICETYPE_IADS_SERVERINFO:
			sb.append(SERVERLABEL_IADS);
			sb.append(DATAURL_MIDPART_IADS_SERVERINFO);
			break;
		case Constants.SERVICETYPE_SMS_CHARGE:
			sb.append(SERVERLABEL_SMS);
			sb.append(DATAURL_MIDPART_SMS_CHARGE);
			break;
		case Constants.SERVICETYPE_AMPS_SYSINFO:
			sb.append(SERVERLABEL_AMPS);
			sb.append(DATAURL_MIDPART_AMPS_SYSINFO);
			break;
		case Constants.SERVICETYPE_AMPS_HEARTBEAT:
			sb.append(SERVERLABEL_AMPS);
			sb.append(DATAURL_MIDPART_AMPS_HEARTBEAT);
			break;
		case Constants.SERVICETYPE_AMPS_CMDRESULT:
			sb.append(SERVERLABEL_AMPS);
			sb.append(DATAURL_MIDPART_AMPS_CMDRESULT);
			break;
		case Constants.SERVICETYPE_AMPS_PGMLIST:
			sb.append(SERVERLABEL_AMPS);
			sb.append(DATAURL_MIDPART_AMPS_PGMLIST);
			break;
		}
		sb.append(token);
		
		return sb.toString();
	}
	
	public static ServerResponse getServerData(int servicetype, String host, String token) {
		if (!isServiceTypeValid(servicetype)) {
			sLogger.e("Service type is invalid, servicetype = " + servicetype + ".");
			return null;
		}
		if (host == null) {
			sLogger.e("Host is null");
			return null;
		}
		if (token == null) {
			sLogger.e("Token is null.");
			return null;
		}
		
		ServerResponse resp = null;
		HttpURLConnection urlConnection = null;
    	BufferedReader in = null;
		try {
			URL url = new URL(getDataUrl(servicetype, host, token));
			urlConnection = (HttpURLConnection)url.openConnection();
			urlConnection.setRequestMethod(REQUESTMETHOD_GET);
			urlConnection.setRequestProperty(REQUESTPROPERTY_KEY_ACCEPT, REQUESTPROPERTY_VALUE_APPJSON);
			urlConnection.setConnectTimeout(DEFAULT_CONNECT_TIMEOUT);
			urlConnection.setReadTimeout(DEFAULT_READ_TIMEOUT);
			urlConnection.setDoInput(true);
			
			int respCode = urlConnection.getResponseCode();
			resp = new ServerResponse();
			resp.code = respCode;
			if (respCode == HttpsURLConnection.HTTP_OK) {
				in = new BufferedReader(new InputStreamReader(
						urlConnection.getInputStream(), DEFAULT_CHARSET));
				StringBuilder sb = new StringBuilder();
				String line;
				while((line = in.readLine()) != null) {
					sb.append(line);
				}
				resp.data = sb.toString();
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
        	if (in != null) {
        		try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
        	}
        	
            if (urlConnection != null) {
            	urlConnection.disconnect();
            }
        }
		
		return resp;
	}
	
	public static ServerResponse postServerData(int servicetype, String host, String token, String data) {
		if (!isServiceTypeValid(servicetype)) {
			sLogger.e("Service type is invalid, servicetype = " + servicetype + ".");
			return null;
		}
		if (host == null) {
			sLogger.e("Host is null");
			return null;
		}
		if (token == null) {
			sLogger.e("Token is null.");
			return null;
		}
		if (data == null) {
			sLogger.e("Data is null");
			return null;
		}
		
		ServerResponse resp = null;
		HttpURLConnection urlConnection = null;
		OutputStreamWriter out = null;
    	BufferedReader in = null;
		try {
			URL url = new URL(getDataUrl(servicetype, host, token));
			urlConnection = (HttpURLConnection)url.openConnection();
			urlConnection.setRequestMethod(REQUESTMETHOD_POST);
			urlConnection.setRequestProperty(REQUESTPROPERTY_KEY_CONTTYPE, REQUESTPROPERTY_VALUE_APPJSON);
			urlConnection.setConnectTimeout(DEFAULT_CONNECT_TIMEOUT);
			urlConnection.setReadTimeout(DEFAULT_READ_TIMEOUT);
			urlConnection.setDoInput(true);
			urlConnection.setDoOutput(true);
			
			out = new OutputStreamWriter(urlConnection.getOutputStream(), DEFAULT_CHARSET);
			out.write(data);
			out.flush();
			
			int respCode = urlConnection.getResponseCode();
			resp = new ServerResponse();
			resp.code = respCode;
			if (respCode == HttpsURLConnection.HTTP_OK) {
				in = new BufferedReader(new InputStreamReader(
						urlConnection.getInputStream(), DEFAULT_CHARSET));
				StringBuilder sb = new StringBuilder();
				String line;
				while((line = in.readLine()) != null) {
					sb.append(line);
				}
				resp.data = sb.toString();
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
        	if (in != null) {
        		try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
        	}
        	
            if (urlConnection != null) {
            	urlConnection.disconnect();
            }
        }
		
		return resp;
	}

}
