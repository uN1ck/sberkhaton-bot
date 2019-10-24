package com.example.demo.jenkins.provider;

import com.example.demo.jenkins.JenkinsProvider;
import com.example.demo.jenkins.exceptions.JenkinsException;
import com.google.common.base.Optional;
import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.FolderJob;
import com.offbytwo.jenkins.model.Job;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class JenkinsProviderImpl implements JenkinsProvider {
    private JenkinsServer jenkinsServer;

    @PostConstruct
    private void init() {
        try {
            //TODO: Скрыть креды?
            jenkinsServer = new JenkinsServer(new URI("http://localhost:8080"), "admin", "passme");

        } catch (URISyntaxException e) {
            e.printStackTrace();
            log.error("Cant do jenkins", e);
        }
        log.info("Jenkins done");
    }

    @Override
    public String getAllJobNames() {
        String result = "";
        try {
            for (Job job : jenkinsServer.getJobs().values()) {
                getAllJobNamesByFolder(job, result, 0);
            }
        } catch (IOException e) {
            throw new JenkinsException("Error while accessing all jobs", e);
        }
        return result;
    }


    private void getAllJobNamesByFolder(Job rootJob, String result, int tabs) {
        try {
            result += StringUtils.repeat("  ", tabs) + rootJob.getName();
            Optional<FolderJob> folderJob = jenkinsServer.getFolderJob(rootJob);
            if (folderJob.isPresent()) {
                int newTabs = tabs + 2;
                for (Job job : folderJob.get().getJobs().values()) {
                    getAllJobNamesByFolder(job, result, newTabs);
                }
            }
        } catch (IOException e) {
            throw new JenkinsException("Error while accessing all jobs", e);
        }
    }

    @Override
    public List<Job> getAllJobs() {
        try {
            return jenkinsServer.getJobs().values().stream()
                                .map(this::getAllJobRecursive)
                                .flatMap(Collection::stream)
                                .collect(Collectors.toList());
        } catch (IOException e) {
            throw new JenkinsException("Error while accessing all jobs", e);
        }
    }

    private List<Job> getAllJobRecursive(Job rootJob) {
        try {
            Optional<FolderJob> folderJob = jenkinsServer.getFolderJob(rootJob);
            if (folderJob.isPresent()) {
                return folderJob.get().getJobs().values().stream()
                                .map(this::getAllJobRecursive)
                                .flatMap(Collection::stream)
                                .collect(Collectors.toList());
            } else {
                return Collections.singletonList(rootJob);
            }
        } catch (Exception e) {
            log.error("ошибка при получении дочерних джобов у " + rootJob);
            return Collections.emptyList();
        }
    }

    @Override
    public List<Job> getFilteredJobs(String criteria) {
        try {
            return getAllJobs()
                    .stream()
                    .filter(job -> job.getName().matches(".*" + criteria + ".*"))
                    .collect(Collectors.toList());
        } catch (Exception e) {
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
    public JenkinsStatus getStatus() {
        return jenkinsServer.isRunning() ? new JenkinsStatus(JenkinsStatus.Status.OK) :
                new JenkinsStatus(JenkinsStatus.Status.FAIL);
    }
}
