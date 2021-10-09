package com.bitmap.hikvideoplugin.helper;

import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.taobao.weex.bridge.JSCallback;

import java.util.HashMap;

/**
 * Create By axd On 2021/10/8.
 * Email 43229097@qq.com
 * Describe：
 */
public class CallBackHelper {
    private static final String TAG = "CallBackHelper";

    public static HashMap<String, JSCallback> eventCallback = new HashMap<>();

//    public static void sendCallBackActionEvent(JSONObject params, int notificationType) {
//        if (notificationType != 1) {
//            CallBackHelper.sendEvent(JConstants.NOTIFICATION_EVENT, params);
//        } else {
//            CallBackHelper.sendEvent(JConstants.LOCAL_NOTIFICATION_EVENT, params);
//        }
//
//    }

    public static void invoke(String eventName, JSONObject params) {
        try {
            if (!TextUtils.isEmpty(eventName) && params != null) {
//                Log.d(TAG,"sendEvent :" + eventName + " params:" + ll );
                JSCallback jsCallback = eventCallback.get(eventName);
                if (jsCallback != null) {
                    jsCallback.invoke(params);
                    Log.e(TAG,"sendEvent :" + eventName + " success");
                    return;
                }
                Log.e(TAG,"sendEvent :" + eventName + " failed");
            }
        } catch (Throwable throwable) {
            Log.e(TAG,"sendEvent error:" + throwable.getMessage());
        }
    }

    public static void invokeAndKeepAlive(String eventName, JSONObject params) {
        try {
            if (!TextUtils.isEmpty(eventName) && params != null) {
//                Log.d(TAG,"sendEvent :" + eventName + " params:" + ll );
                JSCallback jsCallback = eventCallback.get(eventName);
                if (jsCallback != null) {
                    jsCallback.invokeAndKeepAlive(params);
                    Log.e(TAG,"sendEvent :" + eventName + " success");
                    return;
                }
                Log.e(TAG,"sendEvent :" + eventName + " failed");
            }
        } catch (Throwable throwable) {
            Log.e(TAG,"sendEvent error:" + throwable.getMessage());
        }
    }
}
