package com.example.demo.jenkins.handlers;

import com.example.demo.jenkins.JenkinsProvider;
import com.offbytwo.jenkins.model.BuildWithDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobHandler {
    private final JenkinsProvider jenkinsProvider;

    public String handle(String tail) {
        for (String operator : Arrays.asList(JenkinsHandler.STATUS,
                                             JenkinsHandler.SUB,
                                             JenkinsHandler.UNSUB,
                                             JenkinsHandler.START,
                                             JenkinsHandler.START_LAST,
                                             JenkinsHandler.FAV,
                                             JenkinsHandler.UNFAV,
                                             JenkinsHandler.LOG)) {
            String regex = ".*" + operator + "$";
            if (tail.matches(regex)) {
                String newTail = tail.replace(operator, "").trim();
                if (operator.equals(JenkinsHandler.STATUS)) {
                    return statusHandler(newTail);
                }
            }
        }
        return "NOT";
    }

    private String statusHandler(String jobName) {
        try {
            BuildWithDetails lastBuild = jenkinsProvider.getJob(jobName).details().getLastBuild().details();
            String lastBuildString = "Никогда не запускался";
            if (!lastBuild.equals(BuildWithDetails.BUILD_HAS_NEVER_RUN))
                lastBuildString = String.format("Last [%s] status: %s",
                                                new Date(lastBuild.getTimestamp()),
                                                lastBuild.getResult());

            BuildWithDetails lastSuccessfulBuild = jenkinsProvider.getJob(jobName).details().getLastSuccessfulBuild().details();
            String lastSuccessfulBuildString = "Нет успешных сборок";
            if (!lastSuccessfulBuild.equals(BuildWithDetails.BUILD_HAS_NEVER_RUN))
                lastSuccessfulBuildString = String.format("Successful [%s] status: %s",
                                                          new Date(lastSuccessfulBuild.getTimestamp()),
                                                          lastSuccessfulBuild.getResult());

            BuildWithDetails lastFailedBuild = jenkinsProvider.getJob(jobName).details().getLastFailedBuild().details();
            String lastFailedBuildString = "Нет неуспешных сборок";
            if (!lastFailedBuild.equals(BuildWithDetails.BUILD_HAS_NEVER_RUN))
                lastFailedBuildString = String.format("Failed [%s] status: %s",
                                                      new Date(lastFailedBuild.getTimestamp()),
                                                      lastFailedBuild.getResult());
            return String.format("%s\n%s\n%s", lastBuildString, lastSuccessfulBuildString, lastFailedBuildString);
        } catch (Exception e) {
            log.error("Не удалось получить состояние джобы", e);
            return "Неизвестно, проверьте статус Jenkins :(";

        }
    }


}
