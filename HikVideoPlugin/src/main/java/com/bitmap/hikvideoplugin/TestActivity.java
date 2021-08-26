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

import com.bitmap.hikvideoplugin.HikVideo.PreviewActivity;

public class TestActivity extends AppCompatActivity {
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"};
    protected EditText url;
    protected Button btn;
    private static final Integer RequestCode = 10000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        url = findViewById(R.id.url);
        btn = findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (checkPermissionsByArray(PERMISSIONS_STORAGE)) {
                    Intent intent = new Intent(TestActivity.this, PreviewActivity.class);
                    intent.putExtra("previewUri", url.getText().toString());
                    intent.putExtra("cameraCode", "129e41335b704ee5b0f24aa6bfba64b6");
                    intent.putExtra("canControl", "true");
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
     * TODO：检查手机存储读写权限
     *
     * @param permissionArray 权限数组
     * @return 是否获取到全部权限
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
     * TODO：获取权限
     *
     * @param permissionArray 权限数组
     */
    private void requestPermissionsByArray(String[] permissionArray) {

        ActivityCompat.requestPermissions(this, permissionArray, 10);

    }

}