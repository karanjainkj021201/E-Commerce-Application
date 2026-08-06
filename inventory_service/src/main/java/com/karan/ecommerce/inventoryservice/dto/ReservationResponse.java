package com.karan.ecommerce.inventoryservice.dto;

import com.karan.ecommerce.inventoryservice.entity.enums.ReservationStatus;

import java.time.LocalDateTime;
import java.util.List;

public class ReservationResponse {
    private Long id;
    private String reservationNumber;
    private Long orderId;
    private String orderNumber;
    private String warehouseCode;
    private ReservationStatus status;
    private String failureReason;
    private String releaseReason;
    private List<ReservationItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime releasedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime committedAt;

    public ReservationResponse(Long id,
                               String reservationNumber,
                               Long orderId,
                               String orderNumber,
                               String warehouseCode,
                               ReservationStatus status,
                               String failureReason,
                               String releaseReason,
                               List<ReservationItemResponse> items,
                               LocalDateTime createdAt,
                               LocalDateTime updatedAt,
                               LocalDateTime releasedAt,
                               LocalDateTime expiresAt,
                               LocalDateTime committedAt) {
        this.id = id;
        this.reservationNumber = reservationNumber;
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.warehouseCode = warehouseCode;
        this.status = status;
        this.failureReason = failureReason;
        this.releaseReason = releaseReason;
        this.items = items;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.releasedAt = releasedAt;
        this.expiresAt = expiresAt;
        this.committedAt = committedAt;
    }

    public Long getId() { return id; }
    public String getReservationNumber() { return reservationNumber; }
    public Long getOrderId() { return orderId; }
    public String getOrderNumber() { return orderNumber; }
    public String getWarehouseCode() { return warehouseCode; }
    public ReservationStatus getStatus() { return status; }
    public String getFailureReason() { return failureReason; }
    public String getReleaseReason() { return releaseReason; }
    public List<ReservationItemResponse> getItems() { return items; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getReleasedAt() { return releasedAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public LocalDateTime getCommittedAt() { return committedAt; }
}
