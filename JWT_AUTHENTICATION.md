# JWT Authentication Guide

## Overview
All API endpoints now require JWT (JSON Web Token) authentication. Only the login endpoint is publicly accessible.

## Public Endpoints
- `POST /api/auth/login` - Get JWT token

## Protected Endpoints
All other endpoints require a valid JWT token in the `Authorization` header:
- `/api/products/**` (GET, POST, PUT, DELETE)
- `/api/categories/**` (GET, POST, PUT, DELETE)
- `/api/customers/**` (GET, POST, PUT, DELETE)
- `/api/orders/**` (GET, POST, PUT)

## How to Use

### 1. Get JWT Token
```bash
curl -X POST "http://localhost:8080/api/auth/login?username=john_doe"
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huX2RvZSIsImlhdCI6...",
  "type": "Bearer"
}
```

### 2. Use Token in Requests
Add the token to the `Authorization` header with "Bearer " prefix:

```bash
curl -X GET "http://localhost:8080/api/products" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huX2RvZSIsImlhdCI6..."
```

### 3. Validate Token
```bash
curl -X POST "http://localhost:8080/api/validate" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

Response:
```json
{
  "valid": true,
  "username": "john_doe"
}
```

## Configuration

JWT settings can be configured via environment variables in `application.properties`:

- `JWT_SECRET`: Secret key for signing tokens (default: mySecretKeyThatIsAtLeast256BitsLongForHS256Algorithm)
- `JWT_EXPIRATION`: Token expiration time in milliseconds (default: 86400000 = 24 hours)

## Example Requests

### Get all products
```bash
curl -X GET "http://localhost:8080/api/products" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json"
```

### Create a new product
```bash
curl -X POST "http://localhost:8080/api/products" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Product Name",
    "description": "Product Description",
    "price": 100.00,
    "categoryId": 1
  }'
```

### Update a product
```bash
curl -X PUT "http://localhost:8080/api/products/1" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated Product",
    "price": 150.00
  }'
```

### Delete a product
```bash
curl -X DELETE "http://localhost:8080/api/products/1" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

## Error Responses

If you don't provide a token or if the token is invalid:
```json
{
  "error": "Unauthorized",
  "message": "Access denied - missing or invalid JWT token"
}
```

## Implementation Details

### Files Created/Modified:
1. **pom.xml** - Added JWT dependencies (jjwt-api, jjwt-impl, jjwt-jackson, spring-security)
2. **JwtUtils.java** - Utility class for JWT token generation and validation
3. **JwtAuthenticationFilter.java** - Filter to validate JWT tokens on each request
4. **SecurityConfig.java** - Spring Security configuration
5. **AuthController.java** - Endpoints for login and token validation
6. **All Controllers** - Added @PreAuthorize("isAuthenticated()") to all endpoints
7. **application.properties** - JWT configuration properties
