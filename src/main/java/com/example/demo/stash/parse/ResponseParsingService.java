package com.example.demo.stash.parse;

import com.example.demo.stash.dto.PullRequest;
import com.example.demo.stash.dto.PullRequestShorten;
import com.example.demo.stash.dto.StashProject;
import com.example.demo.stash.dto.StashRepository;
import com.example.demo.stash.exceptions.StashResponseParsingException;
import com.example.demo.stash.util.Json;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ResponseParsingService {
    private static final String ERR_MESSAGE = "Ошибка при парсинге ответа от Stash. Обратитесь к администратору";

    @NonNull
    public List<StashProject> listAllProjects(String jsonResponse) throws StashResponseParsingException {
        try {
            return innerListAllProjects(jsonResponse);
        } catch (Exception e) {
            log.error(ERR_MESSAGE, e);
            throw new StashResponseParsingException(ERR_MESSAGE);
        }
    }

    private List<StashProject> innerListAllProjects(String jsonResponse) {
        Objects.requireNonNull(jsonResponse);
        Map<String, Object> map = Json.deserializeToMap(jsonResponse);
        List<Map<String, Object>> values = (List<Map<String, Object>>) map.get("values");
        return values.stream()
                .map(m -> new StashProject((String) m.get("key"), (String) m.get("name")))
                .collect(Collectors.toList());
    }

    @NonNull
    public List<StashRepository> listRepositories(String jsonResponse) throws StashResponseParsingException {
        try {
            return innerListRepositories(jsonResponse);
        } catch (Exception e) {
            log.error(ERR_MESSAGE, e);
            throw new StashResponseParsingException(ERR_MESSAGE);
        }
    }

    private List<StashRepository> innerListRepositories(String jsonResponse) {
        Objects.requireNonNull(jsonResponse);
        Map<String, Object> map = Json.deserializeToMap(jsonResponse);
        List<Map<String, Object>> values = (List<Map<String, Object>>) map.get("values");
        return values.stream()
                .map(m -> StashRepository.builder()
                        .isForkable((boolean) m.get("forkable"))
                        .isPublic((boolean) m.get("public"))
                        .name((String) m.get("slug"))
                        .state((String) m.get("statusMessage"))
                        .build())
                .collect(Collectors.toList());
    }

    @NonNull
    public List<PullRequestShorten> listPullRequests(String jsonResponse) throws StashResponseParsingException {
        try {
            return innerListPullRequests(jsonResponse);
        } catch (Exception e) {
            log.error(ERR_MESSAGE, e);
            throw new StashResponseParsingException(ERR_MESSAGE);
        }
    }

    private List<PullRequestShorten> innerListPullRequests(String jsonResponse) {
        Objects.requireNonNull(jsonResponse);
        Map<String, Object> map = Json.deserializeToMap(jsonResponse);
        List<Map<String, Object>> values = (List<Map<String, Object>>) map.get("values");
        return values.stream()
                .map(m -> PullRequestShorten.builder()
                        .title((String) m.get("title"))
                        .id(Long.parseLong(m.get("id").toString()))
                        .build())
                .collect(Collectors.toList());
    }

    @NonNull
    public PullRequest getPullRequest(String jsonResponse) throws StashResponseParsingException {
        try {
            return innerGetPullRequest(jsonResponse);
        } catch (Exception e) {
            log.error(ERR_MESSAGE, e);
            throw new StashResponseParsingException(ERR_MESSAGE);
        }
    }

    private PullRequest innerGetPullRequest(String jsonResponse) {
        Objects.requireNonNull(jsonResponse);
        Map<String, Object> map = Json.deserializeToMap(jsonResponse);
        Map<String, Object> author = ((Map<String, Map>) map.get("author")).get("user");
        Map<String, Object> fromRef = (Map<String, Object>) map.get("fromRef");
        Map<String, Object> toRef = (Map<String, Object>) map.get("toRef");


        return PullRequest.builder()
                .version((Integer) map.get("version"))
                .authorDisplayName((String) author.get("displayName"))
                .authorEmail((String) author.get("emailAddress"))
                .creationDate(LocalDateTime.ofInstant(Instant.ofEpochMilli(
                        Long.parseLong(map.get("createdDate").toString())
                ), ZoneId.systemDefault()))
                .updatedDate(LocalDateTime.ofInstant(Instant.ofEpochMilli(
                        Long.parseLong(map.get("updatedDate").toString())
                ), ZoneId.systemDefault()))
                .title((String) map.get("title"))
                .id(Long.parseLong(map.get("id").toString()))
                .fromBranch((String) fromRef.get("displayId"))
                .toBranch((String) toRef.get("displayId"))
                .build();
    }

    @NonNull
    public String mergePullRequest(String jsonResponse) {
        Objects.requireNonNull(jsonResponse);
        Map<String, Object> map = Json.deserializeToMap(jsonResponse);
        if (map != null && map.get("errors") != null) {
            List<Map<String, Object>> errors = (List<Map<String, Object>>) map.get("errors");
            return "Ошибка при вливании PR: " + errors.stream()
                    .map(m -> (String) m.get("message"))
                    .collect(Collectors.joining("; "));
        }
        return "PR был успешно влит";
    }

    @NonNull
    public String deletePullRequest(String jsonResponse) {
        Objects.requireNonNull(jsonResponse);
        Map<String, Object> map = Json.deserializeToMap(jsonResponse);
        if (map != null && map.get("errors") != null) {
            List<Map<String, Object>> errors = (List<Map<String, Object>>) map.get("errors");
            return "Ошибка при удалении PR: " + errors.stream()
                    .map(m -> (String) m.get("message"))
                    .collect(Collectors.joining("; "));
        }
        return "PR был успешно удален";
    }
}
