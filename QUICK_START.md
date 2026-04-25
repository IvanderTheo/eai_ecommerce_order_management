# Quick Start Guide - Kafka & OpenAPI Setup

## ⚡ Quick Start (5 Menit)

### Step 1: Start Docker Services

```bash
cd Order_Management
docker-compose up -d
```

**Expected Output:**
```
✓ zookeeper started
✓ kafka started
✓ kafka-ui started
✓ mysql started
```

### Step 2: Verify Kafka is Running

```bash
# Check all containers are running
docker ps

# Expected: 4 containers running (zookeeper, kafka, kafka-ui, mysql)
```

### Step 3: Start Order Management Application

```bash
cd Order_Management
mvn clean install
mvn spring-boot:run
```

**Expected Output:**
```
Order Management Application started on port 8084
Kafka topics created: order-created, order-status-updated, order-cancelled
```

### Step 4: Access Services

| Service | URL | Username | Password |
|---------|-----|----------|----------|
| Swagger UI | http://localhost:8084/swagger-ui.html | - | - |
| Kafka UI | http://localhost:8080 | - | - |
| MySQL | localhost:3306 | order_user | order_password |

---

## 🔧 Detailed Setup

### Prerequisites

- Docker & Docker Compose
- Java 17+
- Maven 3.6+
- MySQL 8.0+

### Environment Variables

Buat file `.env` di Order_Management folder:

```properties
# Database
MYSQLHOST=localhost
MYSQLPORT=3306
MYSQLDATABASE=order_management_db
MYSQLUSER=order_user
MYSQLPASSWORD=order_password

# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
KAFKA_GROUP_ID=order-management-group

# JWT
JWT_SECRET=v7S8D9fghjklMnBvCxzQWERTYUIOPasdfghjklmnbvcxz1234567890QWERTYUIOP
JWT_EXPIRATION=3600000

# Server
PORT=8084
```

---

## 📊 Testing dengan Swagger UI

### 1. Login untuk mendapatkan JWT Token

**Endpoint:** `POST /api/auth/login`

```json
{
  "username": "user1",
  "password": "password123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### 2. Authorize di Swagger

1. Buka http://localhost:8084/swagger-ui.html
2. Klik tombol **"Authorize"** (kanan atas)
3. Masukkan format: `Bearer <token-dari-response>`
4. Klik **"Authorize"**

### 3. Test Create Order

**Endpoint:** `POST /api/orders`

**Parameters:**
- `customerId`: 1 (atau ID customer yang valid)

**Request Body:**
```json
[
  {
    "productId": 1,
    "quantity": 2
  },
  {
    "productId": 3,
    "quantity": 1
  }
]
```

**Response:**
```json
{
  "id": 10,
  "customer": {
    "id": 1,
    "name": "John Doe"
  },
  "items": [...],
  "totalAmount": 1500000,
  "status": "PENDING",
  "createdAt": "2026-04-25T10:30:00"
}
```

### 4. Monitor Kafka Event

Buka Kafka UI: http://localhost:8080

1. Pilih cluster "local"
2. Navigate ke **Topics**
3. Klik **"order-created"**
4. View messages yang dikirim

---

## 🔄 Event Flow Examples

### Flow 1: Create Order

```
User → Swagger UI (POST /api/orders)
  ↓
OrderController.createOrder()
  ↓
OrderService.createOrder() → Save ke Database
  ↓
OrderProducer.sendOrderCreatedEvent() → Send ke Kafka
  ↓
Topic "order-created" menerima event
  ↓
OrderConsumer.consumeOrderCreatedEvent() → Process event
  ↓
Logs appear in application console
```

**Monitor di Kafka UI:**
1. Navigate ke Topics → order-created
2. View Messages section
3. Lihat payload JSON dari event

### Flow 2: Update Order Status

```
User → Swagger UI (PUT /api/orders/1/status?status=SHIPPED)
  ↓
OrderController.updateStatus()
  ↓
OrderService.updateStatus() → Update status di Database
  ↓
OrderProducer.sendOrderStatusUpdatedEvent() → Send ke Kafka
  ↓
Topic "order-status-updated" menerima event
  ↓
OrderConsumer.consumeOrderStatusUpdatedEvent() → Process event
```

### Flow 3: Cancel Order

```
User → Swagger UI (POST /api/orders/1/cancel)
  ↓
OrderController.cancelOrder()
  ↓
OrderService.cancelOrder() → Update status ke CANCELLED
  ↓
OrderProducer.sendOrderCancelledEvent() → Send ke Kafka
  ↓
Topic "order-cancelled" menerima event
  ↓
OrderConsumer.consumeOrderCancelledEvent() → Process event
```

---

## 📈 OpenAPI Features

### Available API Tags

| Tag | Endpoints | Description |
|-----|-----------|-------------|
| Order Management | 5 endpoints | Manage orders dengan Kafka integration |
| Category Management | 5 endpoints | CRUD operations untuk categories |
| Product Management | 5 endpoints | CRUD operations untuk products |
| Customer Management | 5 endpoints | CRUD operations untuk customers |

### API Documentation Features

✅ Full endpoint documentation dengan descriptions
✅ Request/Response schemas
✅ Error responses dengan HTTP status codes
✅ JWT Bearer authentication documentation
✅ Parameter documentation
✅ Try it out functionality

---

## 🛠️ Troubleshooting

### Problem: Kafka Connection Refused

**Error:**
```
KafkaProducerException: Failed to get the list of topics
```

**Solution:**
```bash
# Check if Kafka is running
docker ps | grep kafka

# If not running
docker-compose up -d

# Check logs
docker logs kafka
```

### Problem: Swagger UI tidak muncul

**Error:**
```
404 - /swagger-ui.html not found
```

**Solution:**
```bash
# Rebuild project
mvn clean install

# Verify dependency in pom.xml
# Check for springdoc-openapi-starter-webmvc-ui

# Restart application
mvn spring-boot:run
```

### Problem: Topics tidak terbuat otomatis

**Solution:**
```bash
# Manually create topics
docker exec -it kafka kafka-topics \
  --bootstrap-server localhost:9092 \
  --create \
  --topic order-created \
  --partitions 3 \
  --replication-factor 1

docker exec -it kafka kafka-topics \
  --bootstrap-server localhost:9092 \
  --create \
  --topic order-status-updated \
  --partitions 3 \
  --replication-factor 1

docker exec -it kafka kafka-topics \
  --bootstrap-server localhost:9092 \
  --create \
  --topic order-cancelled \
  --partitions 3 \
  --replication-factor 1
```

### Problem: JWT Token Invalid di Swagger

**Solution:**
```
1. Get valid token dari login endpoint
2. Format: "Bearer eyJhbGciOiJIUzI1NiIsInR5..."
3. Click Authorize button
4. Paste the full string including "Bearer"
5. Click Authorize
```

---

## 🧹 Cleanup Commands

```bash
# Stop all containers
docker-compose down

# Stop and remove volumes
docker-compose down -v

# Remove all containers
docker-compose down --remove-orphans

# View logs
docker-compose logs -f kafka

# Restart specific service
docker-compose restart kafka
```

---

## 📚 Useful Commands

### Kafka Commands

```bash
# List all topics
docker exec -it kafka kafka-topics \
  --bootstrap-server localhost:9092 \
  --list

# View topic details
docker exec -it kafka kafka-topics \
  --bootstrap-server localhost:9092 \
  --describe \
  --topic order-created

# View messages from beginning
docker exec -it kafka kafka-console-consumer \
  --bootstrap-server kafka:29092 \
  --topic order-created \
  --from-beginning

# View messages in real-time
docker exec -it kafka kafka-console-consumer \
  --bootstrap-server kafka:29092 \
  --topic order-status-updated \
  --from-beginning
```

### Maven Commands

```bash
# Clean and build
mvn clean install

# Run tests
mvn test

# Run application
mvn spring-boot:run

# Generate OpenAPI JSON
curl http://localhost:8084/v3/api-docs > openapi.json

# Generate OpenAPI YAML
curl http://localhost:8084/v3/api-docs.yaml > openapi.yaml
```

---

## 📋 Checklist

- [ ] Docker & Docker Compose installed
- [ ] Java 17+ installed
- [ ] Maven 3.6+ installed
- [ ] `docker-compose.yml` exists in Order_Management folder
- [ ] `pom.xml` has Kafka and OpenAPI dependencies
- [ ] Application started successfully
- [ ] Kafka topics created
- [ ] Swagger UI accessible
- [ ] Kafka UI accessible
- [ ] JWT token obtained
- [ ] Can create orders and see events in Kafka

---

## 📞 Support

**Issues or Questions?**

1. Check logs: `docker logs kafka` or application console
2. Verify all containers running: `docker ps`
3. Check network connectivity: `docker network ls`
4. Review KAFKA_OPENAPI_GUIDE.md for detailed information

---

**Last Updated:** April 25, 2026
**Version:** 1.0.0
