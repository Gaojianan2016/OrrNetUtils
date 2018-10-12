package com.gjn.orrnetlibrary;

import android.util.Log;

import com.gjn.orrnetlibrary.utils.FileUtils;
import com.gjn.orrnetlibrary.utils.OkHttpClientFactory;

import java.io.File;
import java.util.Map;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * @author gjn
 * @time 2018/10/9 15:34
 */

public class OkHttpManager {
    private static final String TAG = "OkHttpManager";

    public static final MediaType MEDIA_TYPE_JSON
            = MediaType.parse("application/json; charset=utf-8");
    public static final MediaType MEDIA_TYPE_MARKDOWN
            = MediaType.parse("text/x-markdown; charset=utf-8");

    private static OkHttpClient okHttpClient;
    private static OkHttpManager okHttpManager;

    private OkHttpManager(Interceptor it) {
        if (it == null) {
            it = new DefaultInterceptor();
        }
        if (okHttpClient == null) {
            okHttpClient = OkHttpClientFactory.create(it);
        }
    }

    public static OkHttpManager getInstance() {
        if (okHttpManager == null) {
            synchronized (OkHttpManager.class) {
                if (okHttpManager == null) {
                    okHttpManager = new OkHttpManager(null);
                }
            }
        }
        return okHttpManager;
    }

    public static OkHttpManager getInstance(Interceptor interceptor) {
        if (okHttpManager == null) {
            synchronized (OkHttpManager.class) {
                if (okHttpManager == null) {
                    okHttpManager = new OkHttpManager(interceptor);
                }
            }
        }
        return okHttpManager;
    }

    public static void setOkHttpClient(OkHttpClient client) {
        okHttpClient = client;
    }

    public void get(String url, Callback callback) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        okHttpClient.newCall(request).enqueue(callback);
    }

    public void post(String url, Callback callback) {
        postJson(url, null, callback);
    }

    public void postJson(String url, String json, Callback callback) {
        if (json == null) {
            json = "";
        }
        Request request = new Request.Builder()
                .post(RequestBody.create(MEDIA_TYPE_JSON, json))
                .url(url)
                .build();
        okHttpClient.newCall(request).enqueue(callback);
    }

    public void postKeys(String url, Map<String, String> maps, Callback callback) {
        FormBody.Builder builder = new FormBody.Builder();
        if (maps != null) {
            for (String key : maps.keySet()) {
                builder.add(key, maps.get(key));
            }
        }
        Request request = new Request.Builder()
                .post(builder.build())
                .url(url)
                .build();
        okHttpClient.newCall(request).enqueue(callback);
    }

    public void postFile(String url, File file, Callback callback) {
        if (file == null || !file.exists()) {
            Log.e(TAG, "file is null.");
            return;
        }
        String type = FileUtils.getTypeFromSuffix(file);
        if (type == null || type.isEmpty()) {
            type = "text/x-markdown";
        }
        Request request = new Request.Builder()
                .post(RequestBody.create(
                        MediaType.parse(type + "; charset=utf-8"),
                        file))
                .url(url)
                .build();
        okHttpClient.newCall(request).enqueue(callback);
    }


}
