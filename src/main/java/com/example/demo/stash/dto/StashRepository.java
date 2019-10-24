package com.example.demo.stash.dto;

import com.example.demo.stash.util.Pretty;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class StashRepository {
    private final String name;
    private final boolean isForkable;
    private final boolean isPublic;
    private final String state;

    @Override
    public String toString() {
        return String.format("{ Название = %s; Состояние = %s; Можно форкать = %s; Публичный = %s }",
                name, state.equals("AVAILABLE") ? "Доступен" : "Недоступен",
                Pretty.toString(isForkable), Pretty.toString(isPublic));
    }
}
