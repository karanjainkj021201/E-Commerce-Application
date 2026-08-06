# Shipping Service

Spring Boot shipping microservice for the Amazon-like e-commerce project.

## What it does

- Consumes `OrderConfirmed` from Kafka.
- Creates exactly one shipment per order.
- Generates a shipment number and tracking number.
- Stores carrier, tracking information, timestamps, current status, and status history.
- Publishes `ShipmentCreated` after shipment creation.
- Publishes `ShipmentDelivered` after delivery.
- Consumes `OrderCancelled` and cancels an eligible shipment.
- Provides customer, public tracking, and admin APIs.

## Port and dependencies

- Shipping Service: `8086`
- Keycloak: `8080`
- Kafka: `9092`
- Database: local H2 file at `~/shippingdb`
- Java: 17 or later

No Dockerfile or Docker Compose service is included. Test this service locally first.

## Main API endpoints

### Public

- `GET /actuator/health`
- `GET /shipments/track/{trackingNumber}`

### Authenticated customer

- `GET /shipments/me?page=0&size=20`
- `GET /shipments/me/{id}`

### ADMIN role

- `GET /admin/shipments`
- `GET /admin/shipments/{id}`
- `GET /admin/shipments/order/{orderId}`
- `PUT /admin/shipments/{id}/details`
- `PATCH /admin/shipments/{id}/status`
- `POST /admin/shipments/test/order-confirmed` — local test helper only

## Shipment lifecycle

```text
CREATED
  -> IN_TRANSIT
  -> OUT_FOR_DELIVERY
  -> DELIVERED
```

Cancellation is allowed from `CREATED` or `IN_TRANSIT`. A delivered or cancelled shipment is terminal.

## Run locally

### Case A: Keycloak and Kafka also run locally

```bash
cd shipping_service
./mvnw clean test
./mvnw spring-boot:run
```

### Case B: Your existing Keycloak and Kafka are running in Docker

Your present Docker configuration advertises the hostnames `keycloak` and `kafka`. Add a local hostname mapping once on macOS:

```bash
sudo sh -c 'echo "127.0.0.1 keycloak kafka" >> /etc/hosts'
```

For isolated Postman testing without consuming or publishing real Kafka events:

```bash
cd shipping_service
KEYCLOAK_ISSUER_URI=http://keycloak:8080/realms/ecommerce \
KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
SHIPPING_EVENTS_ENABLED=false \
SPRING_KAFKA_LISTENER_AUTO_STARTUP=false \
./mvnw spring-boot:run
```

For the real integrated Kafka test with the existing services:

```bash
cd shipping_service
KEYCLOAK_ISSUER_URI=http://keycloak:8080/realms/ecommerce \
KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
SHIPPING_EVENTS_ENABLED=true \
SPRING_KAFKA_LISTENER_AUTO_STARTUP=true \
./mvnw spring-boot:run
```

## H2 console

Open:

```text
http://localhost:8086/h2-console
```

Use:

```text
JDBC URL: jdbc:h2:file:~/shippingdb
User: sa
Password: leave blank
```

## Postman

Import:

```text
shipping_service_postman_collection.json
```

Then follow `POSTMAN_TESTING_STEPS.md`.

## Local-only controls

- `SHIPPING_LOCAL_MODE=true` enables the simulated `OrderConfirmed` endpoint.
- Set `SHIPPING_LOCAL_MODE=false` when the service is later deployed.
- `SHIPPING_EVENTS_ENABLED=false` prevents test shipments from sending events into the shared Kafka broker.
