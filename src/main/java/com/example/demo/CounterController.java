package com.example.demo;

public class CounterController {

    private final CounterService service;

    public CounterController(CounterService service) {
        this.service = service;
    }

    public String count(String id, int target) {
        int result = service.countTo(id, target);
        return "{\"id\":\"" + id + "\",\"count\":" + result + "}";
    }
}
