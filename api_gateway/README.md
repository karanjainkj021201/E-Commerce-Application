# API Gateway

Spring Cloud Gateway is the only backend address used by the Angular UI.

- Local port: `8087`
- Public routes: catalog, stock availability, shipment tracking, health
- Authenticated routes: customer profile, orders, payments, shipments
- Admin routes: `/api/admin/**` and require the uppercase Keycloak realm role `ADMIN`
- Internal service routes are intentionally not exposed

## Run locally

Start Keycloak and all six services first, then run:

```bash
./mvnw spring-boot:run
```

Check `http://localhost:8087/actuator/health`.

All route targets can be overridden with environment variables such as
`PRODUCT_SERVICE_URL`, `ORDER_SERVICE_URL`, and `JWT_JWK_SET_URI`.
