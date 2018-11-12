package com.moneybook_android;


import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecordService extends IntentService {
    private Context context;

    public RecordService() {
        super("Restservice");
    }

    @Override
    public void onCreate() {
        Log.i(MainApplication.TAG, "service onCreate!!");
        this.context = this;
    }

    //@Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(MainApplication.TAG, "service onBind!!");
        return null;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.i(MainApplication.TAG, "service onHandleIntent!!");


        String phone = intent.getStringExtra("SMS_PHOME");
        String message = intent.getStringExtra("SMS_MESSAGE");

        RestServiceManager serviceManager = RestServiceManager.getInstance();
        CardMessage msg = new CardMessage(context, phone, message);

        final NotificationHandler handler = new NotificationHandler(context);
        handler.createChannel();

        RestService rs = serviceManager.getRestService();

        final Record record = msg.toRecord();
        if (record == null) {
            handler.sendNotification("Fail to get record:" + phone, message);
            return;
        }
        Call<Record> recordCall = rs.addRecord(record);

        recordCall.enqueue(new Callback<Record>() {

            @Override
            public void onResponse(Call<Record> call, Response<Record> response) {
                if (response.isSuccessful()) {
                    Log.i(MainApplication.TAG, "Added!");
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

        Toast.makeText(this, "alram....wake up", Toast.LENGTH_SHORT).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(MainApplication.TAG, "service onStartCommand!!");
//        if(!this.isRunning) {
//            this.isRunning = true;
//            this.backgroundThread.start();
//        }
//        return START_STICKY;

        String phone = intent.getStringExtra("SMS_PHOME");
        String message = intent.getStringExtra("SMS_MESSAGE");

        RestServiceManager serviceManager = RestServiceManager.getInstance();
        CardMessage msg = new CardMessage(context, phone, message);

        final NotificationHandler handler = new NotificationHandler(context);
        handler.createChannel();

        RestService rs = serviceManager.getRestService();

        final Record record = msg.toRecord();
        if (record == null) {
            handler.sendNotification("Fail to get record:" + phone, message);
            return START_NOT_STICKY;
        }
        Call<Record> recordCall = rs.addRecord(record);

        recordCall.enqueue(new Callback<Record>() {

            @Override
            public void onResponse(Call<Record> call, Response<Record> response) {
                if (response.isSuccessful()) {
                    Log.i(MainApplication.TAG, "Added!");
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

        Toast.makeText(this, "alram....wake up", Toast.LENGTH_SHORT).show();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(MainApplication.TAG, "service destory!!");
    }


}
