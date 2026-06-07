package com.karan.ecommerce.inventoryservice.entity;

import com.karan.ecommerce.inventoryservice.entity.enums.ReservationStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stock_reservations")
public class StockReservationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reservation_number", nullable = false, unique = true, length = 80)
    private String reservationNumber;

    @Column(name = "order_id", unique = true)
    private Long orderId;

    @Column(name = "order_number", length = 80)
    private String orderNumber;

    @Column(name = "warehouse_code", nullable = false, length = 60)
    private String warehouseCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReservationStatus status;

    @Column(name = "failure_reason", length = 1000)
    private String failureReason;

    @Column(name = "release_reason", length = 1000)
    private String releaseReason;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StockReservationItemEntity> items = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "released_at")
    private LocalDateTime releasedAt;

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) this.status = ReservationStatus.RESERVED;
        normalizeFields();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        normalizeFields();
    }

    private void normalizeFields() {
        if (this.reservationNumber != null) this.reservationNumber = this.reservationNumber.trim().toUpperCase();
        if (this.orderNumber != null) this.orderNumber = this.orderNumber.trim().toUpperCase();
        if (this.warehouseCode != null) this.warehouseCode = this.warehouseCode.trim().toUpperCase();
    }

    public void addItem(StockReservationItemEntity item) {
        items.add(item);
        item.setReservation(this);
    }

    public Long getId() { return id; }
    public String getReservationNumber() { return reservationNumber; }
    public void setReservationNumber(String reservationNumber) { this.reservationNumber = reservationNumber; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    public String getWarehouseCode() { return warehouseCode; }
    public void setWarehouseCode(String warehouseCode) { this.warehouseCode = warehouseCode; }
    public ReservationStatus getStatus() { return status; }
    public void setStatus(ReservationStatus status) { this.status = status; }
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    public String getReleaseReason() { return releaseReason; }
    public void setReleaseReason(String releaseReason) { this.releaseReason = releaseReason; }
    public List<StockReservationItemEntity> getItems() { return items; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getReleasedAt() { return releasedAt; }
    public void setReleasedAt(LocalDateTime releasedAt) { this.releasedAt = releasedAt; }
}
