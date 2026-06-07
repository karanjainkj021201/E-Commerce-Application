package com.karan.ecommerce.inventoryservice.service.impl;

import com.karan.ecommerce.inventoryservice.dto.*;
import com.karan.ecommerce.inventoryservice.entity.InventoryLedgerEntity;
import com.karan.ecommerce.inventoryservice.entity.StockBalanceEntity;
import com.karan.ecommerce.inventoryservice.entity.StockReservationEntity;
import com.karan.ecommerce.inventoryservice.entity.StockReservationItemEntity;
import com.karan.ecommerce.inventoryservice.entity.enums.LedgerMovementType;
import com.karan.ecommerce.inventoryservice.entity.enums.ReservationStatus;
import com.karan.ecommerce.inventoryservice.event.OrderCancelledEvent;
import com.karan.ecommerce.inventoryservice.event.OrderCreatedEvent;
import com.karan.ecommerce.inventoryservice.event.OrderItemEvent;
import com.karan.ecommerce.inventoryservice.exception.BadRequestException;
import com.karan.ecommerce.inventoryservice.exception.ResourceNotFoundException;
import com.karan.ecommerce.inventoryservice.messaging.InventoryEventPublisher;
import com.karan.ecommerce.inventoryservice.repository.InventoryLedgerRepository;
import com.karan.ecommerce.inventoryservice.repository.StockBalanceRepository;
import com.karan.ecommerce.inventoryservice.repository.StockReservationRepository;
import com.karan.ecommerce.inventoryservice.service.InventoryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class InventoryServiceImpl implements InventoryService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final DateTimeFormatter RESERVATION_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final StockBalanceRepository stockBalanceRepository;
    private final StockReservationRepository stockReservationRepository;
    private final InventoryLedgerRepository inventoryLedgerRepository;
    private final InventoryEventPublisher eventPublisher;
    private final TransactionTemplate transactionTemplate;
    private final String defaultWarehouseCode;

    public InventoryServiceImpl(StockBalanceRepository stockBalanceRepository,
                                StockReservationRepository stockReservationRepository,
                                InventoryLedgerRepository inventoryLedgerRepository,
                                InventoryEventPublisher eventPublisher,
                                TransactionTemplate transactionTemplate,
                                @Value("${inventory.default-warehouse:WH-DEFAULT}") String defaultWarehouseCode) {
        this.stockBalanceRepository = stockBalanceRepository;
        this.stockReservationRepository = stockReservationRepository;
        this.inventoryLedgerRepository = inventoryLedgerRepository;
        this.eventPublisher = eventPublisher;
        this.transactionTemplate = transactionTemplate;
        this.defaultWarehouseCode = defaultWarehouseCode == null || defaultWarehouseCode.trim().isEmpty()
                ? "WH-DEFAULT"
                : defaultWarehouseCode.trim().toUpperCase();
    }

    @Override
    @Transactional
    public StockBalanceResponse createStock(StockBalanceRequest request) {
        String warehouseCode = normalizeWarehouse(request.getWarehouseCode());
        String sku = normalizeSku(request.getSku());

        stockBalanceRepository.findByProductIdAndWarehouseCode(request.getProductId(), warehouseCode)
                .ifPresent(existing -> {
                    throw new BadRequestException("Stock already exists for product " + request.getProductId()
                            + " in warehouse " + warehouseCode);
                });

        StockBalanceEntity stock = new StockBalanceEntity();
        stock.setProductId(request.getProductId());
        stock.setSku(sku);
        stock.setProductName(blankToNull(request.getProductName()));
        stock.setWarehouseCode(warehouseCode);
        stock.setTotalQuantity(request.getQuantity());
        stock.setReservedQuantity(0);
        stock.setActive(true);

        StockBalanceEntity saved = stockBalanceRepository.save(stock);
        addLedger(saved, LedgerMovementType.STOCK_IN, request.getQuantity(), "ADMIN", null,
                blankToNull(request.getReason()) == null ? "Opening stock" : request.getReason());

        return mapStockToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StockBalanceResponse> getStocks(int page, int size) {
        return stockBalanceRepository.findAll(PageRequest.of(page, size)).map(this::mapStockToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public StockBalanceResponse getStock(Long id) {
        return mapStockToResponse(getStockEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockBalanceResponse> getStocksByProduct(Long productId) {
        return stockBalanceRepository.findByProductIdOrderByWarehouseCodeAsc(productId)
                .stream()
                .map(this::mapStockToResponse)
                .toList();
    }

    @Override
    @Transactional
    public StockBalanceResponse increaseStock(Long id, StockAdjustmentRequest request) {
        StockBalanceEntity stock = getStockEntityByIdForUpdate(id);
        stock.setTotalQuantity(stock.getTotalQuantity() + request.getQuantity());
        StockBalanceEntity saved = stockBalanceRepository.save(stock);
        addLedger(saved, LedgerMovementType.STOCK_IN, request.getQuantity(), "ADMIN", null, blankToNull(request.getReason()));
        return mapStockToResponse(saved);
    }

    @Override
    @Transactional
    public StockBalanceResponse decreaseStock(Long id, StockAdjustmentRequest request) {
        StockBalanceEntity stock = getStockEntityByIdForUpdate(id);

        if (stock.getAvailableQuantity() < request.getQuantity()) {
            throw new BadRequestException("Cannot decrease stock by " + request.getQuantity()
                    + ". Only " + stock.getAvailableQuantity() + " units are available. Reserved stock cannot be removed.");
        }

        stock.setTotalQuantity(stock.getTotalQuantity() - request.getQuantity());
        StockBalanceEntity saved = stockBalanceRepository.save(stock);
        addLedger(saved, LedgerMovementType.STOCK_OUT, -request.getQuantity(), "ADMIN", null, blankToNull(request.getReason()));
        return mapStockToResponse(saved);
    }

    @Override
    @Transactional
    public StockBalanceResponse adjustStock(Long id, StockSetQuantityRequest request) {
        StockBalanceEntity stock = getStockEntityByIdForUpdate(id);

        if (request.getQuantity() < stock.getReservedQuantity()) {
            throw new BadRequestException("Total quantity cannot be less than reserved quantity " + stock.getReservedQuantity());
        }

        int difference = request.getQuantity() - stock.getTotalQuantity();
        stock.setTotalQuantity(request.getQuantity());
        StockBalanceEntity saved = stockBalanceRepository.save(stock);
        addLedger(saved, LedgerMovementType.ADJUSTMENT, difference, "ADMIN", null, blankToNull(request.getReason()));
        return mapStockToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryAvailabilityResponse getAvailability(Long productId, String warehouseCode) {
        String resolvedWarehouse = normalizeWarehouse(firstNonBlank(warehouseCode, defaultWarehouseCode));
        StockBalanceEntity stock = stockBalanceRepository.findByProductIdAndWarehouseCode(productId, resolvedWarehouse)
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found for product " + productId
                        + " in warehouse " + resolvedWarehouse));

        return new InventoryAvailabilityResponse(
                stock.getProductId(),
                stock.getSku(),
                stock.getWarehouseCode(),
                stock.getAvailableQuantity(),
                stock.isActive() && stock.getAvailableQuantity() > 0
        );
    }

    @Override
    @Transactional
    public ReservationResponse reserveInventory(ReserveInventoryRequest request) {
        if (request.getOrderId() != null) {
            StockReservationEntity existing = stockReservationRepository.findByOrderId(request.getOrderId()).orElse(null);
            if (existing != null) {
                if (existing.getStatus() == ReservationStatus.RESERVED) {
                    return mapReservationToResponse(existing);
                }
                if (existing.getStatus() == ReservationStatus.FAILED) {
                    throw new BadRequestException("Reservation already failed for order " + request.getOrderId()
                            + ": " + existing.getFailureReason());
                }
                throw new BadRequestException("Reservation already exists for order " + request.getOrderId()
                        + " with status " + existing.getStatus());
            }
        }

        String warehouseCode = normalizeWarehouse(firstNonBlank(request.getWarehouseCode(), defaultWarehouseCode));
        Map<Long, ReserveInventoryItemRequest> mergedItems = mergeDuplicateItems(request.getItems());

        StockReservationEntity reservation = new StockReservationEntity();
        reservation.setReservationNumber(generateReservationNumber());
        reservation.setOrderId(request.getOrderId());
        reservation.setOrderNumber(blankToNull(request.getOrderNumber()));
        reservation.setWarehouseCode(warehouseCode);
        reservation.setStatus(ReservationStatus.RESERVED);

        for (ReserveInventoryItemRequest item : mergedItems.values()) {
            StockBalanceEntity stock = stockBalanceRepository
                    .findByProductIdAndWarehouseCodeForUpdate(item.getProductId(), warehouseCode)
                    .orElseThrow(() -> new BadRequestException("Stock not found for product " + item.getProductId()
                            + " in warehouse " + warehouseCode));

            if (!stock.isActive()) {
                throw new BadRequestException("Stock is inactive for product " + item.getProductId()
                        + " in warehouse " + warehouseCode);
            }

            if (stock.getAvailableQuantity() < item.getQuantity()) {
                throw new BadRequestException("Insufficient stock for product " + item.getProductId()
                        + ". Required " + item.getQuantity() + ", available " + stock.getAvailableQuantity());
            }

            stock.setReservedQuantity(stock.getReservedQuantity() + item.getQuantity());
            StockBalanceEntity savedStock = stockBalanceRepository.save(stock);

            StockReservationItemEntity reservationItem = new StockReservationItemEntity();
            reservationItem.setStockBalance(savedStock);
            reservationItem.setProductId(savedStock.getProductId());
            reservationItem.setSku(savedStock.getSku());
            reservationItem.setWarehouseCode(savedStock.getWarehouseCode());
            reservationItem.setQuantity(item.getQuantity());
            reservation.addItem(reservationItem);

            addLedger(savedStock, LedgerMovementType.RESERVE, -item.getQuantity(), "ORDER",
                    request.getOrderId() == null ? null : request.getOrderId().toString(),
                    "Reserved for order " + firstNonBlank(request.getOrderNumber(), String.valueOf(request.getOrderId())));
        }

        return mapReservationToResponse(stockReservationRepository.save(reservation));
    }

    @Override
    @Transactional
    public ReservationResponse releaseReservation(String reservationNumber, String reason) {
        StockReservationEntity reservation = stockReservationRepository.findByReservationNumber(normalizeReservation(reservationNumber))
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found for number " + reservationNumber));

        if (reservation.getStatus() == ReservationStatus.RELEASED) {
            return mapReservationToResponse(reservation);
        }

        if (reservation.getStatus() == ReservationStatus.FAILED) {
            throw new BadRequestException("Failed reservation cannot be released");
        }

        for (StockReservationItemEntity item : reservation.getItems()) {
            StockBalanceEntity stock = stockBalanceRepository.findByIdForUpdate(item.getStockBalance().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Stock not found for id " + item.getStockBalance().getId()));

            int newReservedQuantity = stock.getReservedQuantity() - item.getQuantity();
            if (newReservedQuantity < 0) {
                throw new BadRequestException("Reserved quantity cannot become negative for product " + stock.getProductId());
            }

            stock.setReservedQuantity(newReservedQuantity);
            StockBalanceEntity savedStock = stockBalanceRepository.save(stock);

            addLedger(savedStock, LedgerMovementType.RELEASE_RESERVATION, item.getQuantity(), "RESERVATION",
                    reservation.getReservationNumber(),
                    blankToNull(reason) == null ? "Reservation released" : reason);
        }

        reservation.setStatus(ReservationStatus.RELEASED);
        reservation.setReleasedAt(LocalDateTime.now());
        reservation.setReleaseReason(blankToNull(reason));

        return mapReservationToResponse(stockReservationRepository.save(reservation));
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationResponse getReservation(String reservationNumber) {
        return stockReservationRepository.findByReservationNumber(normalizeReservation(reservationNumber))
                .map(this::mapReservationToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found for number " + reservationNumber));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservationResponse> getReservations(int page, int size) {
        return stockReservationRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size))
                .map(this::mapReservationToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LedgerResponse> getLedger(int page, int size) {
        return inventoryLedgerRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size))
                .map(this::mapLedgerToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LedgerResponse> getLedgerByProduct(Long productId, int page, int size) {
        return inventoryLedgerRepository.findByProductIdOrderByCreatedAtDesc(productId, PageRequest.of(page, size))
                .map(this::mapLedgerToResponse);
    }

    @Override
    public void reserveInventoryForOrder(OrderCreatedEvent event) {
        try {
            ReserveInventoryRequest request = new ReserveInventoryRequest();
            request.setOrderId(event.getOrderId());
            request.setOrderNumber(event.getOrderNumber());
            request.setWarehouseCode(defaultWarehouseCode);
            request.setItems(event.getItems() == null ? List.of() : event.getItems().stream()
                    .map(this::mapOrderItemToReservationItem)
                    .toList());

            ReservationResponse reservation = transactionTemplate.execute(status -> reserveInventory(request));
            eventPublisher.publishStockReserved(event.getOrderId(), reservation.getReservationNumber());
        } catch (Exception ex) {
            transactionTemplate.executeWithoutResult(status -> markOrderReservationFailed(event, ex.getMessage()));
            eventPublisher.publishStockReservationFailed(event.getOrderId(), ex.getMessage());
        }
    }

    @Override
    public void releaseReservationForOrder(OrderCancelledEvent event) {
        transactionTemplate.executeWithoutResult(status -> {
            StockReservationEntity reservation = stockReservationRepository.findByOrderId(event.getOrderId()).orElse(null);
            if (reservation == null || reservation.getStatus() != ReservationStatus.RESERVED) {
                return;
            }

            releaseReservation(reservation.getReservationNumber(),
                    blankToNull(event.getReason()) == null ? "Order cancelled" : event.getReason());
        });
    }

    protected void markOrderReservationFailed(OrderCreatedEvent event, String failureReason) {
        if (event.getOrderId() == null || stockReservationRepository.findByOrderId(event.getOrderId()).isPresent()) {
            return;
        }

        StockReservationEntity reservation = new StockReservationEntity();
        reservation.setReservationNumber(generateReservationNumber());
        reservation.setOrderId(event.getOrderId());
        reservation.setOrderNumber(blankToNull(event.getOrderNumber()));
        reservation.setWarehouseCode(defaultWarehouseCode);
        reservation.setStatus(ReservationStatus.FAILED);
        reservation.setFailureReason(blankToNull(failureReason));
        stockReservationRepository.save(reservation);
    }

    private ReserveInventoryItemRequest mapOrderItemToReservationItem(OrderItemEvent item) {
        ReserveInventoryItemRequest request = new ReserveInventoryItemRequest();
        request.setProductId(item.getProductId());
        request.setSku(item.getSku());
        request.setQuantity(item.getQuantity());
        return request;
    }

    private Map<Long, ReserveInventoryItemRequest> mergeDuplicateItems(List<ReserveInventoryItemRequest> items) {
        if (items == null || items.isEmpty()) {
            throw new BadRequestException("Reservation must contain at least one item");
        }

        Map<Long, ReserveInventoryItemRequest> merged = new LinkedHashMap<>();
        for (ReserveInventoryItemRequest item : items) {
            if (item.getQuantity() == null || item.getQuantity() < 1) {
                throw new BadRequestException("Quantity must be at least 1 for product " + item.getProductId());
            }
            ReserveInventoryItemRequest existing = merged.get(item.getProductId());
            if (existing == null) {
                merged.put(item.getProductId(), item);
            } else {
                existing.setQuantity(existing.getQuantity() + item.getQuantity());
            }
        }

        return merged;
    }

    private void addLedger(StockBalanceEntity stock, LedgerMovementType movementType, Integer quantityChange,
                           String referenceType, String referenceId, String reason) {
        InventoryLedgerEntity ledger = new InventoryLedgerEntity();
        ledger.setStockBalanceId(stock.getId());
        ledger.setProductId(stock.getProductId());
        ledger.setSku(stock.getSku());
        ledger.setWarehouseCode(stock.getWarehouseCode());
        ledger.setMovementType(movementType);
        ledger.setQuantityChange(quantityChange);
        ledger.setTotalQuantityAfter(stock.getTotalQuantity());
        ledger.setReservedQuantityAfter(stock.getReservedQuantity());
        ledger.setAvailableQuantityAfter(stock.getAvailableQuantity());
        ledger.setReferenceType(referenceType);
        ledger.setReferenceId(referenceId);
        ledger.setReason(reason);
        inventoryLedgerRepository.save(ledger);
    }

    private StockBalanceEntity getStockEntityById(Long id) {
        return stockBalanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found for id " + id));
    }

    private StockBalanceEntity getStockEntityByIdForUpdate(Long id) {
        return stockBalanceRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found for id " + id));
    }

    private String generateReservationNumber() {
        String timestamp = LocalDateTime.now().format(RESERVATION_DATE_FORMAT);
        int randomNumber = 100000 + RANDOM.nextInt(900000);
        return "RES-" + timestamp + "-" + randomNumber;
    }

    private StockBalanceResponse mapStockToResponse(StockBalanceEntity stock) {
        return new StockBalanceResponse(
                stock.getId(),
                stock.getProductId(),
                stock.getSku(),
                stock.getProductName(),
                stock.getWarehouseCode(),
                stock.getTotalQuantity(),
                stock.getReservedQuantity(),
                stock.getAvailableQuantity(),
                stock.isActive(),
                stock.getCreatedAt(),
                stock.getUpdatedAt()
        );
    }

    private ReservationResponse mapReservationToResponse(StockReservationEntity reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getReservationNumber(),
                reservation.getOrderId(),
                reservation.getOrderNumber(),
                reservation.getWarehouseCode(),
                reservation.getStatus(),
                reservation.getFailureReason(),
                reservation.getReleaseReason(),
                reservation.getItems().stream().map(this::mapReservationItemToResponse).toList(),
                reservation.getCreatedAt(),
                reservation.getUpdatedAt(),
                reservation.getReleasedAt()
        );
    }

    private ReservationItemResponse mapReservationItemToResponse(StockReservationItemEntity item) {
        return new ReservationItemResponse(
                item.getId(),
                item.getStockBalance().getId(),
                item.getProductId(),
                item.getSku(),
                item.getWarehouseCode(),
                item.getQuantity()
        );
    }

    private LedgerResponse mapLedgerToResponse(InventoryLedgerEntity ledger) {
        return new LedgerResponse(
                ledger.getId(),
                ledger.getStockBalanceId(),
                ledger.getProductId(),
                ledger.getSku(),
                ledger.getWarehouseCode(),
                ledger.getMovementType(),
                ledger.getQuantityChange(),
                ledger.getTotalQuantityAfter(),
                ledger.getReservedQuantityAfter(),
                ledger.getAvailableQuantityAfter(),
                ledger.getReferenceType(),
                ledger.getReferenceId(),
                ledger.getReason(),
                ledger.getCreatedAt()
        );
    }

    private String normalizeWarehouse(String warehouseCode) {
        if (warehouseCode == null || warehouseCode.trim().isEmpty()) {
            return defaultWarehouseCode == null ? "WH-DEFAULT" : defaultWarehouseCode;
        }
        return warehouseCode.trim().toUpperCase();
    }

    private String normalizeSku(String sku) {
        return sku == null ? null : sku.trim().toUpperCase();
    }

    private String normalizeReservation(String reservationNumber) {
        return reservationNumber == null ? null : reservationNumber.trim().toUpperCase();
    }

    private String blankToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.trim().isEmpty()) {
            return first.trim();
        }
        return blankToNull(second);
    }
}
