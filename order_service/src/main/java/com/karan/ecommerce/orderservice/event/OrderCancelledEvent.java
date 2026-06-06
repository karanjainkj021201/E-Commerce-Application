package com.karan.ecommerce.orderservice.event;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
public class OrderCancelledEvent {

    private Long orderId;
    private String orderNumber;
    private String keycloakUserId;
    private BigDecimal totalAmount;
    private String currency;
    private String reason;
    private List<OrderItemEvent> items;
    private LocalDateTime occurredAt;

    public OrderCancelledEvent() {
    }

    public OrderCancelledEvent(Long orderId,
                               String orderNumber,
                               String keycloakUserId,
                               BigDecimal totalAmount,
                               String currency,
                               String reason,
                               List<OrderItemEvent> items,
                               LocalDateTime occurredAt) {
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.keycloakUserId = keycloakUserId;
        this.totalAmount = totalAmount;
        this.currency = currency;
        this.reason = reason;
        this.items = items;
        this.occurredAt = occurredAt;
    }

}
