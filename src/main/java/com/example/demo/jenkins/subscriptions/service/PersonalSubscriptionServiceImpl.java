package com.example.demo.jenkins.subscriptions.service;

import com.example.demo.BotProvider;
import com.example.demo.jenkins.JenkinsProvider;
import com.example.demo.jenkins.subscriptions.Subscription;
import com.offbytwo.jenkins.model.Job;
import im.dlg.botsdk.domain.Peer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class PersonalSubscriptionServiceImpl implements PersonalSubscriptionService {
    private final Peer owner;
    private final BotProvider botProvider;
    private final JenkinsProvider jenkinsProvider;
    @Getter
    private List<Subscription> subscriptions = new ArrayList<>();

    @Override
    public void refreshSubscriptions() {
        subscriptions.forEach(subscription -> {
            Job subscribedJob = jenkinsProvider.getJob(subscription.getJobIdentifier());
            subscription.onEvent(subscribedJob, botProvider, owner);
        });
    }

    @Override
    public void subscribe(Subscription subscription) {
        if (subscriptions.stream().noneMatch(sub -> sub.getJobIdentifier().equals(subscription.getJobIdentifier())))
            subscriptions.add(subscription);
    }

    @Override
    public void unsubscribe(String identifier) {
        subscriptions = subscriptions.stream()
                                     .filter(subscription -> !subscription.getJobIdentifier().equals(identifier))
                                     .collect(Collectors.toList());
    }

}
