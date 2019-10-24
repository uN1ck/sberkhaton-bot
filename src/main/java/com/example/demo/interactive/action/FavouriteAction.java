package com.example.demo.interactive.action;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class FavouriteAction implements ButtonAction {

    private final EntityAction previous;

    @Setter
    private UUID uuid;

    @Override
    public String getOwner() {
        return previous.getOwner();
    }
    
}
