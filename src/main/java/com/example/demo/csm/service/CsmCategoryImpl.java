package com.example.demo.csm.service;

import com.example.demo.interactive.Category;
import com.example.demo.interactive.model.Action;
import com.example.demo.interactive.model.Entity;
import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CsmCategoryImpl implements Category {

    @Autowired
    private CsmRequestService csmRequestService;

    @Override
    public String getCommand() {
        return "csm";
    }

    @Override
    public String getCommandName() {
        return "CSM";
    }

    @Override
    public String getListButtonName() {
        return "Серверы";
    }

    @Override
    public List<Action> getMainMenuCommands() {
        return ImmutableList.of(
                new Action("/csm status", "Статус")
        );
    }

    @Override
    public List<Entity> listEntities(String pattern, String[] previous) {
        if (previous.length == 0)
            return csmRequestService.getPrintNames();

        return csmRequestService.getPrintNames();
        /*ImmutableList.of(
                new Entity("172.30.18.93:9990", "pn for 93", false),
                new Entity("kek2", "Ещё сервер", false),
                new Entity("kek3", "Ну и ещё один", false)
        );*/
    }

    @Override
    public List<Action> getEntityCommands() {
        return ImmutableList.of(
                new Action("/csm server %s", "Статус"),
                new Action("/csm server %s options", "Опции"),
                new Action("/csm server %s modules", "Модули")
                );
    }


}
