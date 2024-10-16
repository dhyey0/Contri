package com.example.contri;

public class Friend {
    private String id;
    private String email;
    private Long netBalance;

    public Friend() {
        // Default constructor required for calls to DataSnapshot.getValue(Friend.class)
    }

    public Friend(String id, String email, Long netBalance) {
        this.id = id;
        this.email = email;
        this.netBalance = netBalance;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getNetBalance() {
        return netBalance;
    }

    public void setNetBalance(Long netBalance) {
        this.netBalance = netBalance;
    }
}
