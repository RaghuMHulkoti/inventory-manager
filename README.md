<div align="center">
  <h1>📦 Inventory Manager</h1>
  <p><em>The source of truth for stock, reservations, and fulfilment logic.</em></p>

  ![Java](https://img.shields.io/badge/Java-21-blue.svg)
  ![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-brightgreen.svg)
  ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)
  ![Kafka](https://img.shields.io/badge/Kafka-Event_Driven-orange.svg)
  ![Docker](https://img.shields.io/badge/Docker-Ready-blue.svg)
</div>

---

## 📖 Overview

The **Inventory Service** is a core microservice in our E-Commerce Marketplace platform. It serves as the absolute single source of truth for stock availability across all suppliers and warehouses.

Unlike monolithic catalogs, this service is explicitly decoupled from product definitions and pricing. Its sole responsibility is guaranteeing the strongest promise in commerce: **no overselling, no double-charging, and no lost orders.**

## ✨ Key Features

- **Reservation-Based Stock:** Holds stock temporarily during the checkout window using TTL-based reservations to prevent overselling without permanently blocking inventory.
- **Optimistic Locking:** Designed for massive concurrency and flash-sale bursts using `@Version` fields.
- **Clean Architecture:** Strict separation of core business logic (Processors, UseCases) from infrastructure (Gateways, Controllers).
- **Event-Driven Resilience:** Integrates with Kafka via the Transactional Outbox pattern, publishing state changes (`INVENTORY_RESERVED`, `INVENTORY_DEDUCTED`) with 100% atomicity.
- **Audit Ledger:** Maintains an append-only `inventory_ledger` for tracking every single stock movement, ensuring total financial and operational traceability.
- **Bulk Operations:** Highly optimized bulk upsert APIs for suppliers syncing thousands of SKUs at once.

## 🏗️ Architecture

This service is built using **Clean Architecture**.
- **Core Domain:** Pure Java objects (`Inventory`, `InventoryLedgerEntry`). Contains no framework dependencies.
- **Ports (Interfaces):** Defines how the outside world talks to the core (`Usecases`, `Processors`) and how the core talks to the outside world (`Gateways`).
- **Adapters:** REST Controllers, Spring Data JPA Repositories, Kafka Producers.

### Database Design
The service owns the `inventory_db` PostgreSQL database, partitioned logically around:
* `warehouses`
* `inventory` (rows per `supplier_product` × `warehouse`)
* `stock_reservations`
* `inventory_ledger` (append-only audit)
* `inventory_outbox`

## 🚀 Getting Started

### Prerequisites
- **JDK 21**
- **Docker & Docker Compose** (for Postgres and Kafka)
- **Maven** (Wrapper included)

### Running Locally

1. **Start dependencies (PostgreSQL):**
   ```bash
   docker run -d --name inventory-db -p 5432:5432 -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=inventory_db postgres:16
   ```

2. **Build the project:**
   ```bash
   ./mvnw clean verify
   ```

3. **Run the Spring Boot application:**
   ```bash
   ./mvnw spring-boot:run
   ```

## 🔌 API Endpoints

All responses are wrapped in a standard unified `Response<T>` envelope ensuring a consistent contract for API consumers.

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/inventory/bulk-upsert` | Bulk update inventory counts for a supplier. |
| `GET`  | `/api/v1/inventory/{supplierProductId}` | Retrieve stock records for a specific product. |
| `GET`  | `/api/v1/inventory/availability` | Aggregate availability across warehouses. |
| `POST` | `/api/v1/inventory/reserve` | Create a temporary stock reservation for checkout. |
| `POST` | `/api/v1/inventory/deduct` | Permanently deduct stock upon successful payment. |
| `POST` | `/api/v1/inventory/release` | Release a reservation back into available stock. |

## 🚢 CI/CD Pipeline

The project includes a GitHub Actions pipeline (`.github/workflows/maven.yml`) that automatically:
1. Compiles and runs all tests.
2. Builds a lightweight Docker image using a multi-stage `Dockerfile`.
3. Performs a smoke test by spinning up the container.
4. Pushes the validated image to the GitHub Container Registry (`ghcr.io`).

## 🛡️ Exception Handling

Global error handling is centralized in `GlobalExceptionHandler`, translating domain errors (`InsufficientStockException`, `OptimisticLockingException`, etc.) into robust REST responses with precise HTTP status codes (e.g., `409 Conflict`, `422 Unprocessable Entity`).
