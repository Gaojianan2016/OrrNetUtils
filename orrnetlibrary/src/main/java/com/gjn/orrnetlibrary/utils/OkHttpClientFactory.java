package com.gjn.orrnetlibrary.utils;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

/**
 * @author gjn
 * @time 2018/10/11 10:55
 */

public class OkHttpClientFactory {

    public static OkHttpClient create(Interceptor interceptor){
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (interceptor != null) {
            builder.addInterceptor(interceptor);
        }
        return builder.build();
    }

    public static OkHttpClient create(){
        return create(null);
    }
}
