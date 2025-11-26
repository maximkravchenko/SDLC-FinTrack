package com.example.financery.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserDtoResponse {

    private long id;

    private String name;
    private String email;

    private double balance;

    private List<BillDtoResponse> bills;
}
