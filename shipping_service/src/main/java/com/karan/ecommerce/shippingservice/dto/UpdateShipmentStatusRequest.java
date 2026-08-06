package com.karan.ecommerce.shippingservice.dto;

import com.karan.ecommerce.shippingservice.entity.enums.ShipmentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UpdateShipmentStatusRequest {
    @NotNull(message = "Shipment status is required")
    private ShipmentStatus status;

    @Size(max = 1000)
    private String note;

    public ShipmentStatus getStatus() { return status; }
    public void setStatus(ShipmentStatus status) { this.status = status; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
