package com.youngsee.adplayer.util;

import java.io.IOException;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

import com.youngsee.adplayer.AdApplication;
import com.youngsee.adplayer.R;
import com.youngsee.adplayer.common.Constants;
import com.youngsee.adplayer.system.XmlSysParam;

public class XmlUtil {

	public static final String SYSPARAMXML_TAG = "sysparam";
	public static final String SYSPARAMXML_VERSION = "version";

	public static final String SYSPARAMXML_TAG_DEVINFO = "devinfo";
	public static final String SYSPARAMXML_TAG_IADSINFO = "iadsinfo";
	
	public static final String SYSPARAMXML_PROP_DEV_ID = "id";
	public static final String SYSPARAMXML_PROP_DEV_MODEL = "model";
	public static final String SYSPARAMXML_PROP_DEV_CRPERIOD = "chargereportperiod";
	public static final String SYSPARAMXML_PROP_DEV_HBPERIOD = "heartbeatperiod";
	public static final String SYSPARAMXML_PROP_DEV_AESKEY = "aeskey";
	public static final String SYSPARAMXML_PROP_DEV_RSAKEY = "rsakey";
	public static final String SYSPARAMXML_PROP_IADS_HOST = "host";
	public static final String SYSPARAMXML_PROP_IADS_PORT = "port";

	private static final Logger sLogger = new Logger();

	public static final void beginDocument(XmlPullParser parser, String firstElementName)
			throws XmlPullParserException, IOException {
	    int type;

	    while ((type=parser.next()) != parser.START_TAG && type != parser.END_DOCUMENT) {
	        ;
	    }

	    if (type != parser.START_TAG) {
	        throw new XmlPullParserException("No start tag found");
	    }

	    if (!parser.getName().equals(firstElementName)) {
	        throw new XmlPullParserException("Unexpected start tag: found " + parser.getName() +
	                ", expected " + firstElementName);
	    }
	}
	
	public static final void nextElement(XmlPullParser parser)
			throws XmlPullParserException, IOException {
	    int type;

	    while ((type=parser.next()) != parser.START_TAG && type != parser.END_DOCUMENT) {
	        ;
	    }
	}
	
	public static final XmlSysParam getSysParam() {
		XmlSysParam param = null;

		InputStream in = null;
		try {
			in = AdApplication.getInstance().getResources().openRawResource(R.raw.sysparam);

			XmlPullParser parser = Xml.newPullParser();
			parser.setInput(in, Constants.DEFAULT_CHARSET);
			
			int eventType = parser.getEventType();
	        while (eventType != XmlPullParser.END_DOCUMENT) {
	            switch (eventType) {
	            case XmlPullParser.START_DOCUMENT:
	            	param = new XmlSysParam();
	            	break;
	            case XmlPullParser.START_TAG:
	            	if (parser.getName().equals(SYSPARAMXML_TAG)) {
	            		int sysparamversion = Integer.parseInt(parser.getAttributeValue(
	        					null, SYSPARAMXML_VERSION));
	            		sLogger.i("System parameter version of XML is " + sysparamversion);
	            	} else if (parser.getName().equals(SYSPARAMXML_TAG_DEVINFO)) {
	            		param.deviceid = parser.getAttributeValue(null, SYSPARAMXML_PROP_DEV_ID);
	            		param.devicemodel = parser.getAttributeValue(null, SYSPARAMXML_PROP_DEV_MODEL);
	            		param.chargereportperiod = Integer.parseInt(parser.getAttributeValue(
	        					null, SYSPARAMXML_PROP_DEV_CRPERIOD));
	            		param.heartbeatperiod = Integer.parseInt(parser.getAttributeValue(
	        					null, SYSPARAMXML_PROP_DEV_HBPERIOD));
	            		param.aeskey = parser.getAttributeValue(null, SYSPARAMXML_PROP_DEV_AESKEY);
	            		param.rsakey = parser.getAttributeValue(null, SYSPARAMXML_PROP_DEV_RSAKEY);
	            	} else if (parser.getName().equals(SYSPARAMXML_TAG_IADSINFO)) {
	            		param.iadshost = parser.getAttributeValue(null, SYSPARAMXML_PROP_IADS_HOST);
	            		param.iadsport = Integer.parseInt(parser.getAttributeValue(
	        					null, SYSPARAMXML_PROP_IADS_PORT));
	            	}
	            	break;
	            }
	
				eventType = parser.next();
	        }
		} catch (XmlPullParserException e) {
			e.printStackTrace();
			param = null;
		} catch (IOException e) {
			e.printStackTrace();
			param = null;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return param;
	}

}
