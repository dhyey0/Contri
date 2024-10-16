package com.example.contri;

public class ExpenseDetail {
    public String payerEmail;
    public String date;
    public long amount;

    public ExpenseDetail(String payerEmail, String date, long amount) {
        this.payerEmail = payerEmail;
        this.date = date;
        this.amount = amount;
    }
}
