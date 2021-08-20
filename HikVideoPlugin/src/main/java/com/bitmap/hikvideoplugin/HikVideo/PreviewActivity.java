package com.bitmap.hikvideoplugin.HikVideo;


import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.DisplayCutout;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bitmap.hikvideoplugin.HikVideo.widget.AutoHideView;
import com.bitmap.hikvideoplugin.HikVideo.widget.PlayWindowContainer;
import com.bitmap.hikvideoplugin.R;
import com.bitmap.hikvideoplugin.http.HttpTools;
import com.bitmap.hikvideoplugin.http.RtspBean;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.Utils;
import com.hikvision.open.hikvideoplayer.HikVideoPlayer;
import com.hikvision.open.hikvideoplayer.HikVideoPlayerCallback;
import com.hikvision.open.hikvideoplayer.HikVideoPlayerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.List;

import static android.os.Environment.DIRECTORY_MOVIES;
import static android.os.Environment.DIRECTORY_PICTURES;

/**
 * 错误码开头：17是mgc或媒体取流SDK的错误，18是vod，19是dac
 */

/**
 * 错误码开头：17是mgc或媒体取流SDK的错误，18是vod，19是dac
 */
public class PreviewActivity extends Activity implements View.OnClickListener, HikVideoPlayerCallback, TextureView.SurfaceTextureListener {
    private static final String TAG = "PreviewActivity";
    private static final Integer ResultCode = 10001;
    private static String previewUri = "";
    private static String cameraCode = "";
    private static final String cameraSpeed = "50";
    private static final String cameraControlUrl = "";
    private static String getURLs = "";
    private static Integer ReRequestCount = 0;   //3次重连机制


    private static Boolean isFirst = true;

    /**
     * 录像操作视频信息暂存
     */
    File videoFile = new File("");
    String videoFileName = "";
    String videoPath = "";

    /**
     * 播放区域
     */
    protected PlayWindowContainer frameLayout;
    protected TextureView textureView;
    protected ProgressBar progressBar;
    protected TextView playHintText;
    protected TextView digitalScaleText;
    protected AutoHideView autoHideView;
    /**
     * 控制按钮
     */
    protected LinearLayout up, left, right, down, close, center;
    protected LinearLayout videoPlus, videoMinus;
    protected LinearLayout camera, videoCamera;


    private String mUri;
    private HikVideoPlayer mPlayer;
    private boolean mSoundOpen = false;
    private boolean mRecording = false;
    private boolean mDigitalZooming = true;
    private PlayerStatus mPlayerStatus = PlayerStatus.IDLE;//默认闲置

    /**
     * 电子放大倍数格式化,显示小数点后一位
     */
    private DecimalFormat decimalFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        HikVideoPlayerFactory.initLib(null, true);
        Utils.init(this);
        ToastUtils.setBgColor(Color.parseColor("#99000000"));
        ToastUtils.setMsgColor(Color.parseColor("#FFFFFFFF"));
        ToastUtils.setGravity(Gravity.BOTTOM, 0, 50);

        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);//防止键盘弹出
        super.setContentView(R.layout.activity_preview);

        //关闭刘海屏显示
        if (Build.VERSION.SDK_INT >= 28) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER ;   //应用申明不使用刘海区显示
            getWindow().setAttributes(lp);
        }
        try {
            Intent intent = getIntent();
            previewUri = intent.getStringExtra("previewUri");
            cameraCode = intent.getStringExtra("cameraCode");
        } catch (Exception e) {
            Log.e("获取出错", e + "");
        }
        initView();
        initBtnView();
        initPlayWindowContainer();
        hideSystemUI();
        mPlayer = HikVideoPlayerFactory.provideHikVideoPlayer();
        //设置默认值
        mPlayer.setHardDecodePlay(true);  // 默认为软件解码，硬件解码时，智能信息会不显示
        mPlayer.setSmartDetect(false);  //默认关闭智能信息展示，智能信息包括智能分析、移动侦测、热成像信息、温度信息等

    }

    @SuppressLint("ClickableViewAccessibility")
    private void initView() {
        textureView = findViewById(R.id.texture_view);
        progressBar = findViewById(R.id.progress_bar);
        playHintText = findViewById(R.id.result_hint_text);
        digitalScaleText = findViewById(R.id.digital_scale_text);
        autoHideView = findViewById(R.id.auto_hide_view);
        textureView.setSurfaceTextureListener(this);

    }

    @SuppressLint("ClickableViewAccessibility")
    private void initBtnView() {
        camera = findViewById(R.id.camera);
        center = findViewById(R.id.center);
        videoCamera = findViewById(R.id.videoCamera);
        close = findViewById(R.id.close);
        up = findViewById(R.id.arrow_up);
        left = findViewById(R.id.arrow_left);
        right = findViewById(R.id.arrow_right);
        down = findViewById(R.id.arrow_down);
        videoPlus = findViewById(R.id.video_plus);
        videoMinus = findViewById(R.id.video_minus);

        videoPlus.setOnClickListener(this);
        videoMinus.setOnClickListener(this);
        //调试刷新按钮
        center.setOnClickListener(this);
        down.setOnClickListener(this);
        right.setOnClickListener(this);
        left.setOnClickListener(this);
        up.setOnClickListener(this);
        camera.setOnClickListener(this);
        videoCamera.setOnClickListener(this);
        close.setOnClickListener(this);


        //放大
        videoPlus.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    // 按下 处理相关逻辑
                    PressControlling(0, "ZOOM_IN");

                } else if (action == MotionEvent.ACTION_UP) {
                    // 松开
                    releaseControlling(1, "ZOOM_IN");
                    releaseControlling(1, "ZOOM_IN");

                }
                return false;
            }
        });

        //缩小
        videoMinus.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    // 按下 处理相关逻辑
                    PressControlling(0, "ZOOM_OUT");
                } else if (action == MotionEvent.ACTION_UP) {
                    // 松开
                    releaseControlling(1, "ZOOM_OUT");
                    releaseControlling(1, "ZOOM_OUT");
                }
                return false;
            }
        });


        up.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    // 按下 处理相关逻辑
                    PressControlling(0, "UP");
                } else if (action == MotionEvent.ACTION_UP) {
                    // 松开
                    releaseControlling(1, "UP");
                    releaseControlling(1, "UP");
                }
                return false;
            }
        });

        left.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    // 按下 处理相关逻辑
                    PressControlling(0, "LEFT");
                } else if (action == MotionEvent.ACTION_UP) {
                    // 松开
                    releaseControlling(1, "LEFT");
                    releaseControlling(1, "LEFT");
                }
                return false;
            }
        });

        right.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    // 按下 处理相关逻辑
                    PressControlling(0, "RIGHT");
                } else if (action == MotionEvent.ACTION_UP) {
                    // 松开
                    releaseControlling(1, "RIGHT");
                    releaseControlling(1, "RIGHT");
                }
                return false;
            }
        });
        down.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    // 按下 处理相关逻辑
                    PressControlling(0, "DOWN");
                } else if (action == MotionEvent.ACTION_UP) {
                    // 松开
                    releaseControlling(1, "DOWN");
                    releaseControlling(1, "DOWN");
                }
                return false;
            }
        });
    }

    /**
     * PressControlling
     * <p>
     * 按住控制云台
     */
    private void PressControlling(Integer action, String command) {
        Log.e("按住控制云台", cameraControlUrl + "action=" + action + "&code=" + cameraCode + "&command=" + command + "&speed=" + cameraSpeed);
        HttpTools.okHttpGet(cameraControlUrl + "action=" + action + "&code=" + cameraCode + "&command=" + command + "&speed=" + cameraSpeed);
    }

    /**
     * PressControlling
     * <p>
     * 释放控制云台
     */
    private void releaseControlling(Integer action, String command) {
        Log.e("释放控制云台", cameraControlUrl + "action=" + action + "&code=" + cameraCode + "&command=" + command + "&speed=" + cameraSpeed);
        HttpTools.okHttpGet(cameraControlUrl + "action=" + action + "&code=" + cameraCode + "&command=" + command + "&speed=" + cameraSpeed);
    }


    private void initPlayWindowContainer() {
        frameLayout = findViewById(R.id.frame_layout);
        frameLayout.setOnClickListener(new PlayWindowContainer.OnClickListener() {
            @Override
            public void onSingleClick() {
                //弹出下方工具栏
//                if (autoHideView.isVisible()) {
//                    autoHideView.hide();
//                } else {
//                    autoHideView.show();
//                }
            }
        });
        frameLayout.setOnDigitalListener(new PlayWindowContainer.OnDigitalZoomListener() {
            @Override
            public void onDigitalZoomOpen() {
//                executeDigitalZoom();
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.close) {
            closePlugin();
            PreviewActivity.this.finish();
        } else if (view.getId() == R.id.camera) {
            executeCaptureEvent();
        } else if (view.getId() == R.id.videoCamera) {
            executeRecordEvent();
        } else if (view.getId() == R.id.center) {
            getNotchParams();
//            GetPreviewURLs();
//            center.startAnimation(getRotateAnimationByCenter(2000, null));

            Log.e(TAG, "中间按钮被点击");
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        layoutViews();
    }

    /**
     * 屏幕方向变化后重新布局View
     */
    private void layoutViews() {
        ViewGroup.LayoutParams layoutParams = frameLayout.getLayoutParams();
        if (ScreenUtils.isPortrait()) {
            //先显示系统状态栏
            showSystemUI();
            //再显示控制按钮区域
            layoutParams.height = SizeUtils.dp2px(250f);
            showOrHideControlArea(true);
        } else if (ScreenUtils.isLandscape()) {
            //隐藏系统UI
            hideSystemUI();
            showOrHideControlArea(false);
            layoutParams.height = ScreenUtils.getScreenHeight();
        }
    }

    /**
     * 显示或隐藏控制区域
     *
     * @param isShow true-显示，false-隐藏
     */
    private void showOrHideControlArea(boolean isShow) {
//        int visibility = isShow ? View.VISIBLE : View.GONE;
//        captureButton.setVisibility(visibility);
//        recordButton.setVisibility(visibility);
//        soundButton.setVisibility(visibility);
//        mRecordFilePathText.setVisibility(visibility);
    }

    /**
     * 隐藏系统ui
     */
    private void hideSystemUI() {
        //隐藏ActionBar 如果使用了NoActionBar的Theme则不需要隐藏actionBar
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        //TODO：View.setSystemUiVisibility(int visibility)中，visibility是Mode与Layout任意取值的组合，可传入的实参为：

        // Mode属性
        //View.SYSTEM_UI_FLAG_LOW_PROFILE：状态栏显示处于低能显示状态(low profile模式)，状态栏上一些图标显示会被隐藏。
        //View.SYSTEM_UI_FLAG_FULLSCREEN：Activity全屏显示，且状态栏被隐藏覆盖掉。等同于（WindowManager.LayoutParams.FLAG_FULLSCREEN）
        //View.SYSTEM_UI_FLAG_HIDE_NAVIGATION：隐藏虚拟按键(导航栏)。有些手机会用虚拟按键来代替物理按键。
        //View.SYSTEM_UI_FLAG_IMMERSIVE：这个flag只有当设置了SYSTEM_UI_FLAG_HIDE_NAVIGATION才起作用。
        // 如果没有设置这个flag，任意的View相互动作都退出SYSTEM_UI_FLAG_HIDE_NAVIGATION模式。如果设置就不会退出。
        //View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY：这个flag只有当设置了SYSTEM_UI_FLAG_FULLSCREEN | SYSTEM_UI_FLAG_HIDE_NAVIGATION 时才起作用。
        // 如果没有设置这个flag，任意的View相互动作都坏退出SYSTEM_UI_FLAG_FULLSCREEN | SYSTEM_UI_FLAG_HIDE_NAVIGATION模式。如果设置就不受影响。

        // Layout属性
        //View.SYSTEM_UI_FLAG_LAYOUT_STABLE： 保持View Layout不变，隐藏状态栏或者导航栏后，View不会拉伸。
        //View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN：让View全屏显示，Layout会被拉伸到StatusBar下面，不包含NavigationBar。
        //View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION：让View全屏显示，Layout会被拉伸到StatusBar和NavigationBar下面。
        View decorView = getWindow().getDecorView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LOW_PROFILE
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                            | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }
        //解决在华为手机上横屏时，状态栏不消失的问题
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); //隐藏状态栏

    }

    /**
     * 显示系统UI
     */
    private void showSystemUI() {
        //显示ActionBar 如果使用了NoActionBar的Theme则不需要显示actionBar
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.show();
        }
        View decorView = getWindow().getDecorView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            // This snippet shows the system bars. It does this by removing all the flags
            // except for the ones that make the content appear under the system bars.
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }
        //解决在华为手机上横屏时，状态栏不消失的问题
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); //显示状态栏
    }


    /**
     * 执行抓图事件
     */
    private void executeCaptureEvent() {
        if (mPlayerStatus != PlayerStatus.SUCCESS) {
            ToastUtils.showShort("没有视频在播放");
            return;
        }
        //抓图
//        String picturePath = MyUtils.getCaptureImagePath(this);

        String fileName = MyUtils.getFileName("") + ".jpg";
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator +"bitmapYZ/fisheryPictures/"+ fileName;
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),"bitmapYZ/fisheryPictures");
        if(!file.exists()){    //如果路径不存在就创建
            file.mkdirs();    //mkdir();只能建一层目录。  mkdirs()多层
        }


//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//             fileName = MyUtils.getFileName("") + ".jpg";
//             path = Environment.getExternalStorageDirectory() + File.separator +"bitmapYZ/Pictures/"+ fileName;
//             file = new File(Environment.getExternalStorageDirectory(),"bitmapYZ/Pictures");
//            if(!file.exists()){    //如果路径不存在就创建
//                file.mkdirs();    //mkdir();只能建一层目录。  mkdirs()多层
//            }
//        }else {  //直接放在 getExternalFilesDir()提供的目录中（Android包名下的文件夹），媒体扫描器(MediaScannerConnection.scanFile)将无法访问相应的文件，因为这些文件对您的应用保持私密状态。
//             file = this.getExternalFilesDir("fishery" + DIRECTORY_PICTURES);
//             fileName = MyUtils.getFileName("") + ".jpg";
//             path = file.getAbsolutePath() + File.separator + fileName;
//        }


        Log.e(TAG, "getCaptureImagePath: " + path);
        if (mPlayer.capturePicture(path)) {
            NotifyAlbumUpdate(file, fileName, path, "image/*");
            ToastUtils.showShort("抓图成功");
        }
    }


    /**
     * 执行录像事件
     */
    private void executeRecordEvent() {
        if (mPlayerStatus != PlayerStatus.SUCCESS) {
            ToastUtils.showShort("没有视频在播放");
            return;
        }
        if (!mRecording) {
            //开始录像
            videoCamera.setSelected(true);

            videoFileName = MyUtils.getFileName("") + ".mp4";
            videoPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator +"bitmapYZ/fisheryVideos/"+videoFileName;
            videoFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),"bitmapYZ/fisheryVideos");
            if(!videoFile.exists()){    //如果路径不存在就创建
                videoFile.mkdirs();    //mkdir();只能建一层目录。  mkdirs()多层
            }


//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                videoFileName = MyUtils.getFileName("") + ".mp4";
//                videoPath = Environment.getExternalStorageDirectory() + File.separator +"bitmapYZ/Videos/"+videoFileName;
//                videoFile = new File(Environment.getExternalStorageDirectory(),"bitmapYZ/Videos");
//                if(!videoFile.exists()){    //如果路径不存在就创建
//                    videoFile.mkdirs();    //mkdir();只能建一层目录。  mkdirs()多层
//                }
//            }else {
//                videoFile = this.getExternalFilesDir("fishery" + DIRECTORY_MOVIES);
//                videoFileName = MyUtils.getFileName("") + ".mp4";
//                videoPath = videoFile.getAbsolutePath() + File.separator + videoFileName;
//            }


//            videoFile = this.getExternalFilesDir("fishery" + DIRECTORY_MOVIES);
//            videoFileName = MyUtils.getFileName("") + ".mp4";
//            videoPath = videoFile.getAbsolutePath() + File.separator + videoFileName;

            if (mPlayer.startRecord(videoPath)) {
                ToastUtils.showShort("开始录像");
                mRecording = true;
            }
        } else {
            //关闭录像
            videoCamera.setSelected(false);
            mPlayer.stopRecord();
            ToastUtils.showShort("录像保存成功");
//            ToastUtils.showShort(MessageFormat.format("当前录像路径: {0}", videoPath));
            NotifyAlbumUpdate(videoFile, videoFileName, videoPath, "video/*");
            mRecording = false;
        }
    }


    /**
     * 通知相册更新 API通用方法包括API29及以上高版本方法
     * 本项目编译api是27 则无需考虑29的问题
     * 如果要升级 下面注释的代码就能兼容API29
     */
    private void NotifyAlbumUpdate(File file, String fileName, String path, String mediaType) {

        MediaScannerConnection.scanFile(this, new String[]{path}, new String[]{mediaType}, (realPath, uri) -> Log.e("资源刷新成功路径为", realPath+"---"+uri.toString()));

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            MediaScannerConnection.scanFile(this, new String[]{path}, new String[]{mediaType}, (realPath, uri) -> Log.e("资源刷新成功路径为", realPath+"---"+uri.toString()));
//        } else {
//            //API29以下的老方法，在API29中已弃用
//            if (mediaType.contains("image")) {
//                File file1 = new File(path);
//                //将图片扫描到相册中显示
//                ContentResolver localContentResolver = this.getContentResolver();
//                ContentValues localContentValues = getImageContentValues(file1, System.currentTimeMillis());
//                localContentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, localContentValues);
//                Intent localIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
//                final Uri localUri = Uri.fromFile(file1);
//                localIntent.setData(localUri);
//                this.sendBroadcast(localIntent);
//            } else {
//                //刷新视频到相册方法二 API29以下的老方法，在API29中已弃用！
//                ContentResolver localContentResolver = this.getContentResolver();
//                ContentValues localContentValues = getVideoContentValues(new File(path), System.currentTimeMillis());
//                Uri localUri = localContentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, localContentValues);
//                this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, localUri));
//            }
//        }
    }


    public static ContentValues getImageContentValues(File paramFile, long paramLong) {
        ContentValues localContentValues = new ContentValues();
        localContentValues.put("title", paramFile.getName());
        localContentValues.put("_display_name", paramFile.getName());
        localContentValues.put("mime_type", "image/jpg");
        localContentValues.put("datetaken", Long.valueOf(paramLong));
        localContentValues.put("date_modified", Long.valueOf(paramLong));
        localContentValues.put("date_added", Long.valueOf(paramLong));
        localContentValues.put("orientation", Integer.valueOf(0));
        localContentValues.put("_data", paramFile.getAbsolutePath());
        localContentValues.put("_size", Long.valueOf(paramFile.length()));
        return localContentValues;
    }


    public static ContentValues getVideoContentValues(File paramFile, long paramLong) {
        ContentValues localContentValues = new ContentValues();
        localContentValues.put(MediaStore.Video.Media.TITLE, paramFile.getName());
        localContentValues.put(MediaStore.Video.Media.DISPLAY_NAME, paramFile.getName());
        localContentValues.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        localContentValues.put(MediaStore.Video.Media.DATE_TAKEN, Long.valueOf(paramLong));
        localContentValues.put(MediaStore.Video.Media.DATE_MODIFIED, Long.valueOf(paramLong));
        localContentValues.put(MediaStore.Video.Media.DATE_ADDED, Long.valueOf(paramLong));
        localContentValues.put(MediaStore.Video.Media.DATA, paramFile.getAbsolutePath());
        localContentValues.put(MediaStore.Video.Media.SIZE, Long.valueOf(paramFile.length()));
        return localContentValues;
    }

    /**
     * 执行声音开关事件
     */
    private void executeSoundEvent() {
        if (mPlayerStatus != PlayerStatus.SUCCESS) {
            ToastUtils.showShort("没有视频在播放");
        }

        if (!mSoundOpen) {
            //打开声音
            if (mPlayer.enableSound(true)) {
                ToastUtils.showShort("声音开");
                mSoundOpen = true;
            }
        } else {
            //关闭声音
            if (mPlayer.enableSound(false)) {
                ToastUtils.showShort("声音关");
                mSoundOpen = false;
            }
        }
    }

    /**
     * 执行电子放大操作
     */
    private void executeDigitalZoom() {
        if (mPlayerStatus != PlayerStatus.SUCCESS) {
            ToastUtils.showShort("没有视频在播放");
        }
        if (decimalFormat == null) {
            decimalFormat = new DecimalFormat("0.0");
        }
//        if (!mDigitalZooming){
//            frameLayout.setOnScaleChangeListener(new PlayWindowContainer.OnDigitalScaleChangeListener() {
//                @Override
//                public void onDigitalScaleChange(float scale) {
//                    Log.i(TAG,"onDigitalScaleChange scale = "+scale);
//                    if (scale < 1.0f && mDigitalZooming){
//                        //如果已经开启了电子放大且倍率小于1就关闭电子放大
//                        executeDigitalZoom();
//                    }
//                    if (scale>= 1.0f){
//                        digitalScaleText.setText(MessageFormat.format("{0}X",decimalFormat.format(scale)));
//                    }
//                }
//
//                @Override
//                public void onDigitalRectChange(CustomRect oRect, CustomRect curRect) {
//                    mPlayer.openDigitalZoom(oRect, curRect);
//                }
//            });
//            ToastUtils.showShort("电子放大开启");
//            mDigitalZooming = true;
//            digitalScaleText.setVisibility(View.VISIBLE);
//            digitalScaleText.setText(MessageFormat.format("{0}X",decimalFormat.format(1.0f)));
//        }else {
//            ToastUtils.showShort("电子放大关闭");
//            mDigitalZooming = false;
//            digitalScaleText.setVisibility(View.GONE);
//            frameLayout.setOnScaleChangeListener(null);
//            mPlayer.closeDigitalZoom();
//        }
    }

    /**
     * 重置所有的操作状态
     */
    private void resetExecuteState() {
//        if (mDigitalZooming){
//            executeDigitalZoom();
//        }
        if (mSoundOpen) {
            executeSoundEvent();
        }
        if (mRecording) {
            executeRecordEvent();
        }
        frameLayout.setAllowOpenDigitalZoom(false);
    }


    @Override
    protected void onResume() {
        super.onResume();
        //TODO 注意:APP前后台切换时 SurfaceTextureListener可能在有某些 华为手机 上不会回调，例如：华为P20，所以我们在这里手动调用
        if (textureView.isAvailable()) {
            Log.e(TAG, "onResume: onSurfaceTextureAvailable");
            onSurfaceTextureAvailable(textureView.getSurfaceTexture(), textureView.getWidth(), textureView.getHeight());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //TODO 注意:APP前后台切换时 SurfaceTextureListener可能在有某些 华为手机 上不会回调，例如：华为P20，所以我们在这里手动调用
        if (textureView.isAvailable()) {
            Log.e(TAG, "onPause: onSurfaceTextureDestroyed");
            onSurfaceTextureDestroyed(textureView.getSurfaceTexture());
        }
    }


    /**
     * 开始播放
     *
     * @param surface 渲染画面
     */
    private void startRealPlay(SurfaceTexture surface) {
        mPlayerStatus = PlayerStatus.LOADING;
        progressBar.setVisibility(View.VISIBLE);
        playHintText.setVisibility(View.GONE);
        mPlayer.setSurfaceTexture(surface);
        //TODO 注意: startRealPlay() 方法会阻塞当前线程，需要在子线程中执行,建议使用RxJava
        new Thread(() -> {
            //TODO 注意: 不要通过判断 startRealPlay() 方法返回 true 来确定播放成功，播放成功会通过HikVideoPlayerCallback回调，startRealPlay() 方法返回 false 即代表 播放失败;
            if (!mPlayer.startRealPlay(mUri, PreviewActivity.this)) {
                onPlayerStatus(Status.FAILED, mPlayer.getLastError());
            }
        }).start();
    }


    /**
     * 播放结果回调
     *
     * @param status    共四种状态：SUCCESS（播放成功）、FAILED（播放失败）、EXCEPTION（取流异常）、FINISH（回放结束）
     * @param errorCode 错误码，只有 FAILED 和 EXCEPTION 才有值
     */
    @Override
    @WorkerThread
    public void onPlayerStatus(@NonNull HikVideoPlayerCallback.Status status, int errorCode) {
        //TODO 注意: 由于 HikVideoPlayerCallback 是在子线程中进行回调的，所以一定要切换到主线程处理UI
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.GONE);
                //只有播放成功时，才允许开启电子放大
                frameLayout.setAllowOpenDigitalZoom(status == Status.SUCCESS);
                switch (status) {
                    case SUCCESS:
                        //播放成功
                        mPlayerStatus = PlayerStatus.SUCCESS;
                        playHintText.setVisibility(View.GONE);
                        textureView.setKeepScreenOn(true);//保持亮屏
                        break;
                    case FAILED:
                        //播放失败
                        if (ReRequestCount < 3){
                            ReRequestCount++;
                            GetPreviewURLs();
                        }else {
                            mPlayerStatus = PlayerStatus.FAILED;
                            playHintText.setVisibility(View.VISIBLE);
                            playHintText.setText(MessageFormat.format("预览失败，错误码：{0}，该相机暂无法预览请退出", Integer.toHexString(errorCode)));
                        }
                        break;
                    case EXCEPTION:
                        //取流异常
                        mPlayerStatus = PlayerStatus.EXCEPTION;
                        mPlayer.stopPlay();//TODO 注意:异常时关闭取流
                        playHintText.setVisibility(View.VISIBLE);
                        playHintText.setText(MessageFormat.format("取流发生异常，错误码：{0}", Integer.toHexString(errorCode)));
                        break;
                }
            }
        });
    }

    /**
     * 重新获取RTSP-url地址
     */
    private void GetPreviewURLs() {
        HttpTools.okHttpGet(getURLs + "&code=" + cameraCode, new HttpTools.HttpBackListener() {
            @Override
            public void onSuccess(String data, int code) {
                try {
                    RtspBean rtspBean = JSONArray.parseObject(data, RtspBean.class);
                    if (rtspBean.getCode().equals("20000") && rtspBean.getMessage().equals("成功")) {
                        previewUri = rtspBean.getData().getUrl();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //如果有更新UI的操作，需要自己写runOnUiThread这一类的方法去执行
                                if (getPreviewUri(previewUri)) {
                                    //重新播放
                                    startRealPlay(textureView.getSurfaceTexture());
                                    Log.d(TAG, "GetPreviewURLs: startRealPlay");
                                }
                            }
                        });

                    } else {
                        ToastUtils.showShort(rtspBean.getMessage());
                    }
                } catch (Exception e) {
                    ToastUtils.showShort(e + "");
                }
            }

            @Override
            public void onError(String error, int code) {
                ToastUtils.showShort(error);
            }
        });
    }

    private boolean getPreviewUri(String uri) {
        mUri = uri;
        if (TextUtils.isEmpty(mUri)) {
            ToastUtils.showShort("URI不能为空");
            return false;
        }

        if (!mUri.contains("rtsp")) {
            ToastUtils.showShort("非法URI");
            return false;
        }

        return true;
    }


    /*************************TextureView.SurfaceTextureListener 接口的回调方法********************/
    //TODO 注意:APP前后台切换时 SurfaceTextureListener可能在有某些华为手机上不会回调，例如：华为P20，因此我们需要在Activity生命周期中手动调用回调方法
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (mPlayerStatus == PlayerStatus.STOPPING) {
            //恢复处于暂停播放状态的窗口
            startRealPlay(textureView.getSurfaceTexture());
            Log.d(TAG, "onSurfaceTextureAvailable: startRealPlay");
        } else if (isFirst && mPlayerStatus == PlayerStatus.IDLE) {
            isFirst = false;
            //初次进入，直接播放
            if (getPreviewUri(previewUri) && textureView.isAvailable()) {
                startRealPlay(textureView.getSurfaceTexture());
            }
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (mPlayerStatus == PlayerStatus.SUCCESS) {
            mPlayerStatus = PlayerStatus.STOPPING;//暂停播放，再次进入时恢复播放
            mPlayer.stopPlay();
            Log.d(TAG, "onSurfaceTextureDestroyed: stopPlay");
        }
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    /**
     * 获取一个根据视图自身中心点旋转的动画
     *
     * @param durationMillis    动画持续时间
     * @param animationListener 动画监听器
     * @return 一个根据中心点旋转的动画
     */
    public static RotateAnimation getRotateAnimationByCenter(long durationMillis, Animation.AnimationListener animationListener) {
        return getRotateAnimation(0f, 359f, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f, durationMillis,
                animationListener);
    }

    /**
     * 获取一个旋转动画
     *
     * @param fromDegrees       开始角度
     * @param toDegrees         结束角度
     * @param pivotXType        旋转中心点X轴坐标相对类型
     * @param pivotXValue       旋转中心点X轴坐标
     * @param pivotYType        旋转中心点Y轴坐标相对类型
     * @param pivotYValue       旋转中心点Y轴坐标
     * @param durationMillis    持续时间
     * @param animationListener 动画监听器
     * @return 一个旋转动画
     */
    public static RotateAnimation getRotateAnimation(float fromDegrees, float toDegrees, int pivotXType, float pivotXValue, int pivotYType, float pivotYValue, long durationMillis, Animation.AnimationListener animationListener) {
        RotateAnimation rotateAnimation = new RotateAnimation(fromDegrees,
                toDegrees, pivotXType, pivotXValue, pivotYType, pivotYValue);
        rotateAnimation.setDuration(durationMillis);
        if (animationListener != null) {
            rotateAnimation.setAnimationListener(animationListener);
        }
        return rotateAnimation;
    }

    @Override
    public void onBackPressed() {
        closePlugin();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        closePlugin();
        super.onDestroy();

    }

    public void closePlugin(){
        if (mPlayerStatus == PlayerStatus.SUCCESS) {
            //是否录像状态 是就关闭录像
            if (mRecording) {
                mPlayer.stopRecord();
            }
            mPlayerStatus = PlayerStatus.IDLE;//释放这个窗口
            progressBar.setVisibility(View.GONE);
            playHintText.setVisibility(View.VISIBLE);
            playHintText.setText("");
            resetExecuteState();
            mPlayer.stopPlay();
        }
        isFirst = true;
        ReRequestCount = 0;
        setResult(ResultCode);
    }

    /**
     * 获取状态栏的高度
     */
    public static int getStatusBarHeight(Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return 0;
        }
        Resources resources = activity.getResources();
        int identifier = resources.getIdentifier("status_bar_height", "dimen", "android");
        return resources.getDimensionPixelOffset(identifier);
    }
    /**
     * 获得刘海区域信息
     */
    @TargetApi(28)
    public void getNotchParams() {
        final View decorView = getWindow().getDecorView();
        if (decorView != null) {

            WindowInsets windowInsets = decorView.getRootWindowInsets();
            if (windowInsets != null) {
                // 当全屏顶部显示黑边时，getDisplayCutout()返回为null
                DisplayCutout displayCutout = windowInsets.getDisplayCutout();
                Log.e("TAG", "安全区域距离屏幕左边的距离 SafeInsetLeft:" + displayCutout.getSafeInsetLeft());
                Log.e("TAG", "安全区域距离屏幕右部的距离 SafeInsetRight:" + displayCutout.getSafeInsetRight());
                Log.e("TAG", "安全区域距离屏幕顶部的距离 SafeInsetTop:" + displayCutout.getSafeInsetTop());
                Log.e("TAG", "安全区域距离屏幕底部的距离 SafeInsetBottom:" + displayCutout.getSafeInsetBottom());
                // 获得刘海区域
                List<Rect> rects = displayCutout.getBoundingRects();
                if (rects == null || rects.size() == 0) {
                    Log.e("TAG", "不是刘海屏");
                } else {
                    Log.e("TAG", "刘海屏数量:" + rects.size());
                    for (Rect rect : rects) {
                        Log.e("TAG", "刘海屏区域：" + rect);
                    }
                }
            }
        }
    }




}
