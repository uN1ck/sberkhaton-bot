package com.example.demo.stash.handler;

import com.example.demo.stash.StashService;
import com.example.demo.stash.exceptions.StashCommandException;
import com.example.demo.stash.util.Pretty;
import im.dlg.botsdk.domain.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;

import static com.example.demo.stash.handler.StashHandler.Commands.*;
import static com.example.demo.stash.handler.StashHandler.Placeholders.*;

@Service
@RequiredArgsConstructor
public class StashHandler {

    private final StashService stashService;

    public String onMessage(Message message) {
        try {
            return onMessageInner(message);
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private String formatCommand(String additionalParams) {
        return String.format("%s %s", ROOT_COMMAND, additionalParams);
    }

    private String onMessageInner(Message message) throws Exception {
        if (message.getText().trim().equals(ROOT_COMMAND)) {
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
                                    String.format("%s %s %s %s %s", DELETE_COMMAND, PR_COMMAND, PROJECT_KEY_PLACEHOLDER, REPO_NAME_PLACEHOLDER, PR_KEY_PLACEHOLDER)
                            )
                    )
            );
        }

        String tail = message.getText().replace(ROOT_COMMAND, "").trim();
        if (tail.matches(String.format("^%s.*", LIST_COMMAND))) {
            return listCommand(tail);
        } else if (tail.matches(String.format("^%s.*", GET_COMMAND)))
            return getCommand(tail);
        else if (tail.matches(String.format("^%s.*", MERGE_COMMAND)))
            return mergeCommand(tail);
        else if (tail.matches(String.format("^%s.*", DELETE_COMMAND)))
            return deleteCommand(tail);
        return null;
    }

    private String listCommand(String tail) throws Exception {
        String listTail = tail.replace(LIST_COMMAND, "").trim();
        if (listTail.matches(String.format("^%s.*$", PROJECT_COMMAND))) {
            return Pretty.toString(stashService.listAllProjects());
        } else if (listTail.matches(String.format("^%s.*", REPO_COMMAND))) {
            String stashProjectKey = listTail.replace(REPO_COMMAND, "").trim();
            return Pretty.toString(stashService.listRepositories(stashProjectKey));
        } else if (listTail.matches(String.format("^%s.*", PR_COMMAND))) {
            String[] keys = listTail.replace(PR_COMMAND, "").trim().split("\\s+");
            if (keys.length != 2) {
                throw new StashCommandException(
                        String.format("Необходимо передать два аргумента: %s, %s",
                                PROJECT_KEY_PLACEHOLDER, REPO_NAME_PLACEHOLDER)
                );
            }
            return Pretty.toString(stashService.listPullRequests(keys[0], keys[1]));
        }
        return null;
    }

    private String getCommand(String tail) throws Exception {
        String listTail = tail.replace(GET_COMMAND, "").trim();
        if (listTail.matches(String.format("^%s.*$", PR_COMMAND))) {
            String[] keys = listTail.replace(PR_COMMAND, "").trim().split("\\s+");
            if (keys.length != 3) {
                throw new StashCommandException(
                        String.format("Необходимо передать три аргумента: %s, %s, %s",
                                PROJECT_KEY_PLACEHOLDER, REPO_NAME_PLACEHOLDER, PR_KEY_PLACEHOLDER)
                );
            }
            return stashService.getPullRequest(keys[0], keys[1], keys[2]).toString();
        }
        return null;
    }

    private String mergeCommand(String tail) throws Exception {
        String listTail = tail.replace(MERGE_COMMAND, "").trim();
        if (listTail.matches(String.format("^%s.*$", PR_COMMAND))) {
            String[] keys = listTail.replace(PR_COMMAND, "").trim().split("\\s+");
            if (keys.length != 4) {
                throw new StashCommandException(
                        String.format("Необходимо передать 4 аргумента: %s, %s, %s, %s",
                                PROJECT_KEY_PLACEHOLDER, REPO_NAME_PLACEHOLDER, PR_KEY_PLACEHOLDER, PR_VERSION_PLACEHOLDER)
                );
            }
            return stashService.mergePullRequest(keys[0], keys[1], keys[2], keys[3]);
        }
        return null;
    }

    private String deleteCommand(String tail) throws Exception {
        String listTail = tail.replace(DELETE_COMMAND, "").trim();
        if (listTail.matches(String.format("^%s.*$", PR_COMMAND))) {
            String[] keys = listTail.replace(PR_COMMAND, "").trim().split("\\s+");
            if (keys.length != 4) {
                throw new StashCommandException(
                        String.format("Необходимо передать 4 аргумента: %s %s %s %s",
                                PROJECT_KEY_PLACEHOLDER, REPO_NAME_PLACEHOLDER, PR_KEY_PLACEHOLDER, PR_VERSION_PLACEHOLDER)
                );
            }
            return stashService.deletePullRequest(keys[0], keys[1], keys[2], keys[3]);
        }
        return null;
    }

    public static class Commands {
        public static final String ROOT_COMMAND = "/stash";
        static final String LIST_COMMAND = "list";
        static final String GET_COMMAND = "get";
        static final String MERGE_COMMAND = "merge";
        static final String DELETE_COMMAND = "delete";

        static final String PROJECT_COMMAND = "project";
        static final String REPO_COMMAND = "repo";
        static final String PR_COMMAND = "pr";
    }

    static class Placeholders {
        static final String PROJECT_KEY_PLACEHOLDER = "<Ключ проекта>";
        static final String REPO_NAME_PLACEHOLDER = "<Название репозитория>";
        static final String PR_KEY_PLACEHOLDER = "<Ключ PR>";
        static final String PR_VERSION_PLACEHOLDER = "<Версия PR>";
    }
}
