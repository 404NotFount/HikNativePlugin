package com.bitmap.hikvideoplugin.RecordMusic;

import java.io.File;

/**
 * Create By axd On 2021/9/28.
 * Email 43229097@qq.com
 * Describe：
 */
public interface BitRecordListener {
    String onInitPath();

    void onSuccess(File file);
}
