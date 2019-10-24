package com.example.demo.jenkins.dto;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JenkinsStatusDto {
    private final Status status;
    private final String version;

    @Override
    public String toString() {
        return String.format("Состояние: %s Версия: %s", status, version);
    }

    public enum Status {
        OK, FAIL
    }
}
