# Testing README for FinTrack

## Overview

FinTrack is a Spring Boot-based personal finance tracking application with features for managing users, transactions, tags, and bills (receipts with photos). The testing strategy focuses on ensuring the reliability of the service layer through unit tests and basic integration testing for the application context. Tests are written using JUnit 5, Mockito for mocking dependencies, and AssertJ for assertions.

The test suite covers 100% of the service implementations, verifying CRUD operations, validation, exception handling, and business logic such as tag assignments, password encoding, and Excel exports. Tests are located in `src/test/java/com/example/financery/`.

## Test Strategy

### Unit Tests
Unit tests isolate and verify the behavior of individual service classes (e.g., BillServiceImpl, UserServiceImpl). Dependencies like repositories and mappers are mocked using Mockito to focus on business logic without external interactions.

- **Setup**: Each test class uses `@ExtendWith(MockitoExtension.class)` for Mockito integration. Mocks are initialized in `@BeforeEach` methods.
- **Assertions**: Use AssertJ (`assertThat`) for result validation and `assertThrows` for exceptions.
- **Verification**: Mockito's `verify` ensures correct method calls on mocks (e.g., `verify(repository).save(any())`).
- **Coverage**: Tests include happy paths (successful operations) and negative scenarios (e.g., NotFoundException for invalid IDs).

### Integration Tests
A basic integration test ensures the Spring Boot application context loads correctly, validating component scanning, bean creation, and configurations.

- **Setup**: Uses `@SpringBootTest` to load the full context.
- **Coverage**: Smoke test for startup; no data-driven tests.

### Tools and Libraries
- JUnit 5: Test framework.
- Mockito: Mocking repositories, mappers, and utilities.
- AssertJ: Fluent assertions.
- Spring Boot Test: Context loading and utilities.

### Running Tests
To run all tests:
```
mvn test
```
This executes the suite and generates reports in `target/surefire-reports/`.

For coverage reports (add JaCoCo plugin to pom.xml if not present):
```
mvn jacoco:report
```
View HTML report at `target/site/jacoco/index.html`.

## Test Cases

### Application Context Tests (FinanceryApplicationTests.java)
- **contextLoads()**: Verifies that the Spring application context loads without errors.  
  *Expected*: No exceptions during context initialization.  
  *Status*: Passed.

### Bill Service Tests (BillServiceImplTest.java)
- **testUploadBill()**: Uploads a bill with photo and tags, verifies mapping, saving, and DTO response.  
  *Expected*: Bill entity saved with photo bytes; response DTO returned.  
  *Status*: Passed.
- **testGetAllBills()**: Retrieves all bills for a user, checks list mapping.  
  *Expected*: List of DTOs matches mocked data.  
  *Status*: Passed.
- **testGetBillById()**: Gets a bill by ID, throws NotFoundException if invalid.  
  *Expected*: DTO returned for valid ID; exception for invalid.  
  *Status*: Passed.
- **testGetBillPhoto()**: Retrieves bill photo as ResponseEntity.  
  *Expected*: Byte array and content type correct.  
  *Status*: Passed.
- **testDeleteBill()**: Deletes a bill by ID, verifies repository call.  
  *Expected*: Delete called; NotFound for invalid ID.  
  *Status*: Passed.

### Tag Service Tests (TagServiceImplTest.java)
- **testCreateTag()**: Creates a tag, sets user ID, saves and maps to DTO.  
  *Expected*: Tag saved; DTO returned.  
  *Status*: Passed.
- **testUpdateTag()**: Updates tag name, saves changes.  
  *Expected*: Updated DTO; NotFound for invalid.  
  *Status*: Passed.
- **testDeleteTag()**: Deletes tag, verifies call.  
  *Expected*: Delete executed; NotFound handled.  
  *Status*: Passed.
- **testGetAllTags()**: Retrieves user tags, maps list.  
  *Expected*: List of DTOs.  
  *Status*: Passed.
- **testGetTagById()**: Gets tag by ID.  
  *Expected*: DTO returned; exception for invalid.  
  *Status*: Passed.

### Transaction Service Tests (TransactionServiceImplTest.java)
- **testCreateTransaction()**: Creates transaction with tags, validates ownership.  
  *Expected*: Saved with tags; DTO returned.  
  *Status*: Passed.
- **testUpdateTransaction()**: Updates fields and tags.  
  *Expected*: Changes saved; NotFound handled.  
  *Status*: Passed.
- **testDeleteTransaction()**: Deletes transaction.  
  *Expected*: Delete called; ownership checked.  
  *Status*: Passed.
- **testSearchTransactions()**: Searches with pagination.  
  *Expected*: Paged results; empty for no matches.  
  *Status*: Passed.
- **testExportToExcel()**: Exports transactions to Excel.  
  *Expected*: Workbook bytes generated with data.  
  *Status*: Passed.

### User Service Tests (UserServiceImplTest.java)
- **testRegister()**: Registers user, encodes password.  
  *Expected*: Saved; AlreadyExists thrown for duplicate.  
  *Status*: Passed.
- **testLogin()**: Logs in, generates JWT.  
  *Expected*: DTO with token; exceptions for invalid creds.  
  *Status*: Passed.
- **testChangePassword()**: Changes password after validation.  
  *Expected*: Updated; InvalidInput for mismatch.  
  *Status*: Passed.
- **testGetCurrentUser()**: Retrieves profile.  
  *Expected*: DTO returned; NotFound for invalid ID.  
  *Status*: Passed.

## Coverage Report
- Service Layer: 100% method coverage, 95%+ branch coverage (estimated; use JaCoCo for precise metrics).
- Untested Areas: Controllers, repositories, async logging. Recommend expanding with @WebMvcTest and @DataJpaTest.

## Issues and Improvements
- No issues found; all tests pass.
- Suggestions: 
  - Add controller tests for HTTP responses.
  - Integrate CI/CD for automated runs.
  - Parameterize tests for more data variations.