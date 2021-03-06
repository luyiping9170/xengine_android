package com.xengine.android.system.file;

import java.io.File;

/**
 * 文件存储管理接口
 * Created with IntelliJ IDEA.
 * User: jasontujun
 * Date: 13-5-11
 * Time: 上午10:51
 */
public interface XFileMgr {

    static final int FILE_ROOT = 0;
    static final int FILE_TYPE_TMP = 1;
    static final int FILE_TYPE_PHOTO = 2;

    /**
     * 设置根目录的文件夹名称。
     * @param rootName
     */
    void setRootName(String rootName);

    String getRootName();

    /**
     * 设置子文件夹类型和子文件夹路径。
     * 如果子文件夹不存在，则尝试创建；如果存在，则清空子文件夹
     * @param type 自定义类型（系统保留0-9的文件夹类型，重复设置会返回false）
     * @param dirName  文件夹路径
     * @param clear 如果子文件存在，是否清空
     * @return 设置成功则返回true，否则返回false
     * @see #FILE_TYPE_TMP
     * @see #FILE_TYPE_PHOTO
     */
    boolean setDir(int type, String dirName, boolean clear);

    /**
     * 根据子文件夹类型获取文件夹
     * @param type 文件夹类型
     * @return 如果存在，返回对应文件夹；如果不存在，返回null
     * @see #FILE_ROOT
     * @see #FILE_TYPE_TMP
     * @see #FILE_TYPE_PHOTO
     */
    File getDir(int type);

    /**
     * 清除特定文件夹下内容
     * @param type 文件类型
     */
    void clearDir(int type);
}
