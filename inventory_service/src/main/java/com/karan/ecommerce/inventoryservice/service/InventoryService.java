package com.karan.ecommerce.inventoryservice.service;

import com.karan.ecommerce.inventoryservice.dto.*;
import com.karan.ecommerce.inventoryservice.event.OrderCancelledEvent;
import com.karan.ecommerce.inventoryservice.event.OrderConfirmedEvent;
import com.karan.ecommerce.inventoryservice.event.OrderCreatedEvent;
import org.springframework.data.domain.Page;

import java.util.List;

public interface InventoryService {
    StockBalanceResponse createStock(StockBalanceRequest request);
    Page<StockBalanceResponse> getStocks(int page, int size);
    StockBalanceResponse getStock(Long id);
    List<StockBalanceResponse> getStocksByProduct(Long productId);
    StockBalanceResponse increaseStock(Long id, StockAdjustmentRequest request);
    StockBalanceResponse decreaseStock(Long id, StockAdjustmentRequest request);
    StockBalanceResponse adjustStock(Long id, StockSetQuantityRequest request);
    InventoryAvailabilityResponse getAvailability(Long productId, String warehouseCode);

    ReservationResponse reserveInventory(ReserveInventoryRequest request);
    ReservationResponse commitReservation(String reservationNumber);
    ReservationResponse releaseReservation(String reservationNumber, String reason);
    ReservationResponse expireReservation(String reservationNumber);
    ReservationResponse getReservation(String reservationNumber);
    ReservationResponse getReservationByOrderId(Long orderId);
    Page<ReservationResponse> getReservations(int page, int size);
    int expireDueReservations();

    Page<LedgerResponse> getLedger(int page, int size);
    Page<LedgerResponse> getLedgerByProduct(Long productId, int page, int size);

    void reserveInventoryForOrder(OrderCreatedEvent event);
    void commitReservationForOrder(OrderConfirmedEvent event);
    void releaseReservationForOrder(OrderCancelledEvent event);
}
