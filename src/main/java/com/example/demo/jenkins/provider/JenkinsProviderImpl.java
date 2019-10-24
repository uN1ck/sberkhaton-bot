package com.example.demo.jenkins.provider;

import com.example.demo.jenkins.JenkinsProvider;
import com.example.demo.jenkins.dto.JenkinsStatusDto;
import com.example.demo.jenkins.dto.JobDto;
import com.example.demo.jenkins.exceptions.JenkinsException;
import com.google.common.base.Optional;
import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.FolderJob;
import com.offbytwo.jenkins.model.Job;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class JenkinsProviderImpl implements JenkinsProvider {
    private JenkinsServer jenkinsServer;

    @PostConstruct
    private void init() {
        try {
            //TODO: Скрыть креды?
            jenkinsServer = new JenkinsServer(new URI("http://172.30.18.91:8080"),
                                              "admin",
                                              "11629d94574d44abbadc56d31b2104304f");

        } catch (URISyntaxException e) {
            e.printStackTrace();
            log.error("Cant do jenkins", e);
        }
        log.info("Jenkins done");
    }


    public List<JobDto> getJobsOnLevel(String jobIdentifier, String filter) {
        try {
            Optional<FolderJob> folderJob = jenkinsServer.getFolderJob(jenkinsServer.getJob(jobIdentifier));
            if (folderJob != null && folderJob.isPresent()) {
                return folderJob.get().getJobs()
                                .values()
                                .stream()
                                .map(this::jobMapper)
                                .filter(Objects::nonNull)
                                .filter(jobDto -> jobDto.getName()
                                                        .toLowerCase()
                                                        .contains(filter.toLowerCase()))
                                .collect(Collectors.toList());
            } else {
                throw new JenkinsException("Не удалось получить FolderJob от " + jobIdentifier);
            }
        } catch (Exception e) {
            log.error("Не удалось обработать запрос", e);
            return Collections.emptyList();
        }
    }


    public List<JobDto> getJobsOnLevel(String filter) {
        try {
            return jenkinsServer.getJobs()
                                .values()
                                .stream()
                                .map(this::jobMapper)
                                .filter(Objects::nonNull)
                                .filter(jobDto -> jobDto.getName()
                                                        .toLowerCase()
                                                        .contains(filter.toLowerCase()))
                                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Не удалось");
            return Collections.emptyList();
        }
    }

    private JobDto jobMapper(Job job) {
        try {
            Optional<FolderJob> folderJob = jenkinsServer.getFolderJob(job);
            return JobDto.fromJob(job.details(), folderJob.isPresent() ?
                    JobDto.JobType.FOLDER :
                    JobDto.JobType.JOB);
        } catch (IOException e) {
            log.error("Не удалось собрать DTO от job " + job, e);
            return null;
        }
    }


    public Job getJob(String jobIdentifier) {
        try {
            return jenkinsServer.getJob(jobIdentifier);
        } catch (IOException e) {
            throw new JenkinsException("Error while accessing named job", e);
        }
    }


    public JenkinsStatusDto getStatus() {
        return jenkinsServer.isRunning() ?
                new JenkinsStatusDto(JenkinsStatusDto.Status.OK, jenkinsServer.getVersion().getLiteralVersion()) :
                new JenkinsStatusDto(JenkinsStatusDto.Status.FAIL, jenkinsServer.getVersion().getLiteralVersion());
    }
}
