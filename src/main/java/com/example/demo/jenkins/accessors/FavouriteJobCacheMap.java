package com.example.demo.jenkins.accessors;

import com.offbytwo.jenkins.model.Job;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FavouriteJobCacheMap<T> implements FavouriteJobCache<T> {
    private final Map<T, List<Job>> cache = new HashMap<>();

    @Override
    public List<Job> getFavouriteJobsForUser(T userKey) {
        if (!cache.containsKey(userKey)) {
            cache.put(userKey, new ArrayList<>());
        }
        return cache.get(userKey);
    }

    @Override
    public void addToFavourite(T userKey, Job job) {
        getFavouriteJobsForUser(userKey).add(job);
    }

    @Override
    public void removeFromFavourite(T userKey, String jobIdentifier) {
        List<Job> newJobList = getFavouriteJobsForUser(userKey).stream()
                                                               .filter(job -> job.getFullName().equals(jobIdentifier))
                                                               .collect(Collectors.toList());
        cache.put(userKey, newJobList);

    }

}
