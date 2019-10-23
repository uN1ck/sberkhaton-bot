package com.example.demo.jenkins.subscriptions;

import java.util.List;

public interface PersonalSubscriptionService {
    void refreshSubscriptions();

    void subscribe(Subscription subscription);

    void unsubscribe(Subscription subscription);

    List<Subscription> getSubscriptions();
}
