package com.moneybook_android;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Telephony;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

public class MoneyBookModule extends ReactContextBaseJavaModule implements LifecycleEventListener {


    private static final int SMS_PERMISSION_CODE = 0;
    private boolean isReceiverRegistered = false;
    private BroadcastReceiver mReceiver;

    public MoneyBookModule(ReactApplicationContext reactContext) {
        super(reactContext);
        getReactApplicationContext().addLifecycleEventListener(this);

        mReceiver = new SmsReceiver(getReactApplicationContext());

    }

    @Override
    public String getName() {
        return "MoneyBook";
    }

    @ReactMethod
    public void getDeviceName(Callback cb) {
        Log.i(MainApplication.TAG, "called");

        try {
            cb.invoke(null, android.os.Build.MODEL);
        } catch (Exception e) {
            cb.invoke(e.toString(), null);
        }
    }

    @ReactMethod
    public void getSmsMessage(Callback cb) {
        Log.i(MainApplication.TAG, "getSmsMessage called");


        try {
            cb.invoke(null, "Not Impled..");
        } catch (Exception e) {
            cb.invoke(e.toString(), null);
        }
    }

    /**
     * Runtime permission shenanigans
     */
    private boolean hasReadSmsPermission() {
        if (getCurrentActivity() == null) {

            Log.e(MainApplication.TAG, "Failed to get activity");

            return false;
        }
        return ContextCompat.checkSelfPermission(getCurrentActivity(),
                Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getCurrentActivity(),
                        Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    private void registerReceiverIfNecessary(BroadcastReceiver receiver) {

        Log.i(MainApplication.TAG, "called registerReceiverIfNecessary:::" + hasReadSmsPermission());

        if (!hasReadSmsPermission()) {
            requestReadAndSendSmsPermission();
        }

        Log.i(MainApplication.TAG, "called registerReceiverIfNecessary22:::" + hasReadSmsPermission());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && getCurrentActivity() != null) {
            getCurrentActivity().registerReceiver(
                    receiver,
                    new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)
            );
            isReceiverRegistered = true;
            return;
        }

        if (getCurrentActivity() != null) {
            getCurrentActivity().registerReceiver(
                    receiver,
                    new IntentFilter("android.provider.Telephony.SMS_RECEIVED")
            );
            isReceiverRegistered = true;
        }
    }

    private void unregisterReceiver(BroadcastReceiver receiver) {
        if (isReceiverRegistered && getCurrentActivity() != null) {
            getCurrentActivity().unregisterReceiver(receiver);
            isReceiverRegistered = false;
            Log.i(MainApplication.TAG, "Removed BroadcastReceiver");
        }
    }

    /**
     * Optional informative alert dialog to explain the user why the app needs the Read/Send SMS permission
     */
    private void showRequestPermissionsInfoAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getReactApplicationContext());
        builder.setTitle("Requesting SMS permission");
        builder.setMessage("The app will now request your permission to send and read SMS related services.");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                requestReadAndSendSmsPermission();
            }
        });
        builder.show();
    }

    private void requestReadAndSendSmsPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(getCurrentActivity(), Manifest.permission.READ_SMS)) {
            return;
        }
        ActivityCompat.requestPermissions(getCurrentActivity(), new String[]{Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS},
                SMS_PERMISSION_CODE);
    }

    @Override
    public void onHostResume() {
        Log.i(MainApplication.TAG, "onHostResume");

        if (!hasReadSmsPermission()) {
            requestReadAndSendSmsPermission();
        }
    }

    @Override
    public void onHostPause() {
        Log.i(MainApplication.TAG, "onHostPause");

    }

    @Override
    public void onHostDestroy() {

        Log.i(MainApplication.TAG, "onHostDestroy");
    }
}
