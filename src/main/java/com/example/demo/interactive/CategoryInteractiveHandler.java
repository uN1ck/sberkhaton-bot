package com.example.demo.interactive;

import com.example.demo.interactive.model.Action;
import com.example.demo.interactive.model.Button;
import com.example.demo.interactive.model.Entity;
import im.dlg.botsdk.domain.Peer;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class CategoryInteractiveHandler {
    
    private static final String LIST_ACTION = "list";
    private static final String LIST_FILTER_ACTION = "list-filter";
    private static final String ENTITY_ACTION = "entity";
    
    private final Interactive interactive;
    private final Category category;
    
    public void handle(Peer peer, String[] cmd) {
        if(cmd.length == 0) {
            // Основное меню или избранное
            renderMenu(peer);
        } else if(cmd[0].equals(LIST_ACTION) || cmd[0].equals(LIST_FILTER_ACTION)) {
            // Пока фильтры не работают
            renderEntities(peer, category.listEntities(""));
        } else if(cmd[0].equals(ENTITY_ACTION)) {
            String identifier = cmd[1];
            renderEntityActions(peer, identifier);
        }
    }
    
    private void renderMenu(Peer peer) {
        List<Button> buttons = new ArrayList<>();
        
        buttons.add(new Button(
                category.getListButtonName(),
                category.getCommand(), LIST_ACTION
        ));
        
        buttons.add(new Button(
                category.getListButtonName() + " (фильтр)",
                category.getCommand(), LIST_FILTER_ACTION
        ));
        
        for(Action action : category.getMainMenuCommands())
            buttons.add(new Button(
                    action.getDisplayName(),
                    Interactive.INVOKE_ACTION, action.getCommand()
            ));
        
        interactive.renderButtons(peer, buttons);
    }
    
    private void renderEntities(Peer peer, List<Entity> entities) {
        interactive.renderButtons(
                peer, 
                entities.stream()
                        .map(entity -> new Button(
                                entity.getDisplayName(), 
                                category.getCommand(), ENTITY_ACTION, entity.getIdentifier()
                        ))
                        .collect(Collectors.toList())
        );
    }
    
    private void renderEntityActions(Peer peer, String identifier) {
        interactive.renderButtons(
                peer, 
                category.getEntityCommands()
                        .stream()
                        .map(action -> new Button(
                                action.getDisplayName(), 
                                Interactive.INVOKE_ACTION, String.format(action.getCommand(), identifier)
                        ))
                        .collect(Collectors.toList())
        );
    }
    
}
