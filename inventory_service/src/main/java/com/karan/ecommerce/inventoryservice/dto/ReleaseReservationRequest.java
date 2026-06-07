package com.karan.ecommerce.inventoryservice.dto;

import jakarta.validation.constraints.Size;

public class ReleaseReservationRequest {

    @Size(max = 1000)
    private String reason;

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
