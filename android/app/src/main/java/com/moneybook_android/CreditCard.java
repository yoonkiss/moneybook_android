package com.moneybook_android;

public class CreditCard {
    private int id;
    private String userId;

    private String name;
    private String number;
    private int usingType;

    private String phone;
    private int limited;

    private String startedAt;
    private String endAt;
    private String billingAt;


    public int getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getNumber() {
        return number;
    }

    public int getUsingType() {
        return usingType;
    }

    public String getStartedAt() {
        return startedAt;
    }

    public String getEndAt() {
        return endAt;
    }

    public String getBillingAt() {
        return billingAt;
    }

    public String getPhone() {
        return phone;
    }

    public int getLimited() {
        return limited;
    }
}
