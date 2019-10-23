package com.example.demo.jenkins.subscriptions;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl<T> implements SubscriptionService<T> {
    private final Map<T, PersonalSubscriptionService> subscriptions;

    //TODO: Изменяемо ли значение?
    @Scheduled(fixedRate = 5000)
    public void refreshSubscriptions() {
        subscriptions.values().forEach(PersonalSubscriptionService::refreshSubscriptions);
    }

    @Override
    public void subscribe(T userKey, Subscription subscription) {
        subscriptions.get(userKey).subscribe(subscription);
    }

    @Override
    public void unsubscribe(T userKey, Subscription subscription) {
        subscriptions.get(userKey).unsubscribe(subscription);
    }

    @Override
    public void getSubscriptions(T userKey) {
        subscriptions.get(userKey).getSubscriptions();
    }
}
