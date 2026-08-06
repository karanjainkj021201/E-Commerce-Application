package com.karan.ecommerce.shippingservice.controller;

import com.karan.ecommerce.shippingservice.dto.*;
import com.karan.ecommerce.shippingservice.service.ShipmentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/shipments")
@PreAuthorize("hasRole('ADMIN')")
@Validated
public class AdminShipmentController {

    private final ShipmentService shipmentService;

    public AdminShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @GetMapping
    public ResponseEntity<Page<ShipmentResponse>> getShipments(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(shipmentService.getShipmentsForAdmin(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShipmentResponse> getShipment(@PathVariable Long id) {
        return ResponseEntity.ok(shipmentService.getShipmentForAdmin(id));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ShipmentResponse> getShipmentByOrderId(@PathVariable Long orderId) {
        return ResponseEntity.ok(shipmentService.getShipmentByOrderIdForAdmin(orderId));
    }

    @PutMapping("/{id}/details")
    public ResponseEntity<ShipmentResponse> updateDetails(@PathVariable Long id,
                                                          @Valid @RequestBody UpdateShipmentDetailsRequest request) {
        return ResponseEntity.ok(shipmentService.updateShipmentDetails(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ShipmentResponse> updateStatus(@PathVariable Long id,
                                                         @Valid @RequestBody UpdateShipmentStatusRequest request) {
        return ResponseEntity.ok(shipmentService.updateShipmentStatus(id, request));
    }

    @PostMapping("/test/order-confirmed")
    public ResponseEntity<ShipmentResponse> simulateOrderConfirmed(
            @Valid @RequestBody SimulateOrderConfirmedRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(shipmentService.simulateOrderConfirmed(request));
    }
}
