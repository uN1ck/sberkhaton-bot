package com.example.demo.interactive.action;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class InvokeAction implements ButtonAction {
    
    private final String owner;
    private final String command;

    @Override
    public ButtonAction getPrevious() {
        return null;
    }
    
}
