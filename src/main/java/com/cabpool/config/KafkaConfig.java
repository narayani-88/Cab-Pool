package com.cabpool.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Value("${cabpool.kafka.topics.ride-requested}")
    private String rideRequestedTopic;

    @Value("${cabpool.kafka.topics.ride-matched}")
    private String rideMatchedTopic;

    @Value("${cabpool.kafka.topics.ride-confirmed}")
    private String rideConfirmedTopic;

    @Value("${cabpool.kafka.topics.ride-cancelled}")
    private String rideCancelledTopic;

    @Value("${cabpool.kafka.topics.event-delayed}")
    private String eventDelayedTopic;

    @Bean
    public NewTopic rideRequestedTopic() {
        return TopicBuilder.name(rideRequestedTopic).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic rideMatchedTopic() {
        return TopicBuilder.name(rideMatchedTopic).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic rideConfirmedTopic() {
        return TopicBuilder.name(rideConfirmedTopic).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic rideCancelledTopic() {
        return TopicBuilder.name(rideCancelledTopic).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic eventDelayedTopic() {
        return TopicBuilder.name(eventDelayedTopic).partitions(3).replicas(1).build();
    }
}
