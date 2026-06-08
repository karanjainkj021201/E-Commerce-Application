package com.karan.ecommerce.paymentservice.config;

import com.karan.ecommerce.paymentservice.messaging.KafkaTopics;
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
    public NewTopic paymentSucceededTopic() {
        return TopicBuilder.name(KafkaTopics.PAYMENT_SUCCEEDED).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic paymentFailedTopic() {
        return TopicBuilder.name(KafkaTopics.PAYMENT_FAILED).partitions(1).replicas(1).build();
    }
}
