package com.example.demo.jenkins.handlers;

import com.example.demo.jenkins.provider.JenkinsProviderImpl;
import im.dlg.botsdk.domain.Peer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StatusHandler implements Handler {
    private final JenkinsProviderImpl jenkinsProvider;

    @Override
    public String handle(String tail, Peer sender) {
        return "Состояние " + jenkinsProvider.getStatus();
    }
}