package com.example.demo.jenkins.dto;

import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@RequiredArgsConstructor
public class JenkinsStatusDto {
    private final Status status;
    private final String version;

    public static enum Status {
        OK, FAIL
    }

}
