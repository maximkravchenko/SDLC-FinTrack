package com.example.financery.mapper;

import com.example.financery.dto.TransactionDtoRequest;
import com.example.financery.dto.TransactionDtoResponse;
import com.example.financery.model.Bill;
import com.example.financery.model.Transaction;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class TransactionMapper {

    private final TagMapper tagMapper;

    public TransactionDtoResponse toTransactionDto(Transaction transaction) {
        TransactionDtoResponse transactionDtoResponse = new TransactionDtoResponse();

        transactionDtoResponse.setId(transaction.getId());
        transactionDtoResponse.setName(transaction.getName());
        transactionDtoResponse.setDescription(transaction.getDescription());
        transactionDtoResponse.setType(transaction.isType());
        transactionDtoResponse.setAmount(transaction.getAmount());
        transactionDtoResponse.setDate(transaction.getDate());

        transactionDtoResponse.setUserId(transaction.getUser().getId());
        transactionDtoResponse.setBillId(transaction.getBill().getId());

        transactionDtoResponse.setTags(
                transaction.getTags().stream()
                        .map(tagMapper::toTagDto)
                        .collect(Collectors.toList())
        );

        return transactionDtoResponse;
    }

    public static Transaction toTransaction(TransactionDtoRequest transactionDtoRequest) {
        Transaction transaction = new Transaction();

        transaction.setName(transactionDtoRequest.getName());
        transaction.setDescription(transactionDtoRequest.getDescription());
        transaction.setType(transactionDtoRequest.isType());
        transaction.setAmount(transactionDtoRequest.getAmount());
        transaction.setDate(transactionDtoRequest.getDate());

        return transaction;
    }

}
