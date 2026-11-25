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
@CrossOrigin(origins = "*")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody Map<String, String> body) {
        String email = body.getOrDefault("email", "").trim().toLowerCase();
        if (email.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    String name = body.getOrDefault("name", email.split("@")[0]);
                    com.cabpool.common.enums.Gender gender = "FEMALE".equalsIgnoreCase(body.get("gender")) 
                            ? com.cabpool.common.enums.Gender.FEMALE 
                            : com.cabpool.common.enums.Gender.MALE;
                    User newUser = User.builder()
                            .name(name)
                            .email(email)
                            .phone("+91 98765 43210")
                            .gender(gender)
                            .verified(true)
                            .build();
                    return userRepository.save(newUser);
                });
        return ResponseEntity.ok(user);
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
