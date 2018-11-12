package com.moneybook_android;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import com.facebook.react.ReactApplication;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.react.shell.MainReactPackage;
import com.facebook.soloader.SoLoader;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainApplication extends Application implements ReactApplication {

    public static final String TAG = "MoneyBook";

    private final ReactNativeHost mReactNativeHost = new ReactNativeHost(this) {
        @Override
        public boolean getUseDeveloperSupport() {
            return BuildConfig.DEBUG;
        }

        @Override
        protected List<ReactPackage> getPackages() {
            return Arrays.<ReactPackage>asList(
                    new MainReactPackage(),
                    new MoneyBookPackage()
            );
        }

        @Override
        protected String getJSMainModuleName() {
            return "index";
        }
    };

    @Override
    public ReactNativeHost getReactNativeHost() {
        return mReactNativeHost;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SoLoader.init(this, /* native exopackage */ false);

        SharedPreferences sp = getSharedPreferences("shared", MODE_PRIVATE);
        String strCard = sp.getString("creditcard", "");

        if (strCard.isEmpty()) {
            RestService rs = RestServiceManager.getInstance().getRestService();
            Call<RestResponse> response = rs.getCards();

            response.enqueue(new Callback<RestResponse>() {

                @Override
                public void onResponse(Call<RestResponse> call, Response<RestResponse> response) {
                    if (response.isSuccessful()) {

                        Gson gson = new GsonBuilder().create();

                        RestResponse res = response.body();
                        List<CreditCard> cards = (List<CreditCard>) res.getData();

                        Type listType = new TypeToken<ArrayList<CreditCard>>() {
                        }.getType();
                        String json = gson.toJson(cards, listType);

                        SharedPreferences sp = getSharedPreferences("shared", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("creditcard", json); // JSON으로 변환한 객체를 저장한다.
                        editor.commit();
                        Log.i(MainApplication.TAG, "card list has been registered to preferences.");

                    } else {
                        Log.i(MainApplication.TAG, "response code: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<RestResponse> call, Throwable t) {
                    Log.i(MainApplication.TAG, "Fail to get card list: " + t.getMessage());

                }
            });
        }
    }
}
