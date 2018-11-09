package com.moneybook_android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.annotation.Nullable;
import android.telephony.SmsMessage;
import android.util.Log;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SmsReceiver extends BroadcastReceiver {
    private static final String EVENT = "smsReceived";
    private ReactApplicationContext mContext;


    public SmsReceiver() {
        super();
    }

    public SmsReceiver(ReactApplicationContext context) {
        mContext = context;
    }


    private void receiveMessage(final Context context, final SmsMessage message) {
        Log.i(MainApplication.TAG, "called receiveMessage:" + message.getDisplayMessageBody());

        RestServiceManager serviceManager = RestServiceManager.getInstance();
        CardMessage msg = new CardMessage(context, message.getOriginatingAddress(), message.getMessageBody());

        RestService rs = serviceManager.getRestService();

        final Record record = msg.toRecord();
        if (record == null) {
            return;
        }
        Call<Record> recordCall = rs.addRecord(record);

        recordCall.enqueue(new Callback<Record>() {

            NotificationHandler handler = new NotificationHandler(context);

            @Override
            public void onResponse(Call<Record> call, Response<Record> response) {
                if (response.isSuccessful()) {
                    Log.i(MainApplication.TAG, "Added!");


                    handler.createChannel();
                    handler.sendNotification(record.getDescription(), record.getComments());

                } else {
                    Log.i(MainApplication.TAG, "Fail to add record: " + response.code());
                    handler.sendNotification("Fail to set record", "response code: " + response.code());
                }

            }

            @Override
            public void onFailure(Call<Record> call, Throwable t) {
                Log.i(MainApplication.TAG, "Fail to add record: " + t.getMessage());
                handler.sendNotification("Fail to add record", t.getMessage());
            }
        });

    }

    private void sendEvent(ReactContext reactContext,
                           String eventName,
                           @Nullable WritableMap params) {
        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            Log.e(MainApplication.TAG, "intent is null");
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            for (SmsMessage message : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                receiveMessage(context, message);
            }

            return;
        }

        try {
            final Bundle bundle = intent.getExtras();

            if (bundle == null || !bundle.containsKey("pdus")) {
                return;
            }

            final Object[] pdus = (Object[]) bundle.get("pdus");

            for (Object pdu : pdus) {
                receiveMessage(context, SmsMessage.createFromPdu((byte[]) pdu));
            }
        } catch (Exception e) {
            Log.e(MainApplication.TAG, e.getMessage());
        }
    }


}

