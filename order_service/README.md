# order_service

Order microservice for the Amazon-like e-commerce project.

## What this service does

- Creates orders for logged-in Keycloak users.
- Calls Product Service `/internal/products/{id}/snapshot` to capture immutable product name, SKU and price snapshots.
- Stores order header, order items, shipping address, totals and lifecycle status in its own database.
- Publishes `OrderCreated` and `OrderConfirmed` Kafka events.
- Listens to payment, stock and shipment Kafka events to update order state.

## Local ports

- user_service: `8081`
- product_service: `8082`
- order_service: `8083`

## Run

```bash
cd order_service
mvn spring-boot:run
```

## Important environment variables

```bash
export KEYCLOAK_ISSUER_URI=http://localhost:8080/realms/ecommerce
export PRODUCT_SERVICE_BASE_URL=http://localhost:8082
export PRODUCT_SERVICE_TOKEN=<optional-service-account-token-with-SERVICE-role>
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

If `PRODUCT_SERVICE_TOKEN` is blank, the service forwards the user's JWT to Product Service. For normal customer tokens, Product Service may return 403 because its internal snapshot endpoint requires `ADMIN` or `SERVICE` role.
