package com.example.demo.jenkins;

import com.offbytwo.jenkins.model.Job;

import java.util.List;

public interface JenkinsProvider {

    List<Job> getAllJobs();

    List<Job> getFilteredJobs(String criteria);

    Job getJob(String jobIdentifier);

    boolean isAlive();
}
