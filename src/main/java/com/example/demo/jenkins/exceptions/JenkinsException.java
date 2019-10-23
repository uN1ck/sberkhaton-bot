package com.example.demo.jenkins.exceptions;

public class JenkinsException extends RuntimeException{
    public JenkinsException(String message) {
        super(message);
    }

    public JenkinsException(String message, Throwable cause) {
        super(message, cause);
    }
}
