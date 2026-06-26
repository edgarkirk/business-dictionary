package com.example.demo;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CounterAcceptanceTest {

    @Test
    void counts_to_ten_returns_correct_sequence() {
        Counter counter = new Counter();
        assertEquals(10, counter.countTo(10));
    }

    @Test
    void counts_to_zero_returns_zero() {
        Counter counter = new Counter();
        assertEquals(0, counter.countTo(0));
    }
}
