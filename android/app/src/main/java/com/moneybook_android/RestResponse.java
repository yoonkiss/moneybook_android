package com.moneybook_android;

public class RestResponse {
    private String status;
    private String code;
    private String message;
    private Object data;


    public String getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }
}
