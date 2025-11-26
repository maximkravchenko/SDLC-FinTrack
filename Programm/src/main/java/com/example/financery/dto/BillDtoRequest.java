package com.example.financery.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BillDtoRequest {

    @Size(max = 50, message = "Длина названия слишком большая")
    @NotBlank(message = "Название не должно быть пустым")
    private String name;
    @Min(value = 0, message = "Баланс не может быть меньше 0")
    @NotNull(message = "Баланс счета должен быть указан")
    private double balance;
    @Min(value = 1, message = "Id не может быть меньше 1")
    @NotNull(message = "Id пользователя должен быть указан")
    private long userId;
}
