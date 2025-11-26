package com.example.financery.mapper;

import com.example.financery.dto.UserDtoRequest;
import com.example.financery.dto.UserDtoResponse;
import com.example.financery.model.User;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class UserMapper {

    private final BillMapper billMapper;
    private final TransactionMapper transactionMapper;

    public User toEntity(UserDtoRequest userDto) {
        User user = new User();

        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setBalance(userDto.getBalance());

        return user;
    }

    public UserDtoResponse toDto(User user) {
        UserDtoResponse userDtoResponse = new UserDtoResponse();

        userDtoResponse.setId(user.getId());
        userDtoResponse.setName(user.getName());
        userDtoResponse.setEmail(user.getEmail());
        userDtoResponse.setBalance(user.getBalance());

        userDtoResponse.setBills(
                user.getBills().stream()
                        .map(billMapper::toBillDto)
                        .collect(Collectors.toList())
        );

        return userDtoResponse;
    }
}
