# TableSnap Backend

Spring Boot backend for the TableSnap food ordering system.

## Quick Start

### Using Gradle Wrapper (Recommended)

```bash
# Start the server
./gradlew bootRun

# Build the project
./gradlew build

# Clean and rebuild
./gradlew clean build
```

### Using IDE

**IntelliJ IDEA:**
- Open the project
- Run configuration "TableSnapApplication" is already configured in `.run/`
- Click the Run button or press `Shift + F10`

**VS Code:**
- Open the project
- Go to Run and Debug (Ctrl/Cmd + Shift + D)
- Select "TableSnap Backend" or "Spring Boot-TableSnapApplication"
- Press F5 to start

### Manual Run

```bash
# Build the JAR
./gradlew build

# Run the JAR
java -jar build/libs/tablesnap-backend-0.1.0.jar
```

## Configuration

The application runs on port **8080** by default.

Database: H2 in-memory (development mode)
- Console: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:tablesnap`
- Username: `sa`
- Password: (empty)

## API Endpoints

Base URL: `http://localhost:8080/api`

### 1. Get Restaurant
```bash
GET /restaurants/{id}
```

### 2. Get Menu
```bash
GET /restaurants/{restaurantId}/menu
```

### 3. Create Order
```bash
POST /orders
Content-Type: application/json

{
  "restaurantId": "restaurant-001",
  "items": [
    {
      "id": "item-001",
      "quantity": 2
    }
  ],
  "specialInstructions": "No onions please"
}
```

### 4. Get Order
```bash
GET /orders/{id}
```

### 5. Process Payment
```bash
POST /payments
Content-Type: application/json

{
  "orderId": "order-xxx",
  "method": "card",
  "amount": 28.49,
  "cardDetails": {
    "holderName": "John Doe",
    "cardNumber": "4111111111111111",
    "expiryDate": "12/25",
    "cvv": "123"
  }
}
```

## Testing Endpoints

Test data is automatically loaded on startup with:
- Restaurant ID: `restaurant-001` (La Bella Vita)
- 12 menu items (Pizza, Pasta, Desserts, Beverages)

Example test with curl:

```bash
# Get restaurant info
curl http://localhost:8080/api/restaurants/restaurant-001

# Get menu
curl http://localhost:8080/api/restaurants/restaurant-001/menu

# Create order
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "restaurantId": "restaurant-001",
    "items": [
      {"id": "item-001", "quantity": 2},
      {"id": "item-004", "quantity": 1}
    ],
    "specialInstructions": "Extra cheese"
  }'

# Get order (replace ORDER_ID with actual ID from previous response)
curl http://localhost:8080/api/orders/ORDER_ID

# Process payment
curl -X POST http://localhost:8080/api/payments \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORDER_ID",
    "method": "card",
    "amount": 39.97,
    "cardDetails": {
      "holderName": "John Doe",
      "cardNumber": "4111111111111111",
      "expiryDate": "12/25",
      "cvv": "123"
    }
  }'
```

## Tech Stack

- Java 17
- Spring Boot 3.2.0
- Spring Data JPA
- H2 Database (in-memory)
- Lombok
- Gradle 8.5

## CORS Configuration

CORS is enabled for `http://localhost:3000` (frontend).

## Switching to PostgreSQL

To use PostgreSQL in production:

1. Uncomment PostgreSQL configuration in `application.properties`
2. Comment out H2 configuration
3. Update database credentials
4. Change `spring.jpa.hibernate.ddl-auto` to `validate` or `update`