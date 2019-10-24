package com.example.demo.stash.exceptions;

public class StashCommandException extends RuntimeException {
    public StashCommandException(String err) {
        super(err);
    }
}
