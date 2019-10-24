package com.example.demo.jenkins.interactive;

import com.example.demo.interactive.Category;
import com.example.demo.interactive.model.Action;
import com.example.demo.interactive.model.Entity;
import com.example.demo.jenkins.dto.JobDto;
import com.example.demo.jenkins.provider.JenkinsProviderImpl;
import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class JenkinsCategory implements Category {
    private final JenkinsProviderImpl jenkinsProvider;

    @Override
    public String getCommand() {
        return "jobs";
    }

    @Override
    public String getCommandName() {
        return "Джобы";
    }

    @Override
    public String getListButtonName() {
        return "Джобы";
    }

    @Override
    public List<Action> getMainMenuCommands() {
        return ImmutableList.of(
                new Action("/jobs status", "Статус Jenkins")
        );
    }

    @Override
    public List<Entity> listEntities(String pattern, String[] previous) {
        //TODO: Дождаться когда можно фильтрить
        List<Entity> result;
        if (previous.length == 0) {
            result = jenkinsProvider.getJobsOnLevel(pattern).stream().map(
                    jobDto -> new Entity(jobDto.getFullName(), jobDto.getDisplayName(),
                                         jobDto.getJobType() == JobDto.JobType.FOLDER)
            ).collect(Collectors.toList());
        } else {
            result = jenkinsProvider.getJobsOnLevel(previous[previous.length - 1], pattern).stream().map(
                    jobDto -> new Entity(jobDto.getFullName(), jobDto.getDisplayName(),
                                         jobDto.getJobType() == JobDto.JobType.FOLDER)
            ).collect(Collectors.toList());
        }
        return result;
    }

    @Override
    public List<Action> getEntityCommands() {
        return Arrays.asList(new Action("/jobs job %s status", "Состояние"),
                             new Action("/jobs job %s sub", "Подписаться"),
                             new Action("/jobs job %s unsub", "Отписаться"),
                             new Action("/jobs job %s start last", "Перезапуск"),
                             new Action("/jobs job %s start", "Запуск"),
                             new Action("/jobs job %s log last", "Лог последнего запуска"));
    }
}
