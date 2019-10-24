package com.example.demo.interactive;

import com.example.demo.RootHandler;
import com.example.demo.interactive.action.ButtonAction;
import com.example.demo.interactive.action.CategoryAction;
import com.example.demo.interactive.input.PeerInputHandler;
import com.example.demo.interactive.model.Button;
import im.dlg.botsdk.domain.InteractiveEvent;
import im.dlg.botsdk.domain.Message;
import im.dlg.botsdk.domain.Peer;
import im.dlg.botsdk.domain.interactive.InteractiveAction;
import im.dlg.botsdk.domain.interactive.InteractiveButton;
import im.dlg.botsdk.domain.interactive.InteractiveGroup;
import im.dlg.botsdk.light.InteractiveEventListener;
import im.dlg.botsdk.light.MessageListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
public class PeerHandler implements MessageListener, InteractiveEventListener {

    private final RootHandler rootHandler;
    private final Peer peer;
    
    private final Map<String, CategoryInteractiveHandler> handlers = new HashMap<>();
    
    private final Map<Long, ButtonAction> actionsCache = new HashMap<>();
    private long actionsCounter = 0;
    
    private PeerInputHandler<String> activeTextRequest = null;

    @Override
    public void onMessage(Message message) {
        onMessage(message.getText().trim());
    }

    public void onMessage(String message) {
        if(!message.startsWith("/")) {
            activeTextRequest.accept(message);
            activeTextRequest = null;
            return;
        }
        activeTextRequest = null;
        
        if(message.equals("/start")) {
            start();
            return;
        }

        String response = null;
        if (message.matches("^/jobs.*")) {
            response = rootHandler.getJenkinsHandler().onMessage(peer, message);
        } else if (message.matches("^/stash.*")) {
            response = rootHandler.getStashHandler().onMessage(peer, message);
        }

        rootHandler.getBotProvider().getBot().messaging().sendText(
                peer,
                Optional.ofNullable(response).orElse("Нет такой команды :) " + message)
        );
    }
    
    public void start() {
        List<Button> buttons = new ArrayList<>();
        for(Category category : rootHandler.getCategories())
            buttons.add(new Button(new CategoryAction(category.getCommand()), category.getCommandName()));

        renderButtons(buttons);
    }
    
    @Override
    public void onEvent(InteractiveEvent event) {
        long id = Long.parseLong(event.getValue());
        log.info("Received action {}", id);

        ButtonAction action = actionsCache.get(id);
        if(action != null) {
            CategoryInteractiveHandler handler = handlers.computeIfAbsent(action.getOwner(), a -> {
                Category category = rootHandler.getCategories()
                                               .stream()
                                               .filter(cat -> cat.getCommand().equals(a))
                                               .findAny()
                                               .orElse(null);
                if(category == null)
                    throw new IllegalStateException();

                return new CategoryInteractiveHandler(this, category);
            });
            handler.handle(action);
        } else {
            throw new NullPointerException();
        }
    }
    
    protected void renderButtons(List<Button> buttons) {
        List<InteractiveAction> actions = new ArrayList<>();
        
        for(Button button : buttons) {
            long id;
            synchronized(actionsCache) {
                id = actionsCounter;
                actionsCounter++;
                actionsCache.put(id, button.getAction());
            }
            
            log.info("Stored action {} {}", id, button.getAction());
            
            actions.add(new InteractiveAction(
                    "bt_" + id, 
                    new InteractiveButton(Long.toString(id), button.getDisplayName())
            ));
        }

        InteractiveGroup group = new InteractiveGroup(actions);
        rootHandler.getBotProvider().getBot().interactiveApi().send(peer, group);
    }
    
    public void requestText(String message, PeerInputHandler<String> handler) {
        rootHandler.getBotProvider().getBot().messaging().sendText(peer, message);
        activeTextRequest = handler;
    }
    
}
