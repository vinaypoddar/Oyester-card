package com.vinay.model;

public class Card {
    private final Long number;
    private Double balance;

    public Card(Long number) {
        this.number = number;
        this.balance = 0.0;
    }

    public Long getNumber() {
        return number;
    }

    public Double getBalance() {
        return balance;
    }
    
    public void setBalance(Double amount) {
    	this.balance = amount;
    }
}
