package com.example.demo.jenkins.handlers;

import com.example.demo.jenkins.BotProvider;
import com.example.demo.jenkins.JenkinsProvider;
import com.offbytwo.jenkins.model.Job;
import im.dlg.botsdk.domain.Message;
import im.dlg.botsdk.domain.Peer;
import im.dlg.botsdk.light.MessageListener;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RootHandler implements MessageListener {
    private final BotProvider botProvider;
    private final JenkinsProvider jenkinsProvider;

    @PostConstruct
    private void init() {
        botProvider.getBot().messaging().onMessage(this);
    }

    @Override
    public void onMessage(Message message) {
        if (message.getText().matches("^/jobs.*")) {
            String tail = message.getText().replace("/jobs", "").trim();
            if (tail.matches("^status$")) {
                statusHandler(message.getSender());
                return;
            } else if (tail.matches("^list.*$")) {
                listHandler(message.getSender(), tail.replace("list", "").trim());
                return;
            } else if (tail.matches("^job.*$")) {
                jobHandler(message.getSender(), tail.replace("job", "").trim());
                return;
            }
        }
        botProvider.getBot().messaging().sendText(message.getPeer(), "Нет такой комманды :) " + message.getText());

    }

    private void statusHandler(Peer sender) {
        botProvider.getBot()
                   .messaging()
                   .sendText(sender, "Состояние Jenkins " + jenkinsProvider.getStatus());
    }

    private void listHandler(Peer sender, String tail) {
        if (tail.length() > 0) {
            botProvider.getBot()
                       .messaging()
                       .sendText(sender,
                                 "Jobs: \n- " + jenkinsProvider.getFilteredJobs(tail)
                                                           .stream()
                                                           .map(Job::getName)
                                                           .collect(Collectors.joining("\n- ")));
        } else {
            botProvider.getBot()
                       .messaging()
                       .sendText(sender,
                                 "Jobs: \n- " + jenkinsProvider.getAllJobs()
                                                             .stream()
                                                             .map(Job::getName)
                                                             .collect(Collectors.joining("\n- ")));
        }
    }

    private void jobHandler(Peer sender, String tail) {
        botProvider.getBot()
                   .messaging()
                   .sendText(sender, "Not implemented yet");
    }
}
