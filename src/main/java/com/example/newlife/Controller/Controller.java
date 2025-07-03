package com.example.newlife.Controller;

import com.example.newlife.Entity.TestEntity;
import com.example.newlife.Repository.TestEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/app")
public class Controller {

    @Autowired
    private TestEntityRepository repo;

    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        // Save a test row to the database
        TestEntity test = new TestEntity("hello my people wher you at");
        repo.save(test);

        // Fetch it back
        String result = repo.findById(test.getId())
                .map(TestEntity::getMessage)
                .orElse("not found");

        return ResponseEntity.ok(result);
    }
}
