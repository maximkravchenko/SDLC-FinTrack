# FinTrack — Personal Finance Tracker

[![Java 17](https://img.shields.io/badge/Java-17-red?logo=openjdk)](https://openjdk.org/)
[![Spring Boot 3.3](https://img.shields.io/badge/Spring_Boot-3.3-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue?logo=postgresql)](https://www.postgresql.org/)
[![JWT](https://img.shields.io/badge/JWT-Authentication-black)](https://jwt.io/)
[![MapStruct](https://img.shields.io/badge/MapStruct-1.5-orange)](https://mapstruct.org/)
[![Lombok](https://img.shields.io/badge/Lombok-1.18.34-yellow)](https://projectlombok.org/)

**Financery** — modern personal finance web application with flexible tagging system, transaction tracking and Excel export. Built as a clean, secure and well-tested Spring Boot 3 monolith.

## Features

### Smart Tagging System
- Fully user-owned tags (no preset categories)
- Unlimited tags per transaction/bill
- Fast search and filtering by any tag combination

### Transaction Management
- Full CRUD for income and expense transactions
- Advanced search with pagination
- Filter by tags, date range, amount, type
- Export any filtered view to Excel (Apache POI)

### Security & Users
- Registration & login
- JWT-based authentication
- All data strictly isolated per user
- Secure password storage (Spring Security + BCrypt)
- Change password endpoint

### Developer & Admin Tools
- Visit counter (for landing page analytics)
- Real-time server log viewer (admin only)
- Async logging system
- Global exception handling with detailed error responses

### Quality & Testing
- 100% unit test coverage of all services
- MapStruct mappers for clean DTO ↔ Entity conversion
- Proper layered architecture

## Tech Stack

### Backend (Java 17 + Spring Boot 3.3.4)
- **Framework**: Spring Boot 3 + Spring Web MVC
- **Security**: Spring Security + JWT
- **Data**: Spring Data JPA (Hibernate)
- **Database**: PostgreSQL (production), H2 (tests)
- **Mapping**: MapStruct + Lombok
- **Excel Export**: Apache POI
- **Validation**: Bean Validation (Jakarta)
- **Testing**: JUnit 5 + Mockito + Spring Test

## Project Structure
```
src/main/java/com/example/financery/
├── controller/          # REST controllers (6)
├── dto/                 # Request/Response DTOs
├── exception/           # Custom exceptions + GlobalExceptionHandler
├── mapper/              # MapStruct mappers
├── model/               # JPA entities (User, Transaction, Tag, Bill)
├── repository/          # JpaRepository interfaces
├── service/
│   └── impl/            # All business logic
├── security/            # SecurityUtil (current user)
└── FinanceryApplication.java
```

### Entities
- `User` → `Transaction` (OneToMany)
- `User` → `Tag` (OneToMany)
- `User` → `Bill` (OneToMany)
- `Transaction` ↔ `Tag` (ManyToMany)
- `Bill` ↔ `Tag` (ManyToMany)

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL 15+ (or use H2 for testing)

## API Documentation (Key Endpoints)

### Authentication
- `POST   /api/users/register`
- `POST   /api/users/login`
- `POST   /api/users/change-password`
- `GET    /api/users/me`

### Transactions
- `GET    /api/transactions` + filters & pagination
- `GET    /api/transactions/search?keyword=...`
- `POST   /api/transactions`
- `PUT    /api/transactions/{id}`
- `DELETE /api/transactions/{id}`
- `GET    /api/transactions/export/excel`

### Tags
- `GET    /api/tags`
- `POST   /api/tags`
- `PUT    /api/tags/{id}`
- `DELETE /api/tags/{id}`

### Bills (Receipts)
- `POST   /api/bills/upload` → multipart photo upload
- `GET    /api/bills/{id}/photo` → view receipt image
- `GET    /api/bills`
- `DELETE /api/bills/{id}`

### Utilities
- `GET  /api/visits` → visit counter
- `POST /api/visits` → increment counter
- `GET  /api/logs?lines=200` → admin-only log viewer

All protected endpoints require `Authorization: Bearer <jwt>`

All service implementations have full unit test coverage.

## Contributing
1. Fork the repo
2. Create your feature branch (`git checkout -b feature/cool-thing`)
3. Commit (`git commit -m 'Add cool thing'`)
4. Push and open a Pull Request

## License
This project is for personal and educational use.

## Future Ideas
- [ ] OCR integration for automatic bill parsing
- [ ] Recurring transactions
- [ ] Budget planning & goals
- [ ] Charts and analytics dashboard
- [ ] Mobile app (React Native / Flutter)
- [ ] Multi-currency support
- [ ] Cloud photo storage (S3)

Made with ❤️ by MAKSIM — simple, clean, and actually works.
