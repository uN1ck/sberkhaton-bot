package com.example.demo.stash.exceptions;

public class StashResponseParsingException extends RuntimeException {
    public StashResponseParsingException(String err) {
        super(err);
    }
}
