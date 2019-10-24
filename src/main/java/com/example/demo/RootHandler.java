package com.example.demo;

import com.example.demo.interactive.Category;
import com.example.demo.interactive.PeerHandler;
import com.example.demo.interactive.SampleCategoryImpl;
import com.example.demo.jenkins.handlers.JenkinsHandler;
import com.example.demo.stash.handler.StashHandler;
import im.dlg.botsdk.domain.InteractiveEvent;
import im.dlg.botsdk.domain.Message;
import im.dlg.botsdk.domain.Peer;
import im.dlg.botsdk.light.InteractiveEventListener;
import im.dlg.botsdk.light.MessageListener;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Getter
@RequiredArgsConstructor
public class RootHandler implements MessageListener, InteractiveEventListener {

    private final BotProvider botProvider;
    private final List<Category> categories;

    private final JenkinsHandler jenkinsHandler;
    private final StashHandler stashHandler;

    private final SampleCategoryImpl sample;

    private final Map<Integer, PeerHandler> peerHandlers = new HashMap<>();

    @PostConstruct
    private void init() {
        botProvider.getBot().messaging().onMessage(this);
        botProvider.getBot().interactiveApi().onEvent(this);
    }

    @Override
    public void onMessage(Message message) {
        try {
            getPeerHandler(message.getPeer()).onMessage(message);
        } catch(Exception e) {
            log.error("Error during message processing", e);
        }
    }

    @Override
    public void onEvent(InteractiveEvent event) {
        try {
            getPeerHandler(event.getPeer()).onEvent(event);
        } catch(Exception e) {
            log.error("Error during interactive event processing", e);
        }
    }

    private PeerHandler getPeerHandler(Peer peer) {
        return peerHandlers.computeIfAbsent(peer.getId(), p -> new PeerHandler(this, peer));
    }
    
}
