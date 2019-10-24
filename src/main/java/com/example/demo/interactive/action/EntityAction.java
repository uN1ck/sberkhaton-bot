package com.example.demo.interactive.action;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class EntityAction implements ButtonAction {

    private final String owner;
    private final String identifier;

    @Override
    public ButtonAction getPrevious() {
        return null;
    }

}
