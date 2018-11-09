package com.moneybook_android;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface RestService {
    //@GET("users/{user}/repos")
    //Call<List<Repo>> listRepos(@Path("user") String user);

    @GET("/api/v1/card")
    Call<RestResponse> getCards();

    @POST("/api/v1/record")
    Call<Record> addRecord(@Body Record record);
}
