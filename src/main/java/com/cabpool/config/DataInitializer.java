package com.cabpool.config;

import com.cabpool.common.enums.Gender;
import com.cabpool.entity.User;
import com.cabpool.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    public static final UUID USER_PRIYA_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    public static final UUID USER_ANANYA_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    public static final UUID USER_RAHUL_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    public static final UUID USER_VIKRAM_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            log.info("Seeding initial test users for cab-sharing demo...");

            User priya = User.builder()
                    .id(USER_PRIYA_ID)
                    .name("Priya Sharma")
                    .email("priya.sharma@example.com")
                    .phone("+91-9876543211")
                    .gender(Gender.FEMALE)
                    .verified(true)
                    .build();

            User ananya = User.builder()
                    .id(USER_ANANYA_ID)
                    .name("Ananya Iyer")
                    .email("ananya.iyer@example.com")
                    .phone("+91-9876543212")
                    .gender(Gender.FEMALE)
                    .verified(true)
                    .build();

            User rahul = User.builder()
                    .id(USER_RAHUL_ID)
                    .name("Rahul Verma")
                    .email("rahul.verma@example.com")
                    .phone("+91-9876543213")
                    .gender(Gender.MALE)
                    .verified(true)
                    .build();

            User vikram = User.builder()
                    .id(USER_VIKRAM_ID)
                    .name("Vikram Rao")
                    .email("vikram.rao@example.com")
                    .phone("+91-9876543214")
                    .gender(Gender.MALE)
                    .verified(true)
                    .build();

            userRepository.saveAll(List.of(priya, ananya, rahul, vikram));
            log.info("Successfully seeded 4 test users (Priya, Ananya, Rahul, Vikram).");
        }
    }
}
