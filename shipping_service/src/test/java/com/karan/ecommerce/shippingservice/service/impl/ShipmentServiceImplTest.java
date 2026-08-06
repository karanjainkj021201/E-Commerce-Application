package com.karan.ecommerce.shippingservice.service.impl;

import com.karan.ecommerce.shippingservice.dto.UpdateShipmentStatusRequest;
import com.karan.ecommerce.shippingservice.entity.ShipmentEntity;
import com.karan.ecommerce.shippingservice.entity.enums.ShipmentStatus;
import com.karan.ecommerce.shippingservice.event.OrderConfirmedEvent;
import com.karan.ecommerce.shippingservice.messaging.ShipmentEventPublisher;
import com.karan.ecommerce.shippingservice.repository.ShipmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShipmentServiceImplTest {

    @Mock
    private ShipmentRepository repository;

    @Mock
    private ShipmentEventPublisher publisher;

    private ShipmentServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ShipmentServiceImpl(repository, publisher, "ECOM EXPRESS", true);
        when(repository.save(any(ShipmentEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createsShipmentIdempotentlyFromOrderConfirmed() {
        OrderConfirmedEvent event = event(10L);
        when(repository.findByOrderId(10L)).thenReturn(Optional.empty());

        var response = service.createShipmentFromOrder(event);

        assertEquals(10L, response.orderId());
        assertEquals(ShipmentStatus.CREATED, response.status());
        verify(repository).save(any(ShipmentEntity.class));
        verify(publisher).publishShipmentCreated(any(ShipmentEntity.class));
    }

    @Test
    void movesShipmentThroughDeliveryAndPublishesDeliveredEvent() {
        ShipmentEntity shipment = shipment();
        when(repository.findById(1L)).thenReturn(Optional.of(shipment));

        service.updateShipmentStatus(1L, status(ShipmentStatus.IN_TRANSIT));
        service.updateShipmentStatus(1L, status(ShipmentStatus.OUT_FOR_DELIVERY));
        var response = service.updateShipmentStatus(1L, status(ShipmentStatus.DELIVERED));

        assertEquals(ShipmentStatus.DELIVERED, response.status());
        verify(publisher).publishShipmentDelivered(shipment);
    }

    private OrderConfirmedEvent event(Long orderId) {
        OrderConfirmedEvent event = new OrderConfirmedEvent();
        event.setOrderId(orderId);
        event.setOrderNumber("ORD-TEST-" + orderId);
        event.setKeycloakUserId("user-123");
        return event;
    }

    private ShipmentEntity shipment() {
        ShipmentEntity shipment = new ShipmentEntity();
        shipment.setOrderId(10L);
        shipment.setOrderNumber("ORD-TEST-10");
        shipment.setKeycloakUserId("user-123");
        shipment.setShipmentNumber("SHP-TEST-10");
        shipment.setTrackingNumber("TRK-TEST-10");
        shipment.setCarrier("ECOM EXPRESS");
        shipment.setStatus(ShipmentStatus.CREATED);
        shipment.addStatusHistory(ShipmentStatus.CREATED, "Created");
        return shipment;
    }

    private UpdateShipmentStatusRequest status(ShipmentStatus status) {
        UpdateShipmentStatusRequest request = new UpdateShipmentStatusRequest();
        request.setStatus(status);
        request.setNote("Test update");
        return request;
    }
}
