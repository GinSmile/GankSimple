package com.neusnow.ganksimple;


import com.neusnow.ganksimple.bean.GirlPageBean;

import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

/**
 * Created by xujin on 16/08/03.
 */
public interface ApiService {
    /*@GET("data/福利/" + 10 + "/{page}")
    Call<GirlPageBean> getData(
            @Path("page") int page);*/

    @GET("data/福利/" + 10 + "/{page}")
    public Observable<GirlPageBean> getData(
            @Path("page") int page);




    /*
    @GET("day/{year}/{month}/{day}")
    Call<AllPageBean> getAll(
            @Path("year") int year,
            @Path("month") int month,
            @Path("day") int day);
            */
}
