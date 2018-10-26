package com.vinay.exception;

public class InsufficientCardBalanceException extends Exception {
    public InsufficientCardBalanceException(String message) {
        super(message);
    }
}
