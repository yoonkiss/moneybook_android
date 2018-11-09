package com.moneybook_android;

public class Record {
    private String userId;

    private int type;

    private String description;

    private int account;

    private int cardId;

    private int categoryId;

    private int amount;

    private int divided;

    private String comments;
    /*
     * @DateTimeFormat(pattern = "yyyy-mm-dd")
     * private LocalTime  recordAt;
     */
    private String recordAt;

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAccount(int account) {
        this.account = account;
    }

    public void setCardId(int cardId) {
        this.cardId = cardId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void setDivided(int divided) {
        this.divided = divided;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public void setRecordAt(String recordAt) {
        this.recordAt = recordAt;
    }

    public String getUserId() {
        return userId;
    }

    public int getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public int getAccount() {
        return account;
    }

    public int getCardId() {
        return cardId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public int getAmount() {
        return amount;
    }

    public int getDivided() {
        return divided;
    }

    public String getComments() {
        return comments;
    }

    public String getRecordAt() {
        return recordAt;
    }
}
