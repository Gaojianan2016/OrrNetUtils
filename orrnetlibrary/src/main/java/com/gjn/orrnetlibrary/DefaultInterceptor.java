package com.gjn.orrnetlibrary;

import android.util.Log;

import java.io.IOException;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @author gjn
 * @time 2018/10/9 15:47
 */

public class DefaultInterceptor implements Interceptor {
    private static final String TAG = "DefaultInterceptor";

    public static boolean isDebug = true;

    private OnHttpHeadersListener onHttpHeadersListener;

    public DefaultInterceptor() {
    }

    public DefaultInterceptor(OnHttpHeadersListener onHttpHeadersListener) {
        this.onHttpHeadersListener = onHttpHeadersListener;
    }

    public void setOnHttpHeadersListener(OnHttpHeadersListener onHttpHeadersListener) {
        this.onHttpHeadersListener = onHttpHeadersListener;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Headers headers;
        RequestBody body;
        Request request = chain.request();
        if (onHttpHeadersListener != null) {
            Map<String, String> heads = onHttpHeadersListener.addRequestHeaders(String.valueOf(request.url()));
            if (heads != null && heads.size() > 0) {
                Request.Builder builder = request.newBuilder()
                        .method(request.method(), request.body());
                for (Map.Entry<String, String> entry : heads.entrySet()) {
                    builder.header(entry.getKey(), entry.getValue());
                }
                request = builder.build();
            }
        }
        log("============ HTTP START ============");
        long startTime = System.nanoTime();
        log("START--> HTTP URL: " + request.url() + " TYPE: " + request.method());
        headers = request.headers();
        if (headers.size() > 0) {
            log("START--> HTTP REQUEST HEAD: ");
            for (int i = 0; i < headers.size(); i++) {
                log("START--> " + headers.name(i) + " = " + headers.value(i));
            }
        }
        body = request.body();
        if (body != null) {
            log("START--> HTTP BODY LENGTH: " + body.contentLength());
            if (body instanceof FormBody) {
                log("START--> HTTP REQUEST KEYS: ");
                for (int i = 0; i < ((FormBody) body).size(); i++) {
                    log("START--> " + ((FormBody) body).encodedName(i) + "=" + ((FormBody) body).encodedValue(i));
                }
            }
        }
        log("============ REQUEST TO RESPONSE ============");
        Response response = chain.proceed(request);
        long endTime = System.nanoTime();
        log(String.format("END--> %s : %.1fms", response.request().url(), (endTime - startTime) / 1e6d));
        log("END--> HTTP CODE: " + response.code());
        headers = response.headers();
        if (headers.size() > 0) {
            log("END--> HTTP RESPONSE HEAD: ");
            for (int i = 0; i < headers.size(); i++) {
                log("END--> " + headers.name(i) + " = " + headers.value(i));
            }
            if (onHttpHeadersListener != null) {
                onHttpHeadersListener.getResponseHeader(String.valueOf(response.request().url()), headers);
            }
        }
        log("============ HTTP END ============");
        return response;
    }

    private void log(String msg) {
        if (isDebug) {
            Log.d(TAG, msg);
        }
    }

    public interface OnHttpHeadersListener{
        Map<String, String> addRequestHeaders(String url);

        void getResponseHeader(String url, Headers headers);
    }
}
