package com.example.demo.stash.util;

import lombok.NonNull;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.Response;
import org.asynchttpclient.request.body.multipart.Part;
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
        RequestBuilder builder = new RequestBuilder(configuration.getRequestType().name())
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Basic " + encodedCredentials)
                .setUrl(String.format("%s%s", stashUrl, configuration.getPath()));
        if (configuration.getBody() != null)
            builder.setBody(configuration.getBody());
        return client.executeRequest(builder.build());
    }

}
