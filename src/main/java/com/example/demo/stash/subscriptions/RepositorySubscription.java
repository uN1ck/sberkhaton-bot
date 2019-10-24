package com.example.demo.stash.subscriptions;

import com.example.demo.BotProvider;
import com.example.demo.stash.StashService;
import com.example.demo.stash.dto.PullRequestShorten;
import com.example.demo.stash.dto.StashProject;
import com.example.demo.stash.dto.StashRepository;
import im.dlg.botsdk.domain.Peer;
import lombok.RequiredArgsConstructor;

import java.util.List;

public class RepositorySubscription implements Runnable {
    private final StashService stashService;
    private final BotProvider botProvider;
    private final Peer peer;
    private final StashProject stashProject;
    private final StashRepository stashRepository;
    private List<PullRequestShorten> oldPullRequests;

    public RepositorySubscription(StashService stashService,
                                  BotProvider botProvider,
                                  Peer peer,
                                  StashProject stashProject,
                                  StashRepository stashRepository) {
        this.stashService = stashService;
        this.botProvider = botProvider;
        this.peer = peer;
        this.stashProject = stashProject;
        this.stashRepository = stashRepository;
        oldPullRequests = retrieveNewPullRequests();
    }

    private List<PullRequestShorten> retrieveNewPullRequests() {
        return stashService.listPullRequests(stashProject.getKey(), stashRepository.getName());
    }

    @Override
    public void run() {
        List<PullRequestShorten> newPullRequests = retrieveNewPullRequests();
        int newAddedPrs = 0;
        int deletedPrs = 0;
        for (PullRequestShorten newPr : newPullRequests) {
            if (oldPullRequests.stream().noneMatch(pr -> pr.getId().equals(newPr.getId()))) {
                newAddedPrs++;
            }
        }

        for (PullRequestShorten oldPr : oldPullRequests) {
            if (newPullRequests.stream().noneMatch(pr -> pr.getId().equals(oldPr.getId()))) {
                deletedPrs++;
            }
        }
        String message = "";
        if (newAddedPrs > 0) {
            message += "новых PR: " + newAddedPrs + "; ";
        }
        if (deletedPrs > 0) {
            message += "было удалено PR: " + deletedPrs + "; ";
        }

        oldPullRequests = newPullRequests;

        if (!message.isEmpty()) {
            botProvider.getBot()
                    .messaging()
                    .sendText(peer, String.format("В проекте %s в репозитории %s - %s",
                            stashProject.getName(), stashRepository.getName(), message));
        }
    }
}
