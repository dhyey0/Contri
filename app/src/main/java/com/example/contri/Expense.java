package com.example.contri;

public class Expense {
    private String id;
    private String title;
    private long amount;
    private String paidBy;
    private String sharedWith;
    private String time;

    public Expense() {
        // Default constructor for Firebase
    }

    public Expense(String id, String title, long amount, String paidBy, String sharedWith, String time) {
        this.id = id;
        this.title = title;
        this.amount = amount;
        this.paidBy = paidBy;
        this.sharedWith = sharedWith;
        this.time = time;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public long getAmount() {
        return amount;
    }

    public String getPaidBy() {
        return paidBy;
    }

    public String getSharedWith() {
        return sharedWith;
    }

    public String getTime() {
        return time;
    }
}
