# Bulk Inventory Upsert - Implementation Guide

## Overview
This guide explains the complete end-to-end flow of the bulk inventory upsert API.

## API Request/Response Flow

### 1. Client Initiates Request
```
POST /api/v1/inventory/bulk-upsert
Content-Type: application/json

{
  "supplierId": "11111111-1111-1111-1111-111111111111",
  "items": [
    {
      "supplierProductId": "22222222-2222-2222-2222-222222222222",
      "warehouseId": "33333333-3333-3333-3333-333333333333",
      "quantityOnHand": 100
    },
    {
      "supplierProductId": "44444444-4444-4444-4444-444444444444",
      "warehouseId": "55555555-5555-5555-5555-555555555555",
      "quantityOnHand": 200
    }
  ]
}
```

### 2. Controller Layer Processing

**Class:** `InventoryController`
**Method:** `bulkUpsert(BulkInventoryUpsertRequest)`

**Steps:**
1. Receives HTTP request
2. Spring validation framework validates:
   - `supplierId` not null
   - `items` not empty
   - Each item has valid UUIDs and non-negative quantity
3. If validation fails → HTTP 400 with error details
4. If valid → passes to processor

**Code:**
```java
@PostMapping("/bulk-upsert")
public ResponseEntity<BulkInventoryUpsertResponse> bulkUpsert(
    @Valid @RequestBody BulkInventoryUpsertRequest request
) {
    var response = processor.process(request);
    return ResponseEntity.ok(response);
}
```

### 3. Processor Layer Processing

**Class:** `BulkInventoryUpsertProcessor`
**Method:** `process(BulkInventoryUpsertRequest)`

**Steps:**
1. Receives request from controller
2. Logs request details (supplier ID, item count)
3. Calls usecase with supplier ID and items
4. Receives result list with individual item results
5. Builds response object with aggregate metrics:
   - Total items processed
   - Count of successes
   - Count of failures
   - Individual results
6. Returns response to controller

**Code:**
```java
public BulkInventoryUpsertResponse process(BulkInventoryUpsertRequest request) {
    // Call usecase
    List<InventoryUpsertItemResult> results = 
        upsertUseCase.execute(request.supplierId(), request.items());
    
    // Build response with counts
    return buildResponse(results);
}
```

### 4. UseCase Layer - Business Orchestration

**Class:** `BulkInventoryUpsertUsecase`
**Method:** `execute(UUID supplierId, List<InventoryItemRequest> items)`

**Transaction:** `@Transactional` (REQUIRED propagation)

**Steps:**
1. Log operation start
2. For each inventory item:
   ```
   try {
       a) Call warehouse gateway to validate warehouse exists & active
       b) Call inventory gateway to check if inventory exists
       c) If exists:
          - Call service to update inventory
          - Capture update result
       d) If not exists:
          - Call service to create inventory
          - Capture create result
   } catch (Exception e) {
       - Log error
       - Create failure result with error message
       - Continue to next item (partial success)
   }
   ```
3. Collect all results (successes and failures)
4. Log operation completion with success/failure counts
5. Return results list

**Partial Success Example:**
```
Item 1: Valid warehouse, product doesn't exist → CREATE SUCCESS
Item 2: Invalid warehouse → WAREHOUSE_NOT_FOUND FAILURE
Item 3: Valid warehouse, product exists → UPDATE SUCCESS

Response: 2 successes, 1 failure (no exceptions thrown)
```

### 5. Service Layer - Business Logic

**Class:** `InventoryService`

#### For CREATE Operation:
**Method:** `createInventory(UUID productId, UUID warehouseId, Integer quantityOnHand, UUID supplierId)`

**Steps:**
1. Validate quantity >= 0
2. Create immutable Inventory domain model:
   ```
   ID: UUID.randomUUID()
   Quantity On Hand: 100
   Quantity Reserved: 0
   Version: 0
   Created At: NOW
   Updated At: NOW
   ```
3. Call inventory gateway to persist
4. Create ledger entry:
   ```
   Change Type: SUPPLIER_PUSH
   Quantity Delta: 100 (new quantity)
   Quantity After: 100
   Reference ID: supplier-uuid
   ```
5. Call ledger gateway to record
6. Return created inventory domain model

#### For UPDATE Operation:
**Method:** `updateInventory(Inventory inventory, Integer newQuantityOnHand, UUID supplierId)`

**Steps:**
1. Validate new quantity >= 0
2. Validate: reserved quantity ≤ new quantity
   - If failure: throw `InventoryValidationException`
3. Calculate delta: delta = newQuantity - oldQuantity
4. Update inventory domain model:
   ```
   Old Quantity: 100
   New Quantity: 150
   Delta: +50
   Version auto-incremented by JPA
   ```
5. Call inventory gateway to persist
6. Create ledger entry:
   ```
   Change Type: SUPPLIER_PUSH
   Quantity Delta: +50
   Quantity After: 150
   Reference ID: supplier-uuid
   ```
7. Call ledger gateway to record
8. Return updated inventory domain model

### 6. Gateway Layer - Persistence Abstraction

**Three Gateway Implementations:**

#### WarehouseGateway
**Method:** `validateWarehouseExists(UUID warehouseId)`

**Implementation:** `WarehouseGatewayImpl`
```java
public void validateWarehouseExists(UUID warehouseId) {
    if (!repository.existsByIdAndIsActiveTrue(warehouseId)) {
        throw new WarehouseNotFoundException(warehouseId);
    }
}
```

#### InventoryGateway
**Methods:**
- `findBySupplierProductIdAndWarehouseId()` → Optional<Inventory>
- `create()` → Inventory
- `save()` → Inventory

**Implementation:** `InventoryGatewayImpl`
```java
// Convert entity to domain model
public Optional<Inventory> findBySupplierProductIdAndWarehouseId(...) {
    return repository.findBySupplierProductIdAndWarehouseId(...)
        .map(mapper::toDomain);  // Entity → Domain Model
}

// Save with optimistic locking handling
public Inventory save(Inventory inventory) {
    try {
        var entity = mapper.toEntity(inventory);  // Domain → Entity
        var saved = repository.save(entity);
        return mapper.toDomain(saved);  // Entity → Domain
    } catch (ObjectOptimisticLockingFailureException e) {
        throw new OptimisticLockingException("...", e);
    }
}
```

#### InventoryLedgerGateway
**Method:** `recordChange(InventoryLedgerEntry entry)`

**Implementation:** `InventoryLedgerGatewayImpl`
```java
public void recordChange(InventoryLedgerEntry entry) {
    var entity = InventoryLedgerEntity.builder()
        .inventoryId(entry.getInventoryId())
        .changeType(entry.getChangeType())
        .quantityDelta(entry.getQuantityDelta())
        .quantityAfter(entry.getQuantityAfter())
        .referenceId(entry.getReferenceId())
        .note(entry.getNote())
        .createdAt(LocalDateTime.now())
        .build();
    repository.save(entity);
}
```

### 7. Repository Layer - Database Access

**Spring Data JPA Repositories:**

#### InventoryRepository
```java
Optional<InventoryEntity> findBySupplierProductIdAndWarehouseId(
    UUID supplierProductId, 
    UUID warehouseId
);
```

#### WarehouseRepository
```java
boolean existsByIdAndIsActiveTrue(UUID id);

Optional<WarehouseEntity> findByIdAndIsActiveTrue(UUID id);
```

#### InventoryLedgerRepository
```java
void save(InventoryLedgerEntity entity);

List<InventoryLedgerEntity> findByInventoryIdOrderByCreatedAtAsc(UUID inventoryId);
```

### 8. Database Operations

#### CREATE inventory:
```sql
-- Generate unique UUID
INSERT INTO inventory (id, supplier_product_id, warehouse_id, quantity_on_hand, 
                       quantity_reserved, version, created_at, updated_at)
VALUES (?, ?, ?, 100, 0, 0, NOW(), NOW());

-- Record in ledger
INSERT INTO inventory_ledger (inventory_id, change_type, quantity_delta, 
                              quantity_after, reference_id, note, created_at)
VALUES (?, 'SUPPLIER_PUSH', 100, 100, ?, 'Inventory push from supplier', NOW());
```

#### UPDATE inventory (with optimistic locking):
```sql
-- JPA/Hibernate generates this with version check
UPDATE inventory 
SET quantity_on_hand = 150, version = version + 1, updated_at = NOW()
WHERE id = ? AND version = ?;

-- If version mismatch: 0 rows updated → OptimisticLockingFailureException
-- If successful: Record in ledger
INSERT INTO inventory_ledger (inventory_id, change_type, quantity_delta, 
                              quantity_after, reference_id, note, created_at)
VALUES (?, 'SUPPLIER_PUSH', +50, 150, ?, 'Inventory push from supplier', NOW());
```

### 9. Response Formation

**Successful response:**
```json
HTTP/1.1 200 OK

{
  "totalProcessed": 2,
  "successCount": 1,
  "failureCount": 1,
  "results": [
    {
      "supplierProductId": "22222222-2222-2222-2222-222222222222",
      "warehouseId": "33333333-3333-3333-3333-333333333333",
      "status": "SUCCESS",
      "operation": "CREATE",
      "errorMessage": null,
      "quantityDelta": 100
    },
    {
      "supplierProductId": "44444444-4444-4444-4444-444444444444",
      "warehouseId": "55555555-5555-5555-5555-555555555555",
      "status": "FAILED",
      "operation": null,
      "errorMessage": "Warehouse not found or is inactive: 55555555-5555-5555-5555-555555555555",
      "quantityDelta": null
    }
  ]
}
```

## Transaction Boundaries & Rollback Scenarios

### SUCCESS SCENARIO
```
[@Transactional] useCase.execute()
  ├─ Item 1:
  │  ├─ Warehouse validation ✓
  │  ├─ Inventory check → not found
  │  ├─ Service.createInventory() ✓
  │  │  ├─ Gateway.create() → INSERT inventory ✓
  │  │  └─ Ledger.recordChange() → INSERT ledger ✓
  │  └─ Result: SUCCESS
  │
  ├─ Item 2:
  │  ├─ Warehouse validation ✗ → Exception caught
  │  ├─ Result: FAILED
  │  └─ Transaction CONTINUES (caught exception)
  │
  └─ [TRANSACTION COMMITS] if all items processed without uncaught exceptions
     Both inventory and ledger changes persisted
```

### FAILURE WITH ROLLBACK
```
[@Transactional] useCase.execute()
  ├─ Item 1:
  │  ├─ Warehouse validation ✓
  │  ├─ Inventory check → not found
  │  ├─ Service.createInventory() ✓
  │  │  ├─ Gateway.create() → INSERT inventory ✓
  │  │  └─ Ledger.recordChange() → INSERT ledger ✓
  │  └─ Result: SUCCESS
  │
  ├─ Item 2:
  │  ├─ Warehouse validation ✓
  │  ├─ Inventory check → found
  │  ├─ Service.updateInventory() → OptimisticLockingException ✗
  │  │  [Exception NOT caught in usecase]
  │  └─ Exception propagates up
  │
  └─ [TRANSACTION ROLLS BACK] on uncaught exception
     Item 1 INSERT rolled back
     Item 2 INSERT rolled back
     Database in consistent state
```

Note: In actual implementation, uncaught exceptions cause full rollback. For a more resilient design, consider using nested transactions or saga pattern for distributed scenarios.

## Optimistic Locking Behavior

### Scenario: Concurrent Updates

**Timeline:**
```
Thread 1: Read inventory (version=5)
Thread 2: Read inventory (version=5)

Thread 1: Update quantity, SET version=6 WHERE version=5 ✓

Thread 2: Update quantity, SET version=6 WHERE version=5 ✗
         0 rows affected → OptimisticLockingException thrown
         Caught as OptimisticLockingException in gateway
         Converted to HTTP 409 CONFLICT response
```

**Response:**
```json
HTTP/1.1 409 Conflict

{
  "errorCode": "OPTIMISTIC_LOCKING_FAILURE",
  "message": "The resource has been modified by another process. Please retry.",
  "status": 409,
  "timestamp": "2026-05-13T10:30:00",
  "details": null
}
```

**Client should:**
1. Retry the request
2. Fetch fresh inventory data
3. Use updated version for next update

## Error Scenarios & Responses

### 1. Validation Error (Invalid Request Format)
```json
HTTP/1.1 400 Bad Request

{
  "errorCode": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "status": 400,
  "timestamp": "2026-05-13T10:30:00",
  "details": {
    "items": "Items list cannot be empty",
    "supplierId": "Supplier ID is required"
  }
}
```

### 2. Warehouse Not Found Error
```json
HTTP/1.1 200 OK (request succeeded, item failed)

{
  "totalProcessed": 1,
  "successCount": 0,
  "failureCount": 1,
  "results": [
    {
      "status": "FAILED",
      "errorMessage": "Warehouse not found or is inactive: 55555555-5555-5555-5555-555555555555"
    }
  ]
}
```

### 3. Inventory Validation Error
```json
HTTP/1.1 400 Bad Request

{
  "errorCode": "INVENTORY_OPERATION_FAILED",
  "message": "Cannot reduce quantity below reserved amount. Reserved: 80, New quantity: 50",
  "status": 400,
  "timestamp": "2026-05-13T10:30:00"
}
```

### 4. Unexpected Error
```json
HTTP/1.1 500 Internal Server Error

{
  "errorCode": "INTERNAL_ERROR",
  "message": "An unexpected error occurred. Please contact support.",
  "status": 500,
  "timestamp": "2026-05-13T10:30:00"
}
```

## Running the Application

### Prerequisites
- Java 25+
- Maven 3.8+
- PostgreSQL 12+ (or change datasource configuration)
- Docker (optional, for running PostgreSQL)

### Setup

#### 1. Database Setup
```bash
# Using Docker
docker run --name inventory-db \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=inventory_manager \
  -p 5432:5432 \
  -d postgres:15

# Or create database manually
createdb inventory_manager
```

#### 2. Update Database Configuration
Edit `src/main/resources/application.yaml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/inventory_manager
    username: postgres
    password: postgres  # Change to your password
```

#### 3. Build & Run
```bash
# Build
mvn clean install

# Run
mvn spring-boot:run

# Or run jar
java -jar target/inventory-manager-0.0.1-SNAPSHOT.jar
```

#### 4. Verify Application Started
```bash
curl http://localhost:8080/actuator/health
```

### Testing

#### Run All Tests
```bash
mvn test
```

#### Run Integration Tests Only
```bash
mvn test -Dtest=*IntegrationTest
```

#### Run Unit Tests Only
```bash
mvn test -Dtest=*ServiceTest,*UseCaseTest
```

## API Testing with cURL

### Create New Inventory
```bash
curl -X POST http://localhost:8080/api/v1/inventory/bulk-upsert \
  -H "Content-Type: application/json" \
  -d '{
    "supplierId": "11111111-1111-1111-1111-111111111111",
    "items": [
      {
        "supplierProductId": "22222222-2222-2222-2222-222222222222",
        "warehouseId": "33333333-3333-3333-3333-333333333333",
        "quantityOnHand": 100
      }
    ]
  }'
```

### Bulk Update Multiple Items
```bash
curl -X POST http://localhost:8080/api/v1/inventory/bulk-upsert \
  -H "Content-Type: application/json" \
  -d '{
    "supplierId": "11111111-1111-1111-1111-111111111111",
    "items": [
      {
        "supplierProductId": "22222222-2222-2222-2222-222222222222",
        "warehouseId": "33333333-3333-3333-3333-333333333333",
        "quantityOnHand": 150
      },
      {
        "supplierProductId": "44444444-4444-4444-4444-444444444444",
        "warehouseId": "55555555-5555-5555-5555-555555555555",
        "quantityOnHand": 200
      }
    ]
  }'
```

## Debugging Tips

### Enable Debug Logging
Edit `application.yaml`:
```yaml
logging:
  level:
    com.learning.inventory_manager: DEBUG
```

### Check Ledger Entries
```sql
SELECT * FROM inventory_ledger 
ORDER BY created_at DESC 
LIMIT 10;
```

### Verify Inventory State
```sql
SELECT id, supplier_product_id, warehouse_id, quantity_on_hand, 
       quantity_reserved, version, created_at, updated_at
FROM inventory;
```

### Find Warehouse Configuration
```sql
SELECT id, code, name, city, is_active FROM warehouses;
```

