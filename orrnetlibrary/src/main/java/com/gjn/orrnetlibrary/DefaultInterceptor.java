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
        StringBuilder requestSb = new StringBuilder();
        requestSb.append(printRequest(request));
        long startTime = System.nanoTime();
        Response response = chain.proceed(request);
        if (response.headers().size() > 0) {
            if (onHttpHeadersListener != null) {
                onHttpHeadersListener.getResponseHeader(String.valueOf(response.request().url()), response.headers());
            }
        }
        StringBuilder responseSb = new StringBuilder();
        responseSb.append(printResponse(response, startTime));
        log(requestSb.toString());
        log(responseSb.toString());
        return response;
    }

    private String printRequest(Request request) throws IOException {
        StringBuilder log = new StringBuilder();
        log.append("╔══════════════════════════════════════════════════════════════════════════════════════════════");
        log.append('\n').append("║ --> "+request.method()+" "+request.url());
        Headers headers = request.headers();
        if (headers.size() > 0) {
            log.append('\n').append("║ RequestHeaders ");
            for (int i = 0; i < headers.size(); i++) {
                log.append('\n').append("║ " + headers.name(i) + " : " + headers.value(i));
            }
        }
        RequestBody body = request.body();
        if (body != null) {
            log.append('\n').append("║ BodyType = " + body.contentType());
            log.append('\n').append("║ BodyLenght = " + body.contentLength());
            if (body instanceof FormBody) {
                log.append('\n').append("║ Post Key Value ");
                for (int i = 0; i < ((FormBody) body).size(); i++) {
                    log.append('\n').append("║ " + ((FormBody) body).encodedName(i) + " = " + ((FormBody) body).encodedValue(i));
                }
            }
        }
        log.append('\n').append("║──────────────────────────────────────────────────────────────────────────────────────────────");
        return log.toString();
    }

    private String printResponse(Response response, long startTime) throws IOException {
        StringBuilder log = new StringBuilder();
        long endTime = System.nanoTime();
        log.append(String.format("║ --> %s : %.1fms", response.request().url(), (endTime - startTime) / 1e6d));
        log.append('\n').append("║ --> HttpCode = " + response.code());
        Headers headers = response.headers();
        if (headers.size() > 0) {
            log.append('\n').append("║ ResponseHeaders ");
            for (int i = 0; i < headers.size(); i++) {
                log.append('\n').append("║ " + headers.name(i) + " : " + headers.value(i));
            }
        }
        log.append('\n').append("║──────────────────────────────────────────────────────────────────────────────────────────────");
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
            if (result.startsWith("{\"")) {
                log.append('\n').append(JsonUtils.formatJson(result));
            }else {
                log.append('\n').append("║ "+result);
            }
        }
        log.append('\n').append("╚══════════════════════════════════════════════════════════════════════════════════════════════");
        return log.toString();
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
