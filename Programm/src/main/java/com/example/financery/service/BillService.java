package com.example.financery.service;

import com.example.financery.dto.BillDtoRequest;
import com.example.financery.dto.BillDtoResponse;
import java.util.List;

public interface BillService {

    List<BillDtoResponse> getAllBills();

    List<BillDtoResponse> getBillsByUserId(long userId);

    BillDtoResponse getBillById(long id);

    BillDtoResponse createBill(BillDtoRequest billDto);

    BillDtoResponse updateBill(long billId, BillDtoRequest billDto);

    void deleteBill(long billId);
}
