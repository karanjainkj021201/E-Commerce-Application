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
        return TopicBuilder.name(KafkaTopics.ORDER_CREATED).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic orderCancelledTopic() {
        return TopicBuilder.name(KafkaTopics.ORDER_CANCELLED).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic stockReservedTopic() {
        return TopicBuilder.name(KafkaTopics.STOCK_RESERVED).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic stockReservationFailedTopic() {
        return TopicBuilder.name(KafkaTopics.STOCK_RESERVATION_FAILED).partitions(1).replicas(1).build();
    }
}
