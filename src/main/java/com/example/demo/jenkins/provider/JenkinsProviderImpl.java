package com.example.demo.jenkins.provider;

import com.example.demo.jenkins.JenkinsProvider;
import com.example.demo.jenkins.dto.JenkinsStatusDto;
import com.example.demo.jenkins.exceptions.JenkinsException;
import com.google.common.base.Optional;
import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.FolderJob;
import com.offbytwo.jenkins.model.Job;
import com.offbytwo.jenkins.model.JobWithDetails;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class JenkinsProviderImpl implements JenkinsProvider {
    public static final String NO_CRITERIA = "!#NO_CRITERIA!";
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
    public List<String> getAllJobNames() {
        return getFilteredJobs(NO_CRITERIA);
    }

    @Override
    public List<String> getFilteredJobs(String criteria) {
        List<String> result = new ArrayList<>();
        try {
            for (Job job : jenkinsServer.getJobs().values()) {
                result.addAll(getAllJobNamesByFolder(job, criteria, 0));
            }
        } catch (Exception e) {
            throw new JenkinsException("Ошибка при получении списка всех имен джобов с критерием " + criteria, e);
        }
        return result;
    }


    private List<String> getAllJobNamesByFolder(Job rootJob, String criteria, int tabs) {
        try {
            JobWithDetails rootJobWithDetails = rootJob.details();
            //TODO: Ломать рекурсию, когда ушли слишком далеко?
            Optional<FolderJob> folderJob = jenkinsServer.getFolderJob(rootJobWithDetails);
            List<String> currentResult = new ArrayList<>();
            if (matchJob(rootJobWithDetails, criteria)) {
                currentResult.add(StringUtils.repeat("  ", tabs)
                                          + "- "
                                          + getNameForJob(rootJobWithDetails, folderJob.isPresent()));
            }

            if (folderJob != null && folderJob.isPresent()) {
                int newTabs = tabs + 2;
                for (Job job : folderJob.get().getJobs().values()) {
                    currentResult.addAll(getAllJobNamesByFolder(job, criteria, newTabs));
                }
            }
            return currentResult;
        } catch (Exception e) {
            throw new JenkinsException("Error while accessing all jobs", e);
        }
    }

    private boolean matchJob(JobWithDetails jobWithDetails, String criteria) {
        if (!criteria.equals(NO_CRITERIA)) {
            String regex = "^.*" + criteria + ".*$";
            return jobWithDetails.getName().matches(regex) ||
                    jobWithDetails.getFullName().matches(regex) ||
                    jobWithDetails.getDisplayName().matches(regex);
        } else {
            return true;
        }
    }

    private String getNameForJob(JobWithDetails job, boolean isFolder) {
        if (isFolder) {
            return String.format("[F] %s Di:%s Fn:%s", job.getName(), job.getDisplayName(), job.getFullName());
        }
        return String.format("%s Di:%s Fn:%s", job.getName(), job.getDisplayName(), job.getFullName());
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
    public JenkinsStatusDto getStatus() {
        return jenkinsServer.isRunning() ?
                new JenkinsStatusDto(JenkinsStatusDto.Status.OK, jenkinsServer.getVersion().getLiteralVersion()) :
                new JenkinsStatusDto(JenkinsStatusDto.Status.FAIL, jenkinsServer.getVersion().getLiteralVersion());
    }
}
