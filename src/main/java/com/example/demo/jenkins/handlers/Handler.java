package com.example.demo.jenkins.handlers;

import im.dlg.botsdk.domain.Peer;

public interface Handler {
    String handle(String tail, Peer sender);
}
