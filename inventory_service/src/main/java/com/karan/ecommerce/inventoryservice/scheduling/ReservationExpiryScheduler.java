package com.karan.ecommerce.inventoryservice.scheduling;

import com.karan.ecommerce.inventoryservice.service.InventoryService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
public class ReservationExpiryScheduler {

    private static final Logger LOGGER = Logger.getLogger(ReservationExpiryScheduler.class.getName());
    private final InventoryService inventoryService;

    public ReservationExpiryScheduler(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @Scheduled(
            fixedDelayString = "${inventory.reservation-expiry-scan-ms:60000}",
            initialDelayString = "${inventory.reservation-expiry-initial-delay-ms:60000}"
    )
    public void expireDueReservations() {
        int expired = inventoryService.expireDueReservations();
        if (expired > 0) {
            LOGGER.info("Expired " + expired + " inventory reservation(s)");
        }
    }
}
