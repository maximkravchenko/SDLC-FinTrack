package com.example.financery.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class TransactionDtoRequest {

    @Size(max = 50, message = "Длина названия слишком большая")
    @NotBlank(message = "Название не должно быть пустым")
    private String name;
    @NotBlank(message = "Название не должно быть пустым")
    private String description;
    @NotNull(message = "Тип транзакции должен быть указан")
    private boolean type;
    @Min(value = 0, message = "Значение транзакции не может быть меньше 0")
    @NotNull(message = "Значение транзакции должено быть указано")
    private double amount;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
    private LocalDate date;

    @Min(value = 1, message = "Id не может быть меньше 1")
    @NotNull(message = "Id пользователя должен быть указан")
    private long userId;
    @Min(value = 1, message = "Id не может быть меньше 1")
    @NotNull(message = "Id счета должен быть указан")
    private long billId;
    private List<Long> tagIds;

}
