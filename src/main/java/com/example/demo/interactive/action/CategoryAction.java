package com.example.demo.interactive.action;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CategoryAction implements ButtonAction {

    private final String owner;

    @Override
    public ButtonAction getPrevious() {
        return null;
    }

}