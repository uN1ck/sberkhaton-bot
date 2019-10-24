package com.example.demo.interactive;

import com.example.demo.interactive.action.*;
import com.example.demo.interactive.model.Action;
import com.example.demo.interactive.model.Button;
import com.example.demo.interactive.model.Entity;
import com.google.common.base.Joiner;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class CategoryInteractiveHandler {
    
    private static final String ARROW_DELIMITER = " → ";
    
    private final PeerHandler peerHandler;
    private final Category category;
    
    private final Map<String, List<String>> favourites = new HashMap<>();
    
    public void handle(ButtonAction action) {
        if(action instanceof StartAction) {
            start((StartAction)action);
        } else if(action instanceof InvokeAction) {
            invoke((InvokeAction)action);
        } else if(action instanceof CategoryAction) {
            renderCategory((CategoryAction) action);
        } else if(action instanceof MenuAction) {
            renderMenu((MenuAction) action);
        } else if(action instanceof FilterRequestAction) {
            requestFilter((FilterRequestAction)action);
        } else if(action instanceof ListAction) {
            renderEntities((ListAction)action);
        } else if(action instanceof EntityAction) {
            renderEntityActions((EntityAction)action);
        } else if(action instanceof FavouriteAction) {
            FavouriteAction fav = (FavouriteAction) action;
            favourite(fav.getPrevious());
            renderEntityActions(fav.getPrevious());
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
    
    private void renderCategory(CategoryAction parent) {
        MenuAction menu = new MenuAction(parent.getPrevious(), category.getCommand());
        
        if(favourites.size() == 0) {
            handle(menu);
        } else {
            List<Button> buttons = new ArrayList<>();
            
            for(Map.Entry<String, List<String>> e : favourites.entrySet()) {
                buttons.add(new Button(
                        new EntityAction(parent, category.getCommand(), e.getKey(), e.getValue()),
                        e.getValue().get(e.getValue().size() - 1)
                ));
            }
            
            buttons.add(new Button(menu, "..."));
            this.peerHandler.renderButtons(parent, category.getCommandName() + ARROW_DELIMITER + "Избранное", buttons);
        }
    }
    
    private void renderMenu(MenuAction parent) {
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
        
        if(entities.size() != 1) {
            List<Button> buttons = entities.stream()
                                           .map(entity -> createButton(parent, parent, entity))
                                           .collect(Collectors.toList());
        
            this.peerHandler.renderButtons(parent, path(parent.getDisplayPath()), buttons);
        } else {
            this.handle(createButton(parent, parent.getPrevious(), entities.get(0)).getAction());
        }
    }
    
    private Button createButton(ListAction parent, ButtonAction prev, Entity entity) {
        ButtonAction action;
        if(entity.isFolder()) {
            action = parent.getChild(prev, entity);
        } else {
            List<String> path = new ArrayList<>(parent.getDisplayPath());
            path.add(entity.getDisplayName());

            action = new EntityAction(prev, category.getCommand(), entity.getIdentifier(), path);
        }

        return new Button(action, entity.getDisplayNameWithIcon());
    }
    
    private void renderEntityActions(EntityAction parent) {
        List<Button> buttons = category.getEntityCommands()
                                       .stream()
                                       .map(action -> new Button(
                                               new InvokeAction(
                                                       category.getCommand(),
                                                       String.format(action.getCommand(), parent.getIdentifier())
                                               ),
                                               action.getDisplayName()
                                       ))
                                       .collect(Collectors.toList());
        
        buttons = new ArrayList<>(buttons);
        buttons.add(new Button(
                new FavouriteAction(parent),
                favourites.containsKey(parent.getIdentifier()) ? "❌" : "\uD83C\uDF1F"
        ));
        
        this.peerHandler.renderButtons(parent, path(parent.getDisplayPath()), buttons);
    }
    
    private String path(List<String> path) {
        List<String> list = new ArrayList<>();
        list.add(category.getCommandName());
        list.add(category.getListButtonName());
        list.addAll(path);
        return Joiner.on(ARROW_DELIMITER).join(list);
    }
    
    private void favourite(EntityAction action) {
        String key = action.getIdentifier();
        if(favourites.containsKey(key)) {
            favourites.remove(key);
        } else {
            favourites.put(key, action.getDisplayPath());
        }
    }
    
}
