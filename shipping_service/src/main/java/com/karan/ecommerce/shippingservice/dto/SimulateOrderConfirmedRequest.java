package com.karan.ecommerce.shippingservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class SimulateOrderConfirmedRequest {
    @NotNull(message = "Order id is required")
    private Long orderId;

    @NotBlank(message = "Order number is required")
    @Size(max = 80)
    private String orderNumber;

    @NotBlank(message = "Keycloak user id is required")
    @Size(max = 100)
    private String keycloakUserId;

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    public String getKeycloakUserId() { return keycloakUserId; }
    public void setKeycloakUserId(String keycloakUserId) { this.keycloakUserId = keycloakUserId; }
}
