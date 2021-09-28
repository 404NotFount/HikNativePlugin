package com.bitmap.hikvideoplugin;

import android.app.Application;
import android.content.Context;

/**
 * Create By axd On 2021/9/28.
 * Email 43229097@qq.com
 * Describe：
 */
public class myApp {

    private static boolean isDebug = false;
    private static Context myContext;

    /**
     * 初始化
     *
     * @param context
     * @param isDebug
     */
    public static void init(Application context, boolean isDebug) {
        myContext = context;
        myApp.isDebug = isDebug;
    }

    /**
     * 返回是否为debug状态
     *
     * @return
     */
    public static boolean isDebug() {
        return isDebug;
    }

    /**
     * 获取context对象
     *
     * @return
     */
    public static Context getContext() {
        if (myContext != null) {
            return myContext;
        } else {
            throw new RuntimeException("Please confirm you are registered in Application ( ZXApp.init( this ,false )) ");
        }
    }
}
