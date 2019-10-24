package com.example.demo.jenkins.handlers;

import com.example.demo.jenkins.JenkinsProvider;
import com.offbytwo.jenkins.model.Job;
import im.dlg.botsdk.domain.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JenkinsHandler {
    private final JenkinsProvider jenkinsProvider;

    @Nullable
    public String onMessage(Message message) {
        String tail = message.getText().replace("/jobs", "").trim();
        if (tail.matches("^status$")) {
            return statusHandler();
        } else if (tail.matches("^list.*$")) {
            return listHandler(tail.replace("list", "").trim());
        } else if (tail.matches("^job.*$")) {
            return jobHandler(tail.replace("job", "").trim());
        }
        return null;
    }


    private String statusHandler() {
        return "Состояние Jenkins " + jenkinsProvider.getStatus();
    }

    private String listHandler(String tail) {
        if (tail.length() > 0) {
            return "Jobs: \n- " + jenkinsProvider.getFilteredJobs(tail)
                    .stream()
                    .map(Job::getName)
                    .collect(Collectors.joining("\n- "));
        } else {
            return "Jobs: \n- " + jenkinsProvider.getAllJobs()
                    .stream()
                    .map(Job::getName)
                    .collect(Collectors.joining("\n- "));
        }
    }

    private String jobHandler(String tail) {
        return "Not implemented yet";
    }
}
