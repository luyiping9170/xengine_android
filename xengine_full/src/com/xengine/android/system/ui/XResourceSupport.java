package com.xengine.android.system.ui;

import android.graphics.Bitmap;
import com.xengine.android.media.audio.XMusic;
import com.xengine.android.media.audio.XSound;
import com.xengine.android.media.graphics.XScreen;

/**
 * Created by 赵之韵.
 * Email: ttxzmorln@163.com
 * Date: 12-3-4
 * Time: 下午5:59
 */
public interface XResourceSupport {
    Bitmap getBitmap(String path);
    Bitmap getScalableBitmap(String path);
    XMusic newMusic(String path);
    XSound newSound(String path);
    XScreen screen();
}
