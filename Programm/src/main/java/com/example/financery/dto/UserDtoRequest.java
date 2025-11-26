package com.example.financery.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UserDtoRequest {

    @Size(max = 50, message = "Длина имени слишком большая")
    @NotBlank(message = "Имя не должно быть пустым")
    private String name;
    @Size(max = 50, message = "Длина электронной почты слишком большая")
    @NotBlank(message = "Электронная почта не должна быть пустой")
    @Email(message = "Электронная почта задана неверно")
    private String email;
    @Min(value = 0, message = "Баланс не может быть меньше 0")
    @NotNull(message = "Баланс счета должен быть указан")
    private double balance;
}
