package com.example.demo.jenkins.handlers;

import com.example.demo.jenkins.JenkinsProvider;
import com.example.demo.stash.util.Pretty;
import im.dlg.botsdk.domain.Message;
import im.dlg.botsdk.domain.Peer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;


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
                return statusHandler(message.getSender());
            } else if (tail.matches("^list.*$")) {
                return listHandler(message.getSender(), tail.replace("list", "").trim());
            } else if (tail.matches("^job.*$")) {
                return jobHandler(message.getSender(), tail.replace("job", "").trim());
            }
        }

        return "Нет такой комманды :) " + text;
    }

    private String statusHandler(Peer sender) {
        return "Состояние Jenkins " + jenkinsProvider.getStatus();
    }

    private String listHandler(Peer sender, String tail) {
        if (tail.length() > 0) {
            return
                    "Jobs: \n" + jenkinsProvider.getAllJobNames();
        } else {
            return
                    "Jobs: \n" + jenkinsProvider.getAllJobNames();
        }
    }

    private String jobHandler(Peer sender, String tail) {
        return "Not implemented yet";
    }
}

