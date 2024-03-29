package com.example.demo.stash.util;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import javax.annotation.Nullable;

@Builder
@Getter
public class RestCallConfiguration {
    @NonNull
    private String username;
    @NonNull
    private String password;
    @NonNull
    private String path;
    @NonNull
    private HttpRequestType requestType;
    @Nullable
    private String body;
}
