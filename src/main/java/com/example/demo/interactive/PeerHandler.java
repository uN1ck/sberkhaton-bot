package com.example.demo.interactive;

import com.example.demo.RootHandler;
import com.example.demo.interactive.action.ButtonAction;
import com.example.demo.interactive.action.StartAction;
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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
public class PeerHandler implements MessageListener, InteractiveEventListener {

    public static final String DELAYED_COMMAND = "~DELAYED~";

    @Getter(AccessLevel.PROTECTED)
    private final RootHandler rootHandler;
    @Getter
    private final Peer peer;

    private final Map<String, CategoryInteractiveHandler> handlers = new HashMap<>();

    private final Map<UUID, ButtonAction> actionsCache = new HashMap<>();

    private PeerInputHandler activeTextRequest = null;

    private PeerInputHandler activeSelectHandler = null;
    private UUID activeSelectUuid = null;

    private UUID buttonsMessageUuid = null;
    private boolean resetButtonsUuid = false;

    @Override
    public void onMessage(Message message) {
        onMessage(message.getText().trim());
    }

    public void onMessage(String message) {
        resetButtonsUuid = true;
        resetSelectHandler();

        if (!message.startsWith("/")) {
            activeTextRequest.accept(message);
            activeTextRequest = null;
            return;
        }
        activeTextRequest = null;

        if (message.equals("/start")) {
            handleAction(new StartAction(rootHandler.getCategories().get(0).getCommand()));
            return;
        }

        String response = null;
        if (message.startsWith("/sample")) {
            response = rootHandler.getSample().onMessage(this, message);
        } else if (message.matches("^/jobs.*")) {
            response = rootHandler.getJenkinsCategory().onMessage(this, message);
        } else if (message.matches("^/stash.*")) {
            response = rootHandler.getStashHandler().onMessage(peer, message);
        } else if (message.matches("^//stash.*")) {
            response = rootHandler.getStashCategory().onMessage(this, message);
        }

        if (response == null || !response.equals(DELAYED_COMMAND))
            sendMessage(Optional.ofNullable(response).orElse("Нет такой команды :) " + message));
    }

    @Override
    public void onEvent(InteractiveEvent event) {
        if (event.getId().startsWith("request_")) {
            if (activeSelectHandler != null && event.getMid().equals(activeSelectUuid)) {
                PeerInputHandler local = activeSelectHandler;
                activeSelectHandler = null;
                local.accept(event.getValue());
            }
            return;
        }
        resetSelectHandler();

        UUID uuid = UUID.fromString(event.getValue());
        log.info("Received action {}", uuid);

        handleAction(actionsCache.get(uuid));
    }

    private void handleAction(ButtonAction action) {
        if (action != null) {
            CategoryInteractiveHandler handler = handlers.computeIfAbsent(action.getOwner(), a -> {
                Category category = rootHandler.getCategories()
                                               .stream()
                                               .filter(cat -> cat.getCommand().equals(a))
                                               .findAny()
                                               .orElse(null);
                if (category == null)
                    throw new IllegalStateException();

                return new CategoryInteractiveHandler(this, category);
            });
            handler.handle(action);
        } else {
            throw new NullPointerException();
        }
    }

    @SneakyThrows
    protected void renderButtons(ButtonAction action, String title, List<Button> buttons) {
        if (action != null && action.getPrevious() != null) {
            buttons = new ArrayList<>(buttons);
            buttons.add(new Button(action.getPrevious(), "⬅️"));
        }

        List<InteractiveAction> actions = new ArrayList<>();
        for (Button button : buttons) {
            UUID uuid = button.getAction().getUuid();
            if (uuid == null) {
                uuid = UUID.randomUUID();
                actionsCache.put(uuid, button.getAction());
            }

            log.info("Stored action {} {}", uuid, button.getAction());

            actions.add(new InteractiveAction(
                    "action_" + uuid,
                    new InteractiveButton(uuid.toString(), button.getDisplayName())
            ));
        }

        InteractiveGroup group = new InteractiveGroup(null, title, actions);

        if (resetButtonsUuid && buttonsMessageUuid != null) {
            rootHandler.getBotProvider().getBot().messaging().delete(buttonsMessageUuid);
            buttonsMessageUuid = null;
        }
        resetButtonsUuid = false;

        if (buttonsMessageUuid != null) {
            rootHandler.getBotProvider().getBot().interactiveApi().update(buttonsMessageUuid, group).get();
        } else {
            buttonsMessageUuid = rootHandler.getBotProvider().getBot().interactiveApi().send(peer, group).get();
        }
    }

    public void requestText(String message, PeerInputHandler handler) {
        sendMessage(message);
        activeTextRequest = handler;
    }

    @SneakyThrows
    public void requestSelect(String message, List<Entity> entities, PeerInputHandler handler) {
        activeSelectHandler = handler;

        List<InteractiveAction> actions = new ArrayList<>();
        int counter = 0;

        for (Entity button : entities) {
            actions.add(new InteractiveAction(
                    "request_" + counter,
                    new InteractiveButton(button.getIdentifier(), button.getDisplayName())
            ));
            counter++;
        }

        InteractiveGroup group = new InteractiveGroup(null, message, actions);
        activeSelectUuid = rootHandler.getBotProvider().getBot().interactiveApi().send(peer, group).get();

        resetButtonsUuid = true;
    }

    public void sendMessage(String message) {
        rootHandler.getBotProvider().getBot().messaging().sendText(peer, message);
        resetButtonsUuid = true;
    }

    private void resetSelectHandler() {
        activeSelectHandler = null;
        if (activeSelectUuid != null) {
            rootHandler.getBotProvider().getBot().messaging().delete(activeSelectUuid);
            activeSelectUuid = null;
        }
    }

}
