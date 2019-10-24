package com.example.demo.interactive;

import com.example.demo.interactive.action.*;
import com.example.demo.interactive.model.Action;
import com.example.demo.interactive.model.Button;
import com.example.demo.interactive.model.Entity;
import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class CategoryInteractiveHandler {
    
    private final PeerHandler peerHandler;
    private final Category category;
    
    public void handle(ButtonAction action) {
        if(action instanceof InvokeAction) {
            invoke((InvokeAction)action);
        } else if(action instanceof CategoryAction) {
            // Основное меню или избранное
            renderMenu((CategoryAction)action);
        } else if(action instanceof ListAction) {
            // Фильтры пока не поддерживаются
            renderEntities((ListAction)action);
        } else if(action instanceof EntityAction) {
            renderEntityActions((EntityAction)action);
        }
    }
    
    private void invoke(InvokeAction parent) {
        this.peerHandler.onMessage(parent.getCommand());
    }
    
    private void renderMenu(CategoryAction parent) {
        List<Button> buttons = new ArrayList<>();
        
        buttons.add(new Button(
                new ListAction(category.getCommand(), "", ImmutableList.of()),
                category.getListButtonName()
        ));
        
        buttons.add(new Button(
                new ListAction(category.getCommand(), "", ImmutableList.of()),
                category.getListButtonName() + " (фильтр)"
        ));
        
        for(Action cmd : category.getMainMenuCommands())
            buttons.add(new Button(
                    new InvokeAction(category.getCommand(), cmd.getCommand()),
                    cmd.getDisplayName()
            ));
        
        this.peerHandler.renderButtons(buttons);
    }
    
    private void renderEntities(ListAction parent) {
        List<Entity> entities = category.listEntities(parent.getFilter(), parent.getPath().toArray(new String[0]));
        
        this.peerHandler.renderButtons(
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
    
    private void renderEntityActions(EntityAction parent) {
        this.peerHandler.renderButtons(
                category.getEntityCommands()
                        .stream()
                        .map(action -> new Button(
                                new InvokeAction(
                                        category.getCommand(), 
                                        String.format(action.getCommand(), parent.getIdentifier())
                                ),
                                action.getDisplayName() 
                        ))
                        .collect(Collectors.toList())
        );
    }
    
}
