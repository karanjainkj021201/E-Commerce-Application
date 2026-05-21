package com.karan.ecommerce.orderservice.client;

import com.karan.ecommerce.orderservice.dto.ProductSnapshotResponse;
import com.karan.ecommerce.orderservice.exception.ExternalServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class ProductClient {

    private final RestClient productRestClient;
    private final String productServiceToken;

    public ProductClient(RestClient productRestClient,
                         @Value("${services.product.service-token:}") String productServiceToken) {
        this.productRestClient = productRestClient;
        this.productServiceToken = productServiceToken;
    }

    public ProductSnapshotResponse getProductSnapshot(Long productId, String userAuthorizationHeader) {
        String bearerToken = resolveBearerToken(userAuthorizationHeader);
        try {
            ProductSnapshotResponse response = productRestClient.get()
                    .uri("/internal/products/{id}/snapshot", productId)
                    .headers(headers -> headers.setBearerAuth(bearerToken))
                    .retrieve()
                    .body(ProductSnapshotResponse.class);

            if (response == null) {
                throw new ExternalServiceException("Product Service returned empty product snapshot");
            }
            return response;
        } catch (RestClientResponseException ex) {
            throw new ExternalServiceException("Product snapshot fetch failed for product id " + productId
                    + ". Product Service status: " + ex.getStatusCode(), ex);
        } catch (RestClientException ex) {
            throw new ExternalServiceException("Product Service is not reachable", ex);
        }
    }

    private String resolveBearerToken(String userAuthorizationHeader) {
        String token = isBlank(productServiceToken) ? userAuthorizationHeader : productServiceToken;
        if (isBlank(token)) {
            throw new ExternalServiceException("No token available to call Product Service snapshot API");
        }
        return token.replace("Bearer ", "").trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
