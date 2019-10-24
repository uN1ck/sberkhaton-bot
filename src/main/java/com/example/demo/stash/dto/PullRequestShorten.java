package com.example.demo.stash.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PullRequestShorten {
    private final Long id;
    private final String title;

    @Override
    public String toString() {
        return String.format("{ Ключ PR = %s; Заголовок = %s }", id, title);
    }
}
