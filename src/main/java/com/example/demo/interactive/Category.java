package com.example.demo.interactive;

import com.example.demo.interactive.model.Action;
import com.example.demo.interactive.model.Entity;

import java.util.List;

public interface Category {
    
    String getCommand();
    
    String getCommandName();
    
    String getListButtonName();
    
    List<Action> getMainMenuCommands();
    
    List<Entity> listEntities(String pattern);
    
    List<Action> getEntityCommands();
    
}
