package com.example.demo;

import com.example.demo.jenkins.handlers.JenkinsHandler;
import com.example.demo.stash.handler.StashHandler;
import im.dlg.botsdk.domain.Message;
import im.dlg.botsdk.light.MessageListener;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RootHandler implements MessageListener {
    private final BotProvider botProvider;
    private final JenkinsHandler jenkinsHandler;
    private final StashHandler stashHandler;

    @PostConstruct
    private void init() {
        botProvider.getBot().messaging().onMessage(this);
    }

    @Override
    public void onMessage(Message message) {
        String response = null;
        if (message.getText().matches("^/jobs.*")) {
            response = jenkinsHandler.onMessage(message);
        } else if (message.getText().matches("^/stash.*")) {
            response = stashHandler.onMessage(message);
        }
        botProvider.getBot().messaging().sendText(
                message.getSender(),
                Optional.ofNullable(response)
                        .orElse("Нет такой команды :) " + message.getText()));

    }
}
