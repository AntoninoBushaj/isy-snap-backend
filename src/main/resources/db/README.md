# Database Reset & Populate Scripts

## Opzione 1: Script SQL Completo (Veloce)

### Vantaggi
- ✅ Tutto in un colpo solo
- ✅ Non serve riavviare l'applicazione
- ✅ Controllo completo sui dati

### Come Usare

```bash
# 1. Ferma l'applicazione se è in esecuzione
# (Ctrl+C nel terminale dove gira)

# 2. Esegui lo script SQL
mysql -u root -p tablesnap < src/main/resources/db/reset_and_populate.sql

# 3. Riavvia l'applicazione
./gradlew bootRun
```

### Credenziali Utenti Creati

| Email | Password | Nome | Ruolo |
|-------|----------|------|-------|
| `admin@tablesnap.com` | `admin123` | Admin System | ADMIN |
| `staff@labellavita.com` | `staff123` | Mario Rossi | STAFF |
| `waiter@labellavita.com` | `waiter123` | Giovanni Bianchi | STAFF |

**Note**:
- ADMIN vede tutti gli ordini di tutti i ristoranti
- STAFF vede solo ordini del suo ristorante ("La Bella Vita")
- NON creiamo utenti CUSTOMER perché i clienti ordinano via QR code senza registrarsi

### Dati Creati

- **1 Ristorante**: La Bella Vita
- **12 Menu Items**: Pizza, Pasta, Dessert, Bevande
- **2 Utenti STAFF**: Collegati a "La Bella Vita"
- **1 Utente ADMIN**: Ha accesso globale

---

## Opzione 2: DataInitializer (Automatico)

### Vantaggi
- ✅ Più semplice
- ✅ Usa il codice Java esistente
- ✅ Consistente con il codice

### Come Usare

```bash
# 1. Ferma l'applicazione (Ctrl+C)

# 2. Pulisci il database
mysql -u root -p tablesnap << 'EOF'
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS refresh_tokens;
DROP TABLE IF EXISTS restaurant_users;
DROP TABLE IF EXISTS menu_items;
DROP TABLE IF EXISTS restaurants;
DROP TABLE IF EXISTS users;
SET FOREIGN_KEY_CHECKS = 1;
EOF

# 3. Riabilita DataInitializer
# Modifica: src/main/java/com/tablesnap/config/DataInitializer.java
# Rimuovi il commento da: @Component

# 4. Riavvia l'applicazione
./gradlew bootRun
```

**NOTA**: Con DataInitializer non vengono creati utenti, solo ristorante e menu. Dovrai registrare gli utenti via API.

---

## Quick Start per Testing

### 1. Login come STAFF

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "staff@labellavita.com",
    "password": "staff123"
  }'
```

Copia il `token` dalla response.

### 2. Visualizza Ordini (solo del tuo ristorante)

```bash
curl http://localhost:8080/api/orders \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### 3. Login come ADMIN

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@tablesnap.com",
    "password": "admin123"
  }'
```

### 4. Visualizza Tutti gli Ordini (ADMIN vede tutto)

```bash
curl http://localhost:8080/api/orders \
  -H "Authorization: Bearer ADMIN_TOKEN_HERE"
```

### 5. Test QR Code Flow (senza autenticazione)

```bash
# Visualizza menu ristorante
curl http://localhost:8080/api/restaurants/restaurant-001

# Crea ordine anonimo (come da QR code)
curl -X POST http://localhost:8080/api/orders/createOrder \
  -H "Content-Type: application/json" \
  -d '{
    "restaurantId": "restaurant-001",
    "tableNumber": "12",
    "items": [
      {
        "menuItemId": "item-001",
        "quantity": 2
      },
      {
        "menuItemId": "item-010",
        "quantity": 1
      }
    ]
  }'
```

Riceverai un `sessionId` che il cliente può usare per tracciare l'ordine:

```bash
curl http://localhost:8080/api/orders/my/YOUR_SESSION_ID
```

---

## Verifica Database

```sql
-- Controlla ristoranti
SELECT * FROM restaurants;

-- Controlla menu items
SELECT id, name, category, price FROM menu_items;

-- Controlla utenti
SELECT id, email, role FROM users;

-- Controlla associazioni staff-ristorante
SELECT ru.*, u.email, r.name
FROM restaurant_users ru
JOIN users u ON ru.user_id = u.id
JOIN restaurants r ON ru.restaurant_id = r.id
WHERE ru.removed_at IS NULL;

-- Controlla ordini
SELECT id, restaurant_id, table_number, session_id, status, total_amount, created_at
FROM orders
ORDER BY created_at DESC;
```

---

## Troubleshooting

### Problema: "Access denied for user 'root'@'localhost'"

**Soluzione**: Verifica le credenziali MySQL in `application.properties`:
```properties
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD
```

### Problema: "Unknown database 'tablesnap'"

**Soluzione**: Crea il database:
```bash
mysql -u root -p
CREATE DATABASE tablesnap CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Problema: Date sbagliate nel database

**Verifica**: Le date devono essere in UTC
```sql
SELECT id, created_at FROM orders;
-- Output dovrebbe mostrare ora UTC (non locale)
```

Se vedi ore locali (es: CET), verifica `application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/tablesnap?serverTimezone=UTC
spring.jpa.properties.hibernate.jdbc.time_zone=UTC
```

---

## Password Hash Generator

Se vuoi aggiungere altri utenti con password custom:

```java
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "mypassword";
        String hash = encoder.encode(password);
        System.out.println(hash);
    }
}
```

Oppure online: https://bcrypt-generator.com/ (usa round 10)

---

## Note Importanti

1. **UTC Timezone**: Tutte le date sono salvate in UTC, JavaScript le convertirà automaticamente nel timezone locale
2. **BCrypt Passwords**: Le password sono hashate con BCrypt (round 10)
3. **Foreign Keys**: Lo script gestisce correttamente le foreign keys durante il DROP
4. **Indexes**: Indici ottimizzati per query comuni (restaurant_id, status, created_at)
5. **Session ID**: Generato automaticamente per ordini anonimi (QR code flow)
