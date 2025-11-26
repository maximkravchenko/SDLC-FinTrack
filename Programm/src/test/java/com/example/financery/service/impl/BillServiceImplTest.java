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
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillServiceImplTest {

    @Mock
    private BillRepository billRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BillMapper billMapper;

    @InjectMocks
    private BillServiceImpl billService;

    private Bill bill;
    private User user;
    private BillDtoRequest billDtoRequest;
    private BillDtoResponse billDtoResponse;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setBalance(1000.0);

        bill = new Bill();
        bill.setId(1L);
        bill.setName("Test Bill");
        bill.setBalance(500.0);
        bill.setUser(user);
        bill.setTransactions(new ArrayList<>());

        billDtoRequest = new BillDtoRequest();
        billDtoRequest.setName("Test Bill");
        billDtoRequest.setBalance(500.0);
        billDtoRequest.setUserId(1L);

        billDtoResponse = new BillDtoResponse();
        billDtoResponse.setId(1L);
        billDtoResponse.setName("Test Bill");
        billDtoResponse.setBalance(500.0);
        billDtoResponse.setUserId(1L);
    }

    @Test
    void getAllBills_success() {
        try (MockedStatic<Hibernate> mockHibernate = mockStatic(Hibernate.class)) {
            mockHibernate.when(() -> Hibernate.initialize(any())).thenAnswer(invocation -> null);

            List<Bill> bills = List.of(bill);
            when(billRepository.findAll()).thenReturn(bills);
            when(billMapper.toBillDto(bill)).thenReturn(billDtoResponse);

            List<BillDtoResponse> result = billService.getAllBills();

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(billDtoResponse, result.get(0));
            verify(billRepository).findAll();
            verify(billMapper).toBillDto(bill);
        }
    }

    @Test
    void getAllBills_emptyList() {
        try (MockedStatic<Hibernate> mockHibernate = mockStatic(Hibernate.class)) {
            mockHibernate.when(() -> Hibernate.initialize(any())).thenAnswer(invocation -> null);

            when(billRepository.findAll()).thenReturn(new ArrayList<>());

            List<BillDtoResponse> result = billService.getAllBills();

            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(billRepository).findAll();
        }
    }

    @Test
    void getAllBills_hibernateInitializeThrowsException() {
        try (MockedStatic<Hibernate> mockHibernate = mockStatic(Hibernate.class)) {
            mockHibernate.when(() -> Hibernate.initialize(any()))
                    .thenThrow(new RuntimeException("Hibernate initialization failed"));

            List<Bill> bills = List.of(bill);
            when(billRepository.findAll()).thenReturn(bills);

            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> billService.getAllBills());

            assertEquals("Hibernate initialization failed", exception.getMessage());
            verify(billRepository).findAll();
            verify(billMapper, never()).toBillDto(any());
        }
    }

    @Test
    void getAllBills_billMapperThrowsException() {
        try (MockedStatic<Hibernate> mockHibernate = mockStatic(Hibernate.class)) {
            mockHibernate.when(() -> Hibernate.initialize(any())).thenAnswer(invocation -> null);

            List<Bill> bills = List.of(bill);
            when(billRepository.findAll()).thenReturn(bills);
            when(billMapper.toBillDto(bill)).thenThrow(new RuntimeException("Mapping failed"));

            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> billService.getAllBills());

            assertEquals("Mapping failed", exception.getMessage());
            verify(billRepository).findAll();
            verify(billMapper).toBillDto(bill);
        }
    }

    @Test
    void getBillsByUserId_success() {
        try (MockedStatic<Hibernate> mockHibernate = mockStatic(Hibernate.class)) {
            mockHibernate.when(() -> Hibernate.initialize(any())).thenAnswer(invocation -> null);

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(billRepository.findByUser(1L)).thenReturn(List.of(bill));
            when(billMapper.toBillDto(bill)).thenReturn(billDtoResponse);

            List<BillDtoResponse> result = billService.getBillsByUserId(1L);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(billDtoResponse, result.get(0));
            verify(userRepository).findById(1L);
            verify(billRepository).findByUser(1L);
            verify(billMapper).toBillDto(bill);
        }
    }

    @Test
    void getBillsByUserId_userNotFound_throwsNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> billService.getBillsByUserId(1L));

        assertEquals("Пользователь с id 1 не найден", exception.getMessage());
        verify(userRepository).findById(1L);
        verify(billRepository, never()).findByUser(anyLong());
    }

    @Test
    void getBillsByUserId_hibernateInitializeThrowsException() {
        try (MockedStatic<Hibernate> mockHibernate = mockStatic(Hibernate.class)) {
            mockHibernate.when(() -> Hibernate.initialize(any()))
                    .thenThrow(new RuntimeException("Hibernate initialization failed"));

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(billRepository.findByUser(1L)).thenReturn(List.of(bill));

            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> billService.getBillsByUserId(1L));

            assertEquals("Hibernate initialization failed", exception.getMessage());
            verify(userRepository).findById(1L);
            verify(billRepository).findByUser(1L);
            verify(billMapper, never()).toBillDto(any());
        }
    }

    @Test
    void getBillsByUserId_billMapperThrowsException() {
        try (MockedStatic<Hibernate> mockHibernate = mockStatic(Hibernate.class)) {
            mockHibernate.when(() -> Hibernate.initialize(any())).thenAnswer(invocation -> null);

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(billRepository.findByUser(1L)).thenReturn(List.of(bill));
            when(billMapper.toBillDto(bill)).thenThrow(new RuntimeException("Mapping failed"));

            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> billService.getBillsByUserId(1L));

            assertEquals("Mapping failed", exception.getMessage());
            verify(userRepository).findById(1L);
            verify(billRepository).findByUser(1L);
            verify(billMapper).toBillDto(bill);
        }
    }

    @Test
    void getBillById_success() {
        try (MockedStatic<Hibernate> mockHibernate = mockStatic(Hibernate.class)) {
            mockHibernate.when(() -> Hibernate.initialize(any())).thenAnswer(invocation -> null);

            when(billRepository.findById(1L)).thenReturn(Optional.of(bill));
            when(billMapper.toBillDto(bill)).thenReturn(billDtoResponse);

            BillDtoResponse result = billService.getBillById(1L);

            assertNotNull(result);
            assertEquals(billDtoResponse, result);
            verify(billRepository).findById(1L);
            verify(billMapper).toBillDto(bill);
        }
    }

    @Test
    void getBillById_billNotFound_throwsNotFoundException() {
        when(billRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> billService.getBillById(1L));

        assertEquals("Счет с id 1 не найден", exception.getMessage());
        verify(billRepository).findById(1L);
    }

    @Test
    void getBillById_hibernateInitializeThrowsException() {
        try (MockedStatic<Hibernate> mockHibernate = mockStatic(Hibernate.class)) {
            mockHibernate.when(() -> Hibernate.initialize(any()))
                    .thenThrow(new RuntimeException("Hibernate initialization failed"));

            when(billRepository.findById(1L)).thenReturn(Optional.of(bill));

            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> billService.getBillById(1L));

            assertEquals("Hibernate initialization failed", exception.getMessage());
            verify(billRepository).findById(1L);
            verify(billMapper, never()).toBillDto(any());
        }
    }

    @Test
    void getBillById_billMapperThrowsException() {
        try (MockedStatic<Hibernate> mockHibernate = mockStatic(Hibernate.class)) {
            mockHibernate.when(() -> Hibernate.initialize(any())).thenAnswer(invocation -> null);

            when(billRepository.findById(1L)).thenReturn(Optional.of(bill));
            when(billMapper.toBillDto(bill)).thenThrow(new RuntimeException("Mapping failed"));

            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> billService.getBillById(1L));

            assertEquals("Mapping failed", exception.getMessage());
            verify(billRepository).findById(1L);
            verify(billMapper).toBillDto(bill);
        }
    }

    @Test
    void createBill_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(billMapper.toBill(billDtoRequest)).thenReturn(bill);
        when(billRepository.save(bill)).thenReturn(bill);
        when(billMapper.toBillDto(bill)).thenReturn(billDtoResponse);

        BillDtoResponse result = billService.createBill(billDtoRequest);

        assertNotNull(result);
        assertEquals(billDtoResponse, result);
        assertEquals(1500.0, user.getBalance());
        verify(userRepository).findById(1L);
        verify(billRepository).save(bill);
        verify(billMapper).toBill(billDtoRequest);
        verify(billMapper).toBillDto(bill);
    }

    @Test
    void createBill_negativeBalance_throwsInvalidInputException() {
        billDtoRequest.setBalance(-100.0);

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> billService.createBill(billDtoRequest));

        assertEquals("Баланс счёта не может быть отрицательным", exception.getMessage());
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void createBill_nullName_throwsInvalidInputException() {
        billDtoRequest.setName(null);

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> billService.createBill(billDtoRequest));

        assertEquals("Имя счёта не может быть пустым", exception.getMessage());
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void createBill_emptyName_throwsInvalidInputException() {
        billDtoRequest.setName("");

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> billService.createBill(billDtoRequest));

        assertEquals("Имя счёта не может быть пустым", exception.getMessage());
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void createBill_userNotFound_throwsNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> billService.createBill(billDtoRequest));

        assertEquals("Пользователь с id 1 не найден", exception.getMessage());
        verify(userRepository).findById(1L);
        verify(billRepository, never()).save(any());
    }

    @Test
    void createBill_saveThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(billMapper.toBill(billDtoRequest)).thenReturn(bill);
        when(billRepository.save(bill)).thenThrow(new DataAccessException("Database error") {});

        DataAccessException exception = assertThrows(DataAccessException.class,
                () -> billService.createBill(billDtoRequest));

        assertEquals("Database error", exception.getMessage());
        verify(userRepository).findById(1L);
        verify(billRepository).save(bill);
        verify(billMapper).toBill(billDtoRequest);
        verify(billMapper, never()).toBillDto(any());
    }

    @Test
    void updateBill_success() {
        billDtoRequest.setBalance(600.0);
        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(billRepository.save(bill)).thenReturn(bill);
        when(userRepository.save(user)).thenReturn(user);
        when(billMapper.toBillDto(bill)).thenReturn(billDtoResponse);

        BillDtoResponse result = billService.updateBill(1L, billDtoRequest);

        assertNotNull(result);
        assertEquals(billDtoResponse, result);
        assertEquals(1100.0, user.getBalance());
        verify(billRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(billRepository).save(bill);
        verify(userRepository).save(user);
        verify(billMapper).toBillDto(bill);
    }

    @Test
    void updateBill_negativeBalance_throwsInvalidInputException() {
        billDtoRequest.setBalance(-100.0);

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> billService.updateBill(1L, billDtoRequest));

        assertEquals("Баланс счёта не может быть отрицательным", exception.getMessage());
        verify(billRepository, never()).findById(anyLong());
    }

    @Test
    void updateBill_nullName_throwsInvalidInputException() {
        billDtoRequest.setName(null);

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> billService.updateBill(1L, billDtoRequest));

        assertEquals("Имя счёта не может быть пустым", exception.getMessage());
        verify(billRepository, never()).findById(anyLong());
    }

    @Test
    void updateBill_emptyName_throwsInvalidInputException() {
        billDtoRequest.setName("");

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> billService.updateBill(1L, billDtoRequest));

        assertEquals("Имя счёта не может быть пустым", exception.getMessage());
        verify(billRepository, never()).findById(anyLong());
    }

    @Test
    void updateBill_billNotFound_throwsNotFoundException() {
        when(billRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> billService.updateBill(1L, billDtoRequest));

        assertEquals("Счет с id 1 не найден", exception.getMessage());
        verify(billRepository).findById(1L);
    }

    @Test
    void updateBill_userNotFound_throwsNotFoundException() {
        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> billService.updateBill(1L, billDtoRequest));

        assertEquals("Пользователь с id 1 не найден", exception.getMessage());
        verify(billRepository).findById(1L);
        verify(userRepository).findById(1L);
    }

    @Test
    void updateBill_saveBillThrowsException() {
        billDtoRequest.setBalance(600.0);
        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(billRepository.save(bill)).thenThrow(new DataAccessException("Database error") {});
        when(userRepository.save(user)).thenReturn(user);

        DataAccessException exception = assertThrows(DataAccessException.class,
                () -> billService.updateBill(1L, billDtoRequest));

        assertEquals("Database error", exception.getMessage());
        verify(billRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(billRepository).save(bill);
        verify(userRepository).save(user);
        verify(billMapper, never()).toBillDto(any());
    }

    @Test
    void updateBill_saveUserThrowsException() {
        billDtoRequest.setBalance(600.0);
        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenThrow(new DataAccessException("Database error") {});

        DataAccessException exception = assertThrows(DataAccessException.class,
                () -> billService.updateBill(1L, billDtoRequest));

        assertEquals("Database error", exception.getMessage());
        verify(billRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(userRepository).save(user);
        verify(billRepository, never()).save(any());
    }

    @Test
    void deleteBill_success() {
        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        billService.deleteBill(1L);

        assertEquals(500.0, user.getBalance());
        verify(billRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(userRepository).save(user);
        verify(billRepository).deleteById(1L);
    }

    @Test
    void deleteBill_billNotFound_throwsNotFoundException() {
        when(billRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> billService.deleteBill(1L));

        assertEquals("Счет с id 1 не найден", exception.getMessage());
        verify(billRepository).findById(1L);
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void deleteBill_userNotFound_throwsNotFoundException() {
        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> billService.deleteBill(1L));

        assertEquals("Пользователь с id 1 не найден", exception.getMessage());
        verify(billRepository).findById(1L);
        verify(userRepository).findById(1L);
    }

    @Test
    void deleteBill_saveUserThrowsException() {
        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenThrow(new DataAccessException("Database error") {});

        DataAccessException exception = assertThrows(DataAccessException.class,
                () -> billService.deleteBill(1L));

        assertEquals("Database error", exception.getMessage());
        verify(billRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(userRepository).save(user);
        verify(billRepository, never()).deleteById(anyLong());
    }
}