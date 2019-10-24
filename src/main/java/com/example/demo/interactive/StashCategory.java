package com.example.demo.interactive;

import com.example.demo.interactive.model.Action;
import com.example.demo.interactive.model.Entity;
import com.example.demo.stash.StashService;
import com.example.demo.stash.dto.PullRequest;
import com.example.demo.stash.dto.PullRequestShorten;
import com.example.demo.stash.dto.StashProject;
import com.example.demo.stash.dto.StashRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StashCategory implements Category {
    private final StashService stashService;

    @Override
    public String getCommand() {
        return "stash";
    }

    @Override
    public String getCommandName() {
        return "Stash";
    }

    @Override
    public String getListButtonName() {
        return "Проекты";
    }

    @Override
    public List<Action> getMainMenuCommands() {
        return Collections.emptyList();
    }

    @Override
    public List<Entity> listEntities(String pattern, String[] previous) {
        if (previous.length == 0) {
            List<StashProject> stashProjects = stashService.listAllProjects();
            return stashProjects.stream()
                    .map(project -> new Entity(project.getKey(), project.getName(), true))
                    .collect(Collectors.toList());
        } else if (previous.length == 1) {
            List<StashRepository> repositories = stashService.listRepositories(previous[0]);
            return repositories.stream()
                    .map(repo -> new Entity(previous[0] + " " + repo.getName(),
                            repo.getName(), false))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public List<Action> getEntityCommands() {
        return Arrays.asList(
                new Action("/stash sub repo %s", "Подписаться"),
                new Action("/stash unsub repo %s", "Отписаться")
        );
    }
}
