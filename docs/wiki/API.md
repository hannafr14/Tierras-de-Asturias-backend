# API Reference

Local base URL: `http://localhost:8080`. The development frontend uses Vite's `/api` proxy.

## Access and Status Codes

| Method and Route | Access | Typical Response |
|---|---|---|
| `GET /actuator/health` | Public | 200 |
| `GET /api/products?search=text` | Public | 200, list |
| `GET /api/products/{id}` | Public | 200; 404 if missing |
| `GET /api/farms?search=text` | Public | 200, list |
| `GET /api/farms/{id}` | Public | 200; 404 if missing |
| `GET /api/farms/{id}/products` | Public | 200, list; 404 if the farm is missing |
| `GET /api/auth/csrf` | Public | 200, token for that session |
| `POST /api/auth/register` | Public, with CSRF | 200; 400 invalid input; 409 duplicate email |
| `POST /api/auth/login` | Public, with CSRF | 200; 400 invalid input; 401 invalid credentials |
| `GET /api/auth/me` | Authenticated | 200; 401 without a valid session |
| `POST /api/auth/logout` | Session flow, with CSRF | 204 |
| `POST /api/orders` | Guest or authenticated, with CSRF | 200; 400 validation error |
| `GET /api/orders/mine?page=0` | Authenticated | 200; 400 for a negative page; 401 without a session |
| `GET /api/orders/mine/{id}` | Authenticated owner | 200; 404 if missing or owned by another user |

Missing or invalid CSRF may produce 403 before the controller executes. A 403 from login is not evidence of an incorrect password.

## Catalog

```bash
curl "http://localhost:8080/api/products?search=queso"
curl "http://localhost:8080/api/farms"
```

Server search matches names case-insensitively. The general product listing does not implement parameters for price, stock, category, pagination, or sorting. The frontend filters its list by category.

Products return `id`, `name`, `description`, `image`, `quantity`, `price`, `category`, `farmId`, `farmName`, `farmAddress`, `sizeValue`, and `sizeUnit`.

Sizes may be null. A farm's card image and banner image are separate fields. Obtain IDs from responses; they are not guaranteed to match across installations.

## CSRF and Cookies in Postman

1. Always use the same host.
2. Send `GET /api/auth/csrf` and retain cookies through Postman's cookie manager.
3. Read the header name and token from the response.
4. Add that header to the POST, together with `Content-Type: application/json` when sending JSON.
5. After login, fetch a new CSRF token before another POST.

Illustrative format, not a valid token:

```json
{
  "headerName": "X-CSRF-TOKEN",
  "parameterName": "_csrf",
  "token": "<TEMPORARY_TOKEN_FOR_THIS_SESSION>"
}
```

## Registration and Login

Fictitious registration example; choose a private password for an actual test account:

```json
{
  "firstName": "Demo",
  "lastName": "Customer",
  "email": "demo@example.invalid",
  "password": "<LOCAL_PASSWORD_AT_LEAST_8_CHARACTERS>"
}
```

First name, last name, email, and password are required. Email must have a valid format and passwords require at least eight characters.

Registration returns the profile but does not authenticate by itself. The frontend performs a subsequent login:

```json
{
  "email": "demo@example.invalid",
  "password": "<SAME_LOCAL_PASSWORD>",
  "rememberMe": false
}
```

Registration, login, and `/me` return `id`, `firstName`, `lastName`, and `email`. They do not return passwords or JWTs. The `rememberMe` option is processed in the login JSON.

## Order Creation

First obtain IDs and prices from the catalog. This body only illustrates the structure; `12345` is not a guaranteed product ID:

```json
{
  "delivery": {
    "firstName": "Demo",
    "lastName": "Customer",
    "city": "Oviedo",
    "postalCode": "33001",
    "streetAddress": "Fictitious test address",
    "apartment": "",
    "phone": "+34000000000"
  },
  "items": [
    {
      "productId": 12345,
      "name": "Test product",
      "quantity": 1,
      "unitPrice": 10.0
    }
  ],
  "subtotal": 10.0,
  "shipping": 0.0,
  "taxes": 0.0,
  "total": 10.0
}
```

Delivery and at least one item are required. The minimum quantity is one; amounts cannot be negative. Apartment is optional. Postal codes require five digits. Telephone validation allows an optional `+`, digits, and spaces according to the DTO rules.

The response includes `id`, `orderNumber`, `status`, `total`, `orderDate`, and `productCount`. The last field sums units across lines, not just distinct product types.

When a session is present, the server links the order to the authenticated user. Otherwise, the order has no user. The client cannot assign ownership through a `userId` field.

Important limitation: received amounts and unit prices are currently stored. Server-side authoritative recalculation and stock validation are required before commercial use. Creating an order does not execute a payment.

## Own Orders

History uses pages of three orders, a zero-based index, and descending date order with ID as a tie-breaker. The response contains `content` and `page`, including `size`, `number`, `totalElements`, and `totalPages`.

Details return order header data, amounts, `items`, and `delivery`. Ownership is checked in the repository query. Both an unknown order and another customer's order produce 404 for an authenticated user.

## Errors

The global handler returns a `message` object for validation errors, duplicate email, and invalid credentials. Other Spring Security or `ResponseStatusException` errors may have a different format; a single response schema is not assumed for every error.
