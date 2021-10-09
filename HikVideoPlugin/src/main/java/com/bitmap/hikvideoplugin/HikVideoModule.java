package com.bitmap.hikvideoplugin;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Parcelable;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.alibaba.fastjson.JSONObject;
import com.bitmap.hikvideoplugin.HikVideo.PreviewActivity;
import com.bitmap.hikvideoplugin.common.HKConstants;
import com.bitmap.hikvideoplugin.helper.CallBackHelper;
import com.taobao.weex.annotation.JSMethod;
import com.taobao.weex.bridge.JSCallback;
import com.taobao.weex.common.WXModule;
import io.dcloud.feature.uniapp.annotation.UniJSMethod;

/**
 * 作者：bitmap
 * 只限购买者使用，未经授权未经授权私自传播作者有权追究其责任
 */
public class HikVideoModule extends WXModule {

    private static final String TAG = "HikVideoModule";

    Boolean showRecordBtn = false;
    String NAME="name";
    String AGE ="age";
    public static int REQUEST_CODE = 1000;

//    JSCallback jsCallback;
    private static String[] PERMISSIONS_STORAGE = {"android.permission.WRITE_EXTERNAL_STORAGE","android.permission.RECORD_AUDIO" };

    @JSMethod(uiThread = true)
    public void testText(JSONObject options, JSCallback callBack){
        Log.e("HikVideoModule", "成功调用!" );
        String name =options.getString(NAME);
        String age =options.getString(AGE);
        JSONObject data =new JSONObject();
        if (name !=null && !name.isEmpty() && age !=null && !age.isEmpty()){
            int _age =Integer.parseInt(age);
            if (_age<0 || _age>30){
                data.put("code","不合格!");
            }else {
                age=(_age>0 && _age<10) ? "0"+age:age;
                data.put("code","合格:"+"姓名_"+name+",年龄_"+age);
            }
        }else {
            data.put("code","输入无效!");
        }
        callBack.invoke(data);

    }

    @UniJSMethod(uiThread = true)
    public void gotoNativePage(){
        if(mUniSDKInstance != null) {
            Intent intent = new Intent(mUniSDKInstance.getContext(), TestActivity.class);
            mUniSDKInstance.getContext().startActivity(intent);
        }
    }

    @UniJSMethod (uiThread = true)
    public void gotoPreviewActivity(JSONObject options){

        if (checkPermissionsByArray(PERMISSIONS_STORAGE)){
            String previewUri = options.getString("previewUri");
            String cameraCode = options.getString("cameraCode");
            String canControl = options.getString("canControl");
//            jsCallback = callBack;
            if(mUniSDKInstance != null && mUniSDKInstance.getContext() instanceof Activity) {
                //初始化
                myApp.init(((Activity)mUniSDKInstance.getContext()).getApplication(),true);
                //跳转
                Intent intent = new Intent(mUniSDKInstance.getContext(), PreviewActivity.class);
                intent.putExtra("previewUri",previewUri);
                intent.putExtra("cameraCode",cameraCode);
                intent.putExtra("canControl",canControl);
                intent.putExtra("showRecordBtn",showRecordBtn);
                ((Activity)mUniSDKInstance.getContext()).startActivityForResult(intent,REQUEST_CODE);
            }
        }else {
            requestPermissionsByArray(PERMISSIONS_STORAGE);
        }
    }


    @UniJSMethod (uiThread = true)
    public void gotoPreviewActivity(JSONObject options, JSCallback callBack){
        if (checkPermissionsByArray(PERMISSIONS_STORAGE)){
            String previewUri = options.getString("previewUri");
            String cameraCode = options.getString("cameraCode");
            String canControl = options.getString("canControl");
//            jsCallback = callBack;
            if(mUniSDKInstance != null && mUniSDKInstance.getContext() instanceof Activity) {
                //初始化
                myApp.init(((Activity)mUniSDKInstance.getContext()).getApplication(),true);
                //跳转
                Intent intent = new Intent(mUniSDKInstance.getContext(), PreviewActivity.class);
                intent.putExtra("previewUri",previewUri);
                intent.putExtra("cameraCode",cameraCode);
                intent.putExtra("canControl",canControl);
                intent.putExtra("showRecordBtn",showRecordBtn);
                ((Activity)mUniSDKInstance.getContext()).startActivityForResult(intent,REQUEST_CODE);
            }
        }else {
            requestPermissionsByArray(PERMISSIONS_STORAGE);
        }

    }

    /**
     * 添加录音监听回调事件
     * 添加这个监听之后才有录音图标
     * @param callback
     */
    @UniJSMethod(uiThread = true)
    public void addRecordEventListener(JSCallback callback) {
        if (callback != null) {
            Log.w(TAG,"addRecordEventListener");
            showRecordBtn = true;
            CallBackHelper.eventCallback.put(HKConstants.RECORD_VOICE, callback);
        }
    }


    /**
     * 插件销毁监听回调
     * @param callback
     */
    @UniJSMethod(uiThread = true)
    public void addClosePluginListener(JSCallback callback) {
        if (callback != null) {
            Log.w(TAG,"addClosePluginListener");
            CallBackHelper.eventCallback.put(HKConstants.CLOSE_PLUGIN, callback);
        }
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == REQUEST_CODE && resultCode == 10001) {
//            JSONObject jsonObject =new JSONObject();
//            jsonObject.put("msg","this is callBack");
//            jsCallback.invoke(jsonObject);
//        }
//    }

    /**
     * TODO：检查手机存储读写权限
     * @param permissionArray 权限数组
     * @return 是否获取到全部权限
     */
    private boolean checkPermissionsByArray(String[] permissionArray) {
        boolean isRequest = true;
        for (String permission: permissionArray){
            if (ContextCompat.checkSelfPermission((Activity) mUniSDKInstance.getContext(), permission) != PackageManager.PERMISSION_GRANTED){
                isRequest = false;
            }
        }
        return isRequest;
    }
    /**
     * TODO：获取权限
     * @param permissionArray 权限数组
     */
    private void requestPermissionsByArray(String[] permissionArray) {
        ActivityCompat.requestPermissions((Activity) mUniSDKInstance.getContext(), permissionArray, 10);
    }

}