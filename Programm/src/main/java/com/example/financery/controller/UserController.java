package com.example.financery.controller;


import com.example.financery.dto.UserDtoRequest;
import com.example.financery.dto.UserDtoResponse;
import com.example.financery.model.User;
import com.example.financery.service.UserService;
import java.util.List;

import com.example.financery.service.VisitCounterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/users")
@AllArgsConstructor
@Tag(name = "Пользователи", description = "Управление пользователями")
public class UserController {

    private final UserService userService;
    private final VisitCounterService visitCounterService;

    @Operation(
            summary = "Вывод всех пользователей",
            description = "Выводит пользователей и информацию о них"
    )
    @GetMapping("/get-all-users")
    public List<UserDtoResponse> getAllUsers() {
        visitCounterService.increment();
        return userService.getAllUsers();
    }

    @Operation(
            summary = "Поиск пользователя по id",
            description = "Выводит пользователя заданного id"
    )
    @GetMapping("/search-by-id/{id}")
    public UserDtoResponse getUserById(@PathVariable long id) {
        return userService.getUserById(id);
    }

    @Operation(
            summary = "Поиск пользователя по почте",
            description = "Выводит пользователя заданной почты"
    )
    @GetMapping("/search-by-email/{email}")
    public User getUserByEmail(@PathVariable String email) {
        return userService.getUserByEmail(email);
    }

    @Operation(
            summary = "Создание пользователя",
            description = "Создает пользователя"
    )
    @PostMapping("/create")
    public UserDtoResponse createUser(@Valid @RequestBody UserDtoRequest userDtoRequest) {
        return userService.createUser(userDtoRequest);
    }

    @Operation(
            summary = "Обновление пользовательской информации",
            description = "Позволяет обновить информацию о пользователе"
    )
    @PutMapping("/update-by-id/{id}")
    public UserDtoResponse updateUser(
            @PathVariable long id,
            @Valid @RequestBody UserDtoRequest userDtoRequest) {
        return userService.updateUser(id, userDtoRequest);
    }

    @Operation(
            summary = "Удаление пользователя",
            description = "Удаляет пользователя по Id"
    )
    @DeleteMapping("/delete-by-id/{id}")
    public ResponseEntity<Integer> deleteUser(@PathVariable long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(1); // Возвращаем 1 с HTTP 200
    }
}
