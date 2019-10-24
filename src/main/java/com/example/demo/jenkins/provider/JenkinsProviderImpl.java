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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class JenkinsProviderImpl implements JenkinsProvider {
    private JenkinsServer jenkinsServer;

    @PostConstruct
    private void init() {
        try {
            //TODO: Скрыть креды?
            jenkinsServer = new JenkinsServer(new URI("http://172.30.18.91:8080"), "admin", "5a73f64338824a409b159bf3f424cc80");

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
                result += getAllJobNamesByFolder(job, "", 0);
            }
        } catch (Exception e) {
            throw new JenkinsException("Error while accessing all jobs", e);
        }
        return result;
    }


    private String getAllJobNamesByFolder(Job rootJob, String result, int tabs) {
        try {
            //TODO: Ломать рекурсию, когда ушли слишком далеко?
            Optional<FolderJob> folderJob = jenkinsServer.getFolderJob(rootJob);
            String currentResult = result + StringUtils.repeat("  ", tabs)
                    + "- "
                    + getNameForJob(rootJob, folderJob.isPresent())
                    + "\n";

            if (folderJob != null && folderJob.isPresent()) {
                int newTabs = tabs + 2;
                for (Job job : folderJob.get().getJobs().values()) {
                    currentResult += getAllJobNamesByFolder(job, result, newTabs);
                }
            }
            return currentResult;
        } catch (Exception e) {
            throw new JenkinsException("Error while accessing all jobs", e);
        }
    }

    private String getNameForJob(Job job, boolean isFolder) {
        if (isFolder) {
            return String.format("[F] %s", job.getName());
        }
        return String.format("%s", job.getName());
    }

    @Override
    public List<Job> getAllJobs() {
        try {
            return jenkinsServer.getJobs()
                                .values()
                                .stream()
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
                return Stream.concat(Stream.of(rootJob),
                                     folderJob.get()
                                              .getJobs()
                                              .values()
                                              .stream()
                                              .map(this::getAllJobRecursive)
                                              .flatMap(Collection::stream))
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
