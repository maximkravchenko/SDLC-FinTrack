package com.example.financery.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TagDtoRequest {

    @Size(min = 3, max = 15,
            message = "Длина названия тега должна быть в пределах от 3 до 15 символов")
    @NotBlank(message = "Название не должно быть пустым")
    private String title;
    @Min(value = 1, message = "Id не может быть меньше 1")
    @NotNull(message = "Id пользователя должен быть указан")
    private long userId;
}