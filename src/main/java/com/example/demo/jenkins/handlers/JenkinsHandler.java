package com.example.demo.jenkins.handlers;

import com.example.demo.stash.util.Pretty;
import im.dlg.botsdk.domain.Peer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;


@Service
@RequiredArgsConstructor
public class JenkinsHandler {
    private final JobHandler handlerJob;
    private final StatusHandler statusHandler;

    public String onMessage(Peer sender, String message) {
        if (message.matches("^" + CommandList.JOBS + "$")) {
            return Pretty.toString(Arrays.asList("list <filter criteria>",
                                                 "job <name> status",
                                                 "job <name> sub/unsub",
                                                 "job <name> start last",
                                                 "job <name> fav/unfav",
                                                 "job <name> start <args>"));
        } else {
            String tail = message.replace(CommandList.JOBS, "").trim();
            if (tail.matches("^" + CommandList.STATUS + "$")) {
                return statusHandler.handle("", sender);
            } else if (tail.matches("^" + CommandList.JOB + ".*$")) {
                return handlerJob.handle(tail.replace(CommandList.JOB, "").trim(), sender);
            }
        }
        return "Нет такой комманды [" + message + "] =_=";
    }


}

