package com.example.demo.jenkins.handlers;

import com.example.demo.jenkins.JenkinsProvider;
import com.offbytwo.jenkins.model.Job;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobListScanner {
    private final JenkinsProvider jenkinsProvider;

    public String getList(String criteria) {
        String result = "";
        if (criteria.length() > 0) {
            jenkinsProvider.getFilteredJobs(criteria)
                           .stream()
                           .map(Job::getName)
                           .collect(Collectors.joining("\n- "));
        } else {
            jenkinsProvider.getAllJobs()
                           .stream()
                           .map(Job::getName)
                           .collect(Collectors.joining("\n- "));
        }
        return "";
    }

}
