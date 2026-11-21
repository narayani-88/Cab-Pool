package com.cabpool.config;

import com.cabpool.common.enums.Gender;
import com.cabpool.entity.User;
import com.cabpool.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    @Override
    public void run(String... args) {
        log.info("Checking and seeding realistic test personas for cab-sharing demo...");

        List<User> seedUsers = List.of(
                // Verified Females (HSR, Koramangala, Indiranagar, Whitefield)
                User.builder().name("Priya Sharma").email("priya.sharma@example.com").phone("+91-9876543211").gender(Gender.FEMALE).verified(true).build(),
                User.builder().name("Ananya Iyer").email("ananya.iyer@example.com").phone("+91-9876543212").gender(Gender.FEMALE).verified(true).build(),
                User.builder().name("Sneha Patel").email("sneha.patel@example.com").phone("+91-9876543215").gender(Gender.FEMALE).verified(true).build(),
                User.builder().name("Riya Sen").email("riya.sen@example.com").phone("+91-9876543216").gender(Gender.FEMALE).verified(true).build(),
                User.builder().name("Kavita Reddy").email("kavita.reddy@example.com").phone("+91-9876543217").gender(Gender.FEMALE).verified(true).build(),
                User.builder().name("Pooja Hegde").email("pooja.hegde@example.com").phone("+91-9876543218").gender(Gender.FEMALE).verified(true).build(),
                User.builder().name("Neha Gupta").email("neha.gupta@example.com").phone("+91-9876543219").gender(Gender.FEMALE).verified(true).build(),
                User.builder().name("Divya Nair").email("divya.nair@example.com").phone("+91-9876543220").gender(Gender.FEMALE).verified(true).build(),

                // Verified Males (Whitefield, Electronic City, HSR, Bellandur)
                User.builder().name("Rahul Verma").email("rahul.verma@example.com").phone("+91-9876543213").gender(Gender.MALE).verified(true).build(),
                User.builder().name("Vikram Rao").email("vikram.rao@example.com").phone("+91-9876543214").gender(Gender.MALE).verified(true).build(),
                User.builder().name("Rohan Mehta").email("rohan.mehta@example.com").phone("+91-9876543221").gender(Gender.MALE).verified(true).build(),
                User.builder().name("Arjun Nair").email("arjun.nair@example.com").phone("+91-9876543222").gender(Gender.MALE).verified(true).build(),
                User.builder().name("Siddharth Roy").email("siddharth.roy@example.com").phone("+91-9876543223").gender(Gender.MALE).verified(true).build(),
                User.builder().name("Amit Kumar").email("amit.kumar@example.com").phone("+91-9876543224").gender(Gender.MALE).verified(true).build(),
                User.builder().name("Nikhil Joshi").email("nikhil.joshi@example.com").phone("+91-9876543225").gender(Gender.MALE).verified(true).build(),
                User.builder().name("Karthik Subramanian").email("karthik.s@example.com").phone("+91-9876543226").gender(Gender.MALE).verified(true).build()
        );

        List<User> toInsert = new ArrayList<>();
        for (User u : seedUsers) {
            if (userRepository.findByEmail(u.getEmail()).isEmpty()) {
                toInsert.add(u);
            }
        }

        if (!toInsert.isEmpty()) {
            userRepository.saveAll(toInsert);
            log.info("Inserted {} new test personas. Total users in DB: {}", toInsert.size(), userRepository.count());
        } else {
            log.info("All test personas are already present in PostgreSQL (count: {}).", userRepository.count());
        }
    }
}
