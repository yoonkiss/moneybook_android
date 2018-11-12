package com.moneybook_android;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import static android.content.Context.MODE_PRIVATE;

public class CardMessage {
    private final String ADDRESS_SAMSUNG = "15888900";
    private final String ADDRESS_HANA = "18001111";
    private final String ADDRESS_WOORI = "15889955";

    String address;
    String body;
    Record record;
    Context context;

    public CardMessage(Context context, String address, String body) {
        this.address = address;
        this.body = body;
        this.context = context;

        parsingMessage();
    }

    public Record toRecord() {
        return record;
    }

    private void parsingMessage() {
        SharedPreferences sp = context.getSharedPreferences("shared", MODE_PRIVATE);
        String strContact = sp.getString("creditcard", "");

        Log.i(MainApplication.TAG, "card preference:" + strContact);

        Type listType = new TypeToken<ArrayList<CreditCard>>() {
        }.getType();
        Gson gson = new GsonBuilder().create();
        List<CreditCard> cards = gson.fromJson(strContact, listType);

        Log.i(MainApplication.TAG, "card preference cnt:" + cards.size());
        for (CreditCard card : cards) {
            if (body.indexOf("삼성") != -1) {
                if (ADDRESS_SAMSUNG.equals(address) && ADDRESS_SAMSUNG.equals(card.getPhone())) {
                    createSamsungRecord(card);
                }
            } else if (body.indexOf("하나") != -1) {
                if (ADDRESS_HANA.equals(address) && ADDRESS_HANA.equals(card.getPhone())) {
                    createHanaRecord(card);
                }
            } else if (body.indexOf("우리(") != -1) {
                if (ADDRESS_WOORI.equals(address) && ADDRESS_WOORI.equals(card.getPhone())) {
                    createWooriRecord(card);
                }
            } else {
                Log.i(MainApplication.TAG, "Unknown message:(" + address + ")" + body);
            }
        }
    }


    private void createSamsungRecord(CreditCard card) {
        StringTokenizer st = new StringTokenizer(body, "\n");
        String tmpStr = null;
        tmpStr = st.nextToken();
        tmpStr = st.nextToken();

        int cardId = 0;
        int amount = 0;
        String description = null;
        String comment = body;
        String recordAt = null;

        int pos = tmpStr.indexOf(card.getNumber());
        if (pos == -1) {
            Log.i(MainApplication.TAG, "Skipped to parse samsung card: " + tmpStr);
            return;
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

        record = new Record();
        record.setUserId("yoonki");
        record.setAccount(1);
        record.setDescription(description);
        record.setCardId(cardId);
        record.setType(1);
        record.setAmount(amount);
        record.setCategoryId(601);
        record.setComments(body);
        record.setRecordAt(recordAt);
        record.setDivided(1);
    }

    private void createHanaRecord(CreditCard card) {
        StringTokenizer st = new StringTokenizer(body, "\n");
        String tmpStr = null;
        tmpStr = st.nextToken();
        String parsingLineStr = st.nextToken();

        StringTokenizer st2 = new StringTokenizer(parsingLineStr, " ");

        tmpStr = st2.nextToken();
        int pos = tmpStr.indexOf("[하나카드]");

        int cardId = 0;
        int amount = 0;
        String description = "";
        String comment = body;
        String recordAt = "";


        if (pos != -1) { // only for U+
            // [하나카드]박*기님 LG U+ 통신요금 자동(6*3*) 20,900원 정상처리
            String[] strArr = parsingLineStr.split(" ");

            if (!card.getName().contains("U+")) {
                Log.i(MainApplication.TAG, "Skipped to parse hana card not contained U+: " + card.getName());
                return;
            }
            cardId = card.getId();


            if (strArr.length < 4) {
                return;
            }
            if (!"[하나카드]박*기님".equals(strArr[0]) || !"정상처리".equals(strArr[strArr.length - 1])) {
                Log.i(MainApplication.TAG, "Skipped to parse hana card: " + strArr[0] + "///" + strArr[strArr.length - 1]);
                return;
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
                Log.i(MainApplication.TAG, "Skiped to parse hana card: " + tmpStr);
                return;
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

        record = new Record();
        record.setUserId("yoonki");
        record.setAccount(1);
        record.setDescription(description);
        record.setCardId(cardId);
        record.setType(1);
        record.setAmount(amount);
        record.setCategoryId(601);
        record.setComments(body);
        record.setRecordAt(recordAt);
        record.setDivided(1);
    }

    private void createWooriRecord(CreditCard card) {
        StringTokenizer st = new StringTokenizer(body, "\n");

        if (st.countTokens() != 7) {
            Log.i(MainApplication.TAG, "Skipped to parse woori card: token size is not 7: " + body);
            return;
        }

        String tmpStr = null;
        tmpStr = st.nextToken(); // [Web발신]
        tmpStr = st.nextToken(); // 우리(1097)승인

        int cardId = 0;
        int amount = 0;
        String description = "";
        String comment = body;
        String recordAt = "";

        int pos = tmpStr.indexOf(card.getNumber());
        if (pos == -1) {
            Log.i(MainApplication.TAG, "Skipped to parse woori card: " + tmpStr);
            return;
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

        record = new Record();
        record.setUserId("yoonki");
        record.setAccount(1);
        record.setDescription(description);
        record.setCardId(cardId);
        record.setType(1);
        record.setAmount(amount);
        record.setCategoryId(601);
        record.setComments(body);
        record.setRecordAt(recordAt);
        record.setDivided(1);
    }

}
