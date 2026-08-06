# Inventory Service – Reservation Lifecycle Testing

Base URL:

```text
http://localhost:8084
```

Postman variables used:

```text
inventory_base = http://localhost:8084
admin_token    = current Keycloak ADMIN token
product_id     = 3
stock_id       = 3
sku            = RS. 1
reservation_number
manual_order_id
expiry_order_id
```

For protected endpoints use:

```text
Authorization: Bearer {{admin_token}}
Content-Type: application/json
```

---

## A. Deploy the updated Inventory Service

From `~/Desktop/E-Commerce-Application`:

```bash
docker compose build --no-cache inventory-service
docker compose up -d --force-recreate inventory-service
```

Verify startup and Kafka subscriptions:

```bash
docker logs inventory-service 2>&1 \
  | grep -iE "Started InventoryServiceApplication|Subscribed to topic|partitions assigned|ERROR|Exception"
```

Expected subscriptions include:

```text
OrderCreated
OrderConfirmed
OrderCancelled
```

Health:

```http
GET {{inventory_base}}/actuator/health
```

Expected: `200 OK`, `status = UP`.

---

## B. Confirm current stock

```http
GET {{inventory_base}}/admin/inventory/stocks/{{stock_id}}
Authorization: Bearer {{admin_token}}
```

Or by product:

```http
GET {{inventory_base}}/admin/inventory/stocks/product/{{product_id}}
Authorization: Bearer {{admin_token}}
```

Record:

```text
totalQuantity
reservedQuantity
availableQuantity
```

---

## C. Manual reservation test

Use a new order ID that has never been used before.

```http
POST {{inventory_base}}/internal/inventory/reservations
Authorization: Bearer {{admin_token}}
Content-Type: application/json
```

```json
{
  "orderId": 91001,
  "orderNumber": "ORD-MANUAL-91001",
  "warehouseCode": "WH-DEFAULT",
  "items": [
    {
      "productId": 3,
      "sku": "RS. 1",
      "quantity": 2
    }
  ]
}
```

Expected response:

```text
status = RESERVED
expiresAt = approximately 10 minutes after createdAt
committedAt = null
```

Save `reservationNumber` as `{{reservation_number}}`.

Verify by order ID:

```http
GET {{inventory_base}}/admin/inventory/reservations/order/91001
Authorization: Bearer {{admin_token}}
```

Verify stock:

```http
GET {{inventory_base}}/admin/inventory/stocks/{{stock_id}}
Authorization: Bearer {{admin_token}}
```

For a 2-unit reservation:

```text
totalQuantity: unchanged
reservedQuantity: +2
availableQuantity: -2
```

---

## D. Commit test – simulates OrderConfirmed

```http
POST {{inventory_base}}/internal/inventory/reservations/{{reservation_number}}/commit
Authorization: Bearer {{admin_token}}
```

Expected reservation:

```text
status = COMMITTED
committedAt != null
expiresAt = null
```

Verify stock again:

```http
GET {{inventory_base}}/admin/inventory/stocks/{{stock_id}}
Authorization: Bearer {{admin_token}}
```

For 2 units:

```text
totalQuantity: -2
reservedQuantity: -2
availableQuantity: unchanged from the reserved state
```

Example:

```text
Before reservation: total=100, reserved=0, available=100
After reservation:  total=100, reserved=2, available=98
After commit:       total=98,  reserved=0, available=98
```

---

## E. Cancellation after commit – restock test

```http
POST {{inventory_base}}/internal/inventory/reservations/{{reservation_number}}/release
Authorization: Bearer {{admin_token}}
Content-Type: application/json
```

```json
{
  "reason": "Test cancellation after payment confirmation"
}
```

Expected:

```text
status = RELEASED
totalQuantity increases by 2
reservedQuantity remains 0
availableQuantity increases by 2
```

---

## F. Automatic expiry test

The default hold is 10 minutes. For a fast test, temporarily set these under `inventory-service.environment` in `docker-compose.yml`:

```yaml
RESERVATION_HOLD_MINUTES: 1
RESERVATION_EXPIRY_SCAN_MS: 5000
RESERVATION_EXPIRY_INITIAL_DELAY_MS: 5000
```

Recreate only Inventory Service:

```bash
docker compose up -d --build --force-recreate inventory-service
```

Create another reservation with a fresh order ID:

```http
POST {{inventory_base}}/internal/inventory/reservations
Authorization: Bearer {{admin_token}}
Content-Type: application/json
```

```json
{
  "orderId": 92001,
  "orderNumber": "ORD-EXPIRY-92001",
  "warehouseCode": "WH-DEFAULT",
  "items": [
    {
      "productId": 3,
      "sku": "RS. 1",
      "quantity": 2
    }
  ]
}
```

Save its `reservationNumber`. Wait slightly longer than one minute.

The scheduler should expire it automatically. To trigger an immediate due-reservation scan after the wait:

```http
POST {{inventory_base}}/internal/inventory/reservations/expire-due
Authorization: Bearer {{admin_token}}
```

Expected:

```json
{
  "expiredCount": 1
}
```

Verify reservation:

```http
GET {{inventory_base}}/admin/inventory/reservations/order/92001
Authorization: Bearer {{admin_token}}
```

Expected:

```text
status = EXPIRED
releasedAt != null
releaseReason = Reservation expired before order confirmation
```

Verify stock returned:

```text
totalQuantity: unchanged
reservedQuantity: -2
availableQuantity: +2
```

Calling the direct expiry endpoint before `expiresAt` should return `400 Bad Request`:

```http
POST {{inventory_base}}/internal/inventory/reservations/{reservationNumber}/expire
Authorization: Bearer {{admin_token}}
```

---

## G. Release before payment test

Create another fresh reservation, then call:

```http
POST {{inventory_base}}/internal/inventory/reservations/{{reservation_number}}/release
Authorization: Bearer {{admin_token}}
Content-Type: application/json
```

```json
{
  "reason": "Customer cancelled during checkout"
}
```

Expected:

```text
RESERVED -> RELEASED
reservedQuantity decreases
availableQuantity increases
totalQuantity does not change
```

---

## H. Ledger verification

```http
GET {{inventory_base}}/admin/inventory/ledger/product/{{product_id}}?page=0&size=50
Authorization: Bearer {{admin_token}}
```

New movement types:

```text
COMMIT_RESERVATION
EXPIRE_RESERVATION
RESTOCK_CANCELLED_ORDER
```

---

## I. Real Kafka end-to-end test

1. Ensure all services are running.
2. Create a fresh order through Order Service.
3. `OrderCreated` reserves stock.
4. Complete payment.
5. Order becomes `CONFIRMED` and publishes `OrderConfirmed`.
6. Inventory consumes `OrderConfirmed` and changes the reservation to `COMMITTED`.
7. Shipping later creates and delivers the shipment; delivery does not deduct stock again.

Check reservation by the real order ID:

```http
GET {{inventory_base}}/admin/inventory/reservations/order/{{order_id}}
Authorization: Bearer {{admin_token}}
```

Expected after confirmation:

```text
status = COMMITTED
reservedQuantity no longer includes this order
totalQuantity has been reduced by the ordered quantity
```

Check logs:

```bash
docker logs inventory-service --since 10m 2>&1 \
  | grep -iE "OrderCreated|OrderConfirmed|OrderCancelled|Expired|StockReserved|StockReservationFailed|ERROR|Exception"
```

---

## J. Cart rule verification

Adding an item to the UI cart must not call Inventory Service and must not publish `OrderCreated`.

Before checkout:

```text
totalQuantity: unchanged
reservedQuantity: unchanged
availableQuantity: unchanged
```

Only pressing Place Order / starting payment should create the order and start the 10-minute reservation.

---

## K. Restore normal timeout after fast testing

Set:

```yaml
RESERVATION_HOLD_MINUTES: 10
RESERVATION_EXPIRY_SCAN_MS: 60000
RESERVATION_EXPIRY_INITIAL_DELAY_MS: 60000
```

Then recreate Inventory Service.
