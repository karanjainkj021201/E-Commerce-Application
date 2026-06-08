# Payment Service Postman Testing Steps

Base URL:

```text
http://localhost:8085
```

Required tokens:

- `USER_TOKEN`: normal customer token from Keycloak.
- `ADMIN_TOKEN`: admin token from Keycloak.

## 1. Health check

GET

```text
http://localhost:8085/actuator/health
```

No token required.

Expected response:

```json
{
  "status": "UP"
}
```

## 2. Create order from Order Service first

Use your existing Order Service API:

```text
POST http://localhost:8083/orders
Authorization: Bearer {{USER_TOKEN}}
```

When order is created, Order Service publishes `OrderCreated`. Payment Service consumes it and creates a payment attempt.

## 3. Get logged-in user's payments

GET

```text
http://localhost:8085/payments/me?page=0&size=20
```

Headers:

```text
Authorization: Bearer {{USER_TOKEN}}
```

Copy the `paymentReference` from response.

## 4. Get one payment

GET

```text
http://localhost:8085/payments/{{paymentReference}}
```

Headers:

```text
Authorization: Bearer {{USER_TOKEN}}
```

Response contains:

- `paymentReference`
- `status`
- `gatewayPaymentUrl`
- `orderId`
- `amount`

## 5. Redirect to Google Pay / UPI link

GET

```text
http://localhost:8085/payments/{{paymentReference}}/redirect
```

Headers:

```text
Authorization: Bearer {{USER_TOKEN}}
```

In Postman, turn off automatic redirects if you want to inspect the `Location` header. The location will be a UPI URI like `upi://pay?...`.

## 6. Local mock success

POST

```text
http://localhost:8085/admin/payments/{{paymentReference}}/mock-success
```

Headers:

```text
Authorization: Bearer {{ADMIN_TOKEN}}
Content-Type: application/json
```

Body:

```json
{
  "gatewayReference": "LOCAL-GPAY-TEST-001"
}
```

Expected result:

- Payment status becomes `SUCCEEDED`.
- Payment Service publishes `PaymentSucceeded`.
- Order Service receives event and marks payment as successful.

## 7. Local mock failure

Only use this before success.

POST

```text
http://localhost:8085/admin/payments/{{paymentReference}}/mock-failure
```

Headers:

```text
Authorization: Bearer {{ADMIN_TOKEN}}
Content-Type: application/json
```

Body:

```json
{
  "failureReason": "Customer cancelled payment in Google Pay"
}
```

Expected result:

- Payment status becomes `FAILED`.
- Payment Service publishes `PaymentFailed`.
- Order Service receives event and marks payment failed.

## 8. Get payment as admin

GET

```text
http://localhost:8085/admin/payments/{{paymentReference}}
```

Headers:

```text
Authorization: Bearer {{ADMIN_TOKEN}}
```

## 9. Get payment by order id as admin

GET

```text
http://localhost:8085/admin/payments/order/{{orderId}}
```

Headers:

```text
Authorization: Bearer {{ADMIN_TOKEN}}
```

## 10. Refund successful payment locally

POST

```text
http://localhost:8085/admin/payments/{{paymentReference}}/refunds
```

Headers:

```text
Authorization: Bearer {{ADMIN_TOKEN}}
Content-Type: application/json
```

Body:

```json
{
  "amount": 100.00,
  "reason": "Customer requested refund"
}
```

Expected result:

- Refund record is created.
- Payment status becomes `REFUNDED`.
