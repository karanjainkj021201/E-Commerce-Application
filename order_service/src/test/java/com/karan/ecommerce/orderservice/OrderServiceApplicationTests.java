package com.karan.ecommerce.orderservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8080/realms/ecommerce",
        "spring.kafka.bootstrap-servers=localhost:9092"
})
class OrderServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
