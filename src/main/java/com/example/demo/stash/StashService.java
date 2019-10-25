package com.example.demo.stash;

import com.example.demo.stash.dto.PullRequest;
import com.example.demo.stash.dto.PullRequestShorten;
import com.example.demo.stash.dto.StashProject;
import com.example.demo.stash.dto.StashRepository;
import com.example.demo.stash.exceptions.StashConnectionException;
import com.example.demo.stash.parse.ResponseParsingService;
import com.example.demo.stash.util.HttpRequestType;
import com.example.demo.stash.util.Json;
import com.example.demo.stash.util.RestCallConfiguration;
import com.example.demo.stash.util.RestCallService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.Response;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
            Response response = restCallService.call(configuration).get();
            return response.getResponseBody();
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
    public List<StashProject> listAllProjects() {
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
    public Optional<StashProject> getProject(String stashProjectKey) {
        RestCallConfiguration configuration = RestCallConfiguration.builder()
                .username(USERNAME)
                .password(PASSWORD)
                .requestType(HttpRequestType.GET)
                .path(BASE_PATH + "/projects/" + stashProjectKey)
                .build();
        String json = execRequest(configuration);
        return responseParsingService.getProject(json);
    }

    @NonNull
    public List<StashRepository> listRepositories(String stashProjectKey) {
        RestCallConfiguration configuration = RestCallConfiguration.builder()
                .username(USERNAME)
                .password(PASSWORD)
                .requestType(HttpRequestType.GET)
                .path(BASE_PATH + String.format("/projects/%s/repos", stashProjectKey))
                .build();
        String json = execRequest(configuration);
        return responseParsingService.listRepositories(json);
    }

    @NonNull
    public Optional<StashRepository> getRepository(String stashProjectKey, String repoName) {
        RestCallConfiguration configuration = RestCallConfiguration.builder()
                .username(USERNAME)
                .password(PASSWORD)
                .requestType(HttpRequestType.GET)
                .path(BASE_PATH + String.format("/projects/%s/repos/%s", stashProjectKey, repoName))
                .build();
        String json = execRequest(configuration);
        return responseParsingService.getRepository(json);
    }

    @NonNull
    public List<PullRequestShorten> listPullRequests(String stashProjectKey, String repoName) {
        RestCallConfiguration configuration = RestCallConfiguration.builder()
                .username(USERNAME)
                .password(PASSWORD)
                .requestType(HttpRequestType.GET)
                .path(BASE_PATH + String.format("/projects/%s/repos/%s/pull-requests",
                        stashProjectKey,
                        repoName)
                )
                .build();
        String json = execRequest(configuration);
        return responseParsingService.listPullRequests(json);
    }

    public PullRequest getPullRequest(String stashProjectKey, String repoName, String prId) {
        RestCallConfiguration configuration = RestCallConfiguration.builder()
                .username(USERNAME)
                .password(PASSWORD)
                .requestType(HttpRequestType.GET)
                .path(BASE_PATH + String.format("/projects/%s/repos/%s/pull-requests/%s",
                        stashProjectKey,
                        repoName,
                        prId)
                )
                .build();
        String json = execRequest(configuration);
        return responseParsingService.getPullRequest(json);
    }

    public String mergePullRequest(String stashProjectKey, String repoName, String prId, String prVersion) {
        RestCallConfiguration configuration = RestCallConfiguration.builder()
                .username(USERNAME)
                .password(PASSWORD)
                .requestType(HttpRequestType.POST)
                .path(BASE_PATH + String.format("/projects/%s/repos/%s/pull-requests/%s/merge?version=%s",
                        stashProjectKey,
                        repoName,
                        prId,
                        prVersion)
                )
                .build();
        String json = execRequest(configuration);
        return responseParsingService.mergePullRequest(json);
    }

    public String deletePullRequest(String stashProjectKey, String repoName, String prId, String prVersion) {
        Map<String, Integer> mappedBody = new HashMap<>();
        mappedBody.put("version", Integer.parseInt(prVersion));

        RestCallConfiguration configuration = RestCallConfiguration.builder()
                .username(USERNAME)
                .password(PASSWORD)
                .requestType(HttpRequestType.DELETE)
                .path(BASE_PATH + String.format("/projects/%s/repos/%s/pull-requests/%s",
                        stashProjectKey,
                        repoName,
                        prId)
                )
                .body(Json.serialize(mappedBody))
                .build();
        String json = execRequest(configuration);
        return responseParsingService.deletePullRequest(json);
    }
}
