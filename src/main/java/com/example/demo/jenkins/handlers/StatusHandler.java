package com.example.demo.jenkins.handlers;

import com.example.demo.jenkins.provider.JenkinsProviderImpl;
import im.dlg.botsdk.domain.Peer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StatusHandler {
    private final JenkinsProviderImpl jenkinsProvider;

    public String handle() {
        return "Состояние " + jenkinsProvider.getStatus();
    }
}
