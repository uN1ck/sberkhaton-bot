package com.example.demo.stash.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import java.util.Map;

public class Json {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static  <K, V> Map<K, V> deserializeToMap(String json) {
        try {
            return (Map<K, V>) MAPPER.readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
