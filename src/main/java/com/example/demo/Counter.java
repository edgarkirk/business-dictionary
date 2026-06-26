package com.example.demo;

public class Counter {

    private int value;

    public Counter() {
        this.value = 0;
    }

    public int getValue() {
        return value;
    }

    public void increment() {
        value++;
    }

    public int countTo(int target) {
        for (int i = 0; i < target; i++) {
            increment();
        }
        return value;
    }
}
