# Inventory Service

Local-first Spring Boot inventory microservice for the e-commerce microservices project.

## What it owns
- Stock balance per product and warehouse
- Stock reservations for orders
- Inventory ledger for audit/reconciliation

## Local defaults
- Port: `8084`
- H2 DB file: `~/inventorydb`
- H2 console: `http://localhost:8084/h2-console`
- Keycloak issuer: `http://localhost:8080/realms/ecommerce`
- Kafka bootstrap server: `localhost:9092`
- Default warehouse: `WH-DEFAULT`

## Main Kafka topics
- Consumes: `OrderCreated`, `OrderCancelled`
- Produces: `StockReserved`, `StockReservationFailed`

## Run
```bash
cd inventory_service
mvn spring-boot:run
```
