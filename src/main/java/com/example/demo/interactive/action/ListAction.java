package com.example.demo.interactive.action;

import com.example.demo.interactive.model.Entity;
import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class ListAction implements ButtonAction {

    private final String owner;
    private final String filter;
    
    private final List<String> path;
    private final List<String> displayPath;

    public ListAction(String owner, String filter) {
        this(owner, filter, ImmutableList.of(), ImmutableList.of());
    }
    
    @Override
    public ButtonAction getPrevious() {
        return null;
    }
    
    public ListAction getChild(Entity child) {
        List<String> newPath = new ArrayList<>(path);
        newPath.add(child.getIdentifier());
        
        List<String> newDisplayPath = new ArrayList<>(displayPath);
        newDisplayPath.add(child.getDisplayName());
        
        return new ListAction(owner, filter, newPath, newDisplayPath);
    }

}

