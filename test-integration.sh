#!/bin/bash

# Order Management Integration Test Script
# This script tests the integration between Order Management, User Management, and Inventory Management

BASE_URL="http://localhost:8080"
INVENTORY_URL="http://localhost:8086"
USER_URL="http://localhost:8082"

echo "========================================"
echo "Order Management Integration Tests"
echo "========================================"
echo ""

# Test 1: Check all services are running
echo "Test 1: Checking if all services are running..."
echo ""

echo "1.1 Checking Order Management (Port 8080)..."
if curl -s http://localhost:8080/swagger-ui.html > /dev/null 2>&1; then
    echo "✅ Order Management is running"
else
    echo "❌ Order Management is NOT running"
fi

echo "1.2 Checking User Management (Port 8082)..."
if curl -s http://localhost:8082/api/users/1 > /dev/null 2>&1; then
    echo "✅ User Management is running"
else
    echo "❌ User Management is NOT running"
fi

echo "1.3 Checking Inventory Management (Port 8086)..."
if curl -s http://localhost:8086/api/products > /dev/null 2>&1; then
    echo "✅ Inventory Management is running"
else
    echo "❌ Inventory Management is NOT running"
fi
echo ""

# Test 2: Get sample data
echo "Test 2: Getting sample data from services..."
echo ""

echo "2.1 Getting User (ID: 1)..."
curl -s -X GET "$USER_URL/api/users/1" \
  -H "Content-Type: application/json" | jq '.' || echo "Failed"
echo ""

echo "2.2 Getting Product (ID: 1)..."
curl -s -X GET "$INVENTORY_URL/api/products/1" \
  -H "Content-Type: application/json" | jq '.' || echo "Failed"
echo ""

echo "2.3 Getting Stock for Product (ID: 1)..."
curl -s -X GET "$INVENTORY_URL/api/stocks/product/1" \
  -H "Content-Type: application/json" | jq '.' || echo "Failed"
echo ""

# Test 3: Test Order Creation with Validation
echo "Test 3: Testing Order Creation with Integration Validation..."
echo ""

echo "3.1 Create Order with valid customer and product..."
echo "Request: POST $BASE_URL/api/orders?customerId=1"
echo "Payload:"
cat <<EOF | tee order_payload.json
[
  {
    "productId": 1,
    "quantity": 2
  }
]
EOF
echo ""

RESPONSE=$(curl -s -X POST "$BASE_URL/api/orders?customerId=1" \
  -H "Content-Type: application/json" \
  -d '[{"productId": 1, "quantity": 2}]')

echo "Response:"
echo "$RESPONSE" | jq '.' || echo "$RESPONSE"
echo ""

# Extract order ID if successful
ORDER_ID=$(echo "$RESPONSE" | jq -r '.id' 2>/dev/null || echo "")

if [ ! -z "$ORDER_ID" ] && [ "$ORDER_ID" != "null" ]; then
    echo "✅ Order created successfully with ID: $ORDER_ID"
    echo ""
    
    # Test 4: Get created order
    echo "Test 4: Getting created order..."
    echo "Request: GET $BASE_URL/api/orders/$ORDER_ID"
    echo ""
    curl -s -X GET "$BASE_URL/api/orders/$ORDER_ID" \
      -H "Content-Type: application/json" | jq '.'
    echo ""
else
    echo "❌ Order creation failed"
    echo ""
fi

# Test 5: Test error scenarios
echo "Test 5: Testing Error Scenarios..."
echo ""

echo "5.1 Try to create order with invalid customer..."
RESPONSE=$(curl -s -X POST "$BASE_URL/api/orders?customerId=999" \
  -H "Content-Type: application/json" \
  -d '[{"productId": 1, "quantity": 2}]')

if echo "$RESPONSE" | grep -q "error\|tidak\|gagal"; then
    echo "✅ Error handling works - Got expected error"
    echo "$RESPONSE" | jq '.' 2>/dev/null || echo "$RESPONSE"
else
    echo "Response: $RESPONSE"
fi
echo ""

echo "5.2 Try to create order with invalid product..."
RESPONSE=$(curl -s -X POST "$BASE_URL/api/orders?customerId=1" \
  -H "Content-Type: application/json" \
  -d '[{"productId": 9999, "quantity": 2}]')

if echo "$RESPONSE" | grep -q "error\|tidak\|gagal"; then
    echo "✅ Error handling works - Got expected error"
    echo "$RESPONSE" | jq '.' 2>/dev/null || echo "$RESPONSE"
else
    echo "Response: $RESPONSE"
fi
echo ""

echo "5.3 Try to create order with excessive quantity (stock check)..."
RESPONSE=$(curl -s -X POST "$BASE_URL/api/orders?customerId=1" \
  -H "Content-Type: application/json" \
  -d '[{"productId": 1, "quantity": 99999}]')

if echo "$RESPONSE" | grep -q "error\|tidak\|gagal\|stok"; then
    echo "✅ Stock validation works - Got expected error"
    echo "$RESPONSE" | jq '.' 2>/dev/null || echo "$RESPONSE"
else
    echo "Response: $RESPONSE"
fi
echo ""

echo "========================================"
echo "Integration Tests Completed!"
echo "========================================"
