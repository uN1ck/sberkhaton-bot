package com.example.demo;

import com.example.demo.csm.service.CsmRequestService;
import com.example.demo.csm.service.CsmRoutingService;
import com.example.demo.interactive.Interactive;
import com.example.demo.jenkins.handlers.JenkinsHandler;
import com.example.demo.stash.handler.StashHandler;
import im.dlg.botsdk.domain.Message;
import im.dlg.botsdk.domain.Peer;
import im.dlg.botsdk.light.MessageListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RootHandler implements MessageListener {
    private final BotProvider botProvider;
    private final Interactive interactive;
    
    private final JenkinsHandler jenkinsHandler;
    private final StashHandler stashHandler;
    private final CsmRoutingService csmRoutingService;

    @PostConstruct
    private void init() {
        interactive.setRootHandler(this);
        
        botProvider.getBot().messaging().onMessage(this);
        botProvider.getBot().interactiveApi().onEvent(interactive);
    }

    @Override
    public void onMessage(Message message) {
        onMessage(message.getSender(), message.getText().trim());
    }
    
    public void onMessage(Peer peer, String message) {
        if(message.equals("/start")) {
            interactive.start(peer);
            return;
        }
        
        String response = null;
        if (message.matches("^/jobs.*")) {
            response = jenkinsHandler.onMessage(peer, message);
        } else if (message.matches("^/stash.*")) {
            response = stashHandler.onMessage(peer, message);
        }else if (message.matches("^/csm.*")) {
            response = csmRoutingService.onMessage(peer, message);
        }
        
        botProvider.getBot().messaging().sendText(
                peer,
                Optional.ofNullable(response).orElse("Нет такой команды :) " + message)
        );
    }
    
}
