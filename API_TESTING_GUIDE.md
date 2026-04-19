# API Testing Guide - Order Management System

## Prerequisites
- Java 17+
- Maven
- MySQL 8.0+
- Database: `order_management_db`

## Startup Server

```bash
cd order-management
mvn spring-boot:run
```

Server akan running di `http://localhost:8084`

---

## Database Structure

### Tables
- `categories` - Kategori produk
- `products` - Data produk dengan relasi ke kategori
- `customers` - Data pelanggan
- `orders` - Data pesanan
- `order_items` - Detail item dalam pesanan

### Test Data (Auto-Loaded)
- **3 Categories**: Elektronik, Furniture, Fashion
- **8 Products**: Laptop, Mouse, Keyboard, Meja, Kursi, Kemeja, Celana, Sepatu
- **5 Customers**: John Doe, Jane Smith, Rani Wijaya, Budi Santoso, Siti Nurhaliza

---

## API Endpoints

### 1. CATEGORIES API

#### Get All Categories
```
GET http://localhost:8084/api/categories
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "name": "Elektronik",
    "description": "Perangkat elektronik dan gadget"
  },
  {
    "id": 2,
    "name": "Furniture",
    "description": "Perabotan rumah dan kantor"
  },
  {
    "id": 3,
    "name": "Fashion",
    "description": "Pakaian dan aksesori"
  }
]
```

#### Get Category by ID
```
GET http://localhost:8084/api/categories/1
```

#### Create Category
```
POST http://localhost:8084/api/categories
Content-Type: application/json

{
  "name": "Elektronik Gaming",
  "description": "Perangkat gaming terbaru"
}
```

**Response (201 CREATED):**
```json
{
  "id": 4,
  "name": "Elektronik Gaming",
  "description": "Perangkat gaming terbaru"
}
```

#### Update Category
```
PUT http://localhost:8084/api/categories/4
Content-Type: application/json

{
  "name": "Gaming & Elektronik",
  "description": "Perangkat gaming dan elektronik premium"
}
```

#### Delete Category
```
DELETE http://localhost:8084/api/categories/4
```

**Response (204 No Content)**

---

### 2. PRODUCTS API

#### Get All Products
```
GET http://localhost:8084/api/products
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "name": "Laptop Asus VivoBook",
    "price": 8000000.0,
    "stock": 10,
    "sku": "SKU-001",
    "category": {
      "id": 1,
      "name": "Elektronik",
      "description": "Perangkat elektronik dan gadget"
    }
  },
  {
    "id": 2,
    "name": "Mouse Logitech",
    "price": 150000.0,
    "stock": 50,
    "sku": "SKU-002",
    "category": {
      "id": 1,
      "name": "Elektronik",
      "description": "Perangkat elektronik dan gadget"
    }
  }
]
```

#### Get Product by ID
```
GET http://localhost:8084/api/products/1
```

#### Create Product
```
POST http://localhost:8084/api/products
Content-Type: application/json

{
  "name": "Monitor Samsung 27 Inch",
  "price": 2500000.0,
  "stock": 15,
  "sku": "SKU-009",
  "categoryId": 1
}
```

**Response (201 CREATED):**
```json
{
  "id": 9,
  "name": "Monitor Samsung 27 Inch",
  "price": 2500000.0,
  "stock": 15,
  "sku": "SKU-009",
  "category": {
    "id": 1,
    "name": "Elektronik"
  }
}
```

#### Update Product
```
PUT http://localhost:8084/api/products/9
Content-Type: application/json

{
  "name": "Monitor Samsung 27 Inch 4K",
  "price": 3500000.0,
  "stock": 12,
  "sku": "SKU-009",
  "categoryId": 1
}
```

#### Delete Product
```
DELETE http://localhost:8084/api/products/9
```

#### Validation Error Example
```
POST http://localhost:8084/api/products
Content-Type: application/json

{
  "name": "AB",
  "price": -100.0,
  "stock": 10
}
```

**Response (400 Bad Request):**
```json
{
  "timestamp": "2026-04-16T09:40:00",
  "status": 400,
  "errors": {
    "name": "Nama produk minimal 3 karakter",
    "price": "Harga minimal 1"
  }
}
```

---

### 3. CUSTOMERS API

#### Get All Customers
```
GET http://localhost:8084/api/customers
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com",
    "address": "Jl. Merdeka No. 123, Jakarta"
  },
  {
    "id": 2,
    "name": "Jane Smith",
    "email": "jane@example.com",
    "address": "Jl. Sudirman No. 456, Bandung"
  }
]
```

#### Get Customer by ID
```
GET http://localhost:8084/api/customers/1
```

#### Create Customer
```
POST http://localhost:8084/api/customers
Content-Type: application/json

{
  "name": "Ahmad Rizki",
  "email": "ahmad@example.com",
  "address": "Jl. Gatot Subroto No. 999, Jakarta"
}
```

**Response (201 CREATED):**
```json
{
  "id": 6,
  "name": "Ahmad Rizki",
  "email": "ahmad@example.com",
  "address": "Jl. Gatot Subroto No. 999, Jakarta"
}
```

#### Update Customer
```
PUT http://localhost:8084/api/customers/6
Content-Type: application/json

{
  "name": "Ahmad Rizki",
  "email": "ahmad.rizki@example.com",
  "address": "Jl. Gatot Subroto No. 1000, Jakarta"
}
```

#### Delete Customer
```
DELETE http://localhost:8084/api/customers/6
```

#### Validation Error - Invalid Email
```
POST http://localhost:8084/api/customers
Content-Type: application/json

{
  "name": "Test",
  "email": "invalid-email",
  "address": "Jalan Raya"
}
```

**Response (400 Bad Request):**
```json
{
  "timestamp": "2026-04-16T09:40:00",
  "status": 400,
  "errors": {
    "email": "Format email tidak valid"
  }
}
```

---

### 4. ORDERS API

#### Get All Orders
```
GET http://localhost:8084/api/orders
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "orderNumber": "ORD-1713012345678",
    "customer": {
      "id": 1,
      "name": "John Doe",
      "email": "john@example.com"
    },
    "status": "PENDING",
    "totalAmount": 8150000.0,
    "createdAt": "2026-04-16T09:40:00",
    "items": [
      {
        "id": 1,
        "product": {
          "id": 1,
          "name": "Laptop Asus VivoBook",
          "price": 8000000.0
        },
        "quantity": 1,
        "price": 8000000.0,
        "subtotal": 8000000.0
      },
      {
        "id": 2,
        "product": {
          "id": 2,
          "name": "Mouse Logitech",
          "price": 150000.0
        },
        "quantity": 1,
        "price": 150000.0,
        "subtotal": 150000.0
      }
    ]
  }
]
```

#### Get Order by ID
```
GET http://localhost:8084/api/orders/1
```

#### Create Order
```
POST http://localhost:8084/api/orders?customerId=1
Content-Type: application/json

[
  {
    "productId": 1,
    "quantity": 1
  },
  {
    "productId": 2,
    "quantity": 1
  }
]
```

**Response (201 CREATED):**
```json
{
  "id": 1,
  "orderNumber": "ORD-1713012345678",
  "customer": {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com",
    "address": "Jl. Merdeka No. 123, Jakarta"
  },
  "status": "PENDING",
  "totalAmount": 8150000.0,
  "createdAt": "2026-04-16T09:40:00",
  "items": [
    {
      "id": 1,
      "quantity": 1,
      "price": 8000000.0,
      "subtotal": 8000000.0
    },
    {
      "id": 2,
      "quantity": 1,
      "price": 150000.0,
      "subtotal": 150000.0
    }
  ]
}
```

#### Update Order Status
```
PUT http://localhost:8084/api/orders/1/status?status=SHIPPED
```

**Response (200 OK):**
```json
{
  "id": 1,
  "orderNumber": "ORD-1713012345678",
  "customer": {
    "id": 1,
    "name": "John Doe"
  },
  "status": "SHIPPED",
  "totalAmount": 8150000.0,
  "createdAt": "2026-04-16T09:40:00",
  "items": [...]
}
```

#### Cancel Order
```
POST http://localhost:8084/api/orders/1/cancel
```

**Response (200 OK):**
```
Order berhasil dibatalkan
```

#### Order Error - Insufficient Stock
```
POST http://localhost:8084/api/orders?customerId=1
Content-Type: application/json

[
  {
    "productId": 1,
    "quantity": 100
  }
]
```

**Response (400 Bad Request):**
```json
{
  "timestamp": "2026-04-16T09:40:00",
  "status": 400,
  "message": "Stok produk tidak cukup: Laptop Asus VivoBook"
}
```

#### Order Error - Invalid Customer
```
POST http://localhost:8084/api/orders?customerId=999
Content-Type: application/json

[
  {
    "productId": 1,
    "quantity": 1
  }
]
```

**Response (400 Bad Request):**
```json
{
  "timestamp": "2026-04-16T09:40:00",
  "status": 400,
  "message": "Customer tidak ditemukan"
}
```

---

## Testing dengan Postman/cURL

### Complete Workflow Testing

**Step 1: View Categories**
```bash
curl -X GET http://localhost:8084/api/categories
```

**Step 2: View Products**
```bash
curl -X GET http://localhost:8084/api/products
```

**Step 3: View Customers**
```bash
curl -X GET http://localhost:8084/api/customers
```

**Step 4: Create New Order**
```bash
curl -X POST http://localhost:8084/api/orders?customerId=1 \
  -H "Content-Type: application/json" \
  -d '[
    {"productId": 1, "quantity": 1},
    {"productId": 2, "quantity": 2}
  ]'
```

**Step 5: Get Order Details**
```bash
curl -X GET http://localhost:8084/api/orders/1
```

**Step 6: Update Order Status**
```bash
curl -X PUT "http://localhost:8084/api/orders/1/status?status=SHIPPED"
```

**Step 7: View All Orders**
```bash
curl -X GET http://localhost:8084/api/orders
```

---

## Database Queries untuk Verification

```sql
-- Check Categories
SELECT * FROM categories;

-- Check Products with Category
SELECT p.*, c.name as category_name 
FROM products p 
LEFT JOIN categories c ON p.category_id = c.id;

-- Check Customers
SELECT * FROM customers;

-- Check Orders with Items
SELECT o.*, c.name as customer_name, COUNT(oi.id) as item_count 
FROM orders o 
LEFT JOIN customers c ON o.customer_id = c.id 
LEFT JOIN order_items oi ON o.id = oi.order_id 
GROUP BY o.id;

-- Check Stock After Order
SELECT id, name, stock FROM products;
```

---

## Status Codes

| Code | Meaning |
|------|---------|
| 200 | OK - Request berhasil |
| 201 | Created - Resource berhasil dibuat |
| 204 | No Content - Resource berhasil dihapus |
| 400 | Bad Request - Validasi error atau error bisnis |
| 404 | Not Found - Resource tidak ditemukan |
| 500 | Internal Server Error - Error server |

---

## Error Handling

Semua error akan meresponse dengan format:
```json
{
  "timestamp": "2026-04-16T09:40:00",
  "status": 400,
  "message": "Deskripsi error"
}
```

Untuk validation error:
```json
{
  "timestamp": "2026-04-16T09:40:00",
  "status": 400,
  "errors": {
    "fieldName": "Error message"
  }
}
```

---

## Features Implemented

✅ Categories Management (CRUD)
✅ Products Management (CRUD)
✅ Customers Management (CRUD)
✅ Orders Management (Create, Read, Update Status, Cancel)
✅ Order Items with automatic stock reduction
✅ Input Validation (DTO-based)
✅ Global Exception Handler
✅ Data Initialization (Auto-load test data)
✅ Relationship Management (Categories ↔ Products, Customers ↔ Orders, Orders ↔ OrderItems)
✅ Transaction Support (Order creation with stock management)

---

## Notes

- Semua API menggunakan JSON format
- Port: 8084
- Database: MySQL (order_management_db)
- Test data auto-loaded saat aplikasi startup
- Stock produk otomatis berkurang saat order dibuat
- Order tidak dapat dibatalkan jika sudah dalam status SHIPPED atau CANCELLED
