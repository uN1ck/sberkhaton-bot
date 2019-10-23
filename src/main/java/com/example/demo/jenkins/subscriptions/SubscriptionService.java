package com.example.demo.jenkins.subscriptions;

public interface SubscriptionService<T> {
    void subscribe(T userKey, Subscription subscription);

    void unsubscribe(T userKey, Subscription subscription);

    void getSubscriptions(T userKey);
}
