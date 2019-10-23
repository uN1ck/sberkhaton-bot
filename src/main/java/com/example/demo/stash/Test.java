package com.example.demo.stash;


import com.atlassian.bitbucket.auth.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@RequiredArgsConstructor
public class Test {
    private final AuthenticationService authenticationService;

    @PostConstruct
    public void init() {

        System.out.println(authenticationService);
    }
}
