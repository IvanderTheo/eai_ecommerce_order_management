# Kafka & OpenAPI Integration Guide

## Overview

Order Management System telah diintegrasikan dengan **Apache Kafka** untuk event streaming dan **OpenAPI 3.0 (Swagger)** untuk dokumentasi API yang interaktif.

---

## 1. KAFKA INTEGRATION

### 1.1 Topik Kafka yang Tersedia

| Topic | Deskripsi | Event Type |
|-------|-----------|-----------|
| `order-created` | Dipicu ketika pesanan baru dibuat | ORDER_CREATED |
| `order-status-updated` | Dipicu ketika status pesanan berubah | ORDER_STATUS_UPDATED |
| `order-cancelled` | Dipicu ketika pesanan dibatalkan | ORDER_CANCELLED |

### 1.2 Event Payload (OrderEvent)

```json
{
  "orderId": 1,
  "customerId": 5,
  "status": "CONFIRMED",
  "totalAmount": 500000.00,
  "eventType": "ORDER_CREATED",
  "timestamp": "2026-04-25T10:30:00",
  "description": "Pesanan baru telah dibuat"
}
```

### 1.3 Konfigurasi Kafka

File: `application.properties`

```properties
# Kafka Bootstrap Servers
spring.kafka.bootstrap-servers=localhost:9092

# Producer Configuration
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer

# Consumer Configuration
spring.kafka.consumer.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=order-management-group
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer
spring.kafka.consumer.properties.spring.json.trusted.packages=*
```

### 1.4 Startup Kafka (Menggunakan Docker Compose)

Buat file `docker-compose.yml`:

```yaml
version: '3.8'

services:
  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    ports:
      - "2181:2181"

  kafka:
    image: confluentinc/cp-kafka:7.5.0
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:29092,PLAINTEXT_HOST://localhost:9092
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_AUTO_CREATE_TOPICS_ENABLE: 'true'

  kafka-ui:
    image: provectuslabs/kafka-ui:latest
    depends_on:
      - kafka
    ports:
      - "8080:8080"
    environment:
      KAFKA_CLUSTERS_0_NAME: local
      KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS: kafka:29092
      KAFKA_CLUSTERS_0_ZOOKEEPER: zookeeper:2181
```

Jalankan:
```bash
docker-compose up -d
```

Kafka UI akan tersedia di: `http://localhost:8080`

### 1.5 Contoh Event Flow

#### A. Ketika Membuat Pesanan Baru
```
POST /api/orders?customerId=1
Body: [
  { "productId": 1, "quantity": 2 },
  { "productId": 3, "quantity": 1 }
]

↓

OrderController.createOrder() dipanggil

↓

Order dibuat di database

↓

OrderProducer.sendOrderCreatedEvent() mengirim event ke topic 'order-created'

↓

OrderConsumer menerima event dan memproses (logging, notifikasi, dll)
```

#### B. Update Status Pesanan
```
PUT /api/orders/1/status?status=SHIPPED

↓

OrderController.updateStatus() dipanggil

↓

Status diupdate di database

↓

OrderProducer.sendOrderStatusUpdatedEvent() mengirim event ke topic 'order-status-updated'

↓

OrderConsumer menerima event dan memproses
```

#### C. Batalkan Pesanan
```
POST /api/orders/1/cancel

↓

OrderController.cancelOrder() dipanggil

↓

Pesanan dibatalkan di database

↓

OrderProducer.sendOrderCancelledEvent() mengirim event ke topic 'order-cancelled'

↓

OrderConsumer menerima event dan memproses (release inventory, refund, dll)
```

### 1.6 Monitoring Kafka

#### Menggunakan kafka-console-consumer:

```bash
# Consumer dari topic order-created
docker exec -it kafka kafka-console-consumer --bootstrap-server kafka:9092 --topic order-created --from-beginning

# Consumer dari topic order-status-updated
docker exec -it kafka kafka-console-consumer --bootstrap-server kafka:9092 --topic order-status-updated --from-beginning

# Consumer dari topic order-cancelled
docker exec -it kafka kafka-console-consumer --bootstrap-server kafka:9092 --topic order-cancelled --from-beginning
```

---

## 2. OpenAPI & Swagger INTEGRATION

### 2.1 Akses Swagger UI

Setelah aplikasi berjalan di `http://localhost:8084`:

- **Swagger UI**: `http://localhost:8084/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8084/v3/api-docs`
- **OpenAPI YAML**: `http://localhost:8084/v3/api-docs.yaml`

### 2.2 API Endpoints yang Terdokumentasi

#### Order Management
- `GET /api/orders` - Dapatkan semua pesanan
- `POST /api/orders` - Buat pesanan baru
- `GET /api/orders/{id}` - Dapatkan pesanan berdasarkan ID
- `PUT /api/orders/{id}/status` - Update status pesanan
- `POST /api/orders/{id}/cancel` - Batalkan pesanan

#### Category Management
- `GET /api/categories` - Dapatkan semua kategori
- `POST /api/categories` - Buat kategori baru
- `GET /api/categories/{id}` - Dapatkan kategori berdasarkan ID
- `PUT /api/categories/{id}` - Update kategori
- `DELETE /api/categories/{id}` - Hapus kategori

#### Product Management
- `GET /api/products` - Dapatkan semua produk
- `POST /api/products` - Buat produk baru
- `GET /api/products/{id}` - Dapatkan produk berdasarkan ID
- `PUT /api/products/{id}` - Update produk
- `DELETE /api/products/{id}` - Hapus produk

#### Customer Management
- `GET /api/customers` - Dapatkan semua pelanggan
- `POST /api/customers` - Buat pelanggan baru
- `GET /api/customers/{id}` - Dapatkan pelanggan berdasarkan ID
- `PUT /api/customers/{id}` - Update pelanggan
- `DELETE /api/customers/{id}` - Hapus pelanggan

### 2.3 Authentication dengan Swagger

Semua endpoint memerlukan JWT Token. Cara menggunakannya di Swagger:

1. Dapatkan token melalui endpoint authentication
2. Klik tombol "Authorize" di Swagger UI
3. Masukkan token dengan format: `Bearer <your-token>`
4. Semua request akan include token otomatis

### 2.4 OpenAPI Configuration

File: `OpenAPIConfig.java`

- Nama API: "Order Management API"
- Versi: 1.0.0
- Security Scheme: JWT Bearer Token
- Servers: localhost:8084 (development), localhost:8080 (production)

---

## 3. TESTING FLOW

### 3.1 Test Skenario 1: Create Order dengan Kafka Event

```bash
# 1. Get JWT Token (dari /api/auth/login)
TOKEN="your-jwt-token"

# 2. Create Order dan monitor Kafka event
curl -X POST "http://localhost:8084/api/orders?customerId=1" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '[
    {"productId": 1, "quantity": 2},
    {"productId": 2, "quantity": 1}
  ]'

# 3. Monitor event di Kafka UI atau console consumer
# Event akan muncul di topic "order-created"
```

### 3.2 Test Skenario 2: Update Order Status

```bash
# 1. Update order status ke SHIPPED
curl -X PUT "http://localhost:8084/api/orders/1/status?status=SHIPPED" \
  -H "Authorization: Bearer $TOKEN"

# 2. Monitor Kafka UI untuk event di topic "order-status-updated"
```

### 3.3 Test Skenario 3: Cancel Order

```bash
# 1. Cancel order
curl -X POST "http://localhost:8084/api/orders/1/cancel" \
  -H "Authorization: Bearer $TOKEN"

# 2. Monitor Kafka UI untuk event di topic "order-cancelled"
```

---

## 4. STRUKTUR KODE

### Kafka Components

```
src/main/java/com/example/order_management/
├── kafka/
│   ├── KafkaConfig.java                 # Kafka configuration
│   ├── producer/
│   │   └── OrderProducer.java          # Kafka producer service
│   ├── consumer/
│   │   └── OrderConsumer.java          # Kafka consumer service
│   └── event/
│       └── OrderEvent.java             # Event model
```

### OpenAPI Components

```
src/main/java/com/example/order_management/
├── config/
│   └── OpenAPIConfig.java              # OpenAPI configuration
└── controller/
    ├── OrderController.java            # OpenAPI annotations
    ├── CategoryController.java         # OpenAPI annotations
    ├── ProductController.java          # OpenAPI annotations
    └── CustomerController.java         # OpenAPI annotations
```

---

## 5. DEPENDENCIES

### pom.xml

```xml
<!-- Spring Kafka -->
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>

<!-- SpringDoc OpenAPI -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.0.2</version>
</dependency>
```

---

## 6. TROUBLESHOOTING

### Issue 1: Kafka Connection Refused

**Solusi:**
```bash
# Pastikan Kafka berjalan
docker ps | grep kafka

# Jika tidak berjalan
docker-compose up -d
```

### Issue 2: Swagger UI tidak muncul

**Solusi:**
- Pastikan `springdoc-openapi-starter-webmvc-ui` sudah di pom.xml
- Rebuild project: `mvn clean install`
- Akses ulang: `http://localhost:8084/swagger-ui.html`

### Issue 3: JWT Token error di Swagger

**Solusi:**
- Pastikan token valid
- Format: `Bearer <token-tanpa-Bearer-keyword>`
- Click Authorize dengan format yang benar

### Issue 4: Event tidak terkirim ke Kafka

**Solusi:**
- Check Kafka logs: `docker logs kafka`
- Verify topic exist: `docker exec -it kafka kafka-topics --bootstrap-server localhost:9092 --list`
- Check OrderProducer logs untuk error messages

---

## 7. NEXT STEPS

### 7.1 Advanced Kafka Features
- Implement Dead Letter Topic (DLT) untuk error handling
- Add message compression
- Implement transaction support
- Add schema validation dengan Schema Registry

### 7.2 Advanced OpenAPI Features
- Add request/response examples
- Document error responses lebih detail
- Add API rate limiting documentation
- Implement API versioning di OpenAPI

### 7.3 Monitoring & Observability
- Add Kafka metrics dengan Micrometer
- Implement distributed tracing dengan Sleuth
- Add Prometheus monitoring
- Integrate dengan ELK stack untuk logging

---

## 8. USEFUL COMMANDS

```bash
# Start all services
docker-compose up -d

# View Kafka UI
open http://localhost:8080

# View Swagger UI
open http://localhost:8084/swagger-ui.html

# Stop all services
docker-compose down

# Clean all containers and volumes
docker-compose down -v

# View logs
docker-compose logs -f kafka
docker-compose logs -f zookeeper
```

---

**Last Updated:** April 25, 2026
**Version:** 1.0.0
