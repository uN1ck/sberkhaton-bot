package com.example.demo.stash.handler;

import com.example.demo.BotProvider;
import com.example.demo.RootSubscriptionService;
import com.example.demo.stash.StashService;
import com.example.demo.stash.dto.StashProject;
import com.example.demo.stash.dto.StashRepository;
import com.example.demo.stash.exceptions.StashCommandException;
import com.example.demo.stash.subscriptions.RepositorySubscription;
import com.example.demo.stash.util.Pretty;
import im.dlg.botsdk.domain.Peer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;

import static com.example.demo.stash.handler.StashHandler.Commands.*;
import static com.example.demo.stash.handler.StashHandler.Placeholders.*;

@Service
@RequiredArgsConstructor
public class StashHandler {
    private final RootSubscriptionService rootSubscriptionService;
    private final StashService stashService;
    private final BotProvider botProvider;

    public String onMessage(Peer peer, String message) {
        try {
            return onMessageInner(peer, message);
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private String formatCommand(String additionalCommand) {
        return String.format("%s %s", ROOT_COMMAND, additionalCommand);
    }

    private String onMessageInner(Peer peer, String message) {
        if (message.trim().equals(ROOT_COMMAND)) {
            return Pretty.toString(
                    Arrays.asList(
                            formatCommand(
                                    String.format("%s %s", LIST_COMMAND, PROJECT_COMMAND)
                            ),
                            formatCommand(
                                    String.format("%s %s %s", LIST_COMMAND, REPO_COMMAND, PROJECT_KEY_PLACEHOLDER)
                            ),
                            formatCommand(
                                    String.format("%s %s %s %s", LIST_COMMAND, PR_COMMAND, PROJECT_KEY_PLACEHOLDER, REPO_NAME_PLACEHOLDER)
                            ),
                            formatCommand(
                                    String.format("%s %s %s %s %s", GET_COMMAND, PR_COMMAND, PROJECT_KEY_PLACEHOLDER, REPO_NAME_PLACEHOLDER, PR_KEY_PLACEHOLDER)
                            ),
                            formatCommand(
                                    String.format("%s %s %s %s %s %s", MERGE_COMMAND, PR_COMMAND, PROJECT_KEY_PLACEHOLDER, REPO_NAME_PLACEHOLDER, PR_KEY_PLACEHOLDER, PR_VERSION_PLACEHOLDER)
                            ),
                            formatCommand(
                                    String.format("%s %s %s %s %s %s", DELETE_COMMAND, PR_COMMAND, PROJECT_KEY_PLACEHOLDER, REPO_NAME_PLACEHOLDER, PR_KEY_PLACEHOLDER, PR_VERSION_PLACEHOLDER)
                            )
                    )
            );
        }

        String[] command = message.trim().split("\\s+");
        if (command[0].equals(ROOT_COMMAND)) {
            if (command[1].equals(LIST_COMMAND)) {
                return listCommand(command);
            } else if (command[1].equals(GET_COMMAND)) {
                return getCommand(command);
            } else if (command[1].equals(MERGE_COMMAND)) {
                return mergeCommand(command);
            } else if (command[1].equals(DELETE_COMMAND)) {
                return deleteCommand(command);
            } else if (command[1].equals(SUBSCRIBE_COMMAND)) {
                return subscribeCommand(peer, command);
            } else if (command[1].equals(UNSUBSCRIBE_COMMAND)) {
                return unsubscribeCommand(peer, command);
            }
        }
        return null;
    }

    private String listCommand(String[] command) {
        if (command.length == 3 && command[2].equals(PROJECT_COMMAND)) {
            return Pretty.toString(stashService.listAllProjects());
        } else if (command.length == 4 && command[2].equals(REPO_COMMAND)) {
            String stashProjectKey = command[3];
            return Pretty.toString(stashService.listRepositories(stashProjectKey));
        } else if (command.length == 5 && command[2].equals(PR_COMMAND)) {
            String stashProjectKey = command[3];
            String repoName = command[4];
            return Pretty.toString(stashService.listPullRequests(stashProjectKey, repoName));
        }
        return null;
    }

    private String getCommand(String[] command) {
        if (command.length == 6 && command[2].equals(PR_COMMAND)) {
            String stashProjectKey = command[3];
            String repoName = command[4];
            String prId = command[5];
            return stashService.getPullRequest(stashProjectKey, repoName, prId).toString();
        }
        return null;
    }

    private String mergeCommand(String[] command) {
        if (command.length == 7 && command[2].equals(PR_COMMAND)) {
            String stashProjectKey = command[3];
            String repoName = command[4];
            String prId = command[5];
            String version = command[6];
            return stashService.mergePullRequest(stashProjectKey, repoName, prId, version);
        }
        return null;
    }

    private String deleteCommand(String[] command) {
        if (command.length == 7 && command[2].equals(PR_COMMAND)) {
            String stashProjectKey = command[3];
            String repoName = command[4];
            String prId = command[5];
            String version = command[6];
            return stashService.deletePullRequest(stashProjectKey, repoName, prId, version);
        }
        return null;
    }

    private String subscribeCommand(Peer peer, String[] command) {
        if (command.length == 5 && command[2].equals(REPO_COMMAND)) {
            String stashProjectKey = command[3];
            String repoName = command[4];

            StashProject stashProject = stashService.getProject(stashProjectKey)
                    .orElseThrow(() -> new StashCommandException(
                            String.format("Проект c ключом %s отсутствует", stashProjectKey)
                    ));
            StashRepository stashRepository = stashService.getRepository(stashProjectKey, repoName)
                    .orElseThrow(() -> new StashCommandException(
                            String.format("Репозиторий %s отсутствует", repoName)
                    ));
            String subscriptionKey = getRepoSubscriptionKey(peer, stashProjectKey, repoName);
            if (rootSubscriptionService.isSubscribed(subscriptionKey))
                return "Подписка уже оформлена";
            rootSubscriptionService.subscribe(subscriptionKey, new RepositorySubscription(stashService, botProvider, peer, stashProject, stashRepository));
            return "Подписка оформлена";
        }
        return null;
    }

    private String unsubscribeCommand(Peer peer, String[] command) {
        if (command.length == 5 && command[2].equals(REPO_COMMAND)) {
            String stashProjectKey = command[3];
            String repoName = command[4];
            String subscriptionKey = getRepoSubscriptionKey(peer, stashProjectKey, repoName);
            if (!rootSubscriptionService.isSubscribed(subscriptionKey))
                return "Подписка еще не оформлена";
            rootSubscriptionService.unsubscribe(subscriptionKey);
            return "Подписка отменена";
        }
        return null;
    }

    private String getRepoSubscriptionKey(Peer peer, String stashProjectKey, String repoName) {
        return String.format("%s_%s_%s", peer.getId(), stashProjectKey, repoName);
    }

    public static class Commands {
        public static final String ROOT_COMMAND = "/stash";
        public static final String LIST_COMMAND = "list";
        public static final String GET_COMMAND = "get";
        public static final String MERGE_COMMAND = "merge";
        public static final String DELETE_COMMAND = "delete";
        public static final String SUBSCRIBE_COMMAND = "sub";
        public static final String UNSUBSCRIBE_COMMAND = "unsub";

        public static final String PROJECT_COMMAND = "project";
        public static final String REPO_COMMAND = "repo";
        public static final String PR_COMMAND = "pr";
    }

    static class Placeholders {
        static final String PROJECT_KEY_PLACEHOLDER = "<Ключ проекта>";
        static final String REPO_NAME_PLACEHOLDER = "<Название репозитория>";
        static final String PR_KEY_PLACEHOLDER = "<Ключ PR>";
        static final String PR_VERSION_PLACEHOLDER = "<Версия PR>";
    }
}
