package com.example.demo.jenkins.subscriptions.service;

import com.example.demo.BotProvider;
import com.example.demo.jenkins.JenkinsProvider;
import com.example.demo.jenkins.subscriptions.Subscription;
import im.dlg.botsdk.domain.Peer;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {
    private final Map<Integer, PersonalSubscriptionService> subscriptions;
    private final BotProvider botProvider;
    private final JenkinsProvider jenkinsProvider;

    //TODO: Изменяемо ли значение?
    @Scheduled(fixedRate = 5000)
    public void refreshSubscriptions() {
        subscriptions.values().forEach(PersonalSubscriptionService::refreshSubscriptions);
    }

    @Override
    public void subscribe(Peer userKey, Subscription subscription) {
        if (!subscriptions.containsKey(userKey.getId())) {
            subscriptions.put(userKey.getId(), new PersonalSubscriptionServiceImpl(userKey, botProvider, jenkinsProvider));
        }
        subscriptions.get(userKey.getId()).subscribe(subscription);
    }

    @Override
    public void unsubscribe(Peer userKey, String identifier) {
        if (!subscriptions.containsKey(userKey.getId())) {
            subscriptions.put(userKey.getId(), new PersonalSubscriptionServiceImpl(userKey, botProvider, jenkinsProvider));
        }
        subscriptions.get(userKey.getId()).unsubscribe(identifier);
    }

    @Override
    public void getSubscriptions(Peer userKey) {
        if (!subscriptions.containsKey(userKey.getId())) {
            subscriptions.put(userKey.getId(), new PersonalSubscriptionServiceImpl(userKey, botProvider, jenkinsProvider));
        }
        subscriptions.get(userKey.getId()).getSubscriptions();
    }
}
