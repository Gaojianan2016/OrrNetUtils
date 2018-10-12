package com.gjn.orrnetutils;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * @author gjn
 * @time 2018/10/11 17:56
 */

public interface IApi {
    String BASE_URL = "https://gank.io/api/data/";

    @GET("福利/{count}/{page}")
    Observable<GankResponse> fuli(@Path("page") int page, @Path("count") int count);
}
