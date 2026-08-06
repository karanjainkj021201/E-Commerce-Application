package com.karan.ecommerce.inventoryservice.entity.enums;

public enum LedgerMovementType {
    STOCK_IN,
    STOCK_OUT,
    ADJUSTMENT,
    RESERVE,
    RELEASE_RESERVATION,
    COMMIT_RESERVATION,
    EXPIRE_RESERVATION,
    RESTOCK_CANCELLED_ORDER
}
