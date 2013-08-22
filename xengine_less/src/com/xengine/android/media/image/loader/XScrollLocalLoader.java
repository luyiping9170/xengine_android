package com.xengine.android.media.image.loader;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;
import com.xengine.android.media.image.processor.XImageProcessor;
import com.xengine.android.system.series.XBaseSerialMgr;
import com.xengine.android.utils.XLog;

/**
 * 滑动延迟加载的本地图片加载器。（用于ListView或GridView）
 * 特点：
 * 1. 只负责本地加载，不涉及下载.
 * 2. 二级缓存（内存 + sd卡的图片缓存）。
 * 3. 异步方式加载。
 * 4. 延迟加载特性：只有停止时才加载，滑动时候不加载。
 * 5. 单队列（一个异步线程）线性执行，每个时刻只有一个异步任务在执行。
 * Created with IntelliJ IDEA.
 * User: tujun
 * Date: 13-8-6
 * Time: 下午12:57
 * To change this template use File | Settings | File Templates.
 */
public abstract class XScrollLocalLoader extends XImageViewLocalLoader
        implements XScrollLazyLoading {
    private static final String TAG = XScrollLocalLoader.class.getSimpleName();

    private SerialTaskMgr mSerialMgr;// 线性执行器

    public XScrollLocalLoader() {
        super();
        mSerialMgr = new SerialTaskMgr();
    }

    @Override
    public void asyncLoadBitmap(Context context, String imageUrl,
                                ImageView imageView,
                                XImageProcessor.ImageSize size) {
        // 检测是否在缓存中已经存在此图片
        Bitmap bitmap = mImageCache.getCacheBitmap(imageUrl, size);
        if (bitmap != null && !bitmap.isRecycled()) {
            imageView.setImageBitmap(bitmap);
            showViewAnimation(context, imageView);
            return;
        }

        // 如果没有，则启动异步加载（先取消之前可能对同一张图片的加载工作）
        if (cancelPotentialWork(imageUrl, imageView)) {
            XLog.d(TAG, "XScrollLocalLoader cancelPotentialWork true. url:" + imageUrl);
            // 如果是默认不存在的图标提示（“缺省图片”，“加载中”，“加载失败”等），则不需要异步
            if (loadErrorImage(imageUrl, imageView)) // TIP 不要渐变效果
                return;

            // 如果是真正图片，则需要异步加载
            Resources resources = context.getResources();
            Bitmap mPlaceHolderBitmap = BitmapFactory.
                    decodeResource(resources, mEmptyImageResource);// 占位图片
            final LocalImageViewAsyncTask task = new ScrollLocalAsyncTask(context, imageView, imageUrl, size);
            final AsyncDrawable asyncDrawable = new AsyncDrawable(resources, mPlaceHolderBitmap, task);
            imageView.setImageDrawable(asyncDrawable);

            // 添加进队列中，等待执行
            mSerialMgr.addNewTask(task);
            mSerialMgr.tryStart();
        }
    }

    /**
     * 取消该组件上之前的异步加载任务(用于ImageView)
     * @param imageUrl
     * @param imageView
     * @return
     */
    protected static boolean cancelPotentialWork(String imageUrl, ImageView imageView) {
        final ScrollLocalAsyncTask scrollLocalAsyncTask =
                (ScrollLocalAsyncTask) getAsyncImageTask(imageView);
        if (scrollLocalAsyncTask != null) {
            final String url = scrollLocalAsyncTask.getImageUrl();
            if (scrollLocalAsyncTask.isInvalidate()) {
                return true;
            } else if (url == null || !url.equals(imageUrl)) {
                // Cancel previous task
                scrollLocalAsyncTask.cancel(true);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was cancelled
        return true;
    }

    @Override
    public void setWorking() {
        mSerialMgr.setWorking();
        mSerialMgr.tryStart();// 尝试启动
    }

    @Override
    public void onScroll() {
        XLog.d(TAG, "onScroll()");
        mSerialMgr.stop();
    }

    @Override
    public void onIdle() {
        XLog.d(TAG, "onIdle()");
        mSerialMgr.start();
    }

    @Override
    public void stopAndClear() {
        XLog.d(TAG, "stopAndClear()");
        mSerialMgr.stopAndReset();
    }

    private class SerialTaskMgr extends XBaseSerialMgr {
        @Override
        protected String getTaskId(AsyncTask task) {
            return null;// TIP 不是以url为id，每个加载task都是独立的
        }

        @Override
        public void notifyTaskFinished(AsyncTask task) {
            super.notifyTaskFinished(task);
        }

        /**
         * 启动，将标记设置为启动
         */
        public void setWorking() {
            mIsWorking = true;
        }

        /**
         * 根据当前标记，尝试启动。但不改变当前标记。
         */
        public void tryStart() {
            if (!mIsWorking)
                return;

            mNextTask = mTobeExecuted.peek();
            if (mNextTask != null &&
                    mNextTask.getStatus() == AsyncTask.Status.PENDING)
                mNextTask.execute(null);
        }

        /**
         * 覆盖stop方法，停止时并不cancel当前任务，只是设置一个标记。
         */
        @Override
        public void stop() {
            mIsWorking = false;
        }
    }

    /**
     * 线性地本地加载图片的AsyncTask
     */
    private class ScrollLocalAsyncTask extends LocalImageViewAsyncTask {
        // 标识该task是否还在队列中(针对task不再队列里但在imageView里的情况)
        private boolean isInvalidate;

        public ScrollLocalAsyncTask(Context context, ImageView imageView, String imageUrl,
                                    XImageProcessor.ImageSize size) {
            super(context, imageView, imageUrl, size);
            isInvalidate = false;
        }

        public boolean isInvalidate() {
            return isInvalidate;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            XLog.d(TAG, "notifyTaskFinished.");
            isInvalidate = true;
            mSerialMgr.notifyTaskFinished(this);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            XLog.d(TAG, "notifyTaskFinished.");
            isInvalidate = true;
            mSerialMgr.notifyTaskFinished(this);
        }
    }
}
