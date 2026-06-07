# Inventory Service - Postman Testing Steps

Base URL: `http://localhost:8084`

Use your Keycloak access token in Postman:

Authorization tab -> Type: Bearer Token -> Token: `<ADMIN_OR_SERVICE_TOKEN>`

Public availability endpoint does not require token. Admin/internal endpoints require token.

---

## 0. Health Check

Method: `GET`

URL: `http://localhost:8084/actuator/health`

Expected: `200 OK`

---

## 1. Create Opening Stock

Method: `POST`

URL: `http://localhost:8084/admin/inventory/stocks`

Authorization: Bearer Admin token

Body -> raw -> JSON:

```json
{
  "productId": 1,
  "sku": "IPHONE15-128-BLK",
  "productName": "iPhone 15 128GB Black",
  "warehouseCode": "WH-DEFAULT",
  "quantity": 100,
  "reason": "Opening stock"
}
```

Save `id` from response as `stockId`.

---

## 2. Get All Stocks

Method: `GET`

URL: `http://localhost:8084/admin/inventory/stocks?page=0&size=20`

Authorization: Bearer Admin token

---

## 3. Get Stock by Stock ID

Method: `GET`

URL: `http://localhost:8084/admin/inventory/stocks/{{stockId}}`

Authorization: Bearer Admin token

---

## 4. Get Stock by Product ID

Method: `GET`

URL: `http://localhost:8084/admin/inventory/stocks/product/1`

Authorization: Bearer Admin token

---

## 5. Public Product Availability

Method: `GET`

URL: `http://localhost:8084/inventory/products/1/availability?warehouseCode=WH-DEFAULT`

Authorization: Not required

Expected available quantity: `100`

---

## 6. Increase Stock

Method: `POST`

URL: `http://localhost:8084/admin/inventory/stocks/{{stockId}}/increase`

Authorization: Bearer Admin token

Body:

```json
{
  "quantity": 20,
  "reason": "New inward stock received"
}
```

Expected total quantity becomes `120`.

---

## 7. Decrease Stock

Method: `POST`

URL: `http://localhost:8084/admin/inventory/stocks/{{stockId}}/decrease`

Authorization: Bearer Admin token

Body:

```json
{
  "quantity": 5,
  "reason": "Damaged stock removed"
}
```

Expected total quantity becomes `115`.

---

## 8. Adjust Stock to Exact Quantity

This sets total quantity to an exact value. It does not add/subtract.

Method: `PATCH`

URL: `http://localhost:8084/admin/inventory/stocks/{{stockId}}/adjust`

Authorization: Bearer Admin token

Body:

```json
{
  "quantity": 120,
  "reason": "Physical stock reconciliation"
}
```

Expected total quantity becomes exactly `120`.

---

## 9. Manual Reservation Test

Method: `POST`

URL: `http://localhost:8084/internal/inventory/reservations`

Authorization: Bearer Admin or Service token

Body:

```json
{
  "orderId": 1001,
  "orderNumber": "ORD-TEST-1001",
  "warehouseCode": "WH-DEFAULT",
  "items": [
    {
      "productId": 1,
      "sku": "IPHONE15-128-BLK",
      "quantity": 2
    }
  ]
}
```

Save `reservationNumber` from response.

Expected: reserved quantity increases by `2`, available quantity reduces by `2`.

---

## 10. Get Reservation by Reservation Number

Method: `GET`

URL: `http://localhost:8084/admin/inventory/reservations/{{reservationNumber}}`

Authorization: Bearer Admin token

---

## 11. Check Availability After Reservation

Method: `GET`

URL: `http://localhost:8084/inventory/products/1/availability?warehouseCode=WH-DEFAULT`

Expected available quantity should be `118` if total is `120` and reserved is `2`.

---

## 12. Get Inventory Ledger

Method: `GET`

URL: `http://localhost:8084/admin/inventory/ledger?page=0&size=20`

Authorization: Bearer Admin token

You should see movements like `STOCK_IN`, `STOCK_OUT`, `ADJUSTMENT`, and `RESERVE`.

---

## 13. Get Ledger by Product

Method: `GET`

URL: `http://localhost:8084/admin/inventory/ledger/product/1?page=0&size=20`

Authorization: Bearer Admin token

---

## 14. Release Reservation

Method: `POST`

URL: `http://localhost:8084/internal/inventory/reservations/{{reservationNumber}}/release`

Authorization: Bearer Admin or Service token

Body:

```json
{
  "reason": "Customer cancelled order"
}
```

Expected: reservation status becomes `RELEASED`, reserved quantity reduces by `2`, available quantity increases by `2`.

---

## 15. Insufficient Stock Negative Test

Method: `POST`

URL: `http://localhost:8084/internal/inventory/reservations`

Authorization: Bearer Admin or Service token

Body:

```json
{
  "orderId": 1002,
  "orderNumber": "ORD-TEST-1002",
  "warehouseCode": "WH-DEFAULT",
  "items": [
    {
      "productId": 1,
      "sku": "IPHONE15-128-BLK",
      "quantity": 9999
    }
  ]
}
```

Expected: `400 Bad Request` with insufficient stock message.

---

## 16. Multi-Product Reservation Test

First create another stock item:

Method: `POST`

URL: `http://localhost:8084/admin/inventory/stocks`

Body:

```json
{
  "productId": 2,
  "sku": "SAMSUNG-S24-256-GRY",
  "productName": "Samsung S24 256GB Grey",
  "warehouseCode": "WH-DEFAULT",
  "quantity": 50,
  "reason": "Opening stock"
}
```

Then reserve both products:

Method: `POST`

URL: `http://localhost:8084/internal/inventory/reservations`

Body:

```json
{
  "orderId": 1003,
  "orderNumber": "ORD-TEST-1003",
  "warehouseCode": "WH-DEFAULT",
  "items": [
    {
      "productId": 1,
      "sku": "IPHONE15-128-BLK",
      "quantity": 1
    },
    {
      "productId": 2,
      "sku": "SAMSUNG-S24-256-GRY",
      "quantity": 3
    }
  ]
}
```

Expected: one reservation with two reservation items.

---

## 17. End-to-End Order Service + Inventory Service Kafka Test

Prerequisites:

- Kafka running on `localhost:9092`
- Product Service running on `8082`
- Order Service running on `8083`
- Inventory Service running on `8084`
- Stock exists in Inventory Service for the same product IDs used in the order

Flow:

1. Create stock in Inventory Service for the product ID you will order.
2. Create order from Order Service using `POST http://localhost:8083/orders`.
3. Order Service publishes `OrderCreated`.
4. Inventory Service consumes `OrderCreated`, reserves stock, and publishes `StockReserved`.
5. Order Service consumes `StockReserved` and updates inventory status to `RESERVED`.
6. Check reservations using `GET http://localhost:8084/admin/inventory/reservations?page=0&size=20`.
7. Check order using `GET http://localhost:8083/admin/orders/{orderId}`.
8. If you cancel the order, Order Service publishes `OrderCancelled`; Inventory Service releases the stock reservation.

