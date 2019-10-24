package com.example.demo.stash.subscriptions;

import com.example.demo.BotProvider;
import com.example.demo.stash.StashService;
import com.example.demo.stash.dto.PullRequest;
import com.example.demo.stash.dto.StashProject;
import com.example.demo.stash.dto.StashRepository;
import im.dlg.botsdk.domain.Peer;


public class PullRequestSubscription implements Runnable {
    private final StashService stashService;
    private final BotProvider botProvider;
    private final Peer peer;
    private final StashProject stashProject;
    private final StashRepository stashRepository;
    private final String prId;
    private PullRequest oldPullRequest;

    public PullRequestSubscription(StashService stashService,
                                   BotProvider botProvider,
                                   Peer peer,
                                   StashProject stashProject,
                                   StashRepository stashRepository,
                                   String prId) {
        this.stashService = stashService;
        this.botProvider = botProvider;
        this.peer = peer;
        this.stashProject = stashProject;
        this.stashRepository = stashRepository;
        this.prId = prId;
        this.oldPullRequest = getNewPullRequest();
    }

    private PullRequest getNewPullRequest() {
        return stashService.getPullRequest(stashProject.getKey(), stashRepository.getName(), prId);
    }


    @Override
    public void run() {
        PullRequest newPullRequest = getNewPullRequest();
        if (!newPullRequest.getUpdatedDate().equals(oldPullRequest.getUpdatedDate())) {
            botProvider.getBot().messaging().sendText(peer,
                    String.format("PR %s в проекте %s в репозитории %s был обновлен",
                            newPullRequest.getTitle(), stashProject.getName(), stashRepository.getName()));
        }
        oldPullRequest = newPullRequest;
    }
}
