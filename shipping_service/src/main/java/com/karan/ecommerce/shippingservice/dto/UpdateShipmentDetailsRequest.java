package com.karan.ecommerce.shippingservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateShipmentDetailsRequest {
    @NotBlank(message = "Carrier is required")
    @Size(max = 120)
    private String carrier;

    @NotBlank(message = "Tracking number is required")
    @Size(max = 120)
    private String trackingNumber;

    public String getCarrier() { return carrier; }
    public void setCarrier(String carrier) { this.carrier = carrier; }
    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
}
