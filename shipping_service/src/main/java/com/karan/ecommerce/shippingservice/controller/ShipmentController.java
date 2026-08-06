package com.karan.ecommerce.shippingservice.controller;

import com.karan.ecommerce.shippingservice.dto.ShipmentResponse;
import com.karan.ecommerce.shippingservice.dto.TrackingResponse;
import com.karan.ecommerce.shippingservice.service.ShipmentService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shipments")
@Validated
public class ShipmentController {

    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @GetMapping("/me")
    public ResponseEntity<Page<ShipmentResponse>> getMyShipments(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(shipmentService.getMyShipments(jwt.getSubject(), page, size));
    }

    @GetMapping("/me/{id}")
    public ResponseEntity<ShipmentResponse> getMyShipment(@AuthenticationPrincipal Jwt jwt,
                                                          @PathVariable Long id) {
        return ResponseEntity.ok(shipmentService.getMyShipment(id, jwt.getSubject()));
    }

    @GetMapping("/track/{trackingNumber}")
    public ResponseEntity<TrackingResponse> trackShipment(@PathVariable String trackingNumber) {
        return ResponseEntity.ok(shipmentService.trackShipment(trackingNumber));
    }
}
