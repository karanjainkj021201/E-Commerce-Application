package com.karan.ecommerce.inventoryservice.config;

import com.karan.ecommerce.inventoryservice.messaging.KafkaTopics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@EnableKafka
public class KafkaConfig {

    @Bean
    public NewTopic orderCreatedTopic() {
        return topic(KafkaTopics.ORDER_CREATED);
    }

    @Bean
    public NewTopic orderConfirmedTopic() {
        return topic(KafkaTopics.ORDER_CONFIRMED);
    }

    @Bean
    public NewTopic orderCancelledTopic() {
        return topic(KafkaTopics.ORDER_CANCELLED);
    }

    @Bean
    public NewTopic stockReservedTopic() {
        return topic(KafkaTopics.STOCK_RESERVED);
    }

    @Bean
    public NewTopic stockReservationFailedTopic() {
        return topic(KafkaTopics.STOCK_RESERVATION_FAILED);
    }

    private NewTopic topic(String name) {
        return TopicBuilder.name(name).partitions(1).replicas(1).build();
    }
}
