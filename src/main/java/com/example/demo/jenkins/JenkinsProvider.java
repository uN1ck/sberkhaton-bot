package com.example.demo.jenkins;

import com.example.demo.jenkins.provider.JenkinsStatus;
import com.offbytwo.jenkins.model.Job;

import java.util.List;

public interface JenkinsProvider {

    String getAllJobNames();

    List<Job> getAllJobs();

    List<Job> getFilteredJobs(String criteria);

    Job getJob(String jobIdentifier);

    JenkinsStatus getStatus();
}
