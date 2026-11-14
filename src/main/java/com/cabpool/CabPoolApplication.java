package com.cabpool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableKafka
@EnableScheduling
@EnableJpaRepositories(basePackages = "com.cabpool.repository")
public class CabPoolApplication {

    static {
        // Fix for modern PostgreSQL which rejects deprecated legacy "Asia/Calcutta" in favor of "Asia/Kolkata"
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
        System.setProperty("user.timezone", "Asia/Kolkata");
    }

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
        System.setProperty("user.timezone", "Asia/Kolkata");
        SpringApplication.run(CabPoolApplication.class, args);
    }
}
