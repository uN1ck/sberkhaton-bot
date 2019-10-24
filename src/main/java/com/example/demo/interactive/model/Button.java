package com.example.demo.interactive.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Button {

    private final String[] value;
    private final String displayName;
    
    public Button(String displayName, String... cmd) {
        this(cmd, displayName);
    }

}
