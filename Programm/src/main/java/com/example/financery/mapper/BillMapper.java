package com.example.financery.mapper;

import com.example.financery.dto.BillDtoRequest;
import com.example.financery.dto.BillDtoResponse;
import com.example.financery.model.Bill;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class BillMapper {

    private final TransactionMapper transactionMapper;

    public BillDtoResponse toBillDto(Bill bill) {
        BillDtoResponse billDtoResponse = new BillDtoResponse();

        billDtoResponse.setId(bill.getId());
        billDtoResponse.setName(bill.getName());
        billDtoResponse.setBalance(bill.getBalance());
        billDtoResponse.setUserId(bill.getUser().getId());

        billDtoResponse.setTransactions(
                bill.getTransactions().stream()
                        .map(transactionMapper::toTransactionDto)
                        .collect(Collectors.toList())
        );

        return billDtoResponse;
    }

    public Bill toBill(BillDtoRequest billDtoRequest) {
        Bill bill = new Bill();

        bill.setName(billDtoRequest.getName());
        bill.setBalance(billDtoRequest.getBalance());

        return bill;
    }
}
