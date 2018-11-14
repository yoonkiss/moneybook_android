package com.moneybook_android;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;


/**
 * This is the activity that the notification calls.
 * All this does is check to see that the information is in the intent
 * normally you would have some sort of response to the notification
 * <p>
 * http://stackoverflow.com/questions/1198558/how-to-send-parameters-from-a-notification-click-to-an-activity
 * http://mobiforge.com/developing/story/displaying-status-bar-notifications-android
 * http://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html
 * http://mobiforge.com/developing/story/displaying-status-bar-notifications-android
 * http://stackoverflow.com/questions/12006724/set-a-combination-of-vibration-lights-or-sound-for-a-notification-in-android
 * http://developer.android.com/reference/android/app/Notification.html
 */

public class ReceiveActivity extends Activity {
    TextView Logger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);
        String info = "Nothing";
        Logger = findViewById(R.id.logger);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            info = extras.getString(MoneyBookConstant.NOTIFICATION_RECEIVE);
            if (info == null) {
                info = "nothing 2";
            }
        }
        Logger.setText(info);

    }
}
