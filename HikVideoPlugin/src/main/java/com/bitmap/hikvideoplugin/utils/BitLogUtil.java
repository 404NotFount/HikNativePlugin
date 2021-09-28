package com.bitmap.hikvideoplugin.utils;

import android.util.Log;

import com.bitmap.hikvideoplugin.myApp;

import java.util.Locale;

/**
 * Create By axd On 2021/9/28.
 * Email 43229097@qq.com
 * Describe：
 */
public class BitLogUtil {

    /**
     * 展示Log
     *
     * @param message
     */
    public static void loge(String message) {
        if (myApp.isDebug()) {
            Log.e(getClassName(), buildMessage(message));
        }
    }

    /**
     * 展示Log
     *
     * @param title   标题
     * @param message 内容
     */
    public static void loge(String title, String message) {
        if (myApp.isDebug()) {
            Log.e(title, buildMessage(message));
        }
    }

    /**
     * 展示Log
     *
     * @param message
     */
    public static void logw(String message) {
        if (myApp.isDebug()) {
            Log.w(getClassName(), buildMessage(message));
        }
    }

    /**
     * 展示Log
     *
     * @param title   标题
     * @param message 内容
     */
    public static void logw(String title, String message) {
        if (myApp.isDebug()) {
            Log.w(title, buildMessage(message));
        }
    }

    /**
     * 展示Log
     *
     * @param message
     */
    public static void logi(String message) {
        if (myApp.isDebug()) {
            Log.e(getClassName(), buildMessage(message));
        }
    }

    /**
     * 展示Log
     *
     * @param title   标题
     * @param message 内容
     */
    public static void logi(String title, String message) {
        if (myApp.isDebug()) {
            Log.e(title, buildMessage(message));
        }
    }

    /**
     * 展示Log
     *
     * @param message
     */
    public static void logd(String message) {
        if (myApp.isDebug()) {
            Log.e(getClassName(), buildMessage(message));
        }
    }

    /**
     * 展示Log
     *
     * @param title   标题
     * @param message 内容
     */
    public static void logd(String title, String message) {
        if (myApp.isDebug()) {
            Log.e(title, buildMessage(message));
        }
    }

    /**
     * 获取该类的类名
     *
     * @return
     */
    private static String getClassName() {
        try {
            StackTraceElement[] trace = new Throwable().fillInStackTrace()
                    .getStackTrace();
            String callingClass = "";
            for (int i = 2; i < trace.length; i++) {
                Class<?> clazz = trace[i].getClass();
                if (!clazz.equals(BitLogUtil.class)) {
                    callingClass = trace[i].getClassName();
                    callingClass = callingClass.substring(callingClass
                            .lastIndexOf('.') + 1);
                    break;
                }
            }
            return callingClass;
        } catch (Exception e) {
            e.printStackTrace();
            return "debug";
        }
    }

    /**
     * 根据方法名来生成message
     *
     * @param msg
     * @return
     */
    private static String buildMessage(String msg) {
        try {
            StackTraceElement[] trace = new Throwable().fillInStackTrace()
                    .getStackTrace();
            String caller = "";
            String fileName = "";
            int lineNumber = 0;
            for (int i = 2; i < trace.length; i++) {
                Class<?> clazz = trace[i].getClass();
                if (!clazz.equals(BitLogUtil.class)) {
                    fileName = trace[i].getFileName();
                    caller = trace[i].getMethodName();
                    lineNumber = trace[i].getLineNumber();
                    break;
                }
            }
            return String.format(Locale.US, "("+fileName + ":" + lineNumber + ")_%s : %s", caller, msg);
        } catch (Exception e) {
            e.printStackTrace();
            return msg;
        }
    }
}
