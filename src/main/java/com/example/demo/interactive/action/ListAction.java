package com.example.demo.interactive.action;

import com.example.demo.interactive.model.Entity;
import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class ListAction implements ButtonAction {

    private final ButtonAction previous;
    private final String owner;
    private final String filter;
    
    private final List<String> path;
    private final List<String> displayPath;

    @Setter
    private UUID uuid;
    
    public ListAction(ButtonAction previous, String owner, String filter) {
        this(previous, owner, filter, ImmutableList.of(), ImmutableList.of());
    }
    
    public ListAction getChild(Entity child) {
        List<String> newPath = new ArrayList<>(path);
        newPath.add(child.getIdentifier());
        
        List<String> newDisplayPath = new ArrayList<>(displayPath);
        newDisplayPath.add(child.getDisplayName());
        
        return new ListAction(this, owner, filter, newPath, newDisplayPath);
    }

}

