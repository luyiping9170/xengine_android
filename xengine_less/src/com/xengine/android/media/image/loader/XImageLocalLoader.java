package com.xengine.android.media.image.loader;

import android.content.Context;
import android.view.View;
import com.xengine.android.media.image.processor.XImageProcessor;

/**
 * 本地图片加载器的接口。
 * 只负责本地加载，不涉及下载。
 * 二级缓存（内存 + sd卡的图片缓存）。
 * 异步方式加载。
 * 同步方式加载。
 * @see XImageViewLocalLoader
 * @see XImageSwitcherLocalLoader
 * Created with IntelliJ IDEA.
 * User: tujun
 * Date: 13-8-1
 * Time: 下午7:11
 * To change this template use File | Settings | File Templates.
 */
public interface XImageLocalLoader<T extends View> extends XImageLoader{

    /**
     * 异步加载图片(对ImageView)
     * @param context
     * @param imageUrl
     * @param view
     * @param size
     */
    void asyncLoadBitmap(Context context, String imageUrl,
                         T view, XImageProcessor.ImageSize size);

    /**
     * 同步加载图片(对ImageView)
     * @param context
     * @param imageUrl
     * @param view
     * @param size
     */
    void syncLoadBitmap(Context context, String imageUrl,
                               T view, XImageProcessor.ImageSize size);
}
