package com.example.demo.interactive.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Entity {

    private final String identifier;
    private final String displayName;
    private final boolean folder;
    
    public Entity(String identifier, String displayName) {
        this(identifier, displayName, false);
    }
    
    public String getDisplayNameWithIcon() {
        if(folder) return "\uD83D\uDCC1 " + displayName;
        return displayName;
    }

}
