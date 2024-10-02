package com.example.contri;

public class Expense {
    private String user2Email;
    private double amount;
    private boolean settled;

    public Expense() {
        // Default constructor required for calls to DataSnapshot.getValue(Expense.class)
    }

    public Expense(String user2Email, double amount, boolean settled) {
        this.user2Email = user2Email;
        this.amount = amount;
        this.settled = settled;
    }

    public String getUser2Email() {
        return user2Email;
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
