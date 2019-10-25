package com.example.demo.jenkins.subscriptions;

import com.example.demo.BotProvider;
import com.example.demo.jenkins.exceptions.SubscriptionException;
import com.offbytwo.jenkins.model.BuildWithDetails;
import com.offbytwo.jenkins.model.Job;
import com.offbytwo.jenkins.model.JobWithDetails;
import im.dlg.botsdk.domain.Peer;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Map;

@Slf4j
public class CommonEventSubscription extends Subscription {
    private int lastBuildId;

    public CommonEventSubscription(JobWithDetails lastJobState) {
        super(lastJobState.getFullName());
        lastBuildId = lastJobState.getLastBuild().getNumber();
    }

    @Override
    public void onEvent(Job job, BotProvider botProvider, Peer owner) {
        try {
            JobWithDetails jobWithDetails = job.details();
            BuildWithDetails buildWithDetails = jobWithDetails.getLastBuild().details();
            int newLastBuild = buildWithDetails.getNumber();

            if (lastBuildId != buildWithDetails.getNumber()) {
                lastBuildId = newLastBuild;
                String response = formatMessage(jobWithDetails, buildWithDetails);

                botProvider.getBot()
                           .messaging()
                           .sendText(owner, response);

            }

        } catch (Exception e) {
            log.error("Ошибка при попытке получить информацию по подписанной job");
            throw new SubscriptionException("Ошибка при попытке обработать подписку", e);
        }
    }

    private String formatMessage(JobWithDetails jobWithDetails, BuildWithDetails buildWithDetails) {
        String fullName = jobFullName(jobWithDetails);
        String buildResult = buildWithDetails.getResult().name();
        Integer buildNumber = buildWithDetails.getNumber();
        Map<String, String> params = buildWithDetails.getParameters();
        String result = String.format("Job `%s` (%d)\nСтатус: %s\nПараметры: %s", fullName, buildNumber, buildResult,
                                      Arrays.toString(params.entrySet().toArray()));
        return result;

    }

    private String jobFullName(JobWithDetails jobWithDetails) {
        return String.format("%s (%s,%s)", jobWithDetails.getName(),
                             jobWithDetails.getDisplayName(),
                             jobWithDetails.getFullName());
    }
}
