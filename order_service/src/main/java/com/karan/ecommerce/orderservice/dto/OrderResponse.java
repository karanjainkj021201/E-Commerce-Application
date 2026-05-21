package com.karan.ecommerce.orderservice.dto;

import com.karan.ecommerce.orderservice.entity.enums.InventoryStatus;
import com.karan.ecommerce.orderservice.entity.enums.OrderStatus;
import com.karan.ecommerce.orderservice.entity.enums.PaymentStatus;
import com.karan.ecommerce.orderservice.entity.enums.ShippingStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderResponse {

    private Long id;
    private String orderNumber;
    private String keycloakUserId;
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private InventoryStatus inventoryStatus;
    private ShippingStatus shippingStatus;
    private BigDecimal subtotal;
    private BigDecimal shippingFee;
    private BigDecimal totalAmount;
    private String currency;
    private String paymentMethod;
    private String paymentReference;
    private String shipmentId;
    private String carrier;
    private String trackingNumber;
    private String failureReason;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String shippingLine1;
    private String shippingLine2;
    private String shippingCity;
    private String shippingState;
    private String shippingPincode;
    private String shippingCountry;
    private List<OrderItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime deliveredAt;

    public OrderResponse(Long id, String orderNumber, String keycloakUserId, OrderStatus status,
                         PaymentStatus paymentStatus, InventoryStatus inventoryStatus, ShippingStatus shippingStatus,
                         BigDecimal subtotal, BigDecimal shippingFee, BigDecimal totalAmount, String currency,
                         String paymentMethod, String paymentReference, String shipmentId, String carrier,
                         String trackingNumber, String failureReason, String customerName, String customerEmail,
                         String customerPhone, String shippingLine1, String shippingLine2, String shippingCity,
                         String shippingState, String shippingPincode, String shippingCountry,
                         List<OrderItemResponse> items, LocalDateTime createdAt, LocalDateTime updatedAt,
                         LocalDateTime confirmedAt, LocalDateTime cancelledAt, LocalDateTime deliveredAt) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.keycloakUserId = keycloakUserId;
        this.status = status;
        this.paymentStatus = paymentStatus;
        this.inventoryStatus = inventoryStatus;
        this.shippingStatus = shippingStatus;
        this.subtotal = subtotal;
        this.shippingFee = shippingFee;
        this.totalAmount = totalAmount;
        this.currency = currency;
        this.paymentMethod = paymentMethod;
        this.paymentReference = paymentReference;
        this.shipmentId = shipmentId;
        this.carrier = carrier;
        this.trackingNumber = trackingNumber;
        this.failureReason = failureReason;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerPhone = customerPhone;
        this.shippingLine1 = shippingLine1;
        this.shippingLine2 = shippingLine2;
        this.shippingCity = shippingCity;
        this.shippingState = shippingState;
        this.shippingPincode = shippingPincode;
        this.shippingCountry = shippingCountry;
        this.items = items;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.confirmedAt = confirmedAt;
        this.cancelledAt = cancelledAt;
        this.deliveredAt = deliveredAt;
    }

    public Long getId() { return id; }
    public String getOrderNumber() { return orderNumber; }
    public String getKeycloakUserId() { return keycloakUserId; }
    public OrderStatus getStatus() { return status; }
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public InventoryStatus getInventoryStatus() { return inventoryStatus; }
    public ShippingStatus getShippingStatus() { return shippingStatus; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getShippingFee() { return shippingFee; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getCurrency() { return currency; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getPaymentReference() { return paymentReference; }
    public String getShipmentId() { return shipmentId; }
    public String getCarrier() { return carrier; }
    public String getTrackingNumber() { return trackingNumber; }
    public String getFailureReason() { return failureReason; }
    public String getCustomerName() { return customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public String getCustomerPhone() { return customerPhone; }
    public String getShippingLine1() { return shippingLine1; }
    public String getShippingLine2() { return shippingLine2; }
    public String getShippingCity() { return shippingCity; }
    public String getShippingState() { return shippingState; }
    public String getShippingPincode() { return shippingPincode; }
    public String getShippingCountry() { return shippingCountry; }
    public List<OrderItemResponse> getItems() { return items; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getConfirmedAt() { return confirmedAt; }
    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public LocalDateTime getDeliveredAt() { return deliveredAt; }
}
