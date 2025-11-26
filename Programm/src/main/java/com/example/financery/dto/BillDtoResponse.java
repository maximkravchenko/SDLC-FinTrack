package com.example.financery.dto;

import lombok.Data;

import java.util.List;

@Data
public class BillDtoResponse {

    private long id;
    private String name;
    private double balance;
    private long userId;

    private List<TransactionDtoResponse> transactions;
}
