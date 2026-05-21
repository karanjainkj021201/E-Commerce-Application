package com.karan.ecommerce.orderservice.service;

import com.karan.ecommerce.orderservice.dto.CreateOrderRequest;
import com.karan.ecommerce.orderservice.dto.OrderResponse;
import com.karan.ecommerce.orderservice.entity.enums.OrderStatus;
import org.springframework.data.domain.Page;

public interface OrderService {
    OrderResponse createOrder(String keycloakUserId, CreateOrderRequest request, String authorizationHeader);
    OrderResponse getMyOrder(Long id, String keycloakUserId);
    Page<OrderResponse> getMyOrders(String keycloakUserId, int page, int size);
    OrderResponse cancelMyOrder(Long id, String keycloakUserId);
    OrderResponse getOrderForAdmin(Long id);
    Page<OrderResponse> getOrdersForAdmin(int page, int size);
    OrderResponse updateOrderStatusForAdmin(Long id, OrderStatus status, String reason);
    void markPaymentSucceeded(Long orderId, String paymentReference, String gatewayReference);
    void markPaymentFailed(Long orderId, String failureReason);
    void markStockReserved(Long orderId, String reservationId);
    void markStockReservationFailed(Long orderId, String failureReason);
    void markShipmentCreated(Long orderId, String shipmentId, String carrier, String trackingNumber);
    void markShipmentDelivered(Long orderId, String shipmentId, String trackingNumber);
}
