package com.moneybook_android;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RestServiceManager {
    private static final String REST_HOST = "https://enigmatic-waters-28500.herokuapp.com";

    private static Map<String, CreditCard> cards = new HashMap<String, CreditCard>();

    private static RestServiceManager serviceManager;

    private static RestService restService;

    public static RestServiceManager getInstance() {
        if (serviceManager == null) {
            serviceManager = new RestServiceManager();
        }
        return serviceManager;
    }

    public static Map<String, CreditCard> getCards() {
        return cards;
    };

    public static RestService getRestService() {
        if (restService == null) {
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(1, TimeUnit.MINUTES)
                    .readTimeout(1, TimeUnit.MINUTES)
                    .writeTimeout(1, TimeUnit.MINUTES)
                    .build();

            Retrofit retrofit = new Retrofit.Builder().baseUrl(REST_HOST)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            restService = retrofit.create(RestService.class);

        }
        return restService;

    }
}
