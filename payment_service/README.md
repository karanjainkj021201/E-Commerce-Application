# Payment Service

Payment microservice for the local Amazon-like e-commerce project.

## What this service does

- Owns the payment database.
- Consumes `OrderCreated` Kafka events from Order Service.
- Creates one payment attempt per order.
- Generates a Google Pay / UPI deep link locally.
- Emits `PaymentSucceeded` or `PaymentFailed` Kafka events back to Order Service.
- Stores refund records.
- Does not store raw card data.
- Does not use S3 or any cloud storage.

## Important local Google Pay note

For local development, this service creates a UPI deep link such as:

```text
upi://pay?pa=merchant@upi&pn=Karan%20Ecommerce&mc=0000&tr=PAY-...&tn=Order%20ORD-...&am=100.00&cu=INR
```

This can open Google Pay / UPI apps on a mobile device, but local-only backend code cannot safely prove that real money was paid. That is why this service has admin-only mock endpoints:

- `POST /admin/payments/{paymentReference}/mock-success`
- `POST /admin/payments/{paymentReference}/mock-failure`

Later, when you integrate a real PSP/payment aggregator, replace these mock endpoints with verified payment callbacks/webhooks/status-check APIs.

## Local port

```text
http://localhost:8085
```

## H2 console

```text
http://localhost:8085/h2-console
JDBC URL: jdbc:h2:file:~/paymentdb
Username: sa
Password: <blank>
```

## Environment variables

Defaults are already local-friendly.

```bash
export KEYCLOAK_ISSUER_URI=http://localhost:8080/realms/ecommerce
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
export GOOGLE_PAY_MERCHANT_VPA=merchant@upi
export GOOGLE_PAY_MERCHANT_NAME="Karan Ecommerce"
export GOOGLE_PAY_MERCHANT_CODE=0000
```

Use a real merchant VPA only when you are ready to accept actual payments.

## Run locally

```bash
cd payment_service
./mvnw spring-boot:run
```

## Main flow

1. User creates order in Order Service.
2. Order Service publishes `OrderCreated`.
3. Payment Service consumes `OrderCreated` and creates a payment attempt.
4. Check created payment:

```http
GET /payments/me
Authorization: Bearer <USER_TOKEN>
```

5. Redirect/open UPI app:

```http
GET /payments/{paymentReference}/redirect
Authorization: Bearer <USER_TOKEN>
```

6. For local testing, mark payment success with admin token:

```http
POST /admin/payments/{paymentReference}/mock-success
Authorization: Bearer <ADMIN_TOKEN>
Content-Type: application/json

{
  "gatewayReference": "LOCAL-GPAY-TEST-001"
}
```

7. Payment Service publishes `PaymentSucceeded`.
8. Order Service consumes `PaymentSucceeded` and updates the order payment status.

## APIs

### User APIs

| Method | API | Purpose |
|---|---|---|
| GET | `/payments/me?page=0&size=20` | Get logged-in user's payments |
| GET | `/payments/{paymentReference}` | Get logged-in user's payment by reference |
| GET | `/payments/{paymentReference}/redirect` | 302 redirect to generated UPI payment URI |

### Admin APIs

| Method | API | Purpose |
|---|---|---|
| GET | `/admin/payments?page=0&size=20` | List all payments |
| GET | `/admin/payments/{paymentReference}` | Get payment by reference |
| GET | `/admin/payments/order/{orderId}` | Get payment by order id |
| POST | `/admin/payments/{paymentReference}/mock-success` | Local mark payment success |
| POST | `/admin/payments/{paymentReference}/mock-failure` | Local mark payment failure |
| POST | `/admin/payments/{paymentReference}/refunds` | Local mock refund |

### Internal API

| Method | API | Purpose |
|---|---|---|
| GET | `/internal/payments/order/{orderId}` | Get payment by order id |

## Suggested Git commands

From your main `E-Commerce-Application` folder:

```bash
git add payment_service .gitignore
git commit -m "Add payment service with local Google Pay redirect flow"
git push origin main
```
