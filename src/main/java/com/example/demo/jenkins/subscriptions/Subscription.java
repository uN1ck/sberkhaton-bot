package com.example.demo.jenkins.subscriptions;

import com.offbytwo.jenkins.model.Job;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;

@Getter
@RequiredArgsConstructor
public class Subscription {
    private final Consumer<Job> onSubscriptionEvent;
    private final String jobIdentifier;
}
