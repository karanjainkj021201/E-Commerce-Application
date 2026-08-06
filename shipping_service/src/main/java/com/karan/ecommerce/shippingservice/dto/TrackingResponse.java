package com.karan.ecommerce.shippingservice.dto;

import com.karan.ecommerce.shippingservice.entity.enums.ShipmentStatus;

import java.time.LocalDateTime;
import java.util.List;

public record TrackingResponse(
        String shipmentNumber,
        String orderNumber,
        ShipmentStatus status,
        String carrier,
        String trackingNumber,
        List<ShipmentStatusHistoryResponse> statusHistory,
        LocalDateTime createdAt,
        LocalDateTime deliveredAt
) {}
