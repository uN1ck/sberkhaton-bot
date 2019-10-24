package com.example.demo.interactive;

import com.example.demo.interactive.action.*;
import com.example.demo.interactive.model.Action;
import com.example.demo.interactive.model.Button;
import com.example.demo.interactive.model.Entity;
import com.google.common.collect.ImmutableList;
import im.dlg.botsdk.domain.Peer;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class CategoryInteractiveHandler {
    
    private final Interactive interactive;
    private final Category category;
    
    public void handle(Peer peer, ButtonAction action) {
        if(action instanceof InvokeAction) {
            interactive.getRootHandler().onMessage(peer, ((InvokeAction) action).getCommand());
        } else if(action instanceof CategoryAction) {
            // Основное меню или избранное
            renderMenu(peer);
        } else if(action instanceof ListAction) {
            // Фильтры пока не поддерживаются
            ListAction la = (ListAction)action; 
            renderEntities(peer, la, category.listEntities(la.getFilter(), la.getPath().toArray(new String[0])));
        } else if(action instanceof EntityAction) {
            renderEntityActions(peer, ((EntityAction) action).getIdentifier());
        }
    }
    
    private void renderMenu(Peer peer) {
        List<Button> buttons = new ArrayList<>();
        
        buttons.add(new Button(
                new ListAction(category.getCommand(), "", ImmutableList.of()),
                category.getListButtonName()
        ));
        
        buttons.add(new Button(
                new ListAction(category.getCommand(), "", ImmutableList.of()),
                category.getListButtonName() + " (фильтр)"
        ));
        
        for(Action action : category.getMainMenuCommands())
            buttons.add(new Button(
                    new InvokeAction(category.getCommand(), action.getCommand()),
                    action.getDisplayName()
            ));
        
        interactive.renderButtons(peer, buttons);
    }
    
    private void renderEntities(Peer peer, ListAction parent, List<Entity> entities) {
        interactive.renderButtons(
                peer, 
                entities.stream()
                        .map(entity -> {
                            ButtonAction action = new EntityAction(category.getCommand(), entity.getIdentifier());
                            if(entity.isFolder())
                                action = parent.getChild(entity.getIdentifier());
                            
                            return new Button(
                                action,
                                entity.getDisplayNameWithIcon());
                        })
                        .collect(Collectors.toList())
        );
    }
    
    private void renderEntityActions(Peer peer, String identifier) {
        interactive.renderButtons(
                peer, 
                category.getEntityCommands()
                        .stream()
                        .map(action -> new Button(
                                new InvokeAction(category.getCommand(), String.format(action.getCommand(), identifier)),
                                action.getDisplayName() 
                        ))
                        .collect(Collectors.toList())
        );
    }
    
}
