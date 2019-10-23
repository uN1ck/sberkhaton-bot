package com.example.demo.jenkins.subscriptions;

import com.example.demo.jenkins.JenkinsProvider;
import com.offbytwo.jenkins.model.Job;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class PersonalSubscriptionServiceImpl implements PersonalSubscriptionService {
    @Getter
    private final List<Subscription> subscriptions = new ArrayList<>();
    private final JenkinsProvider jenkinsProvider;

    @Override
    public void refreshSubscriptions() {
        subscriptions.forEach(subscription -> {
            //Мб лучше запрашивать все джобы на момент старта апдейта?
            Job subscribedJob = jenkinsProvider.getJob(subscription.getJobIdentifier());
            subscription.getOnSubscriptionEvent().accept(subscribedJob);
        });
    }


    @Override
    public void subscribe(Subscription subscription) {
        subscriptions.add(subscription);
    }

    @Override
    public void unsubscribe(Subscription subscription) {
        subscriptions.remove(subscription);
    }

}
