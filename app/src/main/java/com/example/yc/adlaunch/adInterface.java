package com.example.yc.adlaunch;


import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by yc on 2018/8/19.
 */

public interface adInterface {
    @GET("version")
    Call<List<advertisement>> getAdMsg();
}
