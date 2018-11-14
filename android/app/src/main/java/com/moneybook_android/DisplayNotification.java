package com.moneybook_android;

/**
 * This activity will not display to the screen (hopefully).  It just sets a notification
 * and then exits (finish())
 * <p>
 * http://mobiforge.com/developing/story/displaying-status-bar-notifications-android
 */

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

public class DisplayNotification extends Activity {
    private String id1 = "test_channel_01";
    private String id2 = "test_channel_02";
    private String id3 = "test_channel_03";
    static int NotID = 1;
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //---get the notification ID for the notification; 
        // passed in by the MainActivity---
        int notifID = getIntent().getExtras().getInt("NotifID");
        String title = getIntent().getExtras().getString("NotiTitle");
        String text = getIntent().getExtras().getString("NotiText");

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

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

        //---PendingIntent to launch activity if the user selects 
        // the notification---
        Intent notificationIntent = new Intent(getApplicationContext(), ReceiveActivity.class);
        notificationIntent.putExtra(MoneyBookConstant.NOTIFICATION_RECEIVE, text);
        PendingIntent contentIntent = PendingIntent.getActivity(this, notifID, notificationIntent, 0);

        //create the notification
        Notification notif = new NotificationCompat.Builder(getApplicationContext(), id3)
                .setSmallIcon(R.drawable.ic_baseline_credit_card_24px)
                .setWhen(System.currentTimeMillis())  //When the event occurred, now, since noti are stored by time.
                .setContentTitle(title)   //Title message top row.
                .setContentText(text)  //message when looking at the notification, second row
                //the following 2 lines cause it to show up as popup message at the top in android 5 systems.
                .setPriority(Notification.PRIORITY_MAX)  //could also be PRIORITY_HIGH.  needed for LOLLIPOP, M and N.  But not Oreo
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})  //for the heads/pop up must have sound or vibrate
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(contentIntent)  //what activity to open.
                .setAutoCancel(true)   //allow auto cancel when pressed.
                .setChannelId(id3)
                .build();  //finally build and return a Notification.

        //Show the notification
        nm.notify(notifID++, notif);
        //---destroy the activity---
        finish();
    }


}
