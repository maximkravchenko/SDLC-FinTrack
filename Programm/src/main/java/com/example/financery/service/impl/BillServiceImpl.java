package com.example.financery.service.impl;

import com.example.financery.dto.BillDtoRequest;
import com.example.financery.dto.BillDtoResponse;
import com.example.financery.exception.InvalidInputException;
import com.example.financery.exception.NotFoundException;
import com.example.financery.mapper.BillMapper;
import com.example.financery.model.Bill;
import com.example.financery.model.User;
import com.example.financery.repository.BillRepository;
import com.example.financery.repository.UserRepository;
import com.example.financery.service.BillService;
import java.util.ArrayList;
import java.util.List;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class BillServiceImpl implements BillService {

    private static final String USER_NOT_FOUND = "Пользователь с id %d не найден";
    private static final String BILL_NOT_FOUND = "Счет с id %d не найден";

    private final BillRepository billRepository;
    private final BillMapper billMapper;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public List<BillDtoResponse> getAllBills() {
        List<BillDtoResponse> billsResponse = new ArrayList<>();
        billRepository.findAll().forEach(bill -> {
            Hibernate.initialize(bill.getTransactions());
            billsResponse.add(billMapper.toBillDto(bill));
        });
        return billsResponse;
    }

    @Override
    @Transactional
    public List<BillDtoResponse> getBillsByUserId(long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        String.format(USER_NOT_FOUND, userId)));
        List<Bill> bills = billRepository.findByUser(userId);
        List<BillDtoResponse> billsResponse = new ArrayList<>();

        bills.forEach(bill -> {
            Hibernate.initialize(bill.getTransactions());
            billsResponse.add(billMapper.toBillDto(bill));
        });
        return billsResponse;
    }

    @Transactional
    public BillDtoResponse getBillById(long id) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        String.format(BILL_NOT_FOUND, id)));
        Hibernate.initialize(bill.getTransactions());
        return billMapper.toBillDto(bill);
    }

    @Override
    public BillDtoResponse createBill(BillDtoRequest billDto) {
        if (billDto.getBalance() < 0) {
            throw new InvalidInputException(
                    "Баланс счёта не может быть отрицательным");
        }
        if (billDto.getName() == null || billDto.getName().trim().isEmpty()) {
            throw new InvalidInputException(
                    "Имя счёта не может быть пустым");
        }

        User user = userRepository.findById(billDto.getUserId())
                .orElseThrow(() -> new NotFoundException(
                        String.format(USER_NOT_FOUND, billDto.getUserId())));

        Bill bill = billMapper.toBill(billDto);

        user.setBalance(user.getBalance() + bill.getBalance());
        bill.setUser(user);
        billRepository.save(bill);

        return billMapper.toBillDto(bill);
    }

    @Override
    @Transactional
    public BillDtoResponse updateBill(long billId, BillDtoRequest billDto) {
        if (billDto.getBalance() < 0) {
            throw new InvalidInputException("Баланс счёта не может быть отрицательным");
        }
        if (billDto.getName() == null || billDto.getName().trim().isEmpty()) {
            throw new InvalidInputException("Имя счёта не может быть пустым");
        }

        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new NotFoundException(
                        String.format(BILL_NOT_FOUND, billId)));

        User user = userRepository.findById(bill.getUser().getId())
                .orElseThrow(() -> new NotFoundException(
                        String.format(USER_NOT_FOUND, billDto.getUserId())));

        double currentBalance = bill.getBalance();

        bill.setName(billDto.getName());
        bill.setBalance(billDto.getBalance());

        double newBalance = user.getBalance() - currentBalance + billDto.getBalance();
        user.setBalance(newBalance);

        userRepository.save(user);
        billRepository.save(bill);

        return billMapper.toBillDto(bill);
    }

    public void deleteBill(long billId) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new NotFoundException(
                        String.format(BILL_NOT_FOUND, billId)));

        User user = userRepository.findById(bill.getUser().getId())
                .orElseThrow(() -> new NotFoundException(
                        String.format(USER_NOT_FOUND, bill.getUser().getId())));

        user.setBalance(user.getBalance() - bill.getBalance());

        userRepository.save(user);
        billRepository.deleteById(billId);
    }

}
