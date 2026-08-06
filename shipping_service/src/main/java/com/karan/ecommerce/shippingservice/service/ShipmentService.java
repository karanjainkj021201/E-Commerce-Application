package com.karan.ecommerce.shippingservice.service;

import com.karan.ecommerce.shippingservice.dto.*;
import com.karan.ecommerce.shippingservice.event.OrderCancelledEvent;
import com.karan.ecommerce.shippingservice.event.OrderConfirmedEvent;
import org.springframework.data.domain.Page;

public interface ShipmentService {
    ShipmentResponse createShipmentFromOrder(OrderConfirmedEvent event);
    void cancelShipmentFromOrder(OrderCancelledEvent event);

    Page<ShipmentResponse> getMyShipments(String keycloakUserId, int page, int size);
    ShipmentResponse getMyShipment(Long id, String keycloakUserId);
    TrackingResponse trackShipment(String trackingNumber);

    Page<ShipmentResponse> getShipmentsForAdmin(int page, int size);
    ShipmentResponse getShipmentForAdmin(Long id);
    ShipmentResponse getShipmentByOrderIdForAdmin(Long orderId);
    ShipmentResponse updateShipmentDetails(Long id, UpdateShipmentDetailsRequest request);
    ShipmentResponse updateShipmentStatus(Long id, UpdateShipmentStatusRequest request);
    ShipmentResponse simulateOrderConfirmed(SimulateOrderConfirmedRequest request);
}
