package com.example.demo;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CounterControllerTest {

    @Test
    void count_returns_json_response() {
        CounterRepository repo = new CounterRepository();
        CounterService service = new CounterService(repo);
        CounterController controller = new CounterController(service);
        String result = controller.count("c1", 10);
        assertTrue(result.contains("\"count\":10"));
    }
}
