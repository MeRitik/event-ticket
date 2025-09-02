# Event Management System Backend

A backend application for organizing events, managing ticket types, and handling ticket validation, built with **Spring Boot** and a modern Java stack.

---

## Overview

This system enables event organizers to:
- Create and publish events
- Define and manage ticket types
- Allow users to purchase and access tickets
- Validate and cancel tickets

All features are exposed as a RESTful API with JSON payloads.

---

## Technology Stack

- **Java 23**: Main programming language
- **Spring Boot**: Application framework
- **Spring Data JPA**: ORM and database interaction
- **Lombok**: Reduces Java boilerplate
- **ModelMapper**: Object mapping between entities and DTOs
- **PostgreSQL**: Database
- **Spring Security**: Secures endpoints
- **jwt**: For authentication (see usage in controllers)
- **ZXing**: Google's library for QR code generation
- **Maven**: Build & dependency management

All libraries are defined in [`pom.xml`](pom.xml).

---

## Project Structure

- `controller/` — REST API endpoint handlers (controllers)
- `service/`    — Business logic, domain services
- `repository/` — Data access interfaces
- `entities/`   — JPA entities (database tables)
- `payload/`    — DTO classes for API requests/responses
- `exceptions/` — Custom exception handling
- `config/`     — App and security configuration
- `util/`       — Utility/helper classes

---

## API Versioning

The API uses URI versioning with all endpoints prefixed with `/api/v1/`. This versioning strategy:
- Ensures backward compatibility when introducing new API versions
- Allows for clear distinction between different API iterations
- Provides explicit version information in the request path

Future API versions would be accessible via `/api/v2/`, `/api/v3/`, etc.

---

## REST API Endpoints

### Event Management

**Base path:** `/api/v1/events`

| HTTP Method | Endpoint                      | Description                    |
|-------------|------------------------------|--------------------------------|
| GET         | `/api/v1/events`             | List all events for organizer  |
| POST        | `/api/v1/events`             | Create a new event             |
| GET         | `/api/v1/events/{id}`        | Get event details              |
| PUT         | `/api/v1/events/{id}`        | Update event details           |
| DELETE      | `/api/v1/events/{id}`        | Delete an event                |

---

### Published Events

**Base path:** `/api/v1/published-events`

| HTTP Method | Endpoint                              | Description                                   |
|-------------|---------------------------------------|-----------------------------------------------|
| GET         | `/api/v1/published-events`            | List all published events                     |
| GET         | `/api/v1/published-events/{eventId}`  | Get published event details                   |

---

### Ticket Types

**Base path:** `/api/v1/events/{eventId}/ticket-types`

| HTTP Method | Endpoint                                                  | Description                        |
|-------------|-----------------------------------------------------------|------------------------------------|
| POST        | `/api/v1/events/{eventId}/ticket-types/{ticketTypeId}/tickets` | Purchase ticket for event/type     |

---

### Tickets

**Base path:** `/api/v1/tickets`

| HTTP Method | Endpoint                                     | Description                                |
|-------------|----------------------------------------------|--------------------------------------------|
| GET         | `/api/v1/tickets`                            | List all tickets for the user              |
| GET         | `/api/v1/tickets/{ticketId}`                 | Get ticket details                         |
| GET         | `/api/v1/tickets/{ticketId}/qr-codes`        | Get ticket QR code (as an image)           |

---

### Ticket Validation

(Not all endpoints shown; see source for details)

| HTTP Method | Typical Endpoint                        | Description         |
|-------------|-----------------------------------------|---------------------|
| POST        | `/api/v1/tickets/{ticketId}/validate`   | Validate a ticket   |
| POST        | `/api/v1/tickets/{ticketId}/cancel`     | Cancel a ticket     |


---

## Data Transfer Objects (DTOs)

- **Payloads** are in the `payload.dtos` package. Major examples:
  - `CreateEventRequestDto`, `GetEventDetailsResponseDto`, `ListEventResponseDto`
  - `ListPublishedEventResponseDto`
  - `ListTicketResponseDto`, `GetTicketResponseDto`
  - `TicketValidationRequestDto` (contains: `id`, `method`)

---

## Database & Setup

- Uses **PostgreSQL** as storage.
- All database configs are in `application.properties`.
- JPA entities auto-create tables.

---

## Getting Started

**Requirements:**
- Java 23
- Maven
- PostgreSQL database

**Steps:**

1. Clone the repository.
2. Configure PostgreSQL connection in `application.properties`.

3. Build the backend:
    ```bash
    ./mvnw clean package
    ```

4. Run the backend:
    ```bash
    ./mvnw spring-boot:run
    ```

5. API is available at `http://localhost:8080/api/v1/`

---

## QR Code Support

The system uses Google's ZXing ("Zebra Crossing") library for QR code generation:
- Tickets include QR codes for easy scanning at event entry
- QR codes contain encoded ticket information for validation
- Implementation is in the `QrCodeService` with the following dependencies:
  - `com.google.zxing:core:3.5.1`
  - `com.google.zxing:javase:3.5.1`

---

## Error Handling

Custom exceptions are handled globally for meaningful error messages. See `exceptions/GlobalExceptionHandler.java`.

---

## Libraries & Dependencies (from `pom.xml`)

- org.springframework.boot:spring-boot-starter-web
- org.springframework.boot:spring-boot-starter-data-jpa
- org.springframework.boot:spring-boot-starter-security
- org.postgresql:postgresql
- org.projectlombok:lombok
- org.modelmapper:modelmapper
- com.google.zxing:core
- com.google.zxing:javase
- (Test) org.springframework.boot:spring-boot-starter-test

> See `pom.xml` for exact versions and further dependencies.

---

## Security

Authentication is managed via JWT; endpoints utilize the Jwt principal. Ensure you setup authorization headers as required.

---

## License

[License not specified — add details here]

---

## Author

Developed by Ritik.