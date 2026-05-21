package com.karan.ecommerce.orderservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient productRestClient(
            @Value("${services.product.base-url:http://localhost:8082}") String productServiceBaseUrl
    ) {
        return RestClient.builder()
                .baseUrl(productServiceBaseUrl)
                .build();
    }
}
