package com.cabpool.api.controller;

import com.cabpool.entity.User;
import com.cabpool.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    /**
     * Helper endpoint for demo UI: returns Priya and Ananya by their email.
     */
    @GetMapping("/demo")
    public ResponseEntity<Map<String, User>> getDemoUsers() {
        User priya = userRepository.findByEmail("priya.sharma@example.com")
                .orElse(null);
        User ananya = userRepository.findByEmail("ananya.iyer@example.com")
                .orElse(null);

        return ResponseEntity.ok(Map.of(
                "priya", priya != null ? priya : new User(),
                "ananya", ananya != null ? ananya : new User()
        ));
    }
}
