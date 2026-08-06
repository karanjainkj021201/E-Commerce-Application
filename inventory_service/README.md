# Inventory Service

Spring Boot inventory microservice for the e-commerce application.

## Responsibilities

- Stock balance per product and warehouse
- Temporary stock reservations created at checkout (`OrderCreated`)
- Reservation commit after payment/order confirmation (`OrderConfirmed`)
- Automatic expiry of unpaid reservations
- Release/restock after cancellation (`OrderCancelled`)
- Inventory ledger for audit and reconciliation

## Important cart rule

Adding a product to a cart does **not** reserve stock. Inventory changes only after checkout creates an order and publishes `OrderCreated`.

## Reservation lifecycle

```text
Cart only                       -> no inventory change
OrderCreated                    -> RESERVED for 10 minutes
OrderConfirmed within hold      -> COMMITTED
OrderCancelled while RESERVED   -> RELEASED
OrderCancelled after COMMITTED  -> RELEASED and stock restored
No confirmation before expiry   -> EXPIRED and stock released
```

For a 2-unit order against 100 units:

```text
Initial:    total=100, reserved=0, available=100
Reserved:   total=100, reserved=2, available=98
Committed:  total=98,  reserved=0, available=98
Expired:    total=100, reserved=0, available=100
```

## Local defaults

- Port: `8084`
- H2 DB: `~/inventorydb`
- H2 console: `http://localhost:8084/h2-console`
- Keycloak issuer: `http://localhost:8080/realms/ecommerce`
- Kafka: `localhost:9092`
- Default warehouse: `WH-DEFAULT`
- Reservation hold: `10` minutes
- Expiry scan: every `60` seconds

## Docker defaults

- Container application port: `8080`
- Host port: `8084`
- Kafka bootstrap server: `kafka:9092`
- Keycloak issuer: `http://keycloak:8080/realms/ecommerce`
- H2 DB: `/data/inventorydb`

## Configuration

```yaml
inventory:
  default-warehouse: ${DEFAULT_WAREHOUSE_CODE:WH-DEFAULT}
  reservation-hold-minutes: ${RESERVATION_HOLD_MINUTES:10}
  reservation-expiry-scan-ms: ${RESERVATION_EXPIRY_SCAN_MS:60000}
  reservation-expiry-initial-delay-ms: ${RESERVATION_EXPIRY_INITIAL_DELAY_MS:60000}
```

The initial delay lets Kafka replay historical `OrderConfirmed` events before old reservations are expired after an upgrade.

## Kafka topics

Consumes:

- `OrderCreated`
- `OrderConfirmed`
- `OrderCancelled`

Produces:

- `StockReserved`
- `StockReservationFailed`

When a reservation expires, the service publishes `StockReservationFailed` with an expiry reason so the existing Order Service failure listener can compensate the order.

## New lifecycle endpoints

All require an ADMIN or SERVICE bearer token:

- `POST /internal/inventory/reservations/{reservationNumber}/commit`
- `POST /internal/inventory/reservations/{reservationNumber}/expire`
- `POST /internal/inventory/reservations/expire-due`
- `GET /admin/inventory/reservations/order/{orderId}`

The `/expire` endpoint refuses to expire a reservation before its `expiresAt` time.

## Build

```bash
./mvnw clean package -DskipTests
```

## Docker rebuild

From the monorepo root:

```bash
docker compose build --no-cache inventory-service
docker compose up -d --force-recreate inventory-service
docker logs inventory-service --since 3m
```

## Database migration

`V2__add_reservation_lifecycle.sql` adds:

- `expires_at`
- `committed_at`
- expiry lookup index

Existing `RESERVED` records receive an expiry time of `created_at + 10 minutes`. Historical `OrderConfirmed` events can still commit a reservation when the event's `occurredAt` was within the original hold window.
