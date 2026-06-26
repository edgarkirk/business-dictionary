package com.example.demo;

public class CounterService {

    private final CounterRepository repository;

    public CounterService(CounterRepository repository) {
        this.repository = repository;
    }

    public int countTo(String id, int target) {
        Counter counter = new Counter();
        int result = counter.countTo(target);
        repository.save(id, counter);
        return result;
    }
}
