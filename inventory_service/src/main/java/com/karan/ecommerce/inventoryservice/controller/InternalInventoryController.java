package com.karan.ecommerce.inventoryservice.controller;

import com.karan.ecommerce.inventoryservice.dto.ReleaseReservationRequest;
import com.karan.ecommerce.inventoryservice.dto.ReservationResponse;
import com.karan.ecommerce.inventoryservice.dto.ReserveInventoryRequest;
import com.karan.ecommerce.inventoryservice.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/inventory")
@PreAuthorize("hasAnyRole('ADMIN', 'SERVICE')")
public class InternalInventoryController {

    private final InventoryService inventoryService;

    public InternalInventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> reserveInventory(@Valid @RequestBody ReserveInventoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.reserveInventory(request));
    }

    @PostMapping("/reservations/{reservationNumber}/release")
    public ResponseEntity<ReservationResponse> releaseReservation(@PathVariable String reservationNumber,
                                                                  @Valid @RequestBody ReleaseReservationRequest request) {
        return ResponseEntity.ok(inventoryService.releaseReservation(reservationNumber, request.getReason()));
    }
}
