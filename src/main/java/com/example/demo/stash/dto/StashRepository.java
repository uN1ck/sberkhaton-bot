package com.example.demo.stash.dto;

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
        return String.format("{ name = %s; state = %s; isForkable = %s; isPublic = %s}",
                name, state, isForkable, isPublic);
    }
}
