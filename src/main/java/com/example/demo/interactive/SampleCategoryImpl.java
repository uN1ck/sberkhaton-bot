package com.example.demo.interactive;

import com.example.demo.interactive.model.Action;
import com.example.demo.interactive.model.Entity;
import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SampleCategoryImpl implements Category {
    
    @Override
    public String getCommand() {
        return "sample";
    }

    @Override
    public String getCommandName() {
        return "Сампле";
    }

    @Override
    public String getListButtonName() {
        return "Сервера";
    }

    @Override
    public List<Action> getMainMenuCommands() {
        return ImmutableList.of(
                new Action("/sample status", "Статус")
        );
    }

    @Override
    public List<Entity> listEntities(String pattern, String[] previous) {
        if(previous.length == 0)
            return ImmutableList.of(
                    new Entity("folder", "Мой сервак", true),
                    new Entity("srv2", "Не мой сервак", false)
            );
        
        return ImmutableList.of(
                new Entity("kek1", "В подпапке", false),
                new Entity("kek2", "Ещё сервер", false),
                new Entity("kek3", "Ну и ещё один", false)
        );
    }

    @Override
    public List<Action> getEntityCommands() {
        return ImmutableList.of(
                new Action("/sample server %s status", "Статус"),
                new Action("/sample server %s options", "Опции"),
                new Action("/sample server %s shutdown", "Выключить")
        );
    }

}