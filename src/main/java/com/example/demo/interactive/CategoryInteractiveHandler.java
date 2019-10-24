package com.example.demo.interactive;

import com.example.demo.interactive.action.*;
import com.example.demo.interactive.model.Action;
import com.example.demo.interactive.model.Button;
import com.example.demo.interactive.model.Entity;
import com.google.common.base.Joiner;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class CategoryInteractiveHandler {
    
    private final PeerHandler peerHandler;
    private final Category category;
    
    public void handle(ButtonAction action) {
        if(action instanceof StartAction) {
            start((StartAction)action);
        } else if(action instanceof InvokeAction) {
            invoke((InvokeAction)action);
        } else if(action instanceof CategoryAction) {
            // Основное меню или избранное
            renderMenu((CategoryAction) action);
        } else if(action instanceof FilterRequestAction) {
            requestFilter((FilterRequestAction)action);
        } else if(action instanceof ListAction) {
            renderEntities((ListAction)action);
        } else if(action instanceof EntityAction) {
            renderEntityActions((EntityAction)action);
        }
    }
    
    private void start(StartAction action) {
        this.peerHandler.renderButtons(
                action, 
                "Выберите раздел",
                peerHandler.getRootHandler()
                           .getCategories()
                           .stream()
                           .map(cat -> new Button(new CategoryAction(
                                   action, cat.getCommand()), cat.getCommandName()
                           ))
                           .collect(Collectors.toList())
        );
    }
    
    private void invoke(InvokeAction parent) {
        this.peerHandler.onMessage(parent.getCommand());
    }
    
    private void renderMenu(CategoryAction parent) {
        List<Button> buttons = new ArrayList<>();
        
        buttons.add(new Button(
                new ListAction(parent, category.getCommand(), ""),
                category.getListButtonName()
        ));
        
        buttons.add(new Button(
                new FilterRequestAction(parent, category.getCommand()),
                category.getListButtonName() + " (фильтр)"
        ));
        
        for(Action cmd : category.getMainMenuCommands())
            buttons.add(new Button(
                    new InvokeAction(category.getCommand(), cmd.getCommand()),
                    cmd.getDisplayName()
            ));
        
        this.peerHandler.renderButtons(parent, category.getCommandName(), buttons);
    }
    
    private void requestFilter(FilterRequestAction parent) {
        peerHandler.requestText(
                "Введите фильтр для поиска", 
                text -> handle(new ListAction(parent.getPrevious(), category.getCommand(), text))
        );
    }
    
    private void renderEntities(ListAction parent) {
        List<Entity> entities = category.listEntities(parent.getFilter(), parent.getPath().toArray(new String[0]));
        
        this.peerHandler.renderButtons(
                parent,
                path(parent.getDisplayPath()),
                entities.stream()
                        .map(entity -> {
                            ButtonAction action;
                            if(entity.isFolder()) {
                                action = parent.getChild(entity);
                            } else {
                                List<String> path = new ArrayList<>(parent.getDisplayPath());
                                path.add(entity.getDisplayName());
                                
                                action = new EntityAction(parent, category.getCommand(), entity.getIdentifier(), path);
                            }
                            
                            return new Button(
                                action,
                                entity.getDisplayNameWithIcon());
                        })
                        .collect(Collectors.toList())
        );
    }
    
    private void renderEntityActions(EntityAction parent) {
        this.peerHandler.renderButtons(
                parent,
                path(parent.getDisplayPath()),
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
    
    private String path(List<String> path) {
        List<String> list = new ArrayList<>();
        list.add(category.getCommandName());
        list.addAll(path);
        return Joiner.on(" → ").join(list);
    }
    
}
