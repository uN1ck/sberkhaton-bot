package com.example.demo;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class RootSubscriptionService {
    private final Map<String, Runnable> subscriptions = new ConcurrentHashMap<>();

    @Scheduled(fixedDelay = 5000)
    public void executeSubscriptionsCallbacks() {
        for (Runnable runnable : subscriptions.values()) {
            runnable.run();
        }
    }

    public void subscribe(String key, Runnable runnable) {
        subscriptions.put(key, runnable);
    }

    public void unsubscribe(String key) {
        subscriptions.remove(key);
    }
}
