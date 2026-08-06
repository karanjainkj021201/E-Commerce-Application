package com.karan.ecommerce.inventoryservice.controller;

import com.karan.ecommerce.inventoryservice.dto.*;
import com.karan.ecommerce.inventoryservice.service.InventoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/inventory")
@PreAuthorize("hasRole('ADMIN')")
@Validated
public class AdminInventoryController {

    private final InventoryService inventoryService;

    public AdminInventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/stocks")
    public ResponseEntity<StockBalanceResponse> createStock(@Valid @RequestBody StockBalanceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.createStock(request));
    }

    @GetMapping("/stocks")
    public ResponseEntity<Page<StockBalanceResponse>> getStocks(@RequestParam(defaultValue = "0") @Min(0) int page,
                                                                @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(inventoryService.getStocks(page, size));
    }

    @GetMapping("/stocks/{id}")
    public ResponseEntity<StockBalanceResponse> getStock(@PathVariable Long id) {
        return ResponseEntity.ok(inventoryService.getStock(id));
    }

    @GetMapping("/stocks/product/{productId}")
    public ResponseEntity<List<StockBalanceResponse>> getStocksByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(inventoryService.getStocksByProduct(productId));
    }

    @PostMapping("/stocks/{id}/increase")
    public ResponseEntity<StockBalanceResponse> increaseStock(@PathVariable Long id,
                                                              @Valid @RequestBody StockAdjustmentRequest request) {
        return ResponseEntity.ok(inventoryService.increaseStock(id, request));
    }

    @PostMapping("/stocks/{id}/decrease")
    public ResponseEntity<StockBalanceResponse> decreaseStock(@PathVariable Long id,
                                                              @Valid @RequestBody StockAdjustmentRequest request) {
        return ResponseEntity.ok(inventoryService.decreaseStock(id, request));
    }

    @PatchMapping("/stocks/{id}/adjust")
    public ResponseEntity<StockBalanceResponse> adjustStock(@PathVariable Long id,
                                                            @Valid @RequestBody StockSetQuantityRequest request) {
        return ResponseEntity.ok(inventoryService.adjustStock(id, request));
    }

    @GetMapping("/reservations")
    public ResponseEntity<Page<ReservationResponse>> getReservations(@RequestParam(defaultValue = "0") @Min(0) int page,
                                                                     @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(inventoryService.getReservations(page, size));
    }

    @GetMapping("/reservations/order/{orderId}")
    public ResponseEntity<ReservationResponse> getReservationByOrderId(@PathVariable Long orderId) {
        return ResponseEntity.ok(inventoryService.getReservationByOrderId(orderId));
    }

    @GetMapping("/reservations/{reservationNumber}")
    public ResponseEntity<ReservationResponse> getReservation(@PathVariable String reservationNumber) {
        return ResponseEntity.ok(inventoryService.getReservation(reservationNumber));
    }

    @GetMapping("/ledger")
    public ResponseEntity<Page<LedgerResponse>> getLedger(@RequestParam(defaultValue = "0") @Min(0) int page,
                                                          @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(inventoryService.getLedger(page, size));
    }

    @GetMapping("/ledger/product/{productId}")
    public ResponseEntity<Page<LedgerResponse>> getLedgerByProduct(@PathVariable Long productId,
                                                                   @RequestParam(defaultValue = "0") @Min(0) int page,
                                                                   @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(inventoryService.getLedgerByProduct(productId, page, size));
    }
}
