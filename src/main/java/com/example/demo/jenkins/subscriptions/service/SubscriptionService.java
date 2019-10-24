package com.example.demo.jenkins.subscriptions.service;

import com.example.demo.jenkins.subscriptions.Subscription;
import im.dlg.botsdk.domain.Peer;

public interface SubscriptionService {
    void subscribe(Peer userKey, Subscription subscription);

    void unsubscribe(Peer userKey, String jobIdentifier);

    void getSubscriptions(Peer userKey);
}
