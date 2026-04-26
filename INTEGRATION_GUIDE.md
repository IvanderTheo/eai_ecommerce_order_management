# Order Management Integration Guide

## 📋 Ringkasan Integrasi

Order Management System telah diintegrasikan dengan **User Management** dan **Inventory Management** untuk memastikan data konsistensi dan validasi real-time saat pembuatan order.

---

## 🏗️ Arsitektur Integrasi

```
┌─────────────────────────────────────────────────────────────┐
│                    ORDER MANAGEMENT                          │
│                     (Port: 8080)                            │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌──────────────────┐        ┌─────────────────┐            │
│  │  OrderService    │        │  OrderController│            │
│  │                  │        │                 │            │
│  │  - createOrder() │        │  POST /api/     │            │
│  │  - getOrder()    │        │  orders         │            │
│  │  - updateStatus()│        │                 │            │
│  └────────┬─────────┘        └─────────────────┘            │
│           │                                                   │
│      ┌────┴─────────────────────────────────┐               │
│      │                                       │               │
│      ▼                                       ▼               │
│  ┌────────────────┐        ┌──────────────────────┐         │
│  │ UserService    │        │ InventoryService     │         │
│  │   Client       │        │   Client             │         │
│  │(Port: 8082)    │        │  (Port: 8086)        │         │
│  └────────────────┘        └──────────────────────┘         │
│                                                               │
└─────────────────────────────────────────────────────────────┘
         │                            │
         └─────────────┬──────────────┘
                       │
         ┌─────────────┴──────────────┐
         │                            │
         ▼                            ▼
    ┌──────────────┐        ┌────────────────────┐
    │ USER          │        │   INVENTORY         │
    │ MANAGEMENT    │        │   MANAGEMENT        │
    │ (Port: 8082)  │        │   (Port: 8086)      │
    │               │        │                     │
    │ Validasi:     │        │ Validasi:           │
    │ - User exist  │        │ - Product exist     │
    │ - User active │        │ - Stock available   │
    │               │        │ - Reserve stock     │
    └──────────────┘        └────────────────────┘
```

---

## 🔌 External Service Clients

### 1. **UserServiceClient**
- **Interface**: `com.example.order_management.client.user.UserServiceClient`
- **Base URL**: `http://localhost:8082`
- **Methods**:
  - `getUserById(Long id)` → Get user details

### 2. **ProductServiceClient**
- **Interface**: `com.example.order_management.client.product.ProductServiceClient`
- **Base URL**: `http://localhost:8086` (Inventory Management)
- **Methods**:
  - `getProductById(Long id)` → Get product details & price

### 3. **InventoryServiceClient** (NEW)
- **Interface**: `com.example.order_management.client.inventory.InventoryServiceClient`
- **Base URL**: `http://localhost:8086` (Inventory Management)
- **Methods**:
  - `getAllStocks()` → Get all stocks
  - `getStockByProductId(Long productId)` → Get stock for a product
  - `checkSufficientStock(Long productId, Integer quantity)` → Check if stock is available
  - `getStockById(Long id)` → Get specific stock

---

## 📝 Order Creation Flow dengan Validasi Multi-Service

### Step-by-Step Process:

```
1. Client mengirim POST request ke /api/orders
   └─ Request body: customerId, items[]
   
2. OrderService.createOrder() dipanggil
   │
   ├─ STEP 1: Validasi Customer
   │  └─ UserServiceClient.getUserById(customerId)
   │     └─ ✅ User valid → Lanjut
   │     └─ ❌ User tidak ada → Throw Exception
   │
   ├─ STEP 2: Validasi Setiap Product
   │  └─ untuk setiap item dalam order:
   │     │
   │     ├─ ProductServiceClient.getProductById()
   │     │  └─ ✅ Product exist → Get price
   │     │  └─ ❌ Product tidak ada → Throw Exception
   │     │
   │     └─ InventoryServiceClient.checkSufficientStock()
   │        └─ ✅ Stock tersedia → Lanjut
   │        └─ ❌ Stock kurang → Throw Exception
   │
   ├─ STEP 3: Create Order & OrderItems
   │  └─ Set status: PENDING
   │  └─ Calculate total amount
   │
   └─ STEP 4: Save to Database
      └─ Return OrderResponse (201 Created)
```

---

## 🚀 Endpoints Order Management

### 1. **Create Order dengan Integrasi** ✨
```http
POST /api/orders?customerId=1
Content-Type: application/json

[
  {
    "productId": 1,
    "quantity": 2
  },
  {
    "productId": 2,
    "quantity": 5
  }
]
```

**Request Validation Chain**:
1. ✅ Customer ID 1 exists in User Management (port 8082)
2. ✅ Product ID 1 exists in Inventory Management (port 8086)
3. ✅ Product 1 has stock ≥ 2 units
4. ✅ Product ID 2 exists in Inventory Management
5. ✅ Product 2 has stock ≥ 5 units
6. ✅ Create order jika semua validasi lolos

**Response Success (201 Created)**:
```json
{
  "id": 1,
  "orderNumber": "ORD-1713355200000",
  "customerId": 1,
  "customerNameSnapshot": "John Doe",
  "status": "PENDING",
  "totalAmount": 3500.00,
  "items": [
    {
      "id": 1,
      "productId": 1,
      "quantity": 2,
      "price": 1000.00,
      "subtotal": 2000.00
    },
    {
      "id": 2,
      "productId": 2,
      "quantity": 5,
      "price": 300.00,
      "subtotal": 1500.00
    }
  ],
  "createdAt": "2024-04-26T10:00:00"
}
```

**Response Error - Customer Not Found (400)**:
```json
{
  "status": "error",
  "message": "Gagal validasi customer: Customer tidak ditemukan dengan ID: 1"
}
```

**Response Error - Insufficient Stock (400)**:
```json
{
  "status": "error",
  "message": "Gagal validasi stok: Stok tidak cukup untuk product 1. Dibutuhkan: 100"
}
```

---

## 🔧 Service Endpoints Baru di Inventory Management

### 1. **Get Total Stock by Product ID** (NEW)
```http
GET /api/stocks/product/{productId}
```
- Digunakan oleh OrderService untuk mendapatkan stock info
- Returns: StockResponse dengan detail stock per warehouse

### 2. **Check Sufficient Stock** (NEW)
```http
GET /api/stocks/product/{productId}/check/{quantity}
```
- Digunakan oleh OrderService untuk validasi stock availability
- Returns: `true` atau `false`

---

## 📊 Teknologi yang Digunakan

### **Feign Client**
- **Purpose**: REST client untuk komunikasi antar services
- **Dependency**: `org.springframework.cloud:spring-cloud-starter-openfeign:4.1.0`
- **Configuration**: `@EnableFeignClients` di `OrderManagementApplication`

### **Logging**
- **Framework**: SLF4J (built-in Spring Boot)
- **Logger**: `@Slf4j` annotation dari Lombok
- **Log Levels**:
  - `DEBUG`: Method entry/exit, detailed flow
  - `INFO`: Order creation, status changes
  - `WARN`: Insufficient stock, invalid data
  - `ERROR`: Service failures, exceptions

---

## 🧪 Testing Integration

### Prerequisites:
1. MySQL database running dengan:
   - `order_management_db`
   - `user_db`
   - `inventory_management_db`

2. Ketiga services running:
   ```bash
   # Terminal 1: Order Management
   cd Order_Management
   mvn spring-boot:run  # Port: 8080
   
   # Terminal 2: User Management
   cd User_Management
   mvn spring-boot:run  # Port: 8082
   
   # Terminal 3: Inventory Management
   cd Inventory_Management/eai_ecommerce_inventory_management
   mvn spring-boot:run  # Port: 8086
   ```

### Test Case 1: Successful Order Creation
```bash
curl -X POST http://localhost:8080/api/orders?customerId=1 \
  -H "Content-Type: application/json" \
  -d '[
    {"productId": 1, "quantity": 2}
  ]'
```

### Test Case 2: Customer Not Found
```bash
curl -X POST http://localhost:8080/api/orders?customerId=999 \
  -H "Content-Type: application/json" \
  -d '[
    {"productId": 1, "quantity": 2}
  ]'
```

### Test Case 3: Insufficient Stock
```bash
curl -X POST http://localhost:8080/api/orders?customerId=1 \
  -H "Content-Type: application/json" \
  -d '[
    {"productId": 1, "quantity": 9999}
  ]'
```

---

## 🐛 Troubleshooting

### Error: "Connection refused" untuk User Management
- **Cause**: User Management tidak jalan di port 8082
- **Solution**: 
  ```bash
  cd User_Management
  mvn spring-boot:run
  ```

### Error: "Connection refused" untuk Inventory Management
- **Cause**: Inventory Management tidak jalan di port 8086
- **Solution**: 
  ```bash
  cd Inventory_Management/eai_ecommerce_inventory_management
  mvn spring-boot:run
  ```

### Error: "Product not found"
- **Cause**: Product ID tidak ada di Inventory Management
- **Solution**: Pastikan product sudah dibuat di Inventory Management terlebih dahulu
  ```bash
  curl -X POST http://localhost:8086/api/products \
    -H "Content-Type: application/json" \
    -d '{
      "sku": "PROD-001",
      "name": "Sample Product",
      "price": 1000.00,
      "weight": 1.5,
      "unitSize": 0.5
    }'
  ```

### Error: "Insufficient stock"
- **Cause**: Stock tidak cukup untuk quantity yang diminta
- **Solution**: Check stock di Inventory Management
  ```bash
  curl http://localhost:8086/api/stocks/product/1
  ```

---

## 📈 Monitoring & Logs

Untuk melihat logs dari integrasi:

```bash
# Tail logs Order Management (port 8080)
# Lihat DEBUG logs dari OrderService
# Format: [TIME] [LEVEL] [LOGGER] - MESSAGE
```

Contoh log output:
```
10:30:45.123 [http-nio-8080-exec-1] INFO  OrderService - Creating order for customer: 1 with 2 items
10:30:45.234 [http-nio-8080-exec-1] DEBUG OrderService - Validating product 1 with quantity 2
10:30:45.345 [http-nio-8080-exec-1] INFO  OrderService - Customer validated: John Doe
10:30:45.456 [http-nio-8080-exec-1] INFO  OrderService - Stock validated for product 1
10:30:45.567 [http-nio-8080-exec-1] INFO  OrderService - Order created successfully: 1 with total: 2000.0
```

---

## ✅ Checklist

- [x] Add Feign Client dependency ke Order Management
- [x] Create UserServiceClient untuk validasi customer
- [x] Create ProductServiceClient untuk get product info
- [x] Create InventoryServiceClient untuk validasi stock
- [x] Add repository methods untuk stock checking
- [x] Add service methods untuk stock validation
- [x] Add controller endpoints untuk stock API
- [x] Update OrderService dengan multi-service validation
- [x] Add error handling dan logging
- [x] Update Inventory Management port dari 8083 ke 8086
- [x] Configure logging di application.properties
- [x] Create integration documentation

---

## 📞 Support

Untuk pertanyaan atau issues:
1. Check logs di console untuk error details
2. Pastikan semua services running dengan port yang benar
3. Verify database connections
4. Review request payload dan response
