package com.moneybook_android;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;

public class NotificationHandler {
    public static String id1 = "test_channel_01";
    public static String id2 = "test_channel_02";
    public static String id3 = "test_channel_03";

    public static String intentKey = "MB";
    private Context mContext;
    private NotificationManager notificationManager;

    public NotificationHandler(Context context) {
        mContext = context;
        notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    /*
     * for API 26+ create notification channels
     */
    public void createChannel() {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(id1,
                    mContext.getString(R.string.channel_name),  //name of the channel
                    NotificationManager.IMPORTANCE_DEFAULT);   //importance level
            //important level: default is is high on the phone.  high is urgent on the phone.  low is medium, so none is low?
            // Configure the notification channel.
            mChannel.setDescription(mContext.getString(R.string.channel_description));
            mChannel.enableLights(true);
            // Sets the notification light color for notifications posted to this channel, if the device supports this feature.
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setShowBadge(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            notificationManager.createNotificationChannel(mChannel);

            //a medium level channel
            mChannel = new NotificationChannel(id2,
                    mContext.getString(R.string.channel_name2),  //name of the channel
                    NotificationManager.IMPORTANCE_LOW);   //importance level
            // Configure the notification channel.
            mChannel.setDescription(mContext.getString(R.string.channel_description2));
            mChannel.enableLights(true);
            // Sets the notification light color for notifications posted to this channel, if the device supports this feature.
            mChannel.setLightColor(Color.BLUE);
            mChannel.enableVibration(true);
            mChannel.setShowBadge(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            notificationManager.createNotificationChannel(mChannel);

            //a urgent level channel
            mChannel = new NotificationChannel(id3,
                    mContext.getString(R.string.channel_name2),  //name of the channel
                    NotificationManager.IMPORTANCE_HIGH);   //importance level
            // Configure the notification channel.
            mChannel.setDescription(mContext.getString(R.string.channel_description3));
            mChannel.enableLights(true);
            // Sets the notification light color for notifications posted to this channel, if the device supports this feature.
            mChannel.setLightColor(Color.GREEN);
            mChannel.enableVibration(true);
            mChannel.setShowBadge(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            notificationManager.createNotificationChannel(mChannel);

        }
    }

    /*
     * create a notification with a icon and message, plus a title.
     */
    public void sendNotification(String title, String text) {
        int NotID = 1;

        Intent notificationIntent = new Intent(mContext, ReceiveActivity.class);
        notificationIntent.putExtra(intentKey,  NotID +":" + text); //not required, but used in this example.
        PendingIntent contentIntent = PendingIntent.getActivity(mContext, NotID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT); //PendingIntent.FLAG_UPDATE_CURRENT
        //Create a new notification. The construction Notification(int icon, CharSequence tickerText, long when) is deprecated.
        //If you target API level 11 or above, use Notification.Builder instead
        //With the second parameter, it would show a marquee
        Notification noti = new NotificationCompat.Builder(mContext, id1)
                .setSmallIcon(R.drawable.ic_announcement_black_24dp)
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_launcher))
                .setWhen(System.currentTimeMillis())  //When the event occurred, now, since noti are stored by time.
                .setContentTitle(title)   //Title message top row.
                .setContentText(text)  //message when looking at the notification, second row
                .setContentIntent(contentIntent)  //what activity to open.
                .setAutoCancel(true)   //allow auto cancel when pressed.
                .setChannelId(id2)
                .build();  //finally build and return a Notification.


        notificationManager.notify(NotID, noti);
        NotID++;
    }

}
