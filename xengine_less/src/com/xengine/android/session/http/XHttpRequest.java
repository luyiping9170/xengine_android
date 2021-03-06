package com.xengine.android.session.http;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: tujun
 * Date: 13-9-3
 * Time: 下午1:58
 * To change this template use File | Settings | File Templates.
 */
public interface XHttpRequest {

    public enum HttpMethod {
        GET,
        POST,
        PUT,
        DELETE
    }

    /**
     * 设置请求的url
     * @param url
     * @return
     */
    XHttpRequest setUrl(String url);

    /**
     * 获取请求的url
     * @return
     */
    String getUrl();

    /**
     * 设置请求的类型，默认是GET请求
     * @see HttpMethod
     * @param method
     */
    XHttpRequest setMethod(HttpMethod method);

    /**
     * 获取请求的类型
     * @see HttpMethod
     * @return
     */
    HttpMethod getMethod();

    /**
     * 设置请求的字符编码
     * @return 如果字符编码支持，返回true；否则返回false
     */
    boolean setCharset(String charsetName);

    /**
     * 获取该请求的字符编码
     * @return
     */
    String getCharset();

    /**
     * 请求的数据是否压缩（实际在请求头中添加参数Accept-Encoding : gzip）
     * @param gzip 是否压缩
     */
    XHttpRequest setGzip(boolean gzip);

    /**
     * 添加字符串的参数（键值对）
     * @param key
     * @param value
     */
    XHttpRequest addStringParam(String key, String value);

    /**
     * 添加文件参数（常用于上传）
     * @param key
     * @param file
     */
    XHttpRequest addFileParam(String key, File file);

    /**
     * 添加请求头的参数（键值对）
     * @param key
     * @param value
     */
    XHttpRequest addHeader(String key, String value);
}
