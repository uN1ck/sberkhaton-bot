package com.example.demo.jenkins;

import com.example.demo.jenkins.dto.JenkinsStatusDto;
import com.offbytwo.jenkins.model.Job;

import java.util.List;

public interface JenkinsProvider {

    List<String> getAllJobNames();

    List<String> getFilteredJobs(String criteria);

    Job getJob(String jobIdentifier);

    JenkinsStatusDto getStatus();
}
