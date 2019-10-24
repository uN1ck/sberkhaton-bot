package com.example.demo.jenkins.handlers;

import com.example.demo.jenkins.JenkinsProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StatusHandler {
    private final JenkinsProvider jenkinsProvider;

    public String handle() {
        return jenkinsProvider.getStatus().toString();
    }
}
