# Shipping Service — End-to-End Postman Testing

## Recommended testing order

Test the Shipping Service locally **before** Dockerising it. This is easier because startup errors, database errors, Kafka deserialisation errors, and security errors are visible directly in IntelliJ or the terminal.

Use two phases:

1. **Standalone service test** — tests the Shipping Service API, security, database, idempotency, tracking, and status transitions.
2. **Integrated Kafka test** — tests the real chain from Order Service to Shipping Service and back to Order Service.

---

# Phase 1 — Standalone Shipping Service Test

## 1. Keep infrastructure available

Keycloak must be available because the Shipping Service validates JWT tokens.

Kafka may remain available, but disable the listener and outgoing events for the standalone phase so that fake test orders do not affect the running Order Service.

If Keycloak and Kafka are running through your current Docker Compose setup, add this once on macOS:

```bash
sudo sh -c 'echo "127.0.0.1 keycloak kafka" >> /etc/hosts'
```

## 2. Start the Shipping Service locally

From the `shipping_service` folder:

```bash
KEYCLOAK_ISSUER_URI=http://keycloak:8080/realms/ecommerce \
KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
SHIPPING_EVENTS_ENABLED=false \
SPRING_KAFKA_LISTENER_AUTO_STARTUP=false \
./mvnw spring-boot:run
```

Expected startup result:

```text
Tomcat started on port 8086
Started ShippingServiceApplication
```

## 3. Import the Postman collection

Import:

```text
shipping_service_postman_collection.json
```

Open the collection variables and fill:

| Variable | Value |
|---|---|
| `keycloakBaseUrl` | `http://localhost:8080` |
| `realm` | `ecommerce` |
| `clientId` | Your password-grant Postman client ID |
| `adminUsername` | Your ADMIN user, for example `karanadmin` |
| `adminPassword` | ADMIN password |
| `userUsername` | A normal USER-role username |
| `userPassword` | USER password |
| `shippingBaseUrl` | `http://localhost:8086` |

Do not manually fill `admin_token`, `user_token`, `userSub`, `shipmentId`, or `trackingNumber`. The collection saves them automatically.

## 4. Run Authentication requests

Run in this order:

1. `00 - Authentication / Get Admin Token`
2. `00 - Authentication / Get User Token`

Expected result:

- Both return `200 OK`.
- `admin_token` and `user_token` are saved.
- `userSub` is extracted from the USER token.

## 5. Run the standalone folder in order

Run every request in `01 - Standalone Shipping Service E2E` from top to bottom.

### Expected results

#### Health Check

```text
200 OK
status = UP
```

#### Simulate OrderConfirmed

```text
201 Created
status = CREATED
carrier = ECOM EXPRESS
shipmentNumber starts with SHP-
trackingNumber starts with TRK-
```

This invokes the same service logic used by the real Kafka `OrderConfirmed` listener.

#### Repeat Same OrderConfirmed

Expected:

- No duplicate shipment is created.
- The response contains the same shipment ID.

This verifies Kafka-event idempotency by enforcing one shipment per `orderId`.

#### Get Shipment By Order ID

Expected:

```text
200 OK
orderId matches standaloneOrderId
```

#### Update Carrier and Tracking

Expected:

```text
200 OK
carrier = BlueDart Test
tracking number is updated
```

#### Mark IN_TRANSIT

Expected:

```text
status = IN_TRANSIT
shippedAt is populated
```

#### Mark OUT_FOR_DELIVERY

Expected:

```text
status = OUT_FOR_DELIVERY
outForDeliveryAt is populated
```

#### Public Track Shipment

Expected:

- Works without a token.
- Returns carrier, tracking number, status, and status history.
- Does not expose the customer's Keycloak user ID.

#### User — Get My Shipments

Expected:

- USER token returns the shipment created for that user's JWT subject.

#### User — Get My Shipment

Expected:

```text
200 OK
```

#### Mark DELIVERED

Expected:

```text
status = DELIVERED
deliveredAt is populated
```

#### Verify Delivered Tracking

Expected:

```text
status = DELIVERED
```

#### Negative — USER Cannot Access Admin API

Expected:

```text
403 Forbidden
```

#### Negative — Invalid Transition After Delivery

Expected:

```text
400 Bad Request
```

A delivered shipment cannot move back to `IN_TRANSIT`.

---

# Phase 2 — Real Kafka Integration Test

This phase verifies:

```text
OrderCreated
  -> Inventory reserves stock
  -> Payment succeeds
  -> OrderConfirmed
  -> Shipping creates shipment
  -> ShipmentCreated
  -> Order becomes SHIPMENT_CREATED
  -> Shipping becomes DELIVERED
  -> ShipmentDelivered
  -> Order becomes DELIVERED
```

## 1. Stop the standalone Shipping Service process

Press:

```text
Control + C
```

## 2. Start Shipping Service with Kafka enabled

```bash
KEYCLOAK_ISSUER_URI=http://keycloak:8080/realms/ecommerce \
KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
SHIPPING_EVENTS_ENABLED=true \
SPRING_KAFKA_LISTENER_AUTO_STARTUP=true \
./mvnw spring-boot:run
```

Keep these existing services running:

- Keycloak — `8080`
- Product Service — `8082`
- Order Service — `8083`
- Inventory Service — `8084`
- Payment Service — `8085`
- Kafka — `9092`
- Shipping Service locally — `8086`

The User Service may also remain running.

## 3. Prepare product and inventory

Before running the integrated folder, confirm:

- The product exists and is active.
- The product has enough inventory.
- The collection variable `productId` contains that product's ID.
- `quantity` is less than or equal to available stock.

## 4. Run the integrated folder one request at a time

Use `02 - Full Platform Kafka E2E`.

### Request 1 — Create Order

The supplied request uses the ADMIN token because the current Order Service may still forward the caller's token to the Product Service internal snapshot endpoint. Until service-to-service authentication is corrected, a normal USER token may receive `403` from that internal call.

Expected:

```text
201 Created
```

The collection saves `integratedOrderId`.

### Request 2 — Get Payment By Order ID

Wait approximately 2–5 seconds after creating the order.

Expected:

```text
200 OK
payment status = PENDING or REDIRECT_CREATED
```

The collection saves `integratedPaymentReference`.

If it returns `404`, wait briefly and send it again because Kafka processing is asynchronous.

### Request 3 — Mark Payment Success

Expected:

```text
200 OK
status = SUCCEEDED
```

### Request 4 — Verify Order Reaches SHIPMENT_CREATED

Wait approximately 3–8 seconds and send the request.

The order may first show:

```text
CONFIRMED
```

Send it again. Final expected status:

```text
status = SHIPMENT_CREATED
shippingStatus = CREATED
shipmentId is populated
carrier is populated
trackingNumber is populated
```

### Request 5 — Get Automatically Created Shipment

Expected:

```text
200 OK
status = CREATED
orderId = integratedOrderId
```

This confirms that Shipping Service consumed the real `OrderConfirmed` event.

### Requests 6 and 7 — Progress Shipment

Expected sequence:

```text
IN_TRANSIT
OUT_FOR_DELIVERY
```

### Request 8 — Mark Shipment DELIVERED

Expected:

```text
status = DELIVERED
```

Shipping Service publishes `ShipmentDelivered`.

### Request 9 — Verify Order Becomes DELIVERED

Wait approximately 2–5 seconds.

Expected:

```text
status = DELIVERED
shippingStatus = DELIVERED
deliveredAt is populated
```

This confirms the complete two-way Kafka integration.

---

# H2 Database Verification

Open:

```text
http://localhost:8086/h2-console
```

Use:

```text
JDBC URL: jdbc:h2:file:~/shippingdb
Username: sa
Password: blank
```

Run:

```sql
SELECT * FROM SHIPMENTS;
SELECT * FROM SHIPMENT_STATUS_HISTORY ORDER BY OCCURRED_AT;
```

You should see one shipment row per order and one history row for every lifecycle change.

---

# Common Problems

## 401 Unauthorized

Check that:

- The token is not expired.
- The token issuer matches `KEYCLOAK_ISSUER_URI`.
- No spaces were inserted into the JWT.

## 403 Forbidden

Check that the token used for `/admin/shipments/**` contains the `ADMIN` role.

## Cannot resolve `keycloak` or `kafka`

Add:

```text
127.0.0.1 keycloak kafka
```

to `/etc/hosts`.

## Kafka connection repeatedly fails

Use:

```bash
KAFKA_BOOTSTRAP_SERVERS=kafka:9092
```

with the current Docker Kafka configuration and the `/etc/hosts` mapping.

## Integrated shipment is not created

Check, in order:

1. Inventory reservation succeeded.
2. Payment status is `SUCCEEDED`.
3. Order status reached `CONFIRMED`.
4. Order Service published `OrderConfirmed`.
5. Shipping Service console received the event.
6. Shipping Service is running with the listener enabled.

## Order does not become DELIVERED

Check that:

- `SHIPPING_EVENTS_ENABLED=true`.
- Shipping status was progressed in the valid order.
- Order Service is listening to `ShipmentDelivered`.
- You waited a few seconds for Kafka processing.
