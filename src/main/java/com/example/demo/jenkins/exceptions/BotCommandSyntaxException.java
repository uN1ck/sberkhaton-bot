package com.example.demo.jenkins.exceptions;

public class BotCommandSyntaxException extends RuntimeException {
    public BotCommandSyntaxException(String message) {
        super(message);
    }

    public BotCommandSyntaxException(String message, Throwable cause) {
        super(message, cause);
    }
}
