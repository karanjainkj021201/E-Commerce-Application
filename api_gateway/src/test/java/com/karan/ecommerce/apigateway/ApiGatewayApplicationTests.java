package com.karan.ecommerce.apigateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class ApiGatewayApplicationTests {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private RouteDefinitionLocator routeDefinitionLocator;

    @Test
    void gatewayLoadsAllFrontendRoutes() {
        var routeIds = routeDefinitionLocator.getRouteDefinitions()
                .map(route -> route.getId())
                .collectList()
                .block();

        assertThat(routeIds).containsExactlyInAnyOrder(
                "user-service",
                "product-service",
                "order-service",
                "inventory-service",
                "payment-service",
                "shipping-service"
        );
    }

    @Test
    void healthIsPublic() {
        webTestClient.get()
                .uri("/actuator/health")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void customerEndpointRejectsMissingToken() {
        webTestClient.get()
                .uri("/api/orders/me")
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
