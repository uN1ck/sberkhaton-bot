package com.example.demo.stash.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class StashProject {
    private final String key;
    private final String name;

    @Override
    public String toString() {
        return String.format("{ Ключ проекта = %s; Название = %s }", key, name);
    }
}
