package com.karan.ecommerce.orderservice.service.impl;

import com.karan.ecommerce.orderservice.client.ProductClient;
import com.karan.ecommerce.orderservice.dto.*;
import com.karan.ecommerce.orderservice.entity.OrderEntity;
import com.karan.ecommerce.orderservice.entity.OrderItemEntity;
import com.karan.ecommerce.orderservice.entity.enums.InventoryStatus;
import com.karan.ecommerce.orderservice.entity.enums.OrderStatus;
import com.karan.ecommerce.orderservice.entity.enums.PaymentStatus;
import com.karan.ecommerce.orderservice.entity.enums.ShippingStatus;
import com.karan.ecommerce.orderservice.exception.BadRequestException;
import com.karan.ecommerce.orderservice.exception.ResourceNotFoundException;
import com.karan.ecommerce.orderservice.messaging.OrderEventPublisher;
import com.karan.ecommerce.orderservice.repository.OrderRepository;
import com.karan.ecommerce.orderservice.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class OrderServiceImpl implements OrderService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final DateTimeFormatter ORDER_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final OrderEventPublisher eventPublisher;

    public OrderServiceImpl(OrderRepository orderRepository,
                            ProductClient productClient,
                            OrderEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.productClient = productClient;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public OrderResponse createOrder(String keycloakUserId, CreateOrderRequest request, String authorizationHeader) {
        Map<Long, Integer> productQuantityMap = mergeDuplicateProducts(request);

        OrderEntity order = new OrderEntity();
        order.setOrderNumber(generateOrderNumber());
        order.setKeycloakUserId(keycloakUserId);
        order.setStatus(OrderStatus.CREATED);
        order.setPaymentStatus(PaymentStatus.PAYMENT_PENDING);
        order.setInventoryStatus(InventoryStatus.RESERVATION_PENDING);
        order.setShippingStatus(ShippingStatus.NOT_CREATED);
        order.setPaymentMethod(blankToNull(request.getPaymentMethod()));
        applyAddress(order, request.getShippingAddress());

        BigDecimal subtotal = BigDecimal.ZERO;
        String currency = null;

        for (Map.Entry<Long, Integer> entry : productQuantityMap.entrySet()) {
            Long productId = entry.getKey();
            Integer quantity = entry.getValue();

            ProductSnapshotResponse snapshot = productClient.getProductSnapshot(productId, authorizationHeader);

            if (!snapshot.isAvailableForOrder()) {
                throw new BadRequestException("Product " + productId + " is not available for ordering");
            }

            if (snapshot.getPrice() == null) {
                throw new BadRequestException("Product " + productId + " does not have a valid price");
            }

            if (currency == null) {
                currency = snapshot.getCurrency();
            } else if (snapshot.getCurrency() != null && !currency.equalsIgnoreCase(snapshot.getCurrency())) {
                throw new BadRequestException("All order items must use the same currency");
            }

            BigDecimal lineTotal = snapshot.getPrice().multiply(BigDecimal.valueOf(quantity));
            subtotal = subtotal.add(lineTotal);

            OrderItemEntity item = new OrderItemEntity();
            item.setProductId(snapshot.getProductId());
            item.setSku(snapshot.getSku());
            item.setProductNameSnapshot(snapshot.getName());
            item.setUnitPriceSnapshot(snapshot.getPrice());
            item.setCurrency(snapshot.getCurrency());
            item.setQuantity(quantity);
            item.setLineTotal(lineTotal);

            order.addItem(item);
        }

        BigDecimal shippingFee = request.getShippingFee() == null ? BigDecimal.ZERO : request.getShippingFee();

        order.setSubtotal(subtotal);
        order.setShippingFee(shippingFee);
        order.setTotalAmount(subtotal.add(shippingFee));
        order.setCurrency(currency == null ? "INR" : currency);

        OrderEntity savedOrder = orderRepository.save(order);

        eventPublisher.publishOrderCreated(savedOrder);

        return mapToResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getMyOrder(Long id, String keycloakUserId) {
        OrderEntity order = orderRepository.findByIdAndKeycloakUserId(id, keycloakUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found for logged-in user"));

        return mapToResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getMyOrders(String keycloakUserId, int page, int size) {
        return orderRepository.findByKeycloakUserIdOrderByCreatedAtDesc(
                        keycloakUserId,
                        PageRequest.of(page, size)
                )
                .map(this::mapToResponse);
    }

    @Override
    @Transactional
    public OrderResponse cancelMyOrder(Long id, String keycloakUserId) {
        OrderEntity order = orderRepository.findByIdAndKeycloakUserId(id, keycloakUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found for logged-in user"));

        OrderStatus oldStatus = order.getStatus();
        String reason = "Cancelled by customer";

        cancelOrder(order, reason);

        OrderEntity savedOrder = orderRepository.save(order);

        if (oldStatus != OrderStatus.CANCELLED && savedOrder.getStatus() == OrderStatus.CANCELLED) {
            eventPublisher.publishOrderCancelled(savedOrder, reason);
        }

        return mapToResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderForAdmin(Long id) {
        return mapToResponse(getOrderEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrdersForAdmin(int page, int size) {
        return orderRepository.findAll(PageRequest.of(page, size))
                .map(this::mapToResponse);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatusForAdmin(Long id, OrderStatus status, String reason) {
        OrderEntity order = getOrderEntityById(id);

        OrderStatus oldStatus = order.getStatus();

        order.setStatus(status);
        order.setFailureReason(blankToNull(reason));

        if (status == OrderStatus.CANCELLED) {
            order.setCancelledAt(LocalDateTime.now());
        }

        if (status == OrderStatus.DELIVERED) {
            order.setDeliveredAt(LocalDateTime.now());
            order.setShippingStatus(ShippingStatus.DELIVERED);
        }

        OrderEntity savedOrder = orderRepository.save(order);

        if (oldStatus != OrderStatus.CANCELLED && status == OrderStatus.CANCELLED) {
            String cancellationReason = blankToNull(reason) == null
                    ? "Cancelled by admin"
                    : blankToNull(reason);

            eventPublisher.publishOrderCancelled(savedOrder, cancellationReason);
        }

        return mapToResponse(savedOrder);
    }

    @Override
    @Transactional
    public void markPaymentSucceeded(Long orderId, String paymentReference, String gatewayReference) {
        OrderEntity order = getOrderEntityById(orderId);

        if (isTerminal(order)) {
            return;
        }

        order.setPaymentStatus(PaymentStatus.PAYMENT_SUCCEEDED);
        order.setPaymentReference(firstNonBlank(paymentReference, gatewayReference));

        tryConfirmOrder(order);

        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void markPaymentFailed(Long orderId, String failureReason) {
        OrderEntity order = getOrderEntityById(orderId);

        if (isTerminal(order)) {
            return;
        }

        order.setPaymentStatus(PaymentStatus.PAYMENT_FAILED);
        order.setStatus(OrderStatus.PAYMENT_FAILED);
        order.setFailureReason(blankToNull(failureReason));

        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void markStockReserved(Long orderId, String reservationId) {
        OrderEntity order = getOrderEntityById(orderId);

        if (isTerminal(order)) {
            return;
        }

        order.setInventoryStatus(InventoryStatus.RESERVED);

        tryConfirmOrder(order);

        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void markStockReservationFailed(Long orderId, String failureReason) {
        OrderEntity order = getOrderEntityById(orderId);

        if (isTerminal(order)) {
            return;
        }

        order.setInventoryStatus(InventoryStatus.RESERVATION_FAILED);
        order.setStatus(OrderStatus.STOCK_FAILED);
        order.setFailureReason(blankToNull(failureReason));

        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void markShipmentCreated(Long orderId, String shipmentId, String carrier, String trackingNumber) {
        OrderEntity order = getOrderEntityById(orderId);

        if (isTerminal(order)) {
            return;
        }

        order.setShipmentId(shipmentId);
        order.setCarrier(carrier);
        order.setTrackingNumber(trackingNumber);
        order.setShippingStatus(ShippingStatus.CREATED);
        order.setStatus(OrderStatus.SHIPMENT_CREATED);

        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void markShipmentDelivered(Long orderId, String shipmentId, String trackingNumber) {
        OrderEntity order = getOrderEntityById(orderId);

        if (order.getStatus() == OrderStatus.CANCELLED) {
            return;
        }

        order.setShipmentId(firstNonBlank(shipmentId, order.getShipmentId()));
        order.setTrackingNumber(firstNonBlank(trackingNumber, order.getTrackingNumber()));
        order.setShippingStatus(ShippingStatus.DELIVERED);
        order.setStatus(OrderStatus.DELIVERED);
        order.setDeliveredAt(LocalDateTime.now());

        orderRepository.save(order);
    }

    private void tryConfirmOrder(OrderEntity order) {
        if (order.getPaymentStatus() == PaymentStatus.PAYMENT_SUCCEEDED
                && order.getInventoryStatus() == InventoryStatus.RESERVED
                && order.getStatus() == OrderStatus.CREATED) {

            order.setStatus(OrderStatus.CONFIRMED);
            order.setConfirmedAt(LocalDateTime.now());

            eventPublisher.publishOrderConfirmed(order);
        }
    }

    private void cancelOrder(OrderEntity order, String reason) {
        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new BadRequestException("Delivered order cannot be cancelled");
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            return;
        }

        if (order.getStatus() == OrderStatus.SHIPMENT_CREATED) {
            throw new BadRequestException("Shipment already created. Please use return/refund flow later.");
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        order.setFailureReason(reason);
    }

    private boolean isTerminal(OrderEntity order) {
        return order.getStatus() == OrderStatus.CANCELLED
                || order.getStatus() == OrderStatus.DELIVERED
                || order.getStatus() == OrderStatus.PAYMENT_FAILED
                || order.getStatus() == OrderStatus.STOCK_FAILED;
    }

    private Map<Long, Integer> mergeDuplicateProducts(CreateOrderRequest request) {
        Map<Long, Integer> merged = new LinkedHashMap<>();

        request.getItems().forEach(item ->
                merged.merge(item.getProductId(), item.getQuantity(), Integer::sum)
        );

        return merged;
    }

    private void applyAddress(OrderEntity order, AddressRequest address) {
        order.setCustomerName(address.getCustomerName().trim());
        order.setCustomerEmail(address.getCustomerEmail().trim().toLowerCase());
        order.setCustomerPhone(address.getCustomerPhone().trim());
        order.setShippingLine1(address.getLine1().trim());
        order.setShippingLine2(blankToNull(address.getLine2()));
        order.setShippingCity(address.getCity().trim());
        order.setShippingState(address.getState().trim());
        order.setShippingPincode(address.getPincode().trim());
        order.setShippingCountry(address.getCountry().trim());
    }

    private OrderEntity getOrderEntityById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found for id " + id));
    }

    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(ORDER_DATE_FORMAT);
        int randomNumber = 100000 + RANDOM.nextInt(900000);
        return "ORD-" + timestamp + "-" + randomNumber;
    }

    private OrderResponse mapToResponse(OrderEntity order) {
        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getKeycloakUserId(),
                order.getStatus(),
                order.getPaymentStatus(),
                order.getInventoryStatus(),
                order.getShippingStatus(),
                order.getSubtotal(),
                order.getShippingFee(),
                order.getTotalAmount(),
                order.getCurrency(),
                order.getPaymentMethod(),
                order.getPaymentReference(),
                order.getShipmentId(),
                order.getCarrier(),
                order.getTrackingNumber(),
                order.getFailureReason(),
                order.getCustomerName(),
                order.getCustomerEmail(),
                order.getCustomerPhone(),
                order.getShippingLine1(),
                order.getShippingLine2(),
                order.getShippingCity(),
                order.getShippingState(),
                order.getShippingPincode(),
                order.getShippingCountry(),
                order.getItems().stream().map(this::mapItemToResponse).toList(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getConfirmedAt(),
                order.getCancelledAt(),
                order.getDeliveredAt()
        );
    }

    private OrderItemResponse mapItemToResponse(OrderItemEntity item) {
        return new OrderItemResponse(
                item.getId(),
                item.getProductId(),
                item.getSku(),
                item.getProductNameSnapshot(),
                item.getUnitPriceSnapshot(),
                item.getCurrency(),
                item.getQuantity(),
                item.getLineTotal()
        );
    }

    private String blankToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim();
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.trim().isEmpty()) {
            return first.trim();
        }

        return blankToNull(second);
    }
}