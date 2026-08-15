# Angular UI Service

Customer storefront and admin operations console for the six-service backend.

## Local development

1. Start Keycloak, the six backend services, and API Gateway.
2. Configure the Keycloak public client `ecommerce-ui` as described in the root `UI_AND_GATEWAY_TESTING_GUIDE.md`.
3. Run:

```bash
npm install
npm start
```

4. Open `http://localhost:4200`.

The Angular development proxy forwards `/api/**` to `http://localhost:8087`, so the browser calls only the gateway.

## Main screens

- `/` public catalog and live stock checks
- `/cart` local shopping cart
- `/checkout` authenticated order creation
- `/account` profile, orders, payments, and shipments
- `/track` public shipment tracking
- `/admin` operations console, visible only to the uppercase `ADMIN` realm role
