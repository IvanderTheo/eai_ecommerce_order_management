# Identifikasi 5 Event & Alur EDA Refund

## 🎯 PART 1: Identifikasi 5 Event yang Sudah Ada

### Event 1: ORDER_CREATED
**Topic:** `order-created`
**Triggered by:** OrderController.createOrder()
**Payload:**
```json
{
  "orderId": 1,
  "customerId": 5,
  "status": "PENDING",
  "totalAmount": 500000,
  "eventType": "ORDER_CREATED",
  "timestamp": "2026-04-26T10:00:00",
  "description": "Pesanan baru telah dibuat"
}
```
**Konsumer:** OrderConsumer.consumeOrderCreatedEvent()
**Business Logic:** Notifikasi pelanggan, update inventory, create payment record

---

### Event 2: ORDER_STATUS_UPDATED
**Topic:** `order-status-updated`
**Triggered by:** OrderController.updateStatus()
**Payload:**
```json
{
  "orderId": 1,
  "customerId": 5,
  "status": "SHIPPED",
  "totalAmount": 500000,
  "eventType": "ORDER_STATUS_UPDATED",
  "timestamp": "2026-04-26T11:00:00",
  "description": "Status pesanan diperbarui menjadi: SHIPPED"
}
```
**Status yang mungkin:** PENDING → CONFIRMED → SHIPPED → DELIVERED
**Konsumer:** OrderConsumer.consumeOrderStatusUpdatedEvent()
**Business Logic:** Update dashboard, send notification, tracking info

---

### Event 3: ORDER_CANCELLED
**Topic:** `order-cancelled`
**Triggered by:** OrderController.cancelOrder()
**Payload:**
```json
{
  "orderId": 1,
  "customerId": 5,
  "eventType": "ORDER_CANCELLED",
  "timestamp": "2026-04-26T12:00:00",
  "description": "Pesanan telah dibatalkan oleh user"
}
```
**Konsumer:** OrderConsumer.consumeOrderCancelledEvent()
**Business Logic:** Release inventory, initiate refund, send notification

---

### Event 4: PAYMENT_RECEIVED (NEW - Diperlukan untuk Refund)
**Topic:** `payment-received`
**Triggered by:** PaymentService (bisa dari payment gateway webhook)
**Payload:**
```json
{
  "paymentId": 100,
  "orderId": 1,
  "customerId": 5,
  "amount": 500000,
  "eventType": "PAYMENT_RECEIVED",
  "timestamp": "2026-04-26T10:30:00",
  "description": "Pembayaran diterima untuk order 1",
  "paymentMethod": "CREDIT_CARD",
  "transactionId": "TXN123456789"
}
```
**Business Logic:** Confirm order, update payment status, trigger order processing

---

### Event 5: INVENTORY_UPDATED (NEW - Untuk tracking stock)
**Topic:** `inventory-updated`
**Triggered by:** InventoryService
**Payload:**
```json
{
  "productId": 1,
  "quantity": -5,
  "eventType": "INVENTORY_UPDATED",
  "timestamp": "2026-04-26T10:30:00",
  "description": "Inventory berkurang 5 unit untuk product 1",
  "orderId": 1,
  "remainingStock": 45
}
```
**Business Logic:** Track stock levels, trigger reorder alerts, update warehouse

---

## 📊 PART 2: Event Flow Architecture (Existing)

```
┌─────────────────────────────────────────────────────────────────┐
│                    EVENT SOURCES                                │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  User creates order  →  ORDER_CREATED event                     │
│       ↓                  (published to Kafka)                    │
│  Event Consumer processes:                                      │
│  • Notify customer                                              │
│  • Reserve inventory                                            │
│  • Create payment record                                        │
│                                                                   │
│  Payment gateway webhook  →  PAYMENT_RECEIVED event             │
│       ↓                       (published to Kafka)               │
│  Event Consumer processes:                                      │
│  • Confirm order status to CONFIRMED                            │
│  • Trigger shipment preparation                                 │
│  • Reduce inventory                                             │
│                                                                   │
│  Order status change  →  ORDER_STATUS_UPDATED event             │
│       ↓                 (published to Kafka)                     │
│  Event Consumer processes:                                      │
│  • Update order tracking                                        │
│  • Send shipment notification                                   │
│                                                                   │
│  User requests cancel  →  ORDER_CANCELLED event                 │
│       ↓                 (published to Kafka)                     │
│  Event Consumer processes:                                      │
│  • Release reserved inventory                                   │
│  • Initiate REFUND_INITIATED event                              │
│  • Notify customer of cancellation                              │
│                                                                   │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🔄 PART 3: Event Flow untuk REFUND (New)

### Skenario Refund Process

```
                    REFUND REQUEST
                          ↓
                ┌─────────────────────┐
                │ User requests refund │
                │ POST /api/refunds    │
                └────────┬────────────┘
                         ↓
         ┌───────────────────────────────────┐
         │ Validate refund eligibility       │
         │ • Order status check              │
         │ • Refund window check             │
         │ • Amount validation               │
         └────────┬────────────────────────┘
                  ↓
            ✅ VALID / ❌ INVALID
                  ↓
    ┌─────────────────────────────────┐
    │   REFUND_INITIATED EVENT        │
    │   Topic: refund-initiated       │
    │   (Published to Kafka)          │
    └────────┬────────────────────────┘
             ↓
    ┌─────────────────────────────────┐
    │  Consumer receives event        │
    │  • Log refund request           │
    │  • Create Refund record         │
    │  • Update order status          │
    └────────┬────────────────────────┘
             ↓
    ┌─────────────────────────────────┐
    │   REFUND_PROCESSING EVENT       │
    │   Topic: refund-processing      │
    │   (Published to Kafka)          │
    └────────┬────────────────────────┘
             ↓
    ┌─────────────────────────────────┐
    │ Consumer receives event         │
    │ • Call payment gateway          │
    │ • Process refund transaction    │
    │ • Handle payment API response   │
    └────────┬────────────────────────┘
             ↓
    ┌─────────────────────────────────┐
    │  ✅ SUCCESS / ❌ FAILED          │
    └─┬──────────────────────────────┬┘
      │                              │
      ✅ SUCCESS                  ❌ FAILED
      │                              │
      ↓                              ↓
┌──────────────────┐     ┌──────────────────┐
│ REFUND_COMPLETED │     │  REFUND_FAILED   │
│ EVENT            │     │  EVENT           │
│ Topic:           │     │  Topic:          │
│ refund-completed │     │  refund-failed   │
└────────┬─────────┘     └────────┬─────────┘
         │                        │
         ├─ Consumer processes    │
         │  • Restore inventory   │
         │  • Send confirmation   │
         │  • Update records      │
         │                        │
         │                ├─ Consumer processes
         │                │  • Log error details
         │                │  • Retry mechanism
         │                │  • Manual intervention
         │                │
         ↓                ↓
    CUSTOMER         CUSTOMER
    ✅ REFUNDED    ❌ RETRY / MANUAL
```

---

## 5️⃣ PART 4: Events Refund yang Diperlukan

### Event 1: REFUND_INITIATED
**Topic:** `refund-initiated`
**Publisher:** RefundController.requestRefund()
```json
{
  "refundId": 1001,
  "orderId": 1,
  "customerId": 5,
  "originalAmount": 500000,
  "refundAmount": 500000,
  "reason": "User requested cancellation",
  "eventType": "REFUND_INITIATED",
  "timestamp": "2026-04-26T13:00:00",
  "status": "INITIATED"
}
```

---

### Event 2: REFUND_PROCESSING
**Topic:** `refund-processing`
**Publisher:** RefundService.processRefund()
```json
{
  "refundId": 1001,
  "orderId": 1,
  "customerId": 5,
  "refundAmount": 500000,
  "eventType": "REFUND_PROCESSING",
  "timestamp": "2026-04-26T13:05:00",
  "status": "PROCESSING",
  "paymentMethod": "CREDIT_CARD",
  "transactionId": "ORIG_TXN123456789"
}
```

---

### Event 3: REFUND_COMPLETED
**Topic:** `refund-completed`
**Publisher:** PaymentGatewayService
```json
{
  "refundId": 1001,
  "orderId": 1,
  "customerId": 5,
  "refundAmount": 500000,
  "eventType": "REFUND_COMPLETED",
  "timestamp": "2026-04-26T13:15:00",
  "status": "COMPLETED",
  "refundTransactionId": "REFUND_TXN987654321",
  "estimatedArrival": "2026-04-28"
}
```

---

### Event 4: REFUND_FAILED
**Topic:** `refund-failed`
**Publisher:** PaymentGatewayService
```json
{
  "refundId": 1001,
  "orderId": 1,
  "customerId": 5,
  "refundAmount": 500000,
  "eventType": "REFUND_FAILED",
  "timestamp": "2026-04-26T13:20:00",
  "status": "FAILED",
  "errorCode": "PAYMENT_GATEWAY_ERROR",
  "errorMessage": "Connection timeout to payment gateway",
  "retryCount": 1,
  "nextRetryTime": "2026-04-26T14:00:00"
}
```

---

### Event 5: PAYMENT_REFUNDED
**Topic:** `payment-refunded`
**Publisher:** PaymentService
```json
{
  "paymentId": 100,
  "refundId": 1001,
  "orderId": 1,
  "customerId": 5,
  "originalAmount": 500000,
  "refundAmount": 500000,
  "eventType": "PAYMENT_REFUNDED",
  "timestamp": "2026-04-26T13:15:00",
  "status": "REFUNDED",
  "bankName": "Bank Central Asia",
  "lastFourDigits": "1234"
}
```

---

## 📝 Summary: Events Overview

| # | Event Name | Topic | Trigger | Status | Use Case |
|---|---|---|---|---|---|
| 1 | ORDER_CREATED | order-created | New order | EXISTING | Track new orders |
| 2 | ORDER_STATUS_UPDATED | order-status-updated | Status change | EXISTING | Track order progress |
| 3 | ORDER_CANCELLED | order-cancelled | User cancels | EXISTING | Handle cancellation |
| 4 | PAYMENT_RECEIVED | payment-received | Payment success | NEW | Track payments |
| 5 | INVENTORY_UPDATED | inventory-updated | Stock change | NEW | Track inventory |
| 6 | REFUND_INITIATED | refund-initiated | Refund request | NEW | Start refund |
| 7 | REFUND_PROCESSING | refund-processing | Refund process | NEW | Process refund |
| 8 | REFUND_COMPLETED | refund-completed | Success | NEW | Refund success |
| 9 | REFUND_FAILED | refund-failed | Failure | NEW | Handle error |
| 10 | PAYMENT_REFUNDED | payment-refunded | Confirm refund | NEW | Confirm refund |

---

**Dokumen ini akan dilanjutkan dengan:**
1. ✅ Diagram Draw.io untuk EDA Refund
2. ✅ Implementasi Refund Entity, Service, Controller
3. ✅ Refund Producer & Consumer
4. ✅ API Endpoints untuk Refund

**Last Updated:** April 26, 2026
