package com.example.demo.stash;

import com.example.demo.stash.dto.StashProject;
import com.example.demo.stash.dto.StashRepository;
import com.example.demo.stash.exceptions.StashConnectionException;
import com.example.demo.stash.exceptions.StashResponseParsingException;
import com.example.demo.stash.parse.ResponseParsingService;
import com.example.demo.stash.util.HttpRequestType;
import com.example.demo.stash.util.RestCallConfiguration;
import com.example.demo.stash.util.RestCallService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Slf4j
public class StashService {
    private static final String BASE_PATH = "/rest/api/1.0";
    private static final String USERNAME = "kirekov";
    private static final String PASSWORD = "1234";

    private final RestCallService restCallService;
    private final ResponseParsingService responseParsingService;


    private String execRequest(RestCallConfiguration configuration) throws StashConnectionException {
        try {
            return restCallService.call(configuration).get().getResponseBody();
        } catch (InterruptedException e) {
            String err = "Запрос был прерван";
            log.error(err, e);
            throw new StashConnectionException(err);
        } catch (ExecutionException e) {
            String err = "Непредвиденная ошибка";
            log.error(err, e);
            throw new StashConnectionException(err);
        }
    }

    @NonNull
    public List<StashProject> listAllProjects() throws StashConnectionException, StashResponseParsingException {
        RestCallConfiguration configuration = RestCallConfiguration.builder()
                .username(USERNAME)
                .password(PASSWORD)
                .requestType(HttpRequestType.GET)
                .path(BASE_PATH + "/projects")
                .build();
        String json = execRequest(configuration);
        return responseParsingService.listAllProjects(json);
    }

    @NonNull
    public List<StashRepository> listRepositories(String stashProjectKey) throws StashConnectionException, StashResponseParsingException {
        RestCallConfiguration configuration = RestCallConfiguration.builder()
                .username(USERNAME)
                .password(PASSWORD)
                .requestType(HttpRequestType.GET)
                .path(BASE_PATH + String.format("/projects/%s/repos", stashProjectKey))
                .build();
        String json = execRequest(configuration);
        return responseParsingService.listRepositories(json);
    }

}
