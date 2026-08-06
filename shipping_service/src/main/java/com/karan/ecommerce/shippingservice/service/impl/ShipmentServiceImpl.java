package com.karan.ecommerce.shippingservice.service.impl;

import com.karan.ecommerce.shippingservice.dto.*;
import com.karan.ecommerce.shippingservice.entity.ShipmentEntity;
import com.karan.ecommerce.shippingservice.entity.ShipmentStatusHistoryEntity;
import com.karan.ecommerce.shippingservice.entity.enums.ShipmentStatus;
import com.karan.ecommerce.shippingservice.event.OrderCancelledEvent;
import com.karan.ecommerce.shippingservice.event.OrderConfirmedEvent;
import com.karan.ecommerce.shippingservice.exception.BadRequestException;
import com.karan.ecommerce.shippingservice.exception.ResourceNotFoundException;
import com.karan.ecommerce.shippingservice.messaging.ShipmentEventPublisher;
import com.karan.ecommerce.shippingservice.repository.ShipmentRepository;
import com.karan.ecommerce.shippingservice.service.ShipmentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Service
public class ShipmentServiceImpl implements ShipmentService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final DateTimeFormatter NUMBER_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private static final Map<ShipmentStatus, Set<ShipmentStatus>> ALLOWED_TRANSITIONS = Map.of(
            ShipmentStatus.CREATED, EnumSet.of(ShipmentStatus.IN_TRANSIT, ShipmentStatus.CANCELLED),
            ShipmentStatus.IN_TRANSIT, EnumSet.of(ShipmentStatus.OUT_FOR_DELIVERY, ShipmentStatus.CANCELLED),
            ShipmentStatus.OUT_FOR_DELIVERY, EnumSet.of(ShipmentStatus.DELIVERED),
            ShipmentStatus.DELIVERED, EnumSet.noneOf(ShipmentStatus.class),
            ShipmentStatus.CANCELLED, EnumSet.noneOf(ShipmentStatus.class)
    );

    private final ShipmentRepository shipmentRepository;
    private final ShipmentEventPublisher eventPublisher;
    private final String defaultCarrier;
    private final boolean localMode;

    public ShipmentServiceImpl(ShipmentRepository shipmentRepository,
                               ShipmentEventPublisher eventPublisher,
                               @Value("${shipping.default-carrier:ECOM EXPRESS}") String defaultCarrier,
                               @Value("${shipping.local-mode:true}") boolean localMode) {
        this.shipmentRepository = shipmentRepository;
        this.eventPublisher = eventPublisher;
        this.defaultCarrier = defaultCarrier;
        this.localMode = localMode;
    }

    @Override
    @Transactional
    public ShipmentResponse createShipmentFromOrder(OrderConfirmedEvent event) {
        validateOrderEvent(event);

        return shipmentRepository.findByOrderId(event.getOrderId())
                .map(this::mapToResponse)
                .orElseGet(() -> {
                    ShipmentEntity shipment = new ShipmentEntity();
                    shipment.setShipmentNumber(generateNumber("SHP"));
                    shipment.setTrackingNumber(generateNumber("TRK"));
                    shipment.setOrderId(event.getOrderId());
                    shipment.setOrderNumber(event.getOrderNumber().trim());
                    shipment.setKeycloakUserId(event.getKeycloakUserId().trim());
                    shipment.setCarrier(defaultCarrier.trim());
                    shipment.setStatus(ShipmentStatus.CREATED);
                    shipment.addStatusHistory(ShipmentStatus.CREATED, "Shipment created after order confirmation");

                    ShipmentEntity saved = shipmentRepository.save(shipment);
                    eventPublisher.publishShipmentCreated(saved);
                    return mapToResponse(saved);
                });
    }

    @Override
    @Transactional
    public void cancelShipmentFromOrder(OrderCancelledEvent event) {
        if (event == null || event.getOrderId() == null) {
            return;
        }

        shipmentRepository.findByOrderId(event.getOrderId()).ifPresent(shipment -> {
            if (shipment.getStatus() == ShipmentStatus.DELIVERED || shipment.getStatus() == ShipmentStatus.CANCELLED) {
                return;
            }
            applyStatus(shipment, ShipmentStatus.CANCELLED,
                    blankToDefault(event.getReason(), "Order cancelled"));
            shipmentRepository.save(shipment);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ShipmentResponse> getMyShipments(String keycloakUserId, int page, int size) {
        return shipmentRepository.findByKeycloakUserId(keycloakUserId, PageRequest.of(page, size))
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ShipmentResponse getMyShipment(Long id, String keycloakUserId) {
        ShipmentEntity shipment = shipmentRepository.findByIdAndKeycloakUserId(id, keycloakUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found for logged-in user"));
        return mapToResponse(shipment);
    }

    @Override
    @Transactional(readOnly = true)
    public TrackingResponse trackShipment(String trackingNumber) {
        ShipmentEntity shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found for tracking number " + trackingNumber));
        return mapToTrackingResponse(shipment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ShipmentResponse> getShipmentsForAdmin(int page, int size) {
        return shipmentRepository.findAll(PageRequest.of(page, size)).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ShipmentResponse getShipmentForAdmin(Long id) {
        return mapToResponse(getById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public ShipmentResponse getShipmentByOrderIdForAdmin(Long orderId) {
        ShipmentEntity shipment = shipmentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found for order id " + orderId));
        return mapToResponse(shipment);
    }

    @Override
    @Transactional
    public ShipmentResponse updateShipmentDetails(Long id, UpdateShipmentDetailsRequest request) {
        ShipmentEntity shipment = getById(id);
        if (shipment.getStatus() == ShipmentStatus.DELIVERED || shipment.getStatus() == ShipmentStatus.CANCELLED) {
            throw new BadRequestException("Carrier and tracking number cannot be changed after delivery or cancellation");
        }
        shipment.setCarrier(request.getCarrier().trim());
        shipment.setTrackingNumber(request.getTrackingNumber().trim());
        shipment.addStatusHistory(shipment.getStatus(), "Carrier/tracking details updated");
        return mapToResponse(shipmentRepository.save(shipment));
    }

    @Override
    @Transactional
    public ShipmentResponse updateShipmentStatus(Long id, UpdateShipmentStatusRequest request) {
        ShipmentEntity shipment = getById(id);
        ShipmentStatus newStatus = request.getStatus();

        if (shipment.getStatus() == newStatus) {
            return mapToResponse(shipment);
        }

        Set<ShipmentStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(
                shipment.getStatus(), EnumSet.noneOf(ShipmentStatus.class));
        if (!allowed.contains(newStatus)) {
            throw new BadRequestException("Invalid shipment status transition from "
                    + shipment.getStatus() + " to " + newStatus);
        }

        applyStatus(shipment, newStatus, blankToDefault(request.getNote(), "Status updated by admin"));
        ShipmentEntity saved = shipmentRepository.save(shipment);

        if (newStatus == ShipmentStatus.DELIVERED) {
            eventPublisher.publishShipmentDelivered(saved);
        }

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public ShipmentResponse simulateOrderConfirmed(SimulateOrderConfirmedRequest request) {
        if (!localMode) {
            throw new BadRequestException("Local test endpoint is disabled");
        }
        OrderConfirmedEvent event = new OrderConfirmedEvent();
        event.setOrderId(request.getOrderId());
        event.setOrderNumber(request.getOrderNumber());
        event.setKeycloakUserId(request.getKeycloakUserId());
        event.setOccurredAt(LocalDateTime.now());
        return createShipmentFromOrder(event);
    }

    private void applyStatus(ShipmentEntity shipment, ShipmentStatus status, String note) {
        LocalDateTime now = LocalDateTime.now();
        shipment.setStatus(status);

        switch (status) {
            case IN_TRANSIT -> shipment.setShippedAt(now);
            case OUT_FOR_DELIVERY -> shipment.setOutForDeliveryAt(now);
            case DELIVERED -> shipment.setDeliveredAt(now);
            case CANCELLED -> {
                shipment.setCancelledAt(now);
                shipment.setCancellationReason(note);
            }
            default -> { }
        }

        shipment.addStatusHistory(status, note);
    }

    private void validateOrderEvent(OrderConfirmedEvent event) {
        if (event == null || event.getOrderId() == null) {
            throw new BadRequestException("OrderConfirmed event must contain orderId");
        }
        if (event.getOrderNumber() == null || event.getOrderNumber().isBlank()) {
            throw new BadRequestException("OrderConfirmed event must contain orderNumber");
        }
        if (event.getKeycloakUserId() == null || event.getKeycloakUserId().isBlank()) {
            throw new BadRequestException("OrderConfirmed event must contain keycloakUserId");
        }
    }

    private ShipmentEntity getById(Long id) {
        return shipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found for id " + id));
    }

    private String generateNumber(String prefix) {
        int random = 100000 + RANDOM.nextInt(900000);
        return prefix + "-" + LocalDateTime.now().format(NUMBER_TIME) + "-" + random;
    }

    private ShipmentResponse mapToResponse(ShipmentEntity shipment) {
        return new ShipmentResponse(
                shipment.getId(),
                shipment.getShipmentNumber(),
                shipment.getOrderId(),
                shipment.getOrderNumber(),
                shipment.getKeycloakUserId(),
                shipment.getStatus(),
                shipment.getCarrier(),
                shipment.getTrackingNumber(),
                shipment.getCancellationReason(),
                shipment.getStatusHistory().stream().map(this::mapHistory).toList(),
                shipment.getCreatedAt(),
                shipment.getUpdatedAt(),
                shipment.getShippedAt(),
                shipment.getOutForDeliveryAt(),
                shipment.getDeliveredAt(),
                shipment.getCancelledAt()
        );
    }

    private TrackingResponse mapToTrackingResponse(ShipmentEntity shipment) {
        return new TrackingResponse(
                shipment.getShipmentNumber(),
                shipment.getOrderNumber(),
                shipment.getStatus(),
                shipment.getCarrier(),
                shipment.getTrackingNumber(),
                shipment.getStatusHistory().stream().map(this::mapHistory).toList(),
                shipment.getCreatedAt(),
                shipment.getDeliveredAt()
        );
    }

    private ShipmentStatusHistoryResponse mapHistory(ShipmentStatusHistoryEntity history) {
        return new ShipmentStatusHistoryResponse(
                history.getId(), history.getStatus(), history.getNote(), history.getOccurredAt());
    }

    private String blankToDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }
}
