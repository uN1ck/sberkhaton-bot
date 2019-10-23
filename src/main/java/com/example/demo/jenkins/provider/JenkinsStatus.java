package com.example.demo.jenkins.provider;

import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@RequiredArgsConstructor
public class JenkinsStatus {
    private final Status status;

    public static enum Status {
        OK, FAIL
    }

}
