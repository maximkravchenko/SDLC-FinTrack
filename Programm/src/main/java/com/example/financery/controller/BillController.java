package com.example.financery.controller;

import com.example.financery.dto.BillDtoRequest;
import com.example.financery.dto.BillDtoResponse;
import com.example.financery.service.BillService;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/bills")
@AllArgsConstructor
@Tag(name = "Счета", description = "Управление счетами пользователя")
public class BillController {

    private final BillService billService;

    @Operation(
            summary = "Получение всех существующих счетов",
            description = "Возвращает список всех счетов, доступных в системе."
    )
    @GetMapping("/get-all-bills")
    public List<BillDtoResponse> getAllBills() {
        return billService.getAllBills();
    }

    @Operation(
            summary = "Получение всех счетов пользователя",
            description =
                    "Возвращает список всех счетов,"
                            + " принадлежащих пользователю с указанным ID."
    )
    @GetMapping("/get-all-user-bills/{userId}")
    public List<BillDtoResponse> getAllUserBills(
            @Parameter(description =
                    "ID пользователя, чьи счета необходимо получить",
                    required = true, example = "1")
            @PathVariable long userId) {
        return billService.getBillsByUserId(userId);
    }

    @Operation(
            summary = "Получение счета по ID",
            description = "Возвращает информацию о счете с указанным ID."
    )
    @GetMapping("/get-bill-by-id/{billId}")
    public ResponseEntity<BillDtoResponse> getBillById(
            @Parameter(description =
                    "ID счета, который необходимо получить",
                    required = true, example = "1")
            @PathVariable long billId) {
        return ResponseEntity.ok(billService.getBillById(billId));
    }

    @Operation(
            summary = "Создание нового счета",
            description = "Создает новый счет на основе переданных данных."
    )
    @PostMapping("/create")
    public BillDtoResponse createBill(
            @Parameter(description = "Данные для создания нового счета", required = true)
            @Valid @RequestBody BillDtoRequest billDto) {
        return billService.createBill(billDto);
    }

    @Operation(
            summary = "Обновление счета по ID",
            description =
                    "Обновляет данные счета с указанным ID на основе переданных данных."
    )
    @PutMapping("/update-by-id/{billId}")
    public BillDtoResponse updateBillById(
            @Parameter(description =
                    "ID счета, который необходимо обновить",
                    required = true, example = "1")
            @PathVariable long billId,
            @Parameter(description = "Обновленные данные счета", required = true)
            @Valid @RequestBody BillDtoRequest billDto) {
        return billService.updateBill(billId, billDto);
    }

    @Operation(
            summary = "Удаление счета по ID",
            description = "Удаляет счет с указанным ID."
    )
    @DeleteMapping("/delete-by-id/{billId}")
    public ResponseEntity<Integer>  deleteBillById(
            @Parameter(description =
                    "ID счета, который необходимо удалить",
                    required = true, example = "1")
            @PathVariable long billId) {
        billService.deleteBill(billId);
        return ResponseEntity.ok(1);
    }
}
