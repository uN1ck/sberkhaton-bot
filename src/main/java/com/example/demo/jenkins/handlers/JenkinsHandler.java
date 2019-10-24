package com.example.demo.jenkins.handlers;

import com.example.demo.jenkins.JenkinsProvider;
import com.example.demo.stash.util.Pretty;
import im.dlg.botsdk.domain.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;


@Service
@RequiredArgsConstructor
public class JenkinsHandler {
    public static final String JOBS = "/jobs";
    public static final String LIST = "list";
    public static final String JOB = "job";
    public static final String STATUS = "status";
    public static final String SUB = "sub";
    public static final String UNSUB = "unsub";
    public static final String FAV = "fav";
    public static final String UNFAV = "unfav";
    public static final String START = "start";
    public static final String START_LAST = "start last";
    public static final String LOG = "log";

    private final JenkinsProvider jenkinsProvider;
    private final JobHandler jobHandler;

    public String onMessage(Message message) {
        String text = message.getText().trim();
        if (text.matches("^" + JOBS + "$")) {
            return Pretty.toString(Arrays.asList("list <filter criteria>",
                                                 "job <name> status",
                                                 "job <name> sub/unsub",
                                                 "job <name> start last",
                                                 "job <name> fav/unfav",
                                                 "job <name> start <args>"));
        } else {
            String tail = message.getText().replace(JOBS, "").trim();
            if (tail.matches("^" + STATUS + "$")) {
                return statusHandler();
            } else if (tail.matches("^" + LIST + ".*$")) {
                return listHandler(tail.replace(LIST, "").trim());
            } else if (tail.matches("^" + JOB + ".*$")) {
                return jobHandler.handle(tail.replace(JOB, "").trim());
            }
        }
        return "Нет такой комманды [" + text + "] =_=";
    }

    private String statusHandler() {
        return "Состояние " + jenkinsProvider.getStatus();
    }

    private String listHandler(String tail) {
        if (tail.length() > 0) {
            List<String> jobs = jenkinsProvider.getFilteredJobs(tail);
            return String.format("Jobs [%d]: \n%s", jobs.size(), String.join("\n", Pretty.toString(jobs)));
        } else {
            return String.format("Jobs: \n%s", String.join("\n", jenkinsProvider.getAllJobNames()));
        }
    }

}

