package com.moneybook_android;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RestService extends IntentService {
    private Context context;
    private String phone;
    private String message;
    static NotificationManager nm;
    private String id1 = "test_channel_01";
    private String id2 = "test_channel_02";
    private String id3 = "test_channel_03";
    int NotID = 1;

    public RestService() {
        super("Restservice");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.context = getApplicationContext();
        if (nm == null) {
            nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            createchannel();
        }
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent.getAction().equals(MoneyBookConstant.INTENT_PARSING_SMS)) {
            phone = intent.getStringExtra("SMS_PHOME");
            message = intent.getStringExtra("SMS_MESSAGE");

            RestEndPoint rep = getRestService();

            Call<RestResponse> response = rep.getCards();

            RestResponse res = null;
            try {
                res = response.execute().body();

                List<CreditCard> cards = (List<CreditCard>) res.getData();

                Type listType = new TypeToken<ArrayList<CreditCard>>() {
                }.getType();
                Gson gson = new GsonBuilder().create();

                String json = gson.toJson(cards, listType);

                cards = gson.fromJson(json, listType);

                Log.i(MoneyBookConstant.TAG, "cards cnt:" + cards.size());

                final Record record = getRecord(cards);
                if (record == null) {
                    sendNotification("Fail to get record:" + phone, message);
                    return;
                }
                Call<Record> recordCall = rep.addRecord(record);

                recordCall.enqueue(new Callback<Record>() {

                    @Override
                    public void onResponse(Call<Record> call, Response<Record> response) {
                        if (response.isSuccessful()) {
                            Log.i(MoneyBookConstant.TAG, "Added!");
                            //and5_notificaiton();

                            notlater(record.getDescription(), record.getComments());

                        } else {
                            Log.i(MoneyBookConstant.TAG, "Fail to add record: " + response.code());
                            notlater("Fail to set record", "response code: " + response.code());

                        }

                    }

                    @Override
                    public void onFailure(Call<Record> call, Throwable t) {
                        Log.i(MoneyBookConstant.TAG, "Fail to add record: " + t.getMessage());
                        notlater("Fail to add record", t.getMessage());
                    }
                });
            } catch (IOException e) {
                Log.i(MoneyBookConstant.TAG, "Fail to add record: " + e.getMessage());
            }

        } else {
            Log.i(MoneyBookConstant.TAG, "Unknown intent action:" + intent.getAction());
        }
    }

    public Record getRecord(List<CreditCard> cards) {
        Record record = null;
        for (int i = 0; i < cards.size(); i++) {
            CreditCard card = (CreditCard)cards.get(i);

            if (phone.equals(card.getPhone())) {
                if (message.indexOf("삼성") != -1) {
                    record = createSamsungRecord(card);
                    break;
                } else if (message.indexOf("하나") != -1) {
                    record = createHanaRecord(card);
                    break;
                } else if (message.indexOf("우리(") != -1) {
                    record = createWooriRecord(card);
                    break;
                } else {
                    Log.i(MoneyBookConstant.TAG, "Unknown message:(" + phone + ")" + message);
                    notlater("Unknown message:" + phone, message);
                    break;
                }
            }


        }
        return record;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(MoneyBookConstant.TAG, "service destory!!");
    }

    public RestEndPoint getRestService() {
        Cache cache = new Cache(context.getCacheDir(), MoneyBookConstant.cacheSize);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .cache(cache)
                .addInterceptor(new Interceptor() {
                    @Override
                    public okhttp3.Response intercept(Interceptor.Chain chain)
                            throws IOException {
                        Request request = chain.request();
                        if (!isNetworkAvailable()) {
                            int maxStale = 60 * 60 * 24 * 7; // tolerate 1-weeks stale \
                            request = request
                                    .newBuilder()
                                    .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                                    .build();
                        } else {
                            //  If there is connectivity, we tell the request it can reuse the data for sixty seconds.
                            request = request.newBuilder().header("Cache-Control", "public, max-age=" + 60).build();
                        }
                        return chain.proceed(request);
                    }
                })
                .connectTimeout(MoneyBookConstant.CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(MoneyBookConstant.READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(MoneyBookConstant.WRITE_TIMEOUT, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder().baseUrl(MoneyBookConstant.REST_HOST)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RestEndPoint restService = retrofit.create(RestEndPoint.class);


        return restService;

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private Record createSamsungRecord(CreditCard card) {
        StringTokenizer st = new StringTokenizer(message, "\n");
        String tmpStr = null;
        tmpStr = st.nextToken();
        tmpStr = st.nextToken();

        int cardId = 0;
        int amount = 0;
        String description = null;
        String recordAt = null;

        int pos = tmpStr.indexOf(card.getNumber());
        if (pos == -1) {
            Log.i(MoneyBookConstant.TAG, "Skipped to parse samsung card: " + tmpStr);
            return null;
        }
        cardId = card.getId();

        tmpStr = st.nextToken();

        pos = tmpStr.indexOf("원 ");
        if (pos != -1) {
            String strAmount = tmpStr.replaceAll("[^0-9]", "");
            amount = Integer.parseInt(strAmount);
            tmpStr = st.nextToken();

            StringTokenizer st2 = new StringTokenizer(tmpStr, " ");

            String date = st2.nextToken();
            recordAt = Integer.toString(Calendar.getInstance().get(Calendar.YEAR));
            recordAt += "-";
            recordAt += date.substring(0, 2);
            recordAt += "-";
            recordAt += date.substring(3, 5);
            st2.nextToken();
            description = st2.nextToken();
        }

        Record record = new Record();
        record.setUserId("yoonki");
        record.setAccount(1);
        record.setDescription(description);
        record.setCardId(cardId);
        record.setType(1);
        record.setAmount(amount);
        record.setCategoryId(601);
        record.setComments(message);
        record.setRecordAt(recordAt);
        record.setDivided(1);
        return record;
    }

    private Record createHanaRecord(CreditCard card) {
        StringTokenizer st = new StringTokenizer(message, "\n");
        String tmpStr = null;
        tmpStr = st.nextToken();
        String parsingLineStr = st.nextToken();

        StringTokenizer st2 = new StringTokenizer(parsingLineStr, " ");

        tmpStr = st2.nextToken();
        int pos = tmpStr.indexOf("[하나카드]");

        int cardId = 0;
        int amount = 0;
        String description = "";
        String comment = message;
        String recordAt = "";


        if (pos != -1) { // only for U+
            // [하나카드]박*기님 LG U+ 통신요금 자동(6*3*) 20,900원 정상처리
            String[] strArr = parsingLineStr.split(" ");

            if (!card.getName().contains("U+")) {
                Log.i(MoneyBookConstant.TAG, "Skipped to parse hana card not contained U+: " + card.getName());
                return null;
            }
            cardId = card.getId();


            if (strArr.length < 4) {
                return null;
            }
            if (!"[하나카드]박*기님".equals(strArr[0]) || !"정상처리".equals(strArr[strArr.length - 1])) {
                Log.i(MoneyBookConstant.TAG, "Skipped to parse hana card: " + strArr[0] + "///" + strArr[strArr.length - 1]);
                return null;
            }
            if (strArr[strArr.length - 2].endsWith("원")) {
                String strAmount = strArr[strArr.length - 2].replaceAll("[^0-9]", "");

                amount = Integer.parseInt(strAmount);
            }
            for (int i = strArr.length - 3; i > 0; i--) {
                description = strArr[i] + " " + description;
            }


            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            Date date = new Date();
            recordAt = formatter.format(date); // equal to LocalDate.now()


        } else {
            // 하나(3*0*) 승인 박*기님 20,640원 일시불 10/30 10:08 (주)신세계 누적 1,363,610원
            if (tmpStr.charAt(3) != card.getNumber().charAt(0) || tmpStr.charAt(5) != card.getNumber().charAt(2)) {
                Log.i(MoneyBookConstant.TAG, "Skiped to parse hana card: " + tmpStr);
                return null;
            }
            cardId = card.getId();

            tmpStr = st2.nextToken(); // 승인
            tmpStr = st2.nextToken(); // 박*기님
            tmpStr = st2.nextToken(); // 20,640원
            String strAmount = tmpStr.replaceAll("[^0-9]", "");
            amount = Integer.parseInt(strAmount);
            tmpStr = st2.nextToken(); // 일시불
            tmpStr = st2.nextToken(); // date
            recordAt = Integer.toString(Calendar.getInstance().get(Calendar.YEAR));
            recordAt += "-";
            recordAt += tmpStr.substring(0, 2);
            recordAt += "-";
            recordAt += tmpStr.substring(3, 5);
            tmpStr = st2.nextToken(); // time
            description = st2.nextToken();
        }

        Record record = new Record();
        record.setUserId("yoonki");
        record.setAccount(1);
        record.setDescription(description);
        record.setCardId(cardId);
        record.setType(1);
        record.setAmount(amount);
        record.setCategoryId(601);
        record.setComments(message);
        record.setRecordAt(recordAt);
        record.setDivided(1);
        return record;
    }

    private Record createWooriRecord(CreditCard card) {
        StringTokenizer st = new StringTokenizer(message, "\n");

        if (st.countTokens() != 7) {
            Log.i(MoneyBookConstant.TAG, "Skipped to parse woori card: token size is not 7: " + message);
            return null;
        }

        String tmpStr = null;
        tmpStr = st.nextToken(); // [Web발신]
        tmpStr = st.nextToken(); // 우리(1097)승인

        int cardId = 0;
        int amount = 0;
        String description = "";
        String recordAt = "";

        int pos = tmpStr.indexOf(card.getNumber());
        if (pos == -1) {
            Log.i(MoneyBookConstant.TAG, "Skipped to parse woori card: " + tmpStr);
            return null;
        }
        cardId = card.getId();

        tmpStr = st.nextToken(); //박*기님

        tmpStr = st.nextToken(); //166,000원 일시불

        pos = tmpStr.indexOf("원 ");
        if (pos != -1) {
            String strAmount = tmpStr.replaceAll("[^0-9]", "");
            amount = Integer.parseInt(strAmount);
        }

        tmpStr = st.nextToken(); // "11/08 20:16

        StringTokenizer st2 = new StringTokenizer(tmpStr, " ");

        String date = st2.nextToken();
        recordAt = Integer.toString(Calendar.getInstance().get(Calendar.YEAR));
        recordAt += "-";
        recordAt += date.substring(0, 2);
        recordAt += "-";
        recordAt += date.substring(3, 5);

        description = st.nextToken(); // 깐부

        Record record = new Record();
        record.setUserId("yoonki");
        record.setAccount(1);
        record.setDescription(description);
        record.setCardId(cardId);
        record.setType(1);
        record.setAmount(amount);
        record.setCategoryId(601);
        record.setComments(message);
        record.setRecordAt(recordAt);
        record.setDivided(1);

        return record;
    }

    /*
     * for API 26+ create notification channels
     */
    private void createchannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(id1, "channel1",  //name of the channel
                    NotificationManager.IMPORTANCE_DEFAULT);   //importance level
            //important level: default is is high on the phone.  high is urgent on the phone.  low is medium, so none is low?
            // Configure the notification channel.
            mChannel.setDescription("description..2..");
            mChannel.enableLights(true);
            // Sets the notification light color for notifications posted to this channel, if the device supports this feature.
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setShowBadge(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            nm.createNotificationChannel(mChannel);

            //a medium level channel
            mChannel = new NotificationChannel(id2, "channel2",  //name of the channel
                    NotificationManager.IMPORTANCE_LOW);   //importance level
            // Configure the notification channel.
            mChannel.setDescription("description..2");
            mChannel.enableLights(true);
            // Sets the notification light color for notifications posted to this channel, if the device supports this feature.
            mChannel.setLightColor(Color.BLUE);
            mChannel.enableVibration(true);
            mChannel.setShowBadge(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            nm.createNotificationChannel(mChannel);

            //a urgent level channel
            mChannel = new NotificationChannel(id3, "channel3",  //name of the channel
                    NotificationManager.IMPORTANCE_HIGH);   //importance level
            // Configure the notification channel.
            mChannel.setDescription("description..3");
            mChannel.enableLights(true);
            // Sets the notification light color for notifications posted to this channel, if the device supports this feature.
            mChannel.setLightColor(Color.GREEN);
            mChannel.enableVibration(true);
            mChannel.setShowBadge(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            nm.createNotificationChannel(mChannel);
        }

    }

    //creates a notification for lollipop with a popup/heads up message..
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void and5_notificaiton() {
        Intent notificationIntent = new Intent(getApplicationContext(), ReceiveActivity.class);
        notificationIntent.putExtra("mytype", "iconmsg" + NotID);
        PendingIntent contentIntent = PendingIntent.getActivity(this, NotID, notificationIntent, 0);
        Notification noti = new NotificationCompat.Builder(getApplicationContext(), id3)
                //.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))
                .setSmallIcon(R.drawable.ic_baseline_credit_card_24px)
                .setWhen(System.currentTimeMillis())  //When the event occurred, now, since noti are stored by time.
                .setContentTitle("Lollipop notificaiton")   //Title message top row.
                .setContentText("This should be an annoying heads up message.")  //message when looking at the notification, second row
                //the following 2 lines cause it to show up as popup message at the top in android 5 systems.
                .setPriority(Notification.PRIORITY_MAX)  //could also be PRIORITY_HIGH.  needed for LOLLIPOP, M and N.  But not Oreo
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})  //for the heads/pop up must have sound or vibrate
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)  //VISIBILITY_PRIVATE or VISIBILITY_SECRET
                .setContentIntent(contentIntent)  //what activity to open.
                .setAutoCancel(true)   //allow auto cancel when pressed.
                .setChannelId(id3)  //Oreo notifications
                .build();  //finally build and return a Notification.

        //Show the notification
        nm.notify(NotID, noti);
        NotID++;
    }

    /*
     * create a notification with a icon and message, plus a title.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void sendNotification(String title, String text) {

        Intent notificationIntent = new Intent(context, ReceiveActivity.class);
        notificationIntent.putExtra(MoneyBookConstant.NOTIFICATION_RECEIVE, NotID + ":" + text); //not required, but used in this example.
        PendingIntent contentIntent = PendingIntent.getActivity(context, NotID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT); //PendingIntent.FLAG_UPDATE_CURRENT
        //Create a new notification. The construction Notification(int icon, CharSequence tickerText, long when) is deprecated.
        //If you target API level 11 or above, use Notification.Builder instead
        //With the second parameter, it would show a marquee
        Notification noti = new NotificationCompat.Builder(context, id3)
                .setSmallIcon(R.drawable.ic_baseline_credit_card_24px)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_baseline_alarm_24px))
                .setWhen(System.currentTimeMillis())  //When the event occurred, now, since noti are stored by time.
                .setContentTitle(title)   //Title message top row.
                .setContentText(text)  //message when looking at the notification, second row
                //the following 2 lines cause it to show up as popup message at the top in android 5 systems.
                .setPriority(Notification.PRIORITY_MAX)  //could also be PRIORITY_HIGH.  needed for LOLLIPOP, M and N.  But not Oreo
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})  //for the heads/pop up must have sound or vibrate
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)  //VISIBILITY_PRIVATE or VISIBILITY_SECRET
                .setContentIntent(contentIntent)  //what activity to open.
                .setAutoCancel(true)   //allow auto cancel when pressed.
                .setChannelId(id3)//Oreo notifications
                .build();  //finally build and return a Notification.


        nm.notify(NotID, noti);
        NotID++;

    }

    public void notlater(String title, String text) {

        //---use the AlarmManager to trigger an alarm---
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        //---get current date and time---
        Calendar calendar = Calendar.getInstance();

        //---sets the time for the alarm to trigger in 2 minutes from now---
        calendar.set(Calendar.SECOND, calendar.get(Calendar.SECOND) + 5);
        //calendar.set(Calendar.SECOND, 0);

        //---PendingIntent to launch activity when the alarm triggers-

        //Intent notificationIntent = new Intent(getApplicationContext(), receiveActivity.class);
        Intent notificationIntent = new Intent(MoneyBookConstant.NOTIFICATION_ALARM);
        notificationIntent.putExtra("NotifID", NotID);
        notificationIntent.putExtra("NotiTitle", title);
        notificationIntent.putExtra("NotiText", text);

        PendingIntent contentIntent = PendingIntent.getActivity(this, NotID, notificationIntent, 0);
        Log.i("MainACtivity", "Set alarm, I hope");


        //---sets the alarm to trigger---
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), contentIntent);
        NotID++;
    }
}
