
#!/bin/bash

echo "Testing Kafka Communication Between Microservices"
echo "================================================"

# Wait for services to start
sleep 5

echo "1. Creating a product (should trigger inventory creation)..."
curl -X POST http://localhost:8081/api/v1/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Product",
    "description": "Test Description", 
    "price": 99.99,
    "stockQuantity": 50,
    "category": "Electronics"
  }'

echo -e "\n\n2. Checking if inventory was created..."
sleep 2
curl http://localhost:8084/api/v1/inventory/9

echo -e "\n\n3. Creating an order (should reduce inventory and send notification)..."
curl -X POST http://localhost:8082/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "customer123",
    "items": [
      {
        "productId": 13,
        "productName": "Test Product",
        "quantity": 2,
        "unitPrice": 99.99,
        "totalPrice": 199.98
      }
    ],
    "shippingAddress": "123 Test St",
    "billingAddress": "123 Test St"
  }'

echo -e "\n\n4. Checking updated inventory..."
sleep 2
curl http://localhost:8084/api/v1/inventory/9
sleep 10
echo -e "\n\nTest complete! Check service logs for Kafka message processing."
