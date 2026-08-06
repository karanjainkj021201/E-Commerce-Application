package com.karan.ecommerce.shippingservice.config;

import com.karan.ecommerce.shippingservice.messaging.KafkaTopics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@EnableKafka
public class KafkaConfig {

    @Bean
    public NewTopic orderConfirmedTopic() {
        return TopicBuilder.name(KafkaTopics.ORDER_CONFIRMED).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic orderCancelledTopic() {
        return TopicBuilder.name(KafkaTopics.ORDER_CANCELLED).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic shipmentCreatedTopic() {
        return TopicBuilder.name(KafkaTopics.SHIPMENT_CREATED).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic shipmentDeliveredTopic() {
        return TopicBuilder.name(KafkaTopics.SHIPMENT_DELIVERED).partitions(1).replicas(1).build();
    }
}
