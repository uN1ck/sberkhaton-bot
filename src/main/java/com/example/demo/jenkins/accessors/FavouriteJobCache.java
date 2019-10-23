package com.example.demo.jenkins.accessors;

import com.offbytwo.jenkins.model.Job;

import java.util.List;

public interface FavouriteJobCache<T> {
    List<Job> getFavouriteJobsForUser(T userKey);

    void addToFavourite(T userKey, Job job);

    void removeFromFavourite(T userKey, String jobIdentifier);
}
