package com.dbeast.cricket.dto;

public class WalletResponse {

    private final Long id;
    private final Double balance;

    public WalletResponse(Long id, Double balance) {
        this.id = id;
        this.balance = balance;
    }

    public Long getId() {
        return id;
    }

    public Double getBalance() {
        return balance;
    }
}
