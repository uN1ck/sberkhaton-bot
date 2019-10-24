package com.example.demo.interactive.action;

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

    @Override
    public ButtonAction getPrevious() {
        return null;
    }
    
    public ListAction getChild(String identifier) {
        List<String> newList = new ArrayList<>(path);
        newList.add(identifier);
        return new ListAction(owner, filter, newList);
    }

}

