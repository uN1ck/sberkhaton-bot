package com.example.demo.jenkins.handlers;

import com.example.demo.jenkins.JenkinsProvider;
import com.example.demo.stash.util.Pretty;
import im.dlg.botsdk.domain.Peer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListHandler implements Handler {
    private final JenkinsProvider jenkinsProvider;

    @Override
    public String handle(String tail, Peer sender) {
        if (tail.length() > 0) {
            List<String> jobs = jenkinsProvider.getFilteredJobs(tail);
            return String.format("Jobs [%d]: \n%s", jobs.size(), String.join("\n", Pretty.toString(jobs)));
        } else {
            return String.format("Jobs: \n%s", String.join("\n", jenkinsProvider.getAllJobNames()));
        }
    }
}
