package com.example.financery.controller;

import com.example.financery.dto.TransactionDtoRequest;
import com.example.financery.dto.TransactionDtoResponse;
import com.example.financery.service.TransactionService;
import com.example.financery.utils.InMemoryCache;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
@AllArgsConstructor
@Tag(name = "Транзакции", description = "Управление транзакциями")
public class TransactionController {

    private final TransactionService transactionService;

    private final InMemoryCache cache;

    @Operation(
            summary = "Получение всех существующих транзакций",
            description = "Возвращает список всех транзакций, доступных в системе."
    )
    @GetMapping("/get-all-transactions")
    public List<TransactionDtoResponse> getAllTransactions() {
        return transactionService.getAllTransactions();
    }

    @Operation(
            summary = "Получение транзакции по ID",
            description = "Возвращает информацию о транзакции с указанным ID."
    )
    @GetMapping("/get-transaction-by-id/{transactionId}")
    public ResponseEntity<TransactionDtoResponse> getTransactionById(
            @Parameter(description = "ID транзакции, которую необходимо получить",
                    required = true, example = "1")
            @PathVariable long transactionId) {
        return ResponseEntity.ok(transactionService.getTransactionById(transactionId));
    }

    @Operation(
            summary = "Получение всех транзакций пользователя",
            description = "Возвращает список всех транзакций,"
                    + " принадлежащих пользователю с указанным ID."
    )
    @GetMapping("/get-all-user-transactions/{userId}")
    public List<TransactionDtoResponse> getAllUserTransactions(
            @Parameter(description = "ID пользователя, чьи транзакции необходимо получить",
                    required = true, example = "1")
            @PathVariable long userId) {
        return transactionService.getTransactionsByUserId(userId);
    }

    @Operation(
            summary = "Получение всех транзакций по счету",
            description = "Возвращает список всех транзакций,"
                    + " связанных со счетом по указанному ID счета."
    )
    @GetMapping("/get-all-bill-transactions/{billId}")
    public List<TransactionDtoResponse> getAllBillTransactions(
            @Parameter(description = "ID счета, транзакции которого необходимо получить",
                    required = true, example = "1")
            @PathVariable long billId) {
        return transactionService.getTransactionsByBillId(billId);
    }

    @Operation(
            summary = "Создание новой транзакции",
            description = "Создает новую транзакцию на основе переданных данных."
    )
    @PostMapping("/create")
    public TransactionDtoResponse createBill(
            @Parameter(description = "Данные для создания новой транзакции", required = true)
            @Valid @RequestBody TransactionDtoRequest transactionDto) {
        return transactionService.createTransaction(transactionDto);
    }

    @Operation(
            summary = "Обновление транзакции по ID",
            description = "Обновляет данные транзакции с указанным ID на основе переданных данных."
    )
    @PutMapping("/update-by-id/{transactionId}")
    public TransactionDtoResponse updateTransactionById(
            @Parameter(description = "ID транзакции, которую необходимо обновить",
                    required = true, example = "1")
            @PathVariable long transactionId,
            @Parameter(description = "Обновленные данные транзакции", required = true)
            @Valid @RequestBody TransactionDtoRequest transasctionDto) {
        return transactionService.updateTransaction(transactionId, transasctionDto);
    }

    @Operation(
            summary = "Удаление транзакции по ID",
            description = "Удаляет транзакцию с указанным ID."
    )
    @DeleteMapping("/delete-by-id/{transactionId}")
    public ResponseEntity<Integer> deleteTransactionById(
            @Parameter(description = "ID транзакции, которую необходимо удалить",
                    required = true, example = "1")
            @PathVariable long transactionId) {
        transactionService.deleteTransaction(transactionId);
        return ResponseEntity.ok(1);
    }

    @Operation(
            summary = "Очистка всего кэша",
            description = "Удаляет все данные из кэша транзакций."
    )
    @DeleteMapping("/cache/clear")
    public ResponseEntity<String> clearCache() {
        cache.clear();
        return ResponseEntity.ok("Cache cleared successfully");
    }

    @Operation(
            summary = "Очистка кэша для пользователя",
            description = "Удаляет данные кэша для пользователя с указанным ID."
    )
    @DeleteMapping("/cache/clear/{userId}")
    public ResponseEntity<String> clearCacheForUser(
            @Parameter(description = "ID пользователя, для которого необходимо очистить кэш",
                    required = true, example = "1")
            @PathVariable long userId) {
        cache.clearForUser(userId);
        return ResponseEntity.ok("Cache cleared for userId: " + userId);
    }
}
