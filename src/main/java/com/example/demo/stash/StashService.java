package com.example.demo.stash;

import com.example.demo.stash.exceptions.StashServiceException;
import com.example.demo.stash.util.HttpRequestType;
import com.example.demo.stash.util.Json;
import com.example.demo.stash.util.RestCallConfiguration;
import com.example.demo.stash.util.RestCallService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Slf4j
public class StashService {
    private static final String BASE_PATH = "/rest/api/1.0";
    private final RestCallService restCallService;

    private String execRequest(RestCallConfiguration configuration) throws StashServiceException {
        try {
            return restCallService.call(configuration).get().getResponseBody();
        } catch (InterruptedException e) {
            String err = "Запрос был прерван";
            log.error(err, e);
            throw new StashServiceException(err);
        } catch (ExecutionException e) {
            String err = "Непредвиденная ошибка";
            log.error(err, e);
            throw new StashServiceException(err);
        }
    }


    public List<String> listAllProjects(String username, String password) throws StashServiceException {
        RestCallConfiguration configuration = RestCallConfiguration.builder()
                .username(username)
                .password(password)
                .requestType(HttpRequestType.GET)
                .path(BASE_PATH + "/projects")
                .build();
        String json = execRequest(configuration);
        Map<String, Object> map = Json.deserializeToMap(json);

    }

}
