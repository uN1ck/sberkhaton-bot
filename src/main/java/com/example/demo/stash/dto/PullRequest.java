package com.example.demo.stash.dto;

import com.example.demo.stash.util.Pretty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class PullRequest {
    private final Long id;
    private final String title;
    private final String authorEmail;
    private final String authorDisplayName;
    private final LocalDateTime creationDate;
    private final LocalDateTime updatedDate;
    private final String fromBranch;
    private final String toBranch;
    private final Integer version;

    @Override
    public String toString() {
        return String.format("{ Ключ PR = %s; Заголовок = %s; Версия PR = %s; Автор = %s; Email автора = %s, Дата создания = %s; " +
                        "Дата изменения = %s; Ветка-источник = %s; Ветка-приемник = %s }",
                id, title, version, authorDisplayName, authorEmail,
                Pretty.toString(creationDate), Pretty.toString(updatedDate),
                fromBranch, toBranch);
    }
}
