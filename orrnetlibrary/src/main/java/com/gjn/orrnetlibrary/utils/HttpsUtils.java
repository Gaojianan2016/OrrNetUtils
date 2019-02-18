package com.gjn.orrnetlibrary.utils;

import android.util.Log;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * HttpsUtils
 * Author: gjn.
 * Time: 2017/12/5.
 */

public class HttpsUtils {
    private static final String TAG = "HttpsUtils";
    private static HttpsUtils mHttpsUtils;
    private static HostnameVerifier hostnameVerifier;
    private static X509TrustManager trustManager;

    private HttpsUtils() {
        if (hostnameVerifier == null) {
            hostnameVerifier = new TrustAllHostnameVerifier();
        }
        if (trustManager == null){
            trustManager = new TrustAllManager();
        }
    }

    public static HttpsUtils getInstance() {
        if (mHttpsUtils == null) {
            synchronized (HttpsUtils.class) {
                if (mHttpsUtils == null) {
                    mHttpsUtils = new HttpsUtils();
                }
            }
        }
        return mHttpsUtils;
    }

    public HostnameVerifier getHostnameVerifier() {
        return hostnameVerifier;
    }

    public X509TrustManager getTrustManager() {
        return trustManager;
    }

    public void allowAllSSL(){
        HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
        HttpsURLConnection.setDefaultSSLSocketFactory(createSSLSocketFactory());
    }

    /**
     * 允许全部Https
     *
     * @return
     */
    public SSLSocketFactory createSSLSocketFactory() {
        SSLSocketFactory factory = null;
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{new TrustAllManager()}, new SecureRandom());
            factory = sslContext.getSocketFactory();
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "算法异常", e);
        } catch (KeyManagementException e) {
            Log.e(TAG, "密钥管理异常", e);
        }
        return factory;
    }

    private class TrustAllManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            //接受任意客户端证书
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            //接受任意服务端证书
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    private class TrustAllHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            //接受任意域名服务器
            return true;
        }
    }
}
