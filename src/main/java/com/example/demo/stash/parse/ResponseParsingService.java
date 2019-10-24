package com.example.demo.stash.parse;

import com.example.demo.stash.dto.StashProject;
import com.example.demo.stash.dto.StashRepository;
import com.example.demo.stash.exceptions.StashResponseParsingException;
import com.example.demo.stash.util.Json;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
}
