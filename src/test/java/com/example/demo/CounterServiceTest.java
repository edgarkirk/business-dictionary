package com.example.demo;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CounterServiceTest {

    @Test
    void countTo_stores_and_returns_result() {
        CounterRepository repo = new CounterRepository();
        CounterService service = new CounterService(repo);
        assertEquals(10, service.countTo("test", 10));
        assertTrue(repo.findById("test").isPresent());
    }
}
