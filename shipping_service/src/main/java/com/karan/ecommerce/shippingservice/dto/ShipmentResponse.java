package com.karan.ecommerce.shippingservice.dto;

import com.karan.ecommerce.shippingservice.entity.enums.ShipmentStatus;

import java.time.LocalDateTime;
import java.util.List;

public record ShipmentResponse(
        Long id,
        String shipmentNumber,
        Long orderId,
        String orderNumber,
        String keycloakUserId,
        ShipmentStatus status,
        String carrier,
        String trackingNumber,
        String cancellationReason,
        List<ShipmentStatusHistoryResponse> statusHistory,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime shippedAt,
        LocalDateTime outForDeliveryAt,
        LocalDateTime deliveredAt,
        LocalDateTime cancelledAt
) {}
