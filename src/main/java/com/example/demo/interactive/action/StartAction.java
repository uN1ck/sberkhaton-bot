package com.example.demo.interactive.action;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class StartAction implements ButtonAction {

    private final String owner;

    @Setter
    private UUID uuid;

    @Override
    public ButtonAction getPrevious() {
        return null;
    }

}