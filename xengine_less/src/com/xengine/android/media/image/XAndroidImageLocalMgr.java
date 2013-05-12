package com.xengine.android.media.image;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Environment;
import android.os.StatFs;
import com.xengine.android.utils.XLog;
import com.xengine.android.utils.XStringUtil;

import java.io.*;

/**
 * Android下的本地图片存储及管理类。
 * 统一管理本程序的本地缓存图片。所以用单例模式。
 * Created by jasontujun.
 * Date: 12-10-29
 * Time: 下午9:28
 */
public class XAndroidImageLocalMgr implements XImageLocalMgr {
    private static final String TAG = "IMG";

    private static XAndroidImageLocalMgr instance;

    public static synchronized XAndroidImageLocalMgr getInstance() {
        if(instance == null) {
            instance = new XAndroidImageLocalMgr();
        }
        return instance;
    }

    private XAndroidImageLocalMgr(){}


    private int screenWidth, screenHeight;

    private File imgDir;// 临时图片缓存文件夹

    /**
     * 初始化函数
     * @param sWidth 屏幕宽度（单位：pixel）
     * @param sHeight 屏幕高度（单位：pixel）
     */
    public void init(int sWidth, int sHeight) {
        screenWidth = sWidth;
        screenHeight = sHeight;
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    /**
     * 设置本地缓存图片的文件夹
     * @param dirName 图片文件夹名称。如："/baihewan/tmp"
     */
    @Override
    public void setImgDir(String dirName) {
        this.imgDir = new File(Environment.getExternalStorageDirectory() + dirName);
        if(!imgDir.exists()) {
            this.imgDir.mkdirs();
        }else {
            clearImgDir();
        }
    }

    @Override
    public File getImgDir() {
        return imgDir;
    }

    @Override
    public File getImgFile(String imgName) {
        return new File(imgDir, imgName);
    }


    @Override
    public Bitmap getLocalImage(String imgName, ImageSize size) throws IOException {
        switch (size) {
            case ORIGIN: {
                File imgFile = getImgFile(imgName);
                if(imgFile.exists()) {
                    InputStream is = new FileInputStream(imgFile);
                    Bitmap bmp = BitmapFactory.decodeStream(is);
                    is.close();
                    return bmp;
                }
                XLog.d(TAG, "图片不存在！" + imgName);
                break;
            }
            case SCREEN: {
                return getLocalImage(imgName, screenWidth, screenHeight);
            }
            case SMALL: {
                return getLocalImage(imgName, SMALL_SCREEN_WIDTH, SMALL_SCREEN_HEIGHT);
            }
        }
        return null;
    }



    @Override
    public Bitmap getLocalImage(String imgName, int sampleWidth, int sampleHeight) throws IOException {
        File imgFile = getImgFile(imgName);
        if(imgFile.exists()) {
            InputStream is = new FileInputStream(imgFile);
            // 计算samplesize...
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, new Rect(-1, -1, -1, -1), opts);
            opts.inSampleSize = computeSampleSize(opts, -1, sampleWidth*sampleHeight);
            opts.inJustDecodeBounds = false;
            is.close();
            // 转成bitmap
            InputStream is2 = new FileInputStream(imgFile);
            Bitmap bmp = BitmapFactory.decodeStream(is2, new Rect(-1, -1, -1, -1), opts);
            is2.close();
            return bmp;
        }
        XLog.d(TAG, "图片不存在！" + imgName);
        return null;
    }

    @Override
    public Bitmap processImage2Bmp(byte[] data, int sWidth, int sHeight) {
        try {
            // 计算samplesize...
            if(sWidth <= 0)
                sWidth = screenWidth;
            if(sHeight <= 0)
                sHeight = screenHeight;
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(data, 0, data.length, opts);
            opts.inSampleSize = computeSampleSize(opts, -1, sWidth*sHeight);
            opts.inJustDecodeBounds = false;
            // 转成bitmap
            return BitmapFactory.decodeByteArray(data, 0, data.length, opts);
        } catch (OutOfMemoryError err) {
            XLog.d(TAG,"OutOfMemoryError!" + err.getMessage());
            err.printStackTrace();
        }
        return null;
    }


    @Override
    public Bitmap processImage2Bmp(InputStream is1, InputStream is2,
                                   int sWidth, int sHeight, Rect outPadding) {
        if(is1 == null || is2 == null)
            return null;

        try {
            if(outPadding == null) {
                outPadding = new Rect(-1, -1, -1, -1);
            }
            Rect copyOutPadding = new Rect(outPadding);
            // 计算samplesize...
            if(sWidth <= 0)
                sWidth = screenWidth;
            if(sHeight <= 0)
                sHeight = screenHeight;
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is1, copyOutPadding, opts);
            opts.inSampleSize = computeSampleSize(opts, -1, sWidth*sHeight);
            opts.inJustDecodeBounds = false;
            // 转成bitmap
            return BitmapFactory.decodeStream(is2, outPadding, opts);
        } catch (OutOfMemoryError err) {
            XLog.d(TAG,"OutOfMemoryError!" + err.getMessage());
            err.printStackTrace();
        } finally {
            try {
                is1.close();
                is2.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }



    @Override
    public boolean processImage2File(byte[] data, String fileName,
                                     int compress, int sWidth, int sHeight) {
        Bitmap bmp = processImage2Bmp(data, sWidth, sHeight);

        // 计算压缩率
        if(compress < 0 || compress > 100) {
            compress = DEFAULT_COMPRESS;
        }

        // 将图片保存到本地
        return saveImageToSd(fileName, bmp, Bitmap.CompressFormat.PNG, compress);
    }

    @Override
    public boolean processImage2File(InputStream is1, InputStream is2, String fileName,
                                    int compress, int sWidth, int sHeight, Rect outPadding) {
            Bitmap bmp = processImage2Bmp(is1, is2, sWidth, sHeight, outPadding);

            // 计算压缩率
            if(compress < 0 || compress > 100) {
                compress = DEFAULT_COMPRESS;
            }

            // 将图片保存到本地
            return saveImageToSd(fileName, bmp, Bitmap.CompressFormat.PNG, compress);
    }


    /**
     * 计算bitmag转换的SampleSize
     * @param options
     * @param minSideLength
     * @param maxNumOfPixels
     * @return
     */
    private static int computeSampleSize(BitmapFactory.Options options,
                                         int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength, maxNumOfPixels);

        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }

        return roundedSize;
    }

    /**
     * 计算初始的SampleSize，主要用于computeSampleSize
     * @param options
     * @param minSideLength
     * @param maxNumOfPixels
     * @return
     */
    private static int computeInitialSampleSize(BitmapFactory.Options options,
                                                int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;

        int lowerBound = (maxNumOfPixels == -1) ? 1 :
                (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == -1) ? 128 :
                (int) Math.min(Math.floor(w / minSideLength),
                        Math.floor(h / minSideLength));

        if (upperBound < lowerBound) {
            // return the larger one when there is no overlapping zone.
            return lowerBound;
        }

        if ((maxNumOfPixels == -1) &&
                (minSideLength == -1)) {
            return 1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }

    /**
     * 计算sdcard上的剩余空间
     * @return
     */
    private int freeSpaceOnSd() {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        double sdFreeMB = ((double)stat.getAvailableBlocks() * (double) stat.getBlockSize()) / (1024*1024);
        return (int) sdFreeMB;
    }


    @Override
    public boolean saveImageToSd(String imgName, Bitmap bm,
                                 Bitmap.CompressFormat format, int compress) {
        if (bm == null || XStringUtil.isNullOrEmpty(imgName)) {
            return false;
        }
//        //判断sdcard上的空间
//        if (FREE_SD_SPACE_NEEDED_TO_CACHE >freeSpaceOnSd()) {
//            XLog.d(TAG, "Low free space onsd, do not cache");
//            return false;
//        }
        try {
            File imgFile = getImgFile(imgName);
            if(!imgDir.exists()) {
                imgDir.mkdirs();
            }
            imgFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(imgFile);
            bm.compress(format, compress, fos);
            fos.flush();
            fos.close();
            XLog.d(TAG, "Image saved to sd!");
            return true;
        } catch (FileNotFoundException e) {
            XLog.d(TAG,"FileNotFoundException");
            return false;
        } catch (IOException e) {
            XLog.d(TAG,"IOException");
            return false;
        }
    }


    @Override
    public boolean saveImageToSd(String imgName, InputStream inputStream) {
        if(inputStream == null || XStringUtil.isNullOrEmpty(imgName))
            return false;

        try {
            // 创建文件夹
            File imgFile = getImgFile(imgName);
            if(!imgDir.exists()) {
                imgDir.mkdirs();
            }
            imgFile.createNewFile();
            // 读取数据
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(); // 往内存写数据
            byte[] buffer = new byte[1024]; // 缓冲区
            int len = -1;
            while ((len = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, len);
            }
            byteArrayOutputStream.close();
            inputStream.close();
            byte[] data = byteArrayOutputStream.toByteArray();
            XLog.d(TAG, "解析gif文件大小：" + data.length + " byte");
            // 存入文件
            FileOutputStream fos = new FileOutputStream(imgFile);
            fos.write(data);
            fos.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    @Override
    public void clearImgDir() {
        if(imgDir == null || !imgDir.exists()) {
            return;
        }

        File[] files = imgDir.listFiles();
        for(int i = 0; i <files.length; i++) {
            String name = files[i].getName();
            if(files[i].delete()) {
                XLog.d(TAG,"删除图片成功："+name);
            }else {
                XLog.d(TAG,"删除图片失败："+name);
            }
        }
        if(imgDir.delete()) {
            XLog.d(TAG,"删除临时图片文件夹成功");
        }else {
            XLog.d(TAG,"删除临时图片文件夹失败");
        }
    }
}