package com.example.demo.jenkins.subscriptions.service;

import com.example.demo.jenkins.subscriptions.Subscription;

import java.util.List;

public interface PersonalSubscriptionService {
    void refreshSubscriptions();

    void subscribe(Subscription subscription);

    void unsubscribe(String identifier);

    List<Subscription> getSubscriptions();
}
