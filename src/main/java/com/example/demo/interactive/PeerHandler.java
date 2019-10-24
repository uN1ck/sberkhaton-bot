package com.example.demo.interactive;

import com.example.demo.RootHandler;
import com.example.demo.interactive.action.ButtonAction;
import com.example.demo.interactive.action.CategoryAction;
import com.example.demo.interactive.input.PeerInputHandler;
import com.example.demo.interactive.model.Button;
import com.example.demo.interactive.model.Entity;
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
    
    public static final String DELAYED_COMMAND = "~DELAYED~";

    private final RootHandler rootHandler;
    private final Peer peer;
    
    private final Map<String, CategoryInteractiveHandler> handlers = new HashMap<>();
    
    private final Map<UUID, ButtonAction> actionsCache = new HashMap<>();
    
    private PeerInputHandler activeTextRequest = null;
    
    private PeerInputHandler activeSelectHandler = null;
    private String activeSelectIdentifier = null;

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
        if(message.startsWith("/sample")) {
            response = rootHandler.getSample().onMessage(this, message);
        } else if (message.matches("^/jobs.*")) {
            response = rootHandler.getJenkinsHandler().onMessage(peer, message);
        } else if (message.matches("^/stash.*")) {
            response = rootHandler.getStashHandler().onMessage(peer, message);
        }

        if(response == null || !response.equals(DELAYED_COMMAND)) {
            rootHandler.getBotProvider().getBot().messaging().sendText(
                    peer,
                    Optional.ofNullable(response).orElse("Нет такой команды :) " + message)
            );
        }
    }
    
    public void start() {
        List<Button> buttons = new ArrayList<>();
        for(Category category : rootHandler.getCategories())
            buttons.add(new Button(new CategoryAction(category.getCommand()), category.getCommandName()));

        renderButtons(buttons);
    }
    
    @Override
    public void onEvent(InteractiveEvent event) {
        if(event.getId().startsWith("request_")) {
            if(activeSelectHandler != null && event.getId().startsWith(activeSelectIdentifier)) {
                activeSelectHandler.accept(event.getValue());
                activeSelectHandler = null;
            }
            return;
        }
        activeSelectHandler = null;
        
        UUID uuid = UUID.fromString(event.getValue());
        log.info("Received action {}", uuid);

        ButtonAction action = actionsCache.get(uuid);
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
        renderButtons(null, buttons);
    }
    
    protected void renderButtons(String title, List<Button> buttons) {
        List<InteractiveAction> actions = new ArrayList<>();
        
        for(Button button : buttons) {
            UUID uuid = UUID.randomUUID();
            actionsCache.put(uuid, button.getAction());
            
            log.info("Stored action {} {}", uuid, button.getAction());
            
            actions.add(new InteractiveAction(
                    "action_" + uuid, 
                    new InteractiveButton(uuid.toString(), button.getDisplayName())
            ));
        }

        InteractiveGroup group = new InteractiveGroup(null, title, actions);
        rootHandler.getBotProvider().getBot().interactiveApi().send(peer, group);
    }
    
    public void requestText(String message, PeerInputHandler handler) {
        rootHandler.getBotProvider().getBot().messaging().sendText(peer, message);
        activeTextRequest = handler;
    }
    
    public void requestSelect(List<Entity> entities, PeerInputHandler handler) {
        activeSelectHandler = handler;
        activeSelectIdentifier = "request_" + UUID.randomUUID();
        
        List<InteractiveAction> actions = new ArrayList<>();
        int counter = 0;
        
        for(Entity button : entities) {
            actions.add(new InteractiveAction(
                    activeSelectIdentifier + counter,
                    new InteractiveButton(button.getIdentifier(), button.getDisplayName())
            ));
            counter++;
        }

        InteractiveGroup group = new InteractiveGroup(actions);
        rootHandler.getBotProvider().getBot().interactiveApi().send(peer, group);
    }
    
    public void sendMessage(String message) {
        rootHandler.getBotProvider().getBot().messaging().sendText(peer, message);
    }
    
}
