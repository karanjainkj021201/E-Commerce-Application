package com.karan.ecommerce.orderservice.controller;

import com.karan.ecommerce.orderservice.dto.CreateOrderRequest;
import com.karan.ecommerce.orderservice.dto.OrderResponse;
import com.karan.ecommerce.orderservice.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@Validated
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@AuthenticationPrincipal Jwt jwt,
                                                     @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
                                                     @Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.createOrder(jwt.getSubject(), request, authorizationHeader));
    }

    @GetMapping("/me")
    public ResponseEntity<Page<OrderResponse>> getMyOrders(@AuthenticationPrincipal Jwt jwt,
                                                           @RequestParam(defaultValue = "0") @Min(0) int page,
                                                           @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(orderService.getMyOrders(jwt.getSubject(), page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getMyOrder(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return ResponseEntity.ok(orderService.getMyOrder(id, jwt.getSubject()));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelMyOrder(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return ResponseEntity.ok(orderService.cancelMyOrder(id, jwt.getSubject()));
    }
}
