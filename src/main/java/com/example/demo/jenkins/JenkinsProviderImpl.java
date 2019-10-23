package com.example.demo.jenkins;

import com.example.demo.jenkins.exceptions.JenkinsException;
import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.Job;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class JenkinsProviderImpl implements JenkinsProvider {
    private JenkinsServer jenkinsServer;

    @PostConstruct
    private void init() {
        try {
            jenkinsServer = new JenkinsServer(new URI("http://localhost:8080"), "admin", "passme");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            log.error("Cant do jenkins", e);
        }
        log.info("Jenkins done");
    }


    @Override
    public List<Job> getAllJobs() {
        try {
            return new ArrayList<>(jenkinsServer.getJobs().values());
        } catch (IOException e) {
            throw new JenkinsException("Error while accessing all jobs", e);
        }
    }

    @Override
    public List<Job> getFilteredJobs(String criteria) {
        try {
            return jenkinsServer.getJobs().entrySet().stream()
                                .filter(stringJobEntry -> stringJobEntry.getKey().matches(criteria))
                                .map(Map.Entry::getValue)
                                .collect(Collectors.toList());
        } catch (IOException e) {
            throw new JenkinsException("Error while accessing filtered jobs", e);
        }
    }

    @Override
    public Job getJob(String jobIdentifier) {
        try {
            return jenkinsServer.getJob(jobIdentifier);
        } catch (IOException e) {
            throw new JenkinsException("Error while accessing named job", e);
        }
    }

    @Override
    public boolean isAlive() {
        return jenkinsServer.isRunning();
    }
}
