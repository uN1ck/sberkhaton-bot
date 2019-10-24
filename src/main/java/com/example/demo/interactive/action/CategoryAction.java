package com.example.demo.interactive.action;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class CategoryAction implements ButtonAction {
    
    private final ButtonAction previous;
    private final String owner;
    
    @Setter
    private UUID uuid;

}