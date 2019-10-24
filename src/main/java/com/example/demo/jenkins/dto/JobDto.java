package com.example.demo.jenkins.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JobDto {
    private final String name;
    private final String fullName;
    private final String displayName;
    private final JobType jobType;
    private final long level;

    public static enum JobType {
        FOLDER, JOB
    }
}
