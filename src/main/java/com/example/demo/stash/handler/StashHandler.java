package com.example.demo.stash.handler;

import com.example.demo.stash.StashService;
import com.example.demo.stash.exceptions.StashConnectionException;
import com.example.demo.stash.exceptions.StashResponseParsingException;
import com.example.demo.stash.util.Pretty;
import im.dlg.botsdk.domain.Message;
import im.dlg.botsdk.domain.Peer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class StashHandler {
    private final StashService stashService;

    public String onMessage(Peer peer, String message) {
        try {
            return onMessageInner(peer, message);
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private String onMessageInner(Peer peer, String message) throws StashResponseParsingException, StashConnectionException {
        if(message.trim().equals("/stash")) {
            return Pretty.toString(Arrays.asList("list projects", "list repos <project-key>"));
        }

        String tail = message.replace("/stash", "").trim();
        if (tail.matches("^list.*")) {
            String listTail = tail.replace("list", "").trim();
            if (listTail.matches("^projects.*$")) {
                return Pretty.toString(stashService.listAllProjects());
            }
            if (listTail.matches("^repos.*")) {
                String stashProjectKey = listTail.replace("repos", "").trim();
                return Pretty.toString(stashService.listRepositories(stashProjectKey));
            }
        }
        return null;
    }
}
