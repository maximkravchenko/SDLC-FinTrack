# Test Plan for Financery Application

## 1. Introduction

### 1.1 Purpose
This test plan outlines the testing strategy for the Financery application, a personal finance tracking web service built with Spring Boot. The goal is to ensure the reliability, functionality, and security of the core features based on the existing test suite. Testing focuses on unit and integration levels to verify business logic, data handling, and error management.

The plan is derived from the current test implementation, which includes one integration test for the application context and unit tests for all service implementations (BillServiceImpl, TagServiceImpl, TransactionServiceImpl, UserServiceImpl). It aims to validate that the application meets its requirements for managing transactions, bills, tags, and users.

### 1.2 Scope
- **In Scope**: Unit testing of service layer logic (CRUD operations, validation, exception handling); integration testing of Spring Boot context loading.
- **Out of Scope**: End-to-end (E2E) testing with UI/Frontend, performance testing, load testing, security penetration testing, manual exploratory testing, or tests for controllers/repositories (though they can be extended).
- **Assumptions**: Tests run in a controlled environment with mocked dependencies (Mockito) and in-memory DB (H2 for integration where applicable). All tests assume Java 17 and Spring Boot 3.3.4.

### 1.3 References
- Source Code: https://github.com/Max0neS/Financery
- Existing Tests: src/test/java/com/example/financery/*
- Dependencies: JUnit 5, Mockito, AssertJ, Spring Boot Test

## 2. Test Items

### 2.1 Features to be Tested
The following features are covered by the existing tests:

| Feature | Description | Test Coverage |
|---------|-------------|---------------|
| **User Management** | Registration, login, password change, profile retrieval. | Unit tests in UserServiceImplTest.java (CRUD, security checks, exceptions). |
| **Transaction Management** | Create, update, delete, search with pagination, export to Excel. | Unit tests in TransactionServiceImplTest.java (full CRUD, filters, tags integration, Excel generation). |
| **Tag Management** | Create, update, delete, retrieval. | Unit tests in TagServiceImplTest.java (CRUD, user ownership validation). |
| **Bill Management** | Upload with photo, retrieval, photo viewing, deletion. | Unit tests in BillServiceImplTest.java (multipart handling, photo storage, tags). |
| **Application Startup** | Spring Boot context loading. | Integration test in FinanceryApplicationTests.java. |

### 2.2 Features Not to be Tested
- Controller layer (e.g., HTTP endpoints) — recommend adding @WebMvcTest.
- Repository layer (e.g., JPA queries) — recommend @DataJpaTest.
- Security filters (JWT validation) — partial coverage in services, but full E2E needed.
- Asynchronous logging (AsyncLogExecutorImpl) — no dedicated tests.
- Visit counter (VisitCounterServiceImpl) — no tests present.
- Real database interactions (PostgreSQL) — tests use mocks or H2.

## 3. Test Approach

### 3.1 Testing Levels
- **Unit Testing**: Isolated testing of service implementations using mocks for repositories and mappers. Focus on business logic, input validation, and exception handling.
- **Integration Testing**: Basic context loading to ensure component wiring.
- **Test Tools**: JUnit 5 (for framework), Mockito (for mocking), AssertJ (for assertions), Spring Boot Test (for context).

### 3.2 Test Types
- **Functional Tests**: Verify correct outputs for valid inputs (e.g., create transaction returns DTO).
- **Negative Tests**: Check exceptions (e.g., NotFoundException for invalid IDs).
- **Edge Case Tests**: Empty lists, invalid tags, pagination boundaries.
- **Verification Methods**: assertThat for equality, assertThrows for exceptions, verify for method calls.

### 3.3 Test Environment
- **Hardware/Software**: Local development machine or CI/CD server (e.g., GitHub Actions).
- **Configuration**: Java 17, Maven for build, H2 in-memory DB for any data needs.
- **Data Setup**: Mocked entities (User, Transaction, etc.) created in @BeforeEach.

### 3.4 Entry and Exit Criteria
- **Entry**: Code compiles, dependencies resolved.
- **Exit**: All tests pass with 100% coverage for services (use JaCoCo for metrics).

## 4. Test Deliverables
- Test Code: src/test/java/* (existing files).
- Test Reports: Maven surefire reports (generated via `mvn test`).
- Coverage Report: JaCoCo HTML report (add plugin to pom.xml if needed).
- Defect Logs: Any failed tests logged in console/output.

## 5. Testing Tasks
1. Run unit tests for each service.
2. Verify integration test for context.
3. Update tests for new features (e.g., add @Test for LogService if implemented).
4. Measure coverage and refactor if <95%.

## 6. Schedule
- **Development Phase**: Run tests after each service change.
- **CI/CD**: Automate with Maven on push/PR.
- **Milestones**: Daily runs during development; full suite before release.

## 7. Risks and Contingencies
- **Risk**: Mocks not reflecting real behavior — Contingency: Add integration tests with real DB.
- **Risk**: Incomplete coverage for exceptions — Contingency: Expand negative tests.
- **Risk**: Dependencies update breaks tests — Contingency: Pin versions in pom.xml.


This plan can be extended with more test types as the application grows. For full coverage, consider adding controller tests with MockMvc and E2E with tools like Selenium or Postman.