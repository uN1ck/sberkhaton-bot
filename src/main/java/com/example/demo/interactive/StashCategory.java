package com.example.demo.interactive;

import com.example.demo.BotProvider;
import com.example.demo.RootSubscriptionService;
import com.example.demo.interactive.model.Action;
import com.example.demo.interactive.model.Entity;
import com.example.demo.stash.StashService;
import com.example.demo.stash.dto.PullRequest;
import com.example.demo.stash.dto.PullRequestShorten;
import com.example.demo.stash.dto.StashProject;
import com.example.demo.stash.dto.StashRepository;
import com.example.demo.stash.subscriptions.PullRequestSubscription;
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
    private final RootSubscriptionService rootSubscriptionService;
    private final BotProvider botProvider;

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
                    .filter(project -> {
                        if (pattern.isEmpty())
                            return true;
                        return project.getName().contains(pattern);
                    })
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
                new Action("/stash get repo %s", "Статус"),
                new Action("//stash list pr %s", "Список PR"),
                new Action("/stash sub repo %s", "Подписаться"),
                new Action("/stash unsub repo %s", "Отписаться")
        );
    }

    public String onMessage(PeerHandler peerHandler, String message) {
        String[] command = message.split("\\s+");
        String stashProjectKey = command[3];
        String repoName = command[4];
        StashProject stashProject = stashService.getProject(stashProjectKey).get();
        StashRepository stashRepository = stashService.getRepository(stashProjectKey, repoName).get();

        List<PullRequestShorten> pullRequests =
                stashService.listPullRequests(stashProjectKey, repoName);
        List<Entity> entities = pullRequests.stream()
                .map(pr -> new Entity(String.format(
                        "%s", pr.getId()), pr.getTitle())
                )
                .collect(Collectors.toList());
        peerHandler.requestSelect("Список PR", entities, prId -> {
            PullRequest pullRequest = stashService.getPullRequest(stashProjectKey, repoName, prId);
            final String STATUS_PR = "statusPr";
            final String MERGE_PR = "mergePr";
            final String DELETE_PR = "deletePr";
            final String SUBSCRIBE_PR = "subscribePr";
            final String UNSUBSCRIBE_PR = "unsubscribePr";

            List<Entity> prEntities = Arrays.asList(
                    new Entity(STATUS_PR, "Статус"),
                    new Entity(MERGE_PR, "Влить PR"),
                    new Entity(DELETE_PR, "Удалить PR"),
                    new Entity(SUBSCRIBE_PR, "Подписаться"),
                    new Entity(UNSUBSCRIBE_PR, "Отписаться")
            );
            peerHandler.requestSelect("Действия с PR", prEntities, x -> {
                String subscriptionKey = String.format("%s_%s_%s_%s",
                        peerHandler.getPeer().getId(),
                        stashProjectKey,
                        repoName,
                        pullRequest.getId().toString()
                );
                if (x.equals(STATUS_PR)) {
                    peerHandler.sendMessage(
                            stashService.getPullRequest(stashProjectKey, repoName, prId).toString()
                    );
                } else if (x.equals(MERGE_PR)) {
                    rootSubscriptionService.unsubscribe(subscriptionKey);
                    peerHandler.sendMessage(stashService.mergePullRequest(
                            stashProjectKey,
                            repoName,
                            prId,
                            pullRequest.getVersion().toString())
                    );
                } else if (x.equals(DELETE_PR)) {
                    rootSubscriptionService.unsubscribe(subscriptionKey);
                    peerHandler.sendMessage(stashService.deletePullRequest(
                            stashProjectKey,
                            repoName,
                            prId,
                            pullRequest.getVersion().toString()
                    ));
                } else if (x.equals(SUBSCRIBE_PR)) {
                    if (rootSubscriptionService.isSubscribed(subscriptionKey)) {
                        peerHandler.sendMessage("Подписка уже оформлена");
                    } else {
                        rootSubscriptionService.subscribe(subscriptionKey,
                                new PullRequestSubscription(
                                        stashService,
                                        botProvider,
                                        peerHandler.getPeer(),
                                        stashProject,
                                        stashRepository,
                                        prId
                                )
                        );
                        peerHandler.sendMessage("Подписка оформлена");
                    }
                } else if (x.equals(UNSUBSCRIBE_PR)) {
                    if (!rootSubscriptionService.isSubscribed(subscriptionKey)) {
                        peerHandler.sendMessage("Подписка еще не оформлена");
                    } else {
                        rootSubscriptionService.unsubscribe(subscriptionKey);
                        peerHandler.sendMessage("Подписка отменена");
                    }
                }
            });
        });

        return PeerHandler.DELAYED_COMMAND;
    }

}
