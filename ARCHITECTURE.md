# Architecture & System Design

## 1. HIGH-LEVEL ARCHITECTURE

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              CLIENT LAYER                                    │
├─────────────────┬───────────────────┬──────────────────┬────────────────────┤
│  Swagger UI     │  Postman Client   │  Web Browser     │  Mobile App        │
│  (Docs)         │  (Testing)        │  (Frontend)      │  (REST Client)     │
└────────┬────────┴────────┬──────────┴────────┬─────────┴────────┬───────────┘
         │                 │                   │                 │
         └─────────────────┼───────────────────┼─────────────────┘
                           │
                    ┌──────▼──────────────────────────────┐
                    │   HTTP/REST API Layer               │
                    │  (Spring Boot 4.0.5, Java 17)       │
                    ├─────────────────────────────────────┤
                    │  OpenAPI 3.0 / Swagger UI           │
                    │  JWT Authentication                 │
                    │  Request/Response Validation        │
                    └───────────┬────────────────────────┘
                                │
                ┌───────────────┼───────────────────┐
                │               │                   │
         ┌──────▼───────┐ ┌────▼──────────┐ ┌──────▼──────┐
         │  Order       │ │  Product      │ │  Category   │
         │  Controller  │ │  Controller   │ │  Controller │
         └──────┬───────┘ └────┬──────────┘ └──────┬──────┘
                │               │                   │
         ┌──────▼───────────────▼───────────────────▼──────┐
         │        SERVICE LAYER (Business Logic)          │
         ├──────────────────────────────────────────────────┤
         │ • OrderService      • ProductService           │
         │ • CategoryService   • CustomerService          │
         │ • Validation Logic  • Transaction Management   │
         └──────┬─────────────────────────────────────────┘
                │
        ┌───────┴────────────┬──────────────────┐
        │                    │                  │
  ┌─────▼──────────┐   ┌────▼────────────┐  ┌─▼─────────────────┐
  │  KAFKA         │   │  DATABASE       │  │  Kafka Producer   │
  │  INTEGRATION   │   │  LAYER (JPA)    │  │  OrderEvent       │
  │                │   │                 │  │  Publishing       │
  │ Producers:     │   │ • Order Entity  │  └───────────────────┘
  │ • Order        │   │ • Product       │
  │   Created      │   │ • Category      │
  │ • Order        │   │ • Customer      │
  │   Updated      │   │ • OrderItem     │
  │ • Order        │   │                 │
  │   Cancelled    │   │ MySQL Database  │
  │                │   │ (8.0+)          │
  │ Consumers:     │   │                 │
  │ • Event        │   └─────────────────┘
  │   Listeners    │
  │ • Event        │
  │   Processing   │
  └────────────────┘
```

---

## 2. COMPONENT ARCHITECTURE

### A. REST API Layer

```
┌──────────────────────────────────────┐
│     SecurityConfig (JWT)             │
│  JwtAuthenticationFilter             │
│  JwtUtils                            │
└────────────┬─────────────────────────┘
             │
    ┌────────┴────────┐
    │                 │
┌───▼────────┐  ┌────▼──────────┐
│ Public     │  │ Protected      │
│ Endpoints: │  │ Endpoints:     │
│ • Login    │  │ • /api/orders  │
│ • Register │  │ • /api/products│
│            │  │ • /api/...*    │
└────────────┘  └────┬───────────┘
                     │
            ┌────────▼────────────┐
            │  OpenAPI Config     │
            │  • Swagger UI       │
            │  • API Docs JSON    │
            │  • Security Scheme  │
            └─────────────────────┘
```

### B. Kafka Integration Layer

```
┌─────────────────────────────────────────────────┐
│         KAFKA CLUSTER (3 Topics)                │
├─────────────────────────────────────────────────┤
│  Topic: order-created                           │
│  ├─ Partition 0                                 │
│  ├─ Partition 1                                 │
│  └─ Partition 2                                 │
│                                                  │
│  Topic: order-status-updated                    │
│  ├─ Partition 0                                 │
│  ├─ Partition 1                                 │
│  └─ Partition 2                                 │
│                                                  │
│  Topic: order-cancelled                         │
│  ├─ Partition 0                                 │
│  ├─ Partition 1                                 │
│  └─ Partition 2                                 │
└────────┬──────────────────────────────────────┘
         │
    ┌────┴───────────┐
    │                │
┌───▼──────────┐  ┌─▼──────────┐
│  Producer    │  │  Consumer  │
│              │  │            │
│ OrderProducer   │ OrderConsumer
│ • Send events  │ • Listen to events
│   to Kafka     │ • Process messages
└────────────┘  └────────────┘
```

### C. Database Layer

```
┌────────────────────────────────────────┐
│     MySQL Database (8.0+)              │
│  Database: order_management_db         │
├────────────────────────────────────────┤
│  Tables:                               │
│  ├─ categories                         │
│  │  ├─ id (PK)                         │
│  │  ├─ name                            │
│  │  └─ created_at                      │
│  │                                      │
│  ├─ products                           │
│  │  ├─ id (PK)                         │
│  │  ├─ name                            │
│  │  ├─ price                           │
│  │  ├─ stock                           │
│  │  ├─ category_id (FK)                │
│  │  └─ created_at                      │
│  │                                      │
│  ├─ customers                          │
│  │  ├─ id (PK)                         │
│  │  ├─ name                            │
│  │  ├─ email                           │
│  │  ├─ address                         │
│  │  └─ created_at                      │
│  │                                      │
│  ├─ orders                             │
│  │  ├─ id (PK)                         │
│  │  ├─ customer_id (FK)                │
│  │  ├─ status                          │
│  │  ├─ total_amount                    │
│  │  ├─ created_at                      │
│  │  └─ updated_at                      │
│  │                                      │
│  └─ order_items                        │
│     ├─ id (PK)                         │
│     ├─ order_id (FK)                   │
│     ├─ product_id (FK)                 │
│     ├─ quantity                        │
│     ├─ price                           │
│     └─ created_at                      │
└────────────────────────────────────────┘
```

---

## 3. REQUEST FLOW DIAGRAM

### Flow: Create Order dengan Kafka Event

```
1. CLIENT REQUEST
   ┌──────────────────────────────────────────┐
   │  POST /api/orders?customerId=1           │
   │  Headers: Authorization: Bearer <token>  │
   │  Body: [OrderItemRequest...]             │
   └──────────┬───────────────────────────────┘
              │
2. AUTHENTICATION
   ┌──────────────┴──────────────┐
   │ JwtAuthenticationFilter      │
   │ • Validate JWT Token         │
   │ • Extract User Info          │
   │ • Set SecurityContext        │
   └──────────┬───────────────────┘
              │
3. API VALIDATION
   ┌──────────────┴──────────────┐
   │ OrderController              │
   │ • Validate input             │
   │ • Check authorization        │
   │ • Route to service           │
   └──────────┬───────────────────┘
              │
4. BUSINESS LOGIC
   ┌──────────────┴──────────────┐
   │ OrderService.createOrder()   │
   │ • Fetch customer             │
   │ • Fetch products             │
   │ • Calculate total            │
   │ • Create Order entity        │
   └──────────┬───────────────────┘
              │
5. DATABASE PERSIST
   ┌──────────────┴──────────────┐
   │ OrderRepository              │
   │ • Save to MySQL              │
   │ • Generate Order ID          │
   │ • Return persisted entity    │
   └──────────┬───────────────────┘
              │
6. KAFKA EVENT
   ┌──────────────┴──────────────┐
   │ OrderProducer                │
   │ • Create OrderEvent          │
   │ • Set event metadata         │
   │ • Send to Kafka topic        │
   └──────────┬───────────────────┘
              │
7. KAFKA PROCESSING
   ┌──────────────┴──────────────┐
   │ Kafka Broker (order-created) │
   │ • Store message              │
   │ • Replicate to partitions    │
   │ • Acknowledge producer       │
   └──────────┬───────────────────┘
              │
8. EVENT CONSUMPTION
   ┌──────────────┴──────────────┐
   │ OrderConsumer                │
   │ • Listen to topic            │
   │ • Deserialize message        │
   │ • Process event              │
   │ • Log/Handle event           │
   └──────────┬───────────────────┘
              │
9. RESPONSE
   ┌──────────────┴──────────────┐
   │  201 Created                 │
   │  {                           │
   │    "id": 1,                  │
   │    "customer": {...},        │
   │    "items": [...],           │
   │    "totalAmount": 500000,    │
   │    "status": "PENDING"       │
   │  }                           │
   └──────────────────────────────┘
```

---

## 4. TECHNOLOGY STACK

| Layer | Technology | Version |
|-------|-----------|---------|
| **Language** | Java | 17 LTS |
| **Framework** | Spring Boot | 4.0.5 |
| **API Documentation** | OpenAPI / Swagger | 3.0 |
| **Database** | MySQL | 8.0+ |
| **Message Broker** | Apache Kafka | 7.5.0 |
| **Coordination** | Zookeeper | 7.5.0 |
| **ORM** | Spring Data JPA | Included |
| **Security** | Spring Security + JWT | JJWT 0.12.3 |
| **Validation** | Spring Validation | Included |
| **Build Tool** | Maven | 3.6+ |
| **Monitoring** | Kafka UI | Latest |

---

## 5. DEPLOYMENT DIAGRAM

```
┌─────────────────────────────────────────────────────┐
│              DOCKER ENVIRONMENT                     │
├─────────────────────────────────────────────────────┤
│                                                      │
│  ┌──────────┐  ┌────────┐  ┌──────────┐  ┌────────┐
│  │Zookeeper │  │ Kafka  │  │Kafka UI  │  │ MySQL  │
│  │:2181     │  │:9092   │  │:8080     │  │:3306   │
│  │          │  │        │  │          │  │        │
│  │Network:  │  │Network:│  │Network:  │  │Network:│
│  │:2181     │  │:29092  │  │:8080     │  │:3306   │
│  └────┬─────┘  └───┬────┘  └─────┬────┘  └───┬────┘
│       │            │             │           │
│  ┌────┴─────────────┴─────────────┴───────────┴────┐
│  │         Docker Network: kafka-network          │
│  └───────────────────────────────────────────────┘
│                                                      │
│  ┌────────────────────────────────────────────────┐
│  │  Order Management Application                  │
│  │  :8084 (Host) ← 8080 (Container)              │
│  │                                                 │
│  │  Connections:                                  │
│  │  • Kafka: kafka:29092 (internal network)      │
│  │  • MySQL: mysql:3306 (internal network)       │
│  │                                                 │
│  └────────────────────────────────────────────────┘
│                                                      │
└─────────────────────────────────────────────────────┘
```

---

## 6. SEQUENCE DIAGRAM: Create Order

```
User      Controller    Service      Repository    Kafka        Consumer
 │            │            │              │          │            │
 │─POST /orders────────────────>
 │            │ Validate JWT               │          │            │
 │            ├─check token─────>         │          │            │
 │            │<─authenticated─ │          │          │            │
 │            │                 │          │          │            │
 │            │ Create order    │          │          │            │
 │            ├─────────────────>          │          │            │
 │            │                 ├─save to DB──>      │            │
 │            │                 │<─Order ID┤         │            │
 │            │<─Order object─────┤        │          │            │
 │            │                 │          │          │            │
 │            │ Send Kafka event            │          │            │
 │            ├─────────────────────────────┼──>      │            │
 │            │                            ├─Produce──>            │
 │            │                            │<─Ack─────┤            │
 │            │<──────────────────────────────────────┤            │
 │            │                           │    ├──────Listen──────>
 │            │                           │    │   │<─ consume ──┤
 │<─201 Created───────────────┤           │    │   │  Process   │
 │            │                │          │    │   │  Logging   │
 │            │                │          │    │   │            │
 └            └                └          └    └    └            ┘
```

---

## 7. ERROR HANDLING FLOW

```
API Request
    │
    ├─ Input Validation Error
    │  └─→ 400 Bad Request
    │      {
    │        "error": "Invalid input",
    │        "details": [...]
    │      }
    │
    ├─ Authentication Error
    │  └─→ 401 Unauthorized
    │      {
    │        "error": "Invalid or expired token"
    │      }
    │
    ├─ Authorization Error
    │  └─→ 403 Forbidden
    │      {
    │        "error": "Insufficient permissions"
    │      }
    │
    ├─ Resource Not Found
    │  └─→ 404 Not Found
    │      {
    │        "error": "Resource not found"
    │      }
    │
    ├─ Business Logic Error
    │  └─→ 400 Bad Request / 409 Conflict
    │      {
    │        "error": "Cannot cancel order in current status"
    │      }
    │
    ├─ Kafka Producer Error
    │  └─→ 500 Internal Server Error (logged, order still saved)
    │      {
    │        "error": "Failed to publish event"
    │      }
    │
    └─ System Error
       └─→ 500 Internal Server Error
           {
             "error": "Internal server error"
           }
```

---

## 8. SCALABILITY & PERFORMANCE CONSIDERATIONS

### Current Setup
- **Single Kafka Broker**: 3 partitions per topic
- **Replication Factor**: 1 (development only)
- **Consumer Concurrency**: 3 threads
- **Connection Pool**: HikariCP default

### Production Recommendations
```
Kafka:
├─ Multiple brokers (3+) for replication
├─ Replication factor: 3
├─ Partitions: Scale with throughput
└─ Enable compression (gzip/snappy)

Database:
├─ Connection pool: 20-50 connections
├─ Read replicas for heavy read workloads
├─ Indexing strategy optimization
└─ Backup & recovery plan

Application:
├─ Horizontal scaling with load balancer
├─ Caching layer (Redis) for frequently accessed data
├─ Async processing for heavy operations
└─ Circuit breaker for external service calls
```

---

**Version:** 1.0.0  
**Last Updated:** April 25, 2026
