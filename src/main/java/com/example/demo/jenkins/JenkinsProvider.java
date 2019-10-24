package com.example.demo.jenkins;

import com.example.demo.jenkins.dto.JenkinsStatusDto;
import com.example.demo.jenkins.dto.JobDto;
import com.offbytwo.jenkins.model.Job;

import java.util.List;

public interface JenkinsProvider {
    List<JobDto> getJobsOnLevel(String jobIdentifier, String filter);

    List<JobDto> getJobsOnLevel(String filter);

    Job getJob(String jobIdentifier);

    JenkinsStatusDto getStatus();
}
