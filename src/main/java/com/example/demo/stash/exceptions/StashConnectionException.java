package com.example.demo.stash.exceptions;

public class StashConnectionException extends RuntimeException {
    public StashConnectionException(String err) {
        super(err);
    }
}
