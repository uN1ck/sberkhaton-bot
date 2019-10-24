package com.example.demo.jenkins.handlers;

import com.example.demo.stash.util.Pretty;
import im.dlg.botsdk.domain.Message;
import im.dlg.botsdk.domain.Peer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;


@Service
@RequiredArgsConstructor
public class JenkinsHandler {
    public static final String JOBS = "/jobs";
    public static final String LIST = "list";
    public static final String JOB = "job";
    public static final String STATUS = "status";
    public static final String SUB = "sub";
    public static final String UNSUB = "unsub";
    public static final String START_LAST = "start last";
    public static final String START = "start";
    public static final String LOG = "log";

    private final JobHandler handlerJob;
    private final ListHandler listHandler;
    private final StatusHandler statusHandler;

    public String onMessage(Message message) {
        String text = message.getText().trim();
        Peer sender = message.getSender();
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
                return statusHandler.handle("", sender);
            } else if (tail.matches("^" + LIST + ".*$")) {
                return listHandler.handle(tail.replace(LIST, "").trim(), sender);
            } else if (tail.matches("^" + JOB + ".*$")) {
                return handlerJob.handle(tail.replace(JOB, "").trim(), sender);
            }
        }
        return "Нет такой комманды [" + text + "] =_=";
    }


}

