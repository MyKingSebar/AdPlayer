package com.youngsee.adplayer.util;

import android.util.Log;

public class Logger {

    private final static boolean logFlag = true;

    public final static String tag = "[AdPlayer]";
    private final static int logLevel = Log.VERBOSE;

    /**
     * Get The Current Function Name
     * 
     * @return
     */
    private String getFunctionName() {
        StackTraceElement[] sts = Thread.currentThread().getStackTrace();

        if (sts == null) {
            return null;
        }

        for (StackTraceElement st : sts) {
            if (st.isNativeMethod() || st.getClassName().equals(Thread.class.getName())
            		|| st.getClassName().equals(this.getClass().getName())) {
                continue;
            }

            return "{Thread:" + Thread.currentThread().getName() + "}" + "[ " + st.getFileName() + ":" + st.getLineNumber() + " "
                    + st.getMethodName() + " ]";
        }

        return null;
    }

    /**
     * The Log Level:i
     * 
     * @param str
     */
    public void i(Object str) {
        if (logFlag && logLevel <= Log.INFO) {
            String name = getFunctionName();
            if (name != null) {
                Log.i(tag, name + " - " + str);
            } else {
                Log.i(tag, str.toString());
            }
        }
    }

    /**
     * The Log Level:d
     * 
     * @param str
     */
    public void d(Object str) {
        if (logFlag && logLevel <= Log.DEBUG) {
            String name = getFunctionName();
            if (name != null) {
                Log.d(tag, name + " - " + str);
            } else {
                Log.d(tag, str.toString());
            }
        }
    }

    /**
     * The Log Level:V
     * 
     * @param str
     */
    public void v(Object str) {
        if (logFlag && logLevel <= Log.VERBOSE) {
            String name = getFunctionName();
            if (name != null) {
                Log.v(tag, name + " - " + str);
            } else {
                Log.v(tag, str.toString());
            }
        }
    }

    /**
     * The Log Level:w
     * 
     * @param str
     */
    public void w(Object str) {
        if (logFlag && logLevel <= Log.WARN) {
            String name = getFunctionName();
            if (name != null) {
                Log.w(tag, name + " - " + str);
            } else {
                Log.w(tag, str.toString());
            }
        }
    }

    /**
     * The Log Level:e
     * 
     * @param str
     */
    public void e(Object str) {
        if (logFlag && logLevel <= Log.ERROR) {
            String name = getFunctionName();
            if (name != null) {
                Log.e(tag, name + " - " + str);
            } else {
                Log.e(tag, str.toString());
            }
        }
    }

    /**
     * The Log Level:e
     * 
     * @param ex
     */
    public void e(Exception ex) {
        if (logFlag && logLevel <= Log.ERROR) {
            Log.e(tag, "error", ex);
        }
    }

    /**
     * The Log Level:e
     * 
     * @param log
     * @param tr
     */
    public void e(String log, Throwable tr) {
        if (logFlag) {
            String line = getFunctionName();
            Log.e(tag, "{Thread:" + Thread.currentThread().getName() + "}" + "[" + line + ":] " + log + "\n", tr);
        }
    }

}
