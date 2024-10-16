package com.example.contri;

public class Expense {
    private String id;
    private double amount;
    private boolean settled;

    public Expense() {
        // Default constructor required for calls to DataSnapshot.getValue(Expense.class)
    }

    public Expense(String id, double amount, boolean settled) {
        this.id = id;
        this.amount = amount;
        this.settled = settled;
    }

    public String getId() {
        return id;
    }

    public double getAmount() {
        return amount;
    }

    public boolean isSettled() {
        return settled;
    }

    public void setSettled(boolean settled) {
        this.settled = settled;
    }
}
