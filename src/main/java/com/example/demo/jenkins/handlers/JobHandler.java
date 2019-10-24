package com.example.demo.jenkins.handlers;

import com.example.demo.jenkins.provider.JenkinsProviderImpl;
import com.example.demo.jenkins.subscriptions.CommonEventSubscription;
import com.example.demo.jenkins.subscriptions.service.SubscriptionService;
import com.offbytwo.jenkins.model.BuildWithDetails;
import com.offbytwo.jenkins.model.JobWithDetails;
import im.dlg.botsdk.domain.Peer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobHandler implements Handler {
    private final JenkinsProviderImpl jenkinsProvider;
    private final SubscriptionService subscriptionService;

    @Override
    public String handle(String tail, Peer sender) {
        for (String operator : Arrays.asList(CommandList.STATUS,
                                             CommandList.UNSUB,
                                             CommandList.SUB,
                                             CommandList.START,
                                             CommandList.START_LAST,
                                             CommandList.LOG)) {
            String regex = ".*" + operator + "$";
            if (tail.matches(regex)) {
                String newTail = tail.replace(operator, "").trim();
                if (operator.equals(CommandList.STATUS)) {
                    return statusHandler(newTail);
                } else if (operator.equals(CommandList.UNSUB)) {
                    return subscribeHandler(newTail, false, sender);
                } else if (operator.equals(CommandList.SUB)) {
                    return subscribeHandler(newTail, true, sender);
                }
            }
        }
        return "NOT";
    }

    private String statusHandler(String jobName) {
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");

            BuildWithDetails lastBuild = jenkinsProvider.getJob(jobName).details().getLastBuild().details();
            String lastBuildString = "Никогда не запускался";
            if (!lastBuild.equals(BuildWithDetails.BUILD_HAS_NEVER_RUN))
                lastBuildString = String.format("Последняя `%s` [%s]",
                                                dateFormat.format(new Date(lastBuild.getTimestamp())),
                                                lastBuild.getResult());

            BuildWithDetails lastSuccessfulBuild = jenkinsProvider.getJob(jobName).details().getLastSuccessfulBuild().details();
            String lastSuccessfulBuildString = "Нет успешных сборок";
            if (!lastSuccessfulBuild.equals(BuildWithDetails.BUILD_HAS_NEVER_RUN))
                lastSuccessfulBuildString = String.format("Последняя успешная [%s]",
                                                          dateFormat.format(new Date(lastSuccessfulBuild.getTimestamp())));

            BuildWithDetails lastFailedBuild = jenkinsProvider.getJob(jobName).details().getLastFailedBuild().details();
            String lastFailedBuildString = "Нет неуспешных сборок";
            if (!lastFailedBuild.equals(BuildWithDetails.BUILD_HAS_NEVER_RUN))
                lastFailedBuildString = String.format("Последняя провальная [%s]",
                                                      dateFormat.format(new Date(lastFailedBuild.getTimestamp())));
            return String.format("JOB `%s`\n%s\n%s\n%s",
                                 jobName,
                                 lastBuildString,
                                 lastSuccessfulBuildString,
                                 lastFailedBuildString);
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
