package com.example.demo.stash.util;

import lombok.NonNull;
import org.asynchttpclient.*;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.concurrent.Future;

@Service
public class RestCallService {
    // TODO: инжектить урл стэша из пропертей
    private final String stashUrl = "http://172.30.18.95:7990";

    @NonNull
    public Future<Response> call(@NonNull RestCallConfiguration configuration) {
        AsyncHttpClient client = Dsl.asyncHttpClient();

        String encodedCredentials = Base64.getEncoder().encodeToString(
                (configuration.getUsername() + ":" + configuration.getPassword()).getBytes()
        );
        Request request = new RequestBuilder(configuration.getRequestType().name())
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Basic " + encodedCredentials)
                .setUrl(String.format("%s%s", stashUrl, configuration.getPath()))
                .build();
        return client.executeRequest(request);
    }

}
