package com.example.demo.interactive.model;

import com.example.demo.interactive.action.ButtonAction;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Button {

    private final ButtonAction action;
    private final String displayName;

}
