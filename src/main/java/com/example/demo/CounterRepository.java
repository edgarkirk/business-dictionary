package com.example.demo;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class CounterRepository {

    private final ConcurrentHashMap<String, Counter> store = new ConcurrentHashMap<>();

    public void save(String id, Counter counter) {
        store.put(id, counter);
    }

    public Optional<Counter> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }
}
