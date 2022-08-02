package com.example.hateoas.model;

import org.springframework.hateoas.RepresentationModel;

public class Account extends RepresentationModel<Account> {
    private int id;
    private double balance;
    private Status status;

    public Account() {

    }

    public Account(final int id, final double balance, final Status status) {
        this.id = id;
        this.balance = balance;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(final double balance) {
        this.balance = balance;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(final Status status) {
        this.status = status;
    }
}
