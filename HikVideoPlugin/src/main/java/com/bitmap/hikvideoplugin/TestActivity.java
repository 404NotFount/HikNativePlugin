package com.bitmap.hikvideoplugin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.alibaba.fastjson.JSONObject;
import com.bitmap.hikvideoplugin.HikVideo.PreviewActivity;
import com.bitmap.hikvideoplugin.common.HKConstants;
import com.bitmap.hikvideoplugin.helper.CallBackHelper;
import com.taobao.weex.bridge.JSCallback;

public class TestActivity extends AppCompatActivity {
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.RECORD_AUDIO"};
    protected EditText url;
    protected Button btn;
    private static final Integer RequestCode = 10000;
    JSCallback callback;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        myApp.init(getApplication(),true);

        url = findViewById(R.id.url);
        btn = findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                callback = new JSCallback() {
                    @Override
                    public void invoke(Object o) {
                    }
                    @Override
                    public void invokeAndKeepAlive(Object o) {
                    }
                };
                CallBackHelper.eventCallback.put(HKConstants.RECORD_VOICE, callback);
                if (checkPermissionsByArray(PERMISSIONS_STORAGE)) {
                    Intent intent = new Intent(TestActivity.this, PreviewActivity.class);
                    HKConstants.previewUri = "rtsp://";
                    HKConstants.cameraCode = "";
                    HKConstants.canControl = true;
                    HKConstants.showRecordBtn = true;
                    HKConstants.enableSound = false;
                    startActivityForResult(intent, RequestCode);
                } else {
                    requestPermissionsByArray(PERMISSIONS_STORAGE);
                }

            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("12313", requestCode + "-" + resultCode + "-");
        if (requestCode == RequestCode && resultCode == 10001) {
            Log.e("12313", "2222");
        }
    }

    /**
     * TODO?????????????????????????????????
     *
     * @param permissionArray ????????????
     * @return ???????????????????????????
     */
    private boolean checkPermissionsByArray(String[] permissionArray) {
        boolean isRequest = true;
        for (String permission : permissionArray) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                isRequest = false;
            }
        }
        return isRequest;
    }

    /**
     * TODO???????????????
     *
     * @param permissionArray ????????????
     */
    private void requestPermissionsByArray(String[] permissionArray) {

        ActivityCompat.requestPermissions(this, permissionArray, 10);

    }

}