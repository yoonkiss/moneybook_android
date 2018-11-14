package com.moneybook_android;

public class MoneyBookConstant {
    public static final String TAG = "MoneyBook";
    public static final String INTENT_PARSING_SMS = "com.moneybook.parsing_sms";
    public static final String REST_HOST = "https://enigmatic-waters-28500.herokuapp.com";//"http://192.168.219.119:8080"; //

    public static int cacheSize = 1024 * 1024 * 20; //20 Mb

    public static int CONNECTION_TIMEOUT = 20; // sec
    public static int READ_TIMEOUT = 30; // sec
    public static int WRITE_TIMEOUT = 30; // sec

    public static String NOTIFICATION_RECEIVE = "notification_receive";
    public static String NOTIFICATION_ALARM = "notification_alarm";


}
