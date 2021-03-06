package com.xengine.android.session.http.java;

import com.xengine.android.session.http.XBaseHttpResponse;

import java.net.HttpURLConnection;
import java.nio.charset.Charset;

/**
 * Created with IntelliJ IDEA.
 * User: tujun
 * Date: 13-9-6
 * Time: 下午7:47
 * To change this template use File | Settings | File Templates.
 */
class XJavaHttpResponse extends XBaseHttpResponse {

    private HttpURLConnection mConnection;
    private Charset mCharset;

    protected void setConnection(HttpURLConnection connection) {
        mConnection = connection;
    }

    protected void setContentType(Charset charset) {
        mCharset = charset;
    }

    @Override
    public void consumeContent() {
        super.consumeContent();
        if (mConnection != null)
            mConnection.disconnect();
    }

    @Override
    public Charset getContentType() {
        return mCharset;
    }
}
