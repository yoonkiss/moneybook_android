package com.moneybook_android;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RestServiceManager {
    private static final String REST_HOST = "http://192.168.0.60:8080";

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
            Retrofit retrofit = new Retrofit.Builder().baseUrl(REST_HOST)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            restService = retrofit.create(RestService.class);

        }
        return restService;

    }
}
