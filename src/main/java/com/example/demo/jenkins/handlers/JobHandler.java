package com.example.demo.jenkins.handlers;

import com.example.demo.jenkins.JenkinsProvider;
import com.example.demo.jenkins.subscriptions.CommonEventSubscription;
import com.example.demo.jenkins.subscriptions.service.SubscriptionService;
import com.offbytwo.jenkins.model.BuildWithDetails;
import com.offbytwo.jenkins.model.JobWithDetails;
import im.dlg.botsdk.domain.Peer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobHandler implements Handler {
    private final JenkinsProvider jenkinsProvider;
    private final SubscriptionService subscriptionService;

    @Override
    public String handle(String tail, Peer sender) {
        for (String operator : Arrays.asList(JenkinsHandler.STATUS,
                                             JenkinsHandler.UNSUB,
                                             JenkinsHandler.SUB,
                                             JenkinsHandler.START,
                                             JenkinsHandler.START_LAST,
                                             JenkinsHandler.LOG)) {
            String regex = ".*" + operator + "$";
            if (tail.matches(regex)) {
                String newTail = tail.replace(operator, "").trim();
                if (operator.equals(JenkinsHandler.STATUS)) {
                    return statusHandler(newTail);
                } else if (operator.equals(JenkinsHandler.UNSUB)) {
                    return subscribeHandler(newTail, false, sender);
                } else if (operator.equals(JenkinsHandler.SUB)) {
                    return subscribeHandler(newTail, true, sender);
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

    private String subscribeHandler(String jobName, boolean subscribe, Peer sender) {
        try {
            if (subscribe) {
                JobWithDetails job = jenkinsProvider.getJob(jobName).details();
                subscriptionService.subscribe(sender, new CommonEventSubscription(job));
                return "Подписка оформлена";
            } else {
                subscriptionService.unsubscribe(sender, jobName);
                return "Подписка прекрщена";
            }

        } catch (Exception e) {
            return "Не удалось изменить состояние подписки :(";
        }
    }


}
