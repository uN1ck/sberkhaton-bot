package com.example.demo.interactive;

import com.example.demo.BotProvider;
import com.example.demo.RootHandler;
import com.example.demo.interactive.action.ButtonAction;
import com.example.demo.interactive.action.CategoryAction;
import com.example.demo.interactive.model.Button;
import im.dlg.botsdk.domain.InteractiveEvent;
import im.dlg.botsdk.domain.Peer;
import im.dlg.botsdk.domain.interactive.InteractiveAction;
import im.dlg.botsdk.domain.interactive.InteractiveButton;
import im.dlg.botsdk.domain.interactive.InteractiveGroup;
import im.dlg.botsdk.light.InteractiveEventListener;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class Interactive implements InteractiveEventListener {

    private final BotProvider botProvider;
    private final List<Category> categories;
    @Setter @Getter
    private RootHandler rootHandler;
    
    private final Map<String, CategoryInteractiveHandler> handlers = new HashMap<>();
    
    private final Map<Long, ButtonAction> actionsCache = new HashMap<>(); // В идеальном мире они должны протухать
    private long actionsCounter = 0;

    public void start(Peer peer) {
        List<Button> buttons = new ArrayList<>();
        for(Category category : categories)
            buttons.add(new Button(new CategoryAction(category.getCommand()), category.getCommandName()));
        
        renderButtons(peer, buttons);
    }

    @Override
    public void onEvent(InteractiveEvent event) {
        try {
            long id = Long.parseLong(event.getValue());
            log.info("Received action {}", id);

            ButtonAction action = actionsCache.get(id);
            if(action != null) {
                CategoryInteractiveHandler handler = handlers.computeIfAbsent(action.getOwner(), a -> {
                    Category category = categories.stream()
                                                  .filter(cat -> cat.getCommand().equals(a))
                                                  .findAny()
                                                  .orElse(null);
                    if(category == null)
                        throw new IllegalStateException();

                    return new CategoryInteractiveHandler(this, category);
                });
                handler.handle(event.getPeer(), action);
            }
        } catch(Exception e) {
            log.error("Exception during action handling", e);
            botProvider.getBot().messaging().sendText(event.getPeer(), e.getMessage());
        }
    }
    
    protected void renderButtons(Peer peer, List<Button> buttons) {
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
        botProvider.getBot().interactiveApi().send(peer, group);
    }
    
}
