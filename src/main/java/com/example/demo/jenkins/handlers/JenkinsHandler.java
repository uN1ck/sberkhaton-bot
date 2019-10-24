package com.example.demo.jenkins.handlers;

import com.example.demo.jenkins.JenkinsProvider;
import com.example.demo.stash.util.Pretty;
import com.offbytwo.jenkins.model.Job;
import im.dlg.botsdk.domain.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class JenkinsHandler {
    private final JenkinsProvider jenkinsProvider;

    public String onMessage(Message message) {
        String text = message.getText().trim();
        if (text.matches("^/jobs$")) {
            return Pretty.toString(Arrays.asList("list <filter criteria>",
                                                 "job <name> status",
                                                 "job <name> sub/unsub",
                                                 "job <name> start last",
                                                 "job <name> fav/unfav",
                                                 "job <name> start <args>"));
        } else {
            String tail = message.getText().replace("/jobs", "").trim();
            if (tail.matches("^status$")) {
                return statusHandler();
            } else if (tail.matches("^list.*$")) {
                return listHandler(tail.replace("list", "").trim());
            } else if (tail.matches("^job.*$")) {
                return jobHandler(tail.replace("job", "").trim());
            }
        }

        return "Нет такой комманды :) " + text;
    }

    private String statusHandler() {
        return "Состояние Jenkins " + jenkinsProvider.getStatus();
    }

    private String listHandler(String tail) {
        if (tail.length() > 0) {
            List<Job> jobs = jenkinsProvider.getFilteredJobs(tail);
            //TODO: Конифгурировать размеры списка?
            List<String> jobsTail = jobs.subList(0, Math.min(10, jobs.size()))
                                        .stream()
                                        .map(Job::getName)
                                        .collect(Collectors.toList());
            return String.format("Jobs [%d]: \n%s", jobs.size(), Pretty.toString(jobsTail));
        } else {
            //TODO: вынести получение всех имен внаружу
            return String.format("Jobs: \n%s", jenkinsProvider.getAllJobNames());
        }
    }

    private String jobHandler(String tail) {
        return "Not implemented yet";
    }
}

