package com.example.demo.jenkins.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class CommonEventDto {
    private final boolean needsToBeSent;
    private final String buildResult;
    private final int buildNumber;
    private final String jobFullName;

    public static CommonEventDto notSendable() {
        return CommonEventDto.builder().needsToBeSent(false).build();
    }
}
