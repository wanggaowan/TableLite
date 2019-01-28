package com.keqiang.table.util;

import android.util.Log;

import com.keqiang.table.BuildConfig;


/**
 * 日志工具
 */
public class Logger {
    private static boolean isLogEnable = BuildConfig.DEBUG;
    
    private static String tag = Logger.class.getSimpleName();
    
    public static void debug(boolean isEnable) {
        debug(tag, isEnable);
    }
    
    public static void debug(String logTag, boolean isEnable) {
        tag = logTag;
        isLogEnable = isEnable;
    }
    
    public static void v(String msg) {
        v(tag, msg);
    }
    
    public static void v(String tag, String msg) {
        v(isLogEnable, tag, msg);
    }
    
    public static void v(boolean isLogEnable, String tag, String msg) {
        if (isLogEnable) {
            Log.v(tag, msg);
        }
    }
    
    public static void d(String msg) {
        d(tag, msg);
    }
    
    public static void d(String tag, String msg) {
        d(isLogEnable, tag, msg);
    }
    
    public static void d(boolean isLogEnable, String tag, String msg) {
        if (isLogEnable) {
            Log.d(tag, msg);
        }
    }
    
    public static void i(String msg) {
        i(tag, msg);
    }
    
    public static void i(String tag, String msg) {
        i(isLogEnable, tag, msg);
    }
    
    public static void i(boolean isLogEnable, String tag, String msg) {
        if (isLogEnable) {
            Log.i(tag, msg);
        }
    }
    
    public static void w(String msg) {
        w(tag, msg);
    }
    
    public static void w(String tag, String msg) {
        w(isLogEnable, tag, msg);
    }
    
    public static void w(boolean isLogEnable, String tag, String msg) {
        if (isLogEnable) {
            Log.w(tag, msg);
        }
    }
    
    public static void e(String msg) {
        e(tag, msg);
    }
    
    public static void e(String tag, String msg) {
        e(isLogEnable, tag, msg);
    }
    
    public static void e(boolean isLogEnable, String tag, String msg) {
        if (isLogEnable) {
            Log.e(tag, msg);
        }
    }
    
    public static void printStackTrace(Throwable t) {
        printStackTrace(tag, t);
    }
    
    public static void printStackTrace(String tag, Throwable t) {
        printStackTrace(isLogEnable, tag, t);
    }
    
    public static void printStackTrace(boolean isLogEnable, String tag, Throwable t) {
        e(isLogEnable, tag, Log.getStackTraceString(t));
    }
}
