# Inventory Manager Microservice - Architecture Documentation

## Overview

The Inventory Manager is a production-grade microservice built using Clean Architecture principles. It manages warehouse inventory and supplier stock pushes with support for distributed transactions, optimistic locking, and audit trails.

## Architecture Layers

```
┌─────────────────────────────────────────────────────────────┐
│                 PRESENTATION LAYER                          │
│  (Controllers, DTOs, Exception Handling)                    │
│  com.learning.inventory_manager.presentation.rest.*         │
└────────────────────────┬────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────┐
│              APPLICATION LAYER                              │
│  (Processor, UseCase)                                       │
│  com.learning.inventory_manager.application.*               │
└────────────────────────┬────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────┐
│                 DOMAIN LAYER                                │
│  (Business Logic, Services, Gateways, Models)              │
│  com.learning.inventory_manager.domain.*                    │
│  com.learning.inventory_manager.application.service.*       │
└────────────────────────┬────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────┐
│            INFRASTRUCTURE LAYER                             │
│  (Repositories, Entities, Gateway Implementations)          │
│  com.learning.inventory_manager.infrastructure.*            │
└─────────────────────────────────────────────────────────────┘
```

## Layer Responsibilities

### 1. Presentation Layer (REST)
**Location:** `com.learning.inventory_manager.presentation.rest`

**Components:**
- `InventoryController` - Exposes REST endpoints
- DTOs (Request/Response) - Data transfer objects for API contracts
- `GlobalExceptionHandler` - Centralized exception handling

**Responsibilities:**
- Expose REST endpoints (e.g., `POST /api/v1/inventory/bulk-upsert`)
- Validate request format using Spring validation annotations
- Parse HTTP requests and responses
- Return appropriate HTTP status codes
- Convert exceptions to HTTP responses

**Flow:**
```
HTTP Request → InventoryController.bulkUpsert()
            ↓
         Processor.process()
            ↓
         Service
            ↓
         HTTP Response
```

### 2. Application Layer (Processor & UseCase)
**Location:** `com.learning.inventory_manager.application`

**Components:**
- `BulkInventoryUpsertProcessor` - Orchestrates request flow
- `BulkInventoryUpsertUsecase` - Business workflow orchestration

**Responsibilities:**
- Coordinate flow between layers
- Convert DTOs between layers if needed
- Call appropriate services and gateways
- Handle transaction boundaries using `@Transactional`
- Build response objects
- Support partial success (failures don't block other items)

**Key Feature - Partial Success:**
```
Request with 3 items:
┌─────────────────────────────────────────┐
│ Item 1: VALID WAREHOUSE → SUCCESS       │
│ Item 2: INVALID WAREHOUSE → FAILED      │
│ Item 3: VALID WAREHOUSE → SUCCESS       │
└─────────────────────────────────────────┘
Response: 2 successes, 1 failure
```

### 3. Domain Layer (Business Logic)
**Location:** `com.learning.inventory_manager.domain`

**Components:**
- `InventoryService` - Core business logic
- Domain Models (`Inventory`, `InventoryLedgerEntry`)
- Gateway Interfaces (`WarehouseGateway`, `InventoryGateway`, `InventoryLedgerGateway`)
- Exceptions (Domain-specific)

**Responsibilities:**
- Core inventory business rules
- Quantity calculations and validations
- Delta calculations for ledger entries
- State transitions and validations
- No transaction boundaries (handled at Application layer)
- Pure business logic - no Spring/HTTP/DB awareness

**Business Rules:**
1. Quantity cannot be negative
2. Reserved quantity cannot exceed on-hand quantity
3. Available quantity = on-hand - reserved (must be >= 0)
4. Create/update decisions based on product-warehouse existence

### 4. Infrastructure Layer (Persistence)
**Location:** `com.learning.inventory_manager.infrastructure`

**Components:**
- JPA Entities (`WarehouseEntity`, `InventoryEntity`, etc.)
- Spring Data Repositories
- Gateway Implementations
- Mappers (Entity ↔ Domain Model)

**Responsibilities:**
- JPA entity mapping and persistence
- Database queries and operations
- Convert domain exceptions to application exceptions
- Provide repository abstraction to domain
- Handle optimistic locking exceptions

## Data Flow Example: Bulk Inventory Upsert

```
1. CLIENT REQUEST
   POST /api/v1/inventory/bulk-upsert
   {
     "supplierId": "uuid",
     "items": [{"supplierProductId": "uuid", "warehouseId": "uuid", "quantityOnHand": 100}]
   }

2. CONTROLLER LAYER
   InventoryController.bulkUpsert(request)
   - Validates request format
   - Delegates to processor

3. PROCESSOR LAYER
   BulkInventoryUpsertProcessor.process(request)
   - Calls usecase
   - Builds response

4. USECASE LAYER (Transaction starts)
   BulkInventoryUpsertUsecase.execute()
   For each item:
   a) Validate warehouse exists → Gateway call
   b) Check inventory exists → Gateway call
   c) Create or update → Service call
   d) Record result

5. SERVICE LAYER
   InventoryService.createInventory() or updateInventory()
   - Validates business rules
   - Calculates delta
   - Records ledger entry
   - Calls gateway

6. GATEWAY LAYER (Infrastructure)
   InventoryGatewayImpl
   - Converts domain model to entity
   - Calls repository
   - Handles optimistic locking

7. REPOSITORY LAYER
   InventoryRepository (Spring Data JPA)
   - Persists to database
   - Returns entity

8. LEDGER GATEWAY
   Records change in inventory_ledger table

9. RESPONSE BACK UP THE LAYERS
   UseCase → Processor → Controller → HttpResponse
```

## Key Architectural Decisions

### 1. Optimistic Locking
- **Problem:** Concurrent inventory updates can cause race conditions
- **Solution:** Use `@Version` column with JPA optimistic locking
- **Benefit:** Detects conflicts without database locks
- **Exception:** `OptimisticLockingException` thrown on version mismatch

```java
@Version
@Column(name = "version", nullable = false)
private Long version;
```

### 2. Partial Success Pattern
- **Problem:** One invalid item shouldn't fail entire request
- **Solution:** Try-catch around each item, capture individual results
- **Benefit:** Resilient API, clear error reporting

```java
for (InventoryItemRequest item : items) {
    try {
        result = processItem(item);
    } catch (Exception e) {
        result = InventoryUpsertItemResult.failure(...);
    }
}
```

### 3. Gateway Pattern
- **Problem:** Domain logic tightly coupled to persistence
- **Solution:** Gateway interfaces hide repository details
- **Benefit:** Domain layer is pure business logic, easily testable

```java
public interface InventoryGateway {
    Optional<Inventory> findBySupplierProductIdAndWarehouseId(...);
    Inventory save(Inventory inventory);
}
```

### 4. Immutable Domain Models
- **Problem:** Mutable state can lead to bugs
- **Solution:** Domain models use minimal mutators
- **Benefit:** Easier reasoning about behavior

```java
public final class Inventory {
    // Final fields with private constructor
    // Only necessary setters for business operations
}
```

### 5. Entity ↔ Domain Model Separation
- **Problem:** JPA entities mixed with business logic
- **Solution:** Separate entities (persistence) from models (domain)
- **Benefit:** Business logic not tied to ORM details

## Transaction Management

### Transaction Boundaries
- **Location:** `@Transactional` at UseCase level
- **Reason:** All inventory operations must atomic (warehouse validation → create/update → ledger)
- **Propagation:** `REQUIRED` (default)

```java
@Transactional
public List<InventoryUpsertItemResult> execute(...) {
    // All operations within this method are one transaction
    // Rolls back entirely if any unhandled exception occurs
}
```

### Ledger Transactions
- Each inventory change must have corresponding ledger entry
- Both must commit together or both rollback
- Handled by same `@Transactional` boundary

## Error Handling Strategy

### Exception Hierarchy
```
Throwable
└─ Exception
   └─ InventoryException (domain)
      ├─ WarehouseNotFoundException
      ├─ OptimisticLockingException
      └─ InventoryValidationException
```

### HTTP Response Mapping
```
WarehouseNotFoundException → 404 NOT_FOUND
OptimisticLockingException → 409 CONFLICT
InventoryValidationException → 400 BAD_REQUEST
MethodArgumentNotValidException → 400 BAD_REQUEST
```

## Inventory Ledger (Audit Trail)

**Purpose:** Complete audit trail of all inventory changes

**Change Types:**
- `SUPPLIER_PUSH` - Stock received from supplier
- `ORDER_RESERVATION` - Stock reserved for order
- `ORDER_RELEASE` - Stock released back
- `ADJUSTMENT` - Manual adjustment

**Recorded on Every Change:**
```sql
INSERT INTO inventory_ledger
  (inventory_id, change_type, quantity_delta, quantity_after, reference_id, note, created_at)
VALUES
  (?, 'SUPPLIER_PUSH', 50, 150, 'supplier-uuid', 'Stock push from supplier', NOW())
```

## Deployment Considerations

### Database Migrations
- Uses **Flyway** for version control
- Migrations in `src/main/resources/db/migration/`
- Auto-runs on application startup

### Environment Configuration
- `application.yaml` - Production config (modify for your environment)
- `application-test.yaml` - Test config (H2 in-memory)

### Logging
- Structured logging with SLF4J
- DEBUG logs in common areas
- INFO logs for business operations
- ERROR logs for exceptions
- Configurable per profile

## Testing Strategy

### Unit Tests
- Service layer: Business logic validation
- UseCase layer: Workflow validation
- Mock all dependencies
- No database access

**Files:**
- `InventoryServiceTest.java`
- `BulkInventoryUpsertUsecaseTest.java`

### Integration Tests
- Full stack testing with H2 database
- Tests all layers together
- HTTP client testing with MockMvc
- Uses `application-test.yaml`

**Files:**
- `InventoryBulkUpsertIntegrationTest.java`

## API Specification

### Endpoint: POST /api/v1/inventory/bulk-upsert

**Request:**
```json
{
  "supplierId": "11111111-1111-1111-1111-111111111111",
  "items": [
    {
      "supplierProductId": "22222222-2222-2222-2222-222222222222",
      "warehouseId": "33333333-3333-3333-3333-333333333333",
      "quantityOnHand": 100
    }
  ]
}
```

**Success Response (200 OK):**
```json
{
  "totalProcessed": 1,
  "successCount": 1,
  "failureCount": 0,
  "results": [
    {
      "supplierProductId": "22222222-2222-2222-2222-222222222222",
      "warehouseId": "33333333-3333-3333-3333-333333333333",
      "status": "SUCCESS",
      "operation": "CREATE",
      "errorMessage": null,
      "quantityDelta": 100
    }
  ]
}
```

**Error Response (400 Bad Request):**
```json
{
  "errorCode": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "status": 400,
  "timestamp": "2026-05-13T10:30:00",
  "details": {
    "supplierId": "Supplier ID is required"
  }
}
```

## Future Extensions

### Planned Features
1. **Message Queue Integration** (RabbitMQ)
   - Async inventory updates
   - Event-based architecture

2. **Reservation Management**
   - Reserve inventory for orders
   - Automatic reservation expiry

3. **Warehouse Transfer**
   - Move inventory between warehouses
   - Automatic ledger recording

4. **Reporting & Analytics**
   - Inventory trends
   - Supplier performance
   - Warehouse utilization

### Extension Points
- New UseCase → New layer components
- New Entity → Follow existing entity patterns
- New Gateway → Implement interface, add to Service
- New Exception → Extend InventoryException, add handler

## Performance Considerations

### Optimizations Applied
1. **Optimistic Locking** - No row locks, better concurrency
2. **Partial Success** - Early exits processing don't block others
3. **Database Indexes** - Unique constraint on (supplier_product_id, warehouse_id)
4. **Async Ledger** - Could be made asynchronous in future

### Scalability Notes
- Stateless controllers allow horizontal scaling
- Database becomes bottleneck (typical)
- Consider read replicas for reporting queries
- Message queue recommended for high volume

## Monitoring & Observability

### Logging Coverage
- Request entry/exit
- Business operation success/failure
- Exception details and stack traces
- Ledger recording

### Metrics (Future)
- Request rate and latency
- Success/failure rates
- Database connection pool
- Ledger entry count

### Health Check (Future)
- Database connectivity
- Application readiness
- Dependency availability

