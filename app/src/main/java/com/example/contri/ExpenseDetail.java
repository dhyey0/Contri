package com.example.contri;

public class ExpenseDetail {
    public String payerEmail;
    public String date;
    public long amount;
    public String title;

    public ExpenseDetail(String payerEmail, String date, long amount, String title) {
        this.payerEmail = payerEmail;
        this.date = date;
        this.amount = amount;
        this.title = title;
    }
}
