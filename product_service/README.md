# product_service

This service implements the Product/Catalog microservice from your requirements.

## What it covers
- Product master data
- Category master data
- Public product browsing
- Public category listing
- Admin create/update/status APIs
- JWT validation with Keycloak role mapping
- Image storage by URL only (so later you can point it to S3/CloudFront)

## Public endpoints
- `GET /catalog/products`
- `GET /catalog/products/{id}`
- `GET /catalog/categories`

## Admin endpoints
- `POST /admin/categories`
- `PUT /admin/categories/{id}`
- `PATCH /admin/categories/{id}/active`
- `GET /admin/categories`
- `GET /admin/categories/{id}`

- `POST /admin/products`
- `PUT /admin/products/{id}`
- `PATCH /admin/products/{id}/status`
- `GET /admin/products`
- `GET /admin/products/{id}`

This service stores only image URLs, not file bytes.
