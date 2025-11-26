package com.example.financery.service;

import com.example.financery.dto.TransactionDtoRequest;
import com.example.financery.dto.TransactionDtoResponse;

import java.util.List;

public interface TransactionService {

    List<TransactionDtoResponse> getAllTransactions();

    TransactionDtoResponse getTransactionById(long transactionId);

    List<TransactionDtoResponse> getTransactionsByUserId(long userId);

    List<TransactionDtoResponse> getTransactionsByBillId(long userId);

    TransactionDtoResponse createTransaction(TransactionDtoRequest transactionDto);

    TransactionDtoResponse updateTransaction(
            long transactionId,
            TransactionDtoRequest transactionDto);

    void deleteTransaction(long transactionId);
}
