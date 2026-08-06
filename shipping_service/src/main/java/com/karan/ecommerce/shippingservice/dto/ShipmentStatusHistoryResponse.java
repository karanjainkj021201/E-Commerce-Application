package com.karan.ecommerce.shippingservice.dto;

import com.karan.ecommerce.shippingservice.entity.enums.ShipmentStatus;

import java.time.LocalDateTime;

public record ShipmentStatusHistoryResponse(
        Long id,
        ShipmentStatus status,
        String note,
        LocalDateTime occurredAt
) {}
