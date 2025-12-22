# TableSnap Backend - Setup Docker

## Prerequisiti
- Docker Desktop installato e in esecuzione
- Docker Compose

## Struttura Setup
- **MySQL 8.0.17**: Database principale su porta `3306`
- **Spring Boot Backend**: API REST su porta `8080`
- **Swagger UI**: Interfaccia documentazione API su `http://localhost:8080/swagger-ui.html`

## Come avviare il progetto

**IMPORTANTE**: Tutti i comandi vanno eseguiti dalla cartella `backend/docker/`

### 1. Avviare tutto (MySQL + Backend)
```bash
cd docker
docker-compose up --build
```

### 2. Avviare in background (detached mode)
```bash
cd docker
docker-compose up -d --build
```

### 3. Avviare solo MySQL (per sviluppo locale)
```bash
cd docker
docker-compose up -d mysql-tablesnap
```

Poi avvia il backend da IntelliJ:
- **Opzione A**: Usa il profilo `docker` (se hai già MySQL in Docker)
  - Run/Debug Configurations → Environment variables → `SPRING_PROFILES_ACTIVE=docker`

- **Opzione B**: Usa H2 in-memory (profilo default, non serve MySQL)
  - Avvia normalmente senza variabili d'ambiente

### 4. Verificare i log
```bash
# Tutti i servizi
docker-compose logs -f

# Solo backend
docker-compose logs -f backend

# Solo MySQL
docker-compose logs -f mysql-tablesnap
```

### 5. Fermare i container
```bash
docker-compose down
```

### 6. Fermare e rimuovere volumi (ATTENZIONE: cancella i dati del database)
```bash
docker-compose down -v
```

## Accesso ai servizi

### Backend API
- **URL**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

### Database MySQL
- **Host**: localhost
- **Port**: 3306
- **Database**: tablesnap
- **Username**: tablesnap_user
- **Password**: tablesnap_password
- **Root Password**: root

### Connessione MySQL da client locale
```bash
mysql -h localhost -P 3306 -u tablesnap_user -p
# Password: tablesnap_password
```

## Operazioni CRUD disponibili

Con Swagger UI puoi:
- ✅ **GET**: Recuperare dati dal database
- ✅ **POST**: Aggiungere nuovi record
- ✅ **PUT**: Modificare record esistenti
- ✅ **DELETE**: Eliminare record

Tutti i dati vengono persistiti in MySQL e salvati nel volume Docker `mysql_data`.

## Ricompilare solo il backend
```bash
docker-compose up --build backend
```

## Troubleshooting

### Backend non si connette a MySQL
Verifica che MySQL sia healthy:
```bash
docker-compose ps
```

### Ricostruire da zero
```bash
docker-compose down -v
docker-compose build --no-cache
docker-compose up
```

### Vedere le tabelle create
```bash
docker exec -it tablesnap_mysql mysql -u tablesnap_user -p tablesnap
# Password: tablesnap_password

# Poi esegui:
SHOW TABLES;
DESCRIBE nome_tabella;
```