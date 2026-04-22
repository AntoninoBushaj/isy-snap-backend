# IsySnap Backend — Roadmap

> Stato attuale: MVP funzionante in locale con RBAC enterprise, rate limiting, token revocation.
> Obiettivo: portare il sistema a un livello production-ready con pagamenti reali, affidabilita, e scalabilita.

---

## Legenda Criticita

| Livello | Significato |
|---------|-------------|
| **P0 — BLOCCANTE** | Senza questo, il sistema non puo andare in produzione. Rischio di perdita dati, soldi, o vulnerabilita gravi. |
| **P1 — CRITICO** | Necessario per un lancio serio. Senza questo, il sistema funziona ma e fragile o inaffidabile. |
| **P2 — IMPORTANTE** | Migliora significativamente qualita, manutenibilita, o esperienza utente. Puo essere fatto subito dopo il lancio. |
| **P3 — MIGLIORAMENTO** | Nice-to-have. Aggiunge valore ma non blocca nulla. |

---

## P0 — BLOCCANTE

### 1. Integrazione Stripe (Pagamenti Reali)

**Stato attuale:** Il sistema di pagamento e completamente mockato. `PaymentController.generatePaymentUrl()` restituisce URL finti. `processWebhook()` non fa nulla. Non esiste integrazione con nessun payment provider.

**Obiettivo:** Permettere ai clienti di pagare realmente tramite Stripe Checkout. Il flusso deve essere: guest clicca "Paga" → viene creata una Stripe Checkout Session → il guest viene rediretto su Stripe → Stripe chiama il nostro webhook con il risultato → il pagamento viene confermato/fallito nel nostro DB.

**Cosa serve:**
- Aggiungere la dipendenza Stripe Java SDK
- Creare una Stripe Checkout Session quando il guest chiama `/createCheckout`
- Implementare il webhook handler con validazione della firma Stripe (`Stripe-Signature` header)
- Gestire gli eventi `checkout.session.completed`, `checkout.session.expired`, `payment_intent.payment_failed`
- Salvare il `paymentIntentId` / `checkoutSessionId` di Stripe come `providerReference` nel nostro Payment
- Configurare i webhook URL per ambiente (dev con Stripe CLI, prod con URL reale)
- Gestire il Stripe account ID per ristorante (Stripe Connect) se si vuole il multi-tenant con payout separati

**Rischio se non fatto:** Nessun ricavo. Il sistema e inutilizzabile commercialmente.

---

### 2. Migrazione Database con Flyway

**Stato attuale:** `spring.jpa.hibernate.ddl-auto=update`. Hibernate modifica lo schema del database automaticamente basandosi sulle entity Java. Questo significa che:
- Non c'e traccia di quali modifiche sono state fatte allo schema
- Hibernate non rimuove mai colonne o tabelle — lo schema accumula "spazzatura"
- Se due versioni dell'app partono contemporaneamente, possono corrompere lo schema
- Rollback impossibile: se un deploy introduce un campo sbagliato, non si puo tornare indietro

**Obiettivo:** Ogni modifica allo schema del database deve essere una migration SQL versionata, tracciabile, e reversibile. L'app deve partire solo se tutte le migration sono state applicate correttamente.

**Cosa serve:**
- Aggiungere dipendenza Flyway
- Generare la migration V1 dallo schema attuale (dump dello schema MySQL corrente)
- Cambiare `ddl-auto` da `update` a `validate` (Hibernate verifica che le entity combacino con lo schema, ma non lo modifica)
- Scrivere migration per ogni futura modifica allo schema
- Creare una procedura documentata per generare nuove migration

**Rischio se non fatto:** Corruzione dello schema in produzione, impossibilita di fare rollback, perdita dati.

---

### 3. Idempotenza sui Pagamenti

**Stato attuale:** Se un guest clicca "Paga" due volte rapidamente, vengono creati due pagamenti separati per gli stessi item. Non c'e nessun controllo di idempotenza. Il check "item gia pagato" nella `PaymentService` e basato sulla presenza di `PaymentItem`, ma la race condition tra due richieste concorrenti puo far passare entrambe.

**Obiettivo:** Ogni operazione di pagamento deve essere idempotente. La stessa richiesta fatta due volte deve produrre lo stesso risultato senza effetti collaterali.

**Cosa serve:**
- Aggiungere un campo `idempotencyKey` (UUID generato dal frontend) nella `CheckoutRequest`
- Prima di creare il pagamento, verificare se esiste gia un pagamento con la stessa `idempotencyKey` — se si, restituire il risultato esistente
- Aggiungere un unique index su `idempotency_key` nella tabella `payments`
- Gestire la race condition con un lock optimistico o un `INSERT ... ON DUPLICATE KEY`

**Rischio se non fatto:** Doppi addebiti ai clienti. Problemi legali e di fiducia.

---

### 4. Optimistic Locking sulle Entity Critiche

**Stato attuale:** Nessuna entity ha `@Version`. Se due camerieri aggiornano lo stesso ordine contemporaneamente, l'ultimo vince silenziosamente (last-write-wins). Stessa cosa per menu items, sessioni, e pagamenti.

**Obiettivo:** Le modifiche concorrenti a risorse critiche devono essere rilevate e gestite, non ignorate silenziosamente.

**Cosa serve:**
- Aggiungere campo `@Version private Long version` alle entity: `Order`, `Payment`, `MenuItem`, `DiningSession`
- Aggiungere una Flyway migration per i nuovi campi `version`
- Gestire `OptimisticLockException` nel `GlobalExceptionHandler` con HTTP 409 Conflict e un messaggio chiaro ("La risorsa e stata modificata da un altro utente, riprova")
- Il frontend deve gestire il 409 con un retry o un messaggio all'utente

**Rischio se non fatto:** Dati inconsistenti. Due camerieri possono sovrascrivere le modifiche l'uno dell'altro senza accorgersene.

---

### 5. Validazione Input sui DTO

**Stato attuale:** La validazione e parziale. `AuthRequest` e `RegisterRequest` hanno annotazioni `@Valid`, ma molti altri DTO non hanno nessuna validazione:
- `AddItemRequest`: nessun controllo su `quantity` (potrebbe essere 0 o negativo), `menuItemId` potrebbe essere null
- `CheckoutRequest`: nessun controllo su `provider`
- `UpdateOrderStatusRequest`: nessun controllo che lo status sia un valore valido
- `CreateRestaurantRequest`, `CreateMenuItemRequest`: campi obbligatori non marcati

**Obiettivo:** Ogni input dall'esterno deve essere validato prima di raggiungere la business logic. Errori di validazione devono restituire messaggi chiari e strutturati.

**Cosa serve:**
- Aggiungere annotazioni Bean Validation (`@NotNull`, `@NotBlank`, `@Positive`, `@Size`, `@Pattern`, `@DecimalMin`) a tutti i request DTO
- Aggiungere `@Valid` su tutti i parametri `@RequestBody` nei controller che non ce l'hanno
- Validare che gli status (order status, payment provider, etc.) siano valori ammessi tramite `@Pattern` o custom validator
- Il `GlobalExceptionHandler` gestisce gia `MethodArgumentNotValidException`, quindi le risposte di errore saranno automaticamente strutturate

**Rischio se non fatto:** Dati corrotti nel DB (quantity negativa, nomi vuoti), potenziali crash per NullPointerException nella business logic.

---

## P1 — CRITICO

### 6. Test Automatizzati

**Stato attuale:** Zero test. La directory `src/test` e vuota. Ogni modifica al codice richiede test manuali via Swagger/curl.

**Obiettivo:** Avere una suite di test che copra i percorsi critici del sistema, permettendo di fare refactoring e aggiungere feature con confidenza.

**Cosa serve:**
- **Unit test dei Service** (con mock dei repository): coprire la logica di business critica — calcolo totali ordine, verifica item gia pagato, stato sessione, creazione guest
- **Integration test dei Controller** (con `@SpringBootTest` + `@AutoConfigureMockMvc`): verificare che security, validazione, e business logic lavorino insieme
- **Test specifici per la security**: verificare che STAFF non possa accedere a endpoint ADMIN, che un token revocato venga rifiutato, che un guest senza session JWT riceva 401
- **Test del flusso QR → sessione → ordine → pagamento**: end-to-end test del percorso principale
- Configurare un profilo `test` con H2 in-memory (gia presente come dipendenza) per test veloci

**Rischio se non fatto:** Regressioni non rilevate. Ogni modifica e un rischio. Impossibile fare CI/CD affidabile.

---

### 7. Indici Database

**Stato attuale:** Solo gli indici impliciti creati da JPA sulle primary key e sulle foreign key. Nessun indice esplicito per le query piu frequenti.

**Obiettivo:** Le query piu comuni devono essere veloci anche con volumi di dati reali.

**Cosa serve:**
- `orders(dining_session_id, status)` — usato da `findOrCreatePendingOrder`, chiamato ad ogni interazione col carrello
- `order_items(order_id, guest_id)` — filtraggio item per guest
- `payment_items(order_item_id)` — check "item gia pagato", chiamato per ogni item durante il checkout
- `dining_sessions(table_id, status)` — ricerca sessione attiva per tavolo (ad ogni scan QR)
- `dining_sessions(restaurant_id, status)` — lista sessioni per ristorante (dashboard staff)
- `tokens(user_id, revoked)` — check token validi per utente
- `payments(dining_session_id)` — lista pagamenti per sessione
- Creare le migration Flyway corrispondenti

**Rischio se non fatto:** Degrado prestazioni progressivo. Con 50+ tavoli attivi e centinaia di ordini/giorno, le query diventano lente.

---

### 8. Paginazione sugli Endpoint Lista

**Stato attuale:** Tutti gli endpoint che restituiscono liste caricano TUTTI i risultati in memoria:
- `getRestaurantOrders` → tutti gli ordini di un ristorante, per sempre
- `getSessionsByRestaurant` → tutte le sessioni, compresi anni di storico
- `getAllActiveSessions` → potenzialmente migliaia

**Obiettivo:** Gli endpoint che restituiscono collezioni devono supportare paginazione per evitare OutOfMemoryError e timeout.

**Cosa serve:**
- Usare `Pageable` di Spring Data nei repository e controller
- Parametri standard: `?page=0&size=20&sort=createdAt,desc`
- Risposta wrappata in `Page<T>` che include `totalElements`, `totalPages`, `number`, `size`
- Applicare almeno a: `getRestaurantOrders`, `getSessionsByRestaurant`, `getAllActiveSessions`, `getOrderHistory`
- Impostare un `size` massimo (es. 100) per prevenire abusi

**Rischio se non fatto:** OutOfMemoryError in produzione quando i dati crescono. API lente e inutilizzabili per il frontend.

---

### 9. Hardening Error Handling

**Stato attuale:** Il `GlobalExceptionHandler` gestisce le eccezioni custom ma mancano alcuni casi importanti:
- `DataIntegrityViolationException` (constraint violation DB) non e gestita — restituisce uno stack trace generico 500
- Le `RuntimeException` generiche lanciate nei service (es. "Order item not found", "Session not found") non hanno un error code specifico — passano tutte attraverso il catch-all `Exception` handler
- Lo status `payment.getStatus()` e un plain String senza validazione — qualsiasi valore puo essere settato

**Obiettivo:** Ogni errore prevedibile deve avere una risposta JSON strutturata con un error code specifico. Nessun stack trace deve mai raggiungere il client.

**Cosa serve:**
- Gestire `DataIntegrityViolationException` → 409 Conflict con messaggio chiaro
- Sostituire le `RuntimeException` nei service con eccezioni custom tipizzate (es. `SessionNotFoundException`, `DuplicatePaymentException`)
- Gestire `OptimisticLockException` → 409 (collegato al punto 4)
- Usare enum per gli status di Order e Payment invece di plain String, per prevenire valori invalidi
- Gestire `HttpMessageNotReadableException` (JSON malformato) con messaggio utile

**Rischio se non fatto:** Stack trace esposti (rischio sicurezza), errori incomprensibili per il frontend, debug difficile.

---

### 10. Configurazione Profili Ambiente

**Stato attuale:** Un singolo `application.properties` con variabili d'ambiente e fallback hardcoded. Nessun file `.env.example`. Nessun profilo Spring separato per dev/test/prod.

**Obiettivo:** Configurazione chiara e separata per ogni ambiente. Un nuovo sviluppatore deve poter clonare il repo e partire in 5 minuti.

**Cosa serve:**
- Creare `application-dev.properties` (H2 in-memory, ddl-auto=create-drop, log DEBUG, secret di test)
- Creare `application-prod.properties` (ddl-auto=validate, log WARN, Flyway enabled, nessun fallback secret)
- Creare `application-test.properties` (H2 in-memory, configurazione per test automatizzati)
- Creare `.env.example` con tutte le variabili d'ambiente necessarie documentate
- Assicurarsi che in `prod` l'app non parta se mancano variabili critiche (JWT_SECRET, SPRING_DATASOURCE_URL, STRIPE_SECRET_KEY)

**Rischio se non fatto:** Secret di default usati accidentalmente in produzione. Configurazione fragile e prona a errori.

---

### 11. Protezione Replay QR Code

**Stato attuale:** Lo stesso QR code (stessa combinazione slug + timestamp + ephemeralKey + signature) puo essere riutilizzato infinite volte entro i 5 minuti di validita. Un utente che fa screenshot del QR puo creare guest illimitati.

**Obiettivo:** Ogni QR scan deve essere utilizzabile una sola volta. Scansioni ripetute dello stesso QR devono essere rifiutate.

**Cosa serve:**
- Aggiungere un nonce (one-time random value) al QR code, generato dal frontend ad ogni visualizzazione
- Salvare i nonce usati in un set con TTL (Redis se disponibile, oppure `ConcurrentHashMap` con scadenza come per `TokenRevocationCache`)
- Rifiutare qualsiasi richiesta con un nonce gia visto
- In alternativa, limitare il numero di guest per sessione (es. max 20) come misura di contenimento

**Rischio se non fatto:** Abuso del sistema. Un QR condiviso su social media potrebbe creare centinaia di sessioni fantasma.

---

## P2 — IMPORTANTE

### 12. Endpoint Rimborsi

**Stato attuale:** Esiste la permission `PAYMENT_REFUND` nel sistema RBAC, assegnata solo ad ADMIN. Ma non esiste nessun endpoint o logica per effettuare rimborsi. Se un pagamento va rimborsato, bisogna farlo manualmente dalla dashboard Stripe.

**Obiettivo:** Un admin deve poter emettere un rimborso totale o parziale direttamente dal backend, che a sua volta chiama l'API Stripe Refunds.

**Cosa serve:**
- Creare endpoint `POST /api/payments/refund/{paymentId}` con body `{ "amount": 5.00, "reason": "..." }`
- Chiamare `Stripe.Refund.create()` con il `paymentIntentId` salvato nel nostro Payment
- Aggiornare lo status del Payment a `REFUNDED` o `PARTIALLY_REFUNDED`
- Gestire il webhook `charge.refunded` per conferma asincrona
- Log audit di chi ha fatto il rimborso, quando, e perche

**Nota:** Dipende dal completamento del punto 1 (Integrazione Stripe).

---

### 13. Notifiche Real-Time (WebSocket)

**Stato attuale:** Il frontend deve fare polling per sapere se un ordine e stato aggiornato, se un pagamento e stato confermato, o se un nuovo ordine e arrivato. Non c'e nessun meccanismo push.

**Obiettivo:** Staff e guest ricevono aggiornamenti in tempo reale senza polling.

**Cosa serve:**
- Aggiungere dipendenza `spring-boot-starter-websocket`
- Configurare STOMP over WebSocket con SockJS fallback
- Canali da implementare:
  - `/topic/restaurant/{id}/orders` — nuovi ordini e cambi stato (per la dashboard staff/cucina)
  - `/topic/session/{id}/order-status` — aggiornamenti stato ordine (per il guest)
  - `/topic/session/{id}/payment-status` — conferma pagamento (per il guest)
- Autenticazione WebSocket: validare il JWT/session token al momento della connessione
- Pubblicare eventi quando: ordine creato, stato ordine cambiato, pagamento confermato

**Rischio se non fatto:** Esperienza utente scadente. Il cuoco non vede gli ordini in tempo reale. Il guest non sa quando il suo pagamento e confermato.

---

### 14. Audit Logging

**Stato attuale:** Il sistema ha log applicativi standard (`log.info`, `log.warn`) ma nessun audit trail strutturato. Non c'e modo di sapere chi ha fatto cosa e quando a livello di business (chi ha cancellato un menu item? chi ha chiuso una sessione?).

**Obiettivo:** Ogni azione critica deve essere tracciata in modo strutturato e persistente.

**Cosa serve:**
- Creare entity `AuditLog` con: `id`, `userId`, `action` (enum: CREATE, UPDATE, DELETE, LOGIN, LOGOUT, REFUND...), `resourceType`, `resourceId`, `details` (JSON), `ip`, `timestamp`
- Creare un `@Aspect` o usare Spring Events per catturare automaticamente le azioni sui controller annotati
- Azioni da tracciare: login/logout, creazione/modifica/cancellazione menu item, cambio stato ordine, pagamento, rimborso, modifica utente, creazione ristorante
- Endpoint per consultare l'audit log (solo ADMIN): `GET /api/audit?userId=...&action=...&from=...&to=...`

---

### 15. CI/CD Pipeline

**Stato attuale:** Nessuna pipeline. Il deploy su Railway e manuale. Non c'e nessun check automatico su PR o push.

**Obiettivo:** Ogni push deve essere automaticamente buildato e testato. Il deploy deve essere automatico su merge in main.

**Cosa serve:**
- GitHub Actions workflow:
  - `on: push/pull_request` → `./gradlew build` (compila + esegue test)
  - Check che il build passi prima di poter fare merge
  - Deploy automatico su Railway al merge in `main`
- Configurare secret di GitHub Actions per le variabili d'ambiente di produzione
- Opzionale: step di quality check (Checkstyle, SpotBugs)

---

### 16. Gestione Immagini Menu

**Stato attuale:** L'entity `MenuItem` ha un campo `image` (String) ma non c'e nessun sistema di upload. Le immagini sono probabilmente URL esterni o placeholder.

**Obiettivo:** Lo staff deve poter caricare foto dei piatti che vengono mostrate nel menu digitale.

**Cosa serve:**
- Endpoint `POST /api/menu/uploadImage/{menuItemId}` che accetta `multipart/form-data`
- Storage: AWS S3 o Cloudflare R2 (object storage economico e compatibile S3)
- Validazione: tipo file (solo JPEG/PNG/WebP), dimensione massima (es. 5MB), resize automatico
- Salvare l'URL del file caricato nel campo `image` del `MenuItem`
- Servire le immagini via CDN (non direttamente dal backend)

---

### 17. Logging Strutturato (JSON)

**Stato attuale:** Log in plain text. In produzione, aggregare e cercare nei log richiede parsing testuale.

**Obiettivo:** Log in formato JSON per facile integrazione con sistemi di log aggregation (ELK, Datadog, CloudWatch).

**Cosa serve:**
- Aggiungere dipendenza `logstash-logback-encoder`
- Configurare `logback-spring.xml` con encoder JSON per il profilo `prod`
- Mantenere plain text per il profilo `dev` (leggibilita locale)
- Includere campi strutturati: `requestId`, `userId`, `restaurantId`, `sessionId`, `duration`
- Aggiungere un MDC filter che popola `requestId` e `userId` per ogni richiesta

---

### 18. Health Check e Monitoring

**Stato attuale:** Nessun endpoint di health check. Railway (o qualsiasi load balancer) non ha modo di sapere se l'app e sana.

**Obiettivo:** Endpoint standard per monitorare lo stato dell'applicazione.

**Cosa serve:**
- Aggiungere dipendenza `spring-boot-starter-actuator`
- Esporre `/actuator/health` (pubblico, per il load balancer)
- Configurare health indicators: DB connection, disk space
- Esporre `/actuator/metrics` e `/actuator/prometheus` (protetto, per monitoring)
- Configurare Railway per usare `/actuator/health` come health check

---

## P3 — MIGLIORAMENTO

### 19. GDPR / Privacy

**Obiettivo:** Conformita base al GDPR per operare in Europa.

**Cosa serve:**
- Endpoint per esportazione dati utente (diritto di accesso): `GET /api/users/me/export`
- Endpoint per cancellazione account (diritto all'oblio): `DELETE /api/users/me` con anonimizzazione dei dati collegati (ordini, pagamenti) invece di cancellazione hard
- Retention policy: cancellazione automatica dei dati delle sessioni dopo N giorni (schedulatore)
- Cookie/consent banner per il frontend (non backend, ma da coordinare)

---

### 20. API Versioning

**Obiettivo:** Permettere di evolvere l'API senza rompere i client esistenti.

**Cosa serve:**
- Strategia: URI versioning (`/api/v1/...`) o header versioning (`Accept: application/vnd.isysnap.v1+json`)
- La scelta piu semplice e URI versioning: rinominare tutti gli endpoint attuali sotto `/api/v1/`
- Documentare la politica di deprecation (quanto tempo prima di rimuovere una versione vecchia)

---

### 21. Graceful Shutdown

**Obiettivo:** Quando l'app viene fermata (deploy, restart), le richieste in corso devono completarsi prima che il processo termini.

**Cosa serve:**
- Abilitare in `application.properties`: `server.shutdown=graceful`
- Configurare timeout: `spring.lifecycle.timeout-per-shutdown-phase=30s`
- Assicurarsi che le transazioni in corso (specialmente pagamenti) non vengano interrotte

---

### 22. Internazionalizzazione (i18n)

**Obiettivo:** Supportare menu e messaggi in piu lingue (almeno italiano e inglese).

**Cosa serve:**
- Aggiungere campo `locale` o tabella di traduzione per `MenuCategory.name`, `MenuItem.name`, `MenuItem.description`
- Header `Accept-Language` per selezionare la lingua nella risposta
- Messaggi di errore localizzati (file `messages_it.properties`, `messages_en.properties`)

---

### 23. Email / Notifiche

**Obiettivo:** Inviare email per eventi importanti (registrazione utente, reset password, ricevuta pagamento).

**Cosa serve:**
- Aggiungere `spring-boot-starter-mail`
- Configurare SMTP provider (es. SendGrid, Mailgun, o SES)
- Template email per: welcome, reset password, ricevuta pagamento
- Endpoint `POST /api/auth/forgotPassword` e `POST /api/auth/resetPassword`
- Coda asincrona per l'invio (non bloccare la risposta HTTP in attesa dell'invio mail)

---

### 24. Dashboard Analytics per Restaurant Owner

**Obiettivo:** Il proprietario del ristorante deve poter vedere statistiche base: ricavi giornalieri, piatti piu ordinati, tempi medi di servizio.

**Cosa serve:**
- Endpoint `GET /api/analytics/{restaurantId}/summary?from=...&to=...`
- Metriche: totale ordini, totale ricavi, piatti top 10, tempo medio tra ordine e "READY", numero sessioni
- Query aggregate ottimizzate (non caricare tutti gli ordini in memoria)
- Protezione: solo ADMIN o STAFF del ristorante specifico

---

### 25. Backup Strategy

**Obiettivo:** Garantire che i dati non vengano persi in caso di guasto.

**Cosa serve:**
- Configurare backup automatici del database MySQL (daily)
- Retention: almeno 30 giorni
- Testare periodicamente il ripristino da backup
- Se su Railway: verificare che il provider DB offra backup automatici, altrimenti configurare `mysqldump` schedulato su storage esterno (S3)

---

## Ordine di Implementazione Consigliato

```
Fase 1 (Fondamenta):
  [P0] 2. Flyway migrations
  [P0] 5. Validazione input DTO
  [P0] 4. Optimistic locking
  [P1] 10. Profili ambiente + .env.example
  [P1] 9. Hardening error handling

Fase 2 (Pagamenti):
  [P0] 1. Integrazione Stripe
  [P0] 3. Idempotenza pagamenti
  [P2] 12. Endpoint rimborsi

Fase 3 (Qualita):
  [P1] 6. Test automatizzati
  [P1] 7. Indici database
  [P1] 8. Paginazione
  [P2] 15. CI/CD pipeline

Fase 4 (Produzione):
  [P1] 11. Protezione replay QR
  [P2] 18. Health check + monitoring
  [P2] 17. Logging strutturato
  [P2] 14. Audit logging
  [P3] 21. Graceful shutdown

Fase 5 (Feature):
  [P2] 13. WebSocket real-time
  [P2] 16. Upload immagini menu
  [P3] 23. Email / notifiche
  [P3] 24. Dashboard analytics

Fase 6 (Polish):
  [P3] 19. GDPR
  [P3] 20. API versioning
  [P3] 22. i18n
  [P3] 25. Backup strategy
```

---

## Riepilogo

| Criticita | Conteggio | Descrizione |
|-----------|-----------|-------------|
| P0 — BLOCCANTE | 5 | Stripe, Flyway, idempotenza, optimistic locking, validazione input |
| P1 — CRITICO | 6 | Test, indici DB, paginazione, error handling, profili ambiente, QR replay |
| P2 — IMPORTANTE | 7 | Rimborsi, WebSocket, audit, CI/CD, immagini, logging JSON, health check |
| P3 — MIGLIORAMENTO | 7 | GDPR, versioning, graceful shutdown, i18n, email, analytics, backup |
| **Totale** | **25** | |
