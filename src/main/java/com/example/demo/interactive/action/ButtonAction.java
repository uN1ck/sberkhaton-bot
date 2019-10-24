package com.example.demo.interactive.action;

import java.util.UUID;

public interface ButtonAction {

    String getOwner();

    ButtonAction getPrevious();
    
    void setUuid(UUID uuid);
    
    UUID getUuid();
    
}
