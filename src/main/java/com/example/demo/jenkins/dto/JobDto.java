package com.example.demo.jenkins.dto;

import com.offbytwo.jenkins.model.JobWithDetails;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JobDto {
    private final String name;
    private final String fullName;
    private final String displayName;
    private final JobType jobType;

    public static JobDto fromJob(JobWithDetails job, JobType jobType) {
        return JobDto.builder()
                     .name(job.getName())
                     .fullName(job.getFullName())
                     .displayName(job.getDisplayName())
                     .jobType(jobType)
                     .build();
    }

    public static enum JobType {
        FOLDER, JOB
    }
}
