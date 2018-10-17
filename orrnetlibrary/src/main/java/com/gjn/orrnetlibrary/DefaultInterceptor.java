package com.gjn.orrnetlibrary;

import android.util.Log;

import com.gjn.orrnetlibrary.utils.JsonUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.Util;
import okio.Buffer;
import okio.BufferedSource;

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
        Request request = chain.request();
        if (onHttpHeadersListener != null) {
            Map<String, String> oldHeads = new HashMap<>();
            for (int i = 0; i < request.headers().size(); i++) {
                oldHeads.put(request.headers().name(i), request.headers().value(i));
            }
            Map<String, String> heads = onHttpHeadersListener.addRequestHeaders(String.valueOf(request.url()), oldHeads);
            if (heads != null && heads.size() > 0) {
                Request.Builder builder = request.newBuilder()
                        .method(request.method(), request.body());
                for (Map.Entry<String, String> entry : heads.entrySet()) {
                    builder.header(entry.getKey(), entry.getValue());
                }
                request = builder.build();
            }
        }
        printRequest(request);
        long startTime = System.nanoTime();
        Response response = chain.proceed(request);
        if (response.headers().size() > 0) {
            if (onHttpHeadersListener != null) {
                onHttpHeadersListener.getResponseHeader(String.valueOf(response.request().url()), response.headers());
            }
        }
        printResponse(response, startTime);
        return response;
    }

    private long printRequest(Request request) throws IOException {
        long time = System.nanoTime();
        log("╔══════════════════════════════════════════════════════════════════════════════════════════════");
        log("║ --> "+request.method()+" "+request.url());
        Headers headers = request.headers();
        if (headers.size() > 0) {
            log("║ RequestHeaders ");
            for (int i = 0; i < headers.size(); i++) {
                log("║ " + headers.name(i) + " : " + headers.value(i));
            }
        }
        RequestBody body = request.body();
        if (body != null) {
            log("║ BodyType = " + body.contentType());
            log("║ BodyLenght = " + body.contentLength());
            if (body instanceof FormBody) {
                log("║ Post Key Value ");
                for (int i = 0; i < ((FormBody) body).size(); i++) {
                    log("║ " + ((FormBody) body).encodedName(i) + " = " + ((FormBody) body).encodedValue(i));
                }
            }
        }
        log("║──────────────────────────────────────────────────────────────────────────────────────────────");
        return time;
    }

    private void printResponse(Response response, long startTime) throws IOException {
        long endTime = System.nanoTime();
        log(String.format("║ --> %s : %.1fms", response.request().url(), (endTime - startTime) / 1e6d));
        log("║ --> HttpCode = " + response.code());
        Headers headers = response.headers();
        if (headers.size() > 0) {
            log("║ ResponseHeaders ");
            for (int i = 0; i < headers.size(); i++) {
                log("║ " + headers.name(i) + " : " + headers.value(i));
            }
        }
        log("║──────────────────────────────────────────────────────────────────────────────────────────────");
        ResponseBody responseBody = response.body();
        long contentLength = responseBody.contentLength();
        BufferedSource source = responseBody.source();
        source.request(Long.MAX_VALUE);
        Buffer buffer = source.buffer();
        Charset charset = Util.UTF_8;
        MediaType contentType = responseBody.contentType();
        if (contentType != null) {
            charset = contentType.charset() == null ? Util.UTF_8 : contentType.charset();
        }
        if (contentLength != 0) {
            String result = JsonUtils.decodeUnicode(buffer.clone().readString(charset));
            log("║ "+result);
            log("║──────────────────────────────────────────────────────────────────────────────────────────────");
            if (result.startsWith("{\"")) {
                log(JsonUtils.formatJson(result));
            }
        }
        log("╚══════════════════════════════════════════════════════════════════════════════════════════════");
    }

    private void log(String msg) {
        if (isDebug) {
            Log.d(TAG, msg);
        }
    }

    public interface OnHttpHeadersListener{
        Map<String, String> addRequestHeaders(String url, Map<String, String> headers);

        void getResponseHeader(String url, Headers headers);
    }
}
