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

public class SmsReceiver extends BroadcastReceiver {
    private ReactApplicationContext mContext;

    public SmsReceiver() {
        super();
    }

    public SmsReceiver(ReactApplicationContext context) {
        mContext = context;
    }


    private void receiveMessage(final Context context, final SmsMessage message) {
        Log.i(MoneyBookConstant.TAG, "called receiveMessage:" + message.getDisplayMessageBody());

        Intent smsService = new Intent(context, RestService.class);
        smsService.setAction(MoneyBookConstant.INTENT_PARSING_SMS);
        smsService.putExtra("SMS_PHOME", message.getDisplayOriginatingAddress());
        smsService.putExtra("SMS_MESSAGE", message.getDisplayMessageBody());

        context.startService(smsService);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
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
            Log.e(MoneyBookConstant.TAG, e.getMessage());
        }
    }


}