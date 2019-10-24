package com.example.demo.jenkins.subscriptions;

import com.example.demo.BotProvider;
import com.offbytwo.jenkins.model.Job;
import im.dlg.botsdk.domain.Peer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public abstract class Subscription {
    private final String jobIdentifier;

    public abstract void onEvent(Job job, BotProvider botProvider, Peer owner);
}
