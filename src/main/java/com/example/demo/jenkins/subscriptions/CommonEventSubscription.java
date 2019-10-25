package com.example.demo.jenkins.subscriptions;

import com.example.demo.BotProvider;
import com.example.demo.jenkins.dto.CommonEventDto;
import com.example.demo.jenkins.exceptions.SubscriptionException;
import com.offbytwo.jenkins.model.BuildWithDetails;
import com.offbytwo.jenkins.model.Job;
import com.offbytwo.jenkins.model.JobWithDetails;
import im.dlg.botsdk.domain.Peer;
import lombok.extern.slf4j.Slf4j;

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
            int newLastBuild = jobWithDetails.getLastBuild().getNumber();
            if (lastBuildId != jobWithDetails.getLastBuild().getNumber()) {
                lastBuildId = newLastBuild;
                CommonEventDto commonEventDto = CommonEventDto.builder()
                                                              .buildResult(buildWithDetails.getResult().name())
                                                              .buildNumber(buildWithDetails.getNumber())
                                                              .jobFullName(jobFullName(jobWithDetails))
                                                              .build();
                botProvider.getBot()
                           .messaging()
                           .sendText(owner, commonEventDto.toString());

            }

        } catch (Exception e) {
            log.error("Ошибка при попытке получить информацию по подписанной job");
            throw new SubscriptionException("Ошибка при попытке обработать подписку", e);
        }
    }

    private String jobFullName(JobWithDetails jobWithDetails) {
        return String.format("%s (%s,%s)", jobWithDetails.getName(),
                             jobWithDetails.getDisplayName(),
                             jobWithDetails.getFullName());
    }
}
