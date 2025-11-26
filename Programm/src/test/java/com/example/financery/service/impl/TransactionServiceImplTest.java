package com.example.financery.service.impl;

import com.example.financery.dto.TransactionDtoRequest;
import com.example.financery.dto.TransactionDtoResponse;
import com.example.financery.exception.InvalidInputException;
import com.example.financery.exception.NotFoundException;
import com.example.financery.mapper.TransactionMapper;
import com.example.financery.model.Bill;
import com.example.financery.model.Tag;
import com.example.financery.model.Transaction;
import com.example.financery.model.User;
import com.example.financery.repository.BillRepository;
import com.example.financery.repository.TagRepository;
import com.example.financery.repository.TransactionRepository;
import com.example.financery.repository.UserRepository;
import com.example.financery.utils.InMemoryCache;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.DataAccessException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BillRepository billRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private InMemoryCache cache;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private User user;
    private Bill bill;
    private Tag tag;
    private Transaction transaction;
    private TransactionDtoRequest transactionDtoRequest;
    private TransactionDtoResponse transactionDtoResponse;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        bill = new Bill();
        bill.setId(1L);
        bill.setBalance(1000.0);
        bill.setUser(user);

        tag = new Tag();
        tag.setId(1L);
        tag.setUser(user);

        transaction = new Transaction();
        transaction.setId(1L);
        transaction.setName("Test Transaction");
        transaction.setDescription("Test Description");
        transaction.setType(true); // Доход
        transaction.setAmount(100.0);
        transaction.setDate(LocalDate.now());
        transaction.setUser(user);
        transaction.setBill(bill);
        transaction.setTags(new ArrayList<>(List.of(tag)));

        transactionDtoRequest = new TransactionDtoRequest();
        transactionDtoRequest.setName("Test Transaction");
        transactionDtoRequest.setDescription("Test Description");
        transactionDtoRequest.setType(true);
        transactionDtoRequest.setAmount(100.0);
        transactionDtoRequest.setDate(LocalDate.now());
        transactionDtoRequest.setUserId(1L);
        transactionDtoRequest.setBillId(1L);
        transactionDtoRequest.setTagIds(List.of(1L));

        transactionDtoResponse = new TransactionDtoResponse();
        transactionDtoResponse.setId(1L);
        transactionDtoResponse.setName("Test Transaction");
        transactionDtoResponse.setDescription("Test Description");
        transactionDtoResponse.setType(true);
        transactionDtoResponse.setAmount(100.0);
        transactionDtoResponse.setDate(LocalDate.now());
        transactionDtoResponse.setUserId(1L);
        transactionDtoResponse.setBillId(1L);
    }

    @AfterEach
    void tearDown() {
        Mockito.reset(transactionRepository,
                userRepository,
                billRepository,
                tagRepository,
                transactionMapper,
                cache);
    }

    @Test
    void getAllTransactions_success() {
        try (MockedStatic<Hibernate> mockHibernate = mockStatic(Hibernate.class)) {
            mockHibernate.when(() -> Hibernate.initialize(any())).thenAnswer(invocation -> null);

            when(transactionRepository.findAll()).thenReturn(List.of(transaction));
            when(transactionMapper
                    .toTransactionDto(transaction))
                    .thenReturn(transactionDtoResponse);

            List<TransactionDtoResponse> result = transactionService
                    .getAllTransactions();

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(transactionDtoResponse, result.get(0));
            verify(transactionRepository).findAll();
            verify(transactionMapper).toTransactionDto(transaction);
        }
    }

    @Test
    void getAllTransactions_emptyList() {
        try (MockedStatic<Hibernate> mockHibernate = mockStatic(Hibernate.class)) {
            mockHibernate.when(() -> Hibernate.initialize(any())).thenAnswer(invocation -> null);

            when(transactionRepository.findAll()).thenReturn(Collections.emptyList());

            List<TransactionDtoResponse> result = transactionService.getAllTransactions();

            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(transactionRepository).findAll();
        }
    }

    @Test
    void getTransactionById_success() {
        try (MockedStatic<Hibernate> mockHibernate = mockStatic(Hibernate.class)) {
            mockHibernate.when(() -> Hibernate.initialize(any())).thenAnswer(invocation -> null);

            when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
            when(transactionMapper
                    .toTransactionDto(transaction))
                    .thenReturn(transactionDtoResponse);

            TransactionDtoResponse result = transactionService.getTransactionById(1L);

            assertNotNull(result);
            assertEquals(transactionDtoResponse, result);
            verify(transactionRepository).findById(1L);
            verify(transactionMapper).toTransactionDto(transaction);
        }
    }

    @Test
    void getTransactionById_notFound_throwsNotFoundException() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> transactionService.getTransactionById(1L));

        assertEquals("Транзакция с id 1 не найдена", exception.getMessage());
        verify(transactionRepository).findById(1L);
    }

    @Test
    void getTransactionsByUserId_fromCache_success() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(cache.get(1L)).thenReturn(List.of(transactionDtoResponse));

        List<TransactionDtoResponse> result = transactionService.getTransactionsByUserId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(transactionDtoResponse, result.get(0));
        verify(userRepository).existsById(1L);
        verify(cache).get(1L);
        verify(transactionRepository, never()).findByUserId(anyLong());
    }

    @Test
    void getTransactionsByUserId_fromRepository_success() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(cache.get(1L)).thenReturn(null);
        when(transactionRepository.findByUserId(1L)).thenReturn(List.of(transaction));
        when(transactionMapper.toTransactionDto(transaction)).thenReturn(transactionDtoResponse);

        List<TransactionDtoResponse> result = transactionService.getTransactionsByUserId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(transactionDtoResponse, result.get(0));
        verify(userRepository).existsById(1L);
        verify(cache).get(1L);
        verify(transactionRepository).findByUserId(1L);
        verify(transactionMapper).toTransactionDto(transaction);
        verify(cache).put(1L, result);
    }

    @Test
    void getTransactionsByUserId_userNotFound_throwsNotFoundException() {
        when(userRepository.existsById(1L)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> transactionService.getTransactionsByUserId(1L));

        assertEquals("Пользователь с id 1 не найден", exception.getMessage());
        verify(userRepository).existsById(1L);
        verify(cache, never()).get(anyLong());
    }

    @Test
    void getTransactionsByBillId_success() {
        try (MockedStatic<Hibernate> mockHibernate = mockStatic(Hibernate.class)) {
            mockHibernate.when(() -> Hibernate.initialize(any())).thenAnswer(invocation -> null);

            when(billRepository.findById(1L)).thenReturn(Optional.of(bill));
            when(transactionRepository
                    .findByBill(1L))
                    .thenReturn(List.of(transaction));
            when(transactionMapper
                    .toTransactionDto(transaction))
                    .thenReturn(transactionDtoResponse);

            List<TransactionDtoResponse> result = transactionService.getTransactionsByBillId(1L);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(transactionDtoResponse, result.get(0));
            verify(billRepository).findById(1L);
            verify(transactionRepository).findByBill(1L);
            verify(transactionMapper).toTransactionDto(transaction);
        }
    }

    @Test
    void getTransactionsByBillId_billNotFound_throwsNotFoundException() {
        when(billRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> transactionService.getTransactionsByBillId(1L));

        assertEquals("Счет с id 1 не найден", exception.getMessage());
        verify(billRepository).findById(1L);
        verify(transactionRepository, never()).findByBill(anyLong());
    }

    @Test
    void createTransaction_success_income() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(billRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(bill));
        when(tagRepository.findAllById(List.of(1L))).thenReturn(List.of(tag));
        when(transactionMapper.toTransactionDto(any())).thenReturn(transactionDtoResponse);

        TransactionDtoResponse result = transactionService.createTransaction(transactionDtoRequest);

        assertNotNull(result);
        assertEquals(transactionDtoResponse, result);
        assertEquals(1100.0, bill.getBalance()); // 1000 + 100 (доход)
        verify(userRepository).findById(1L);
        verify(billRepository).findByIdAndUserId(1L, 1L);
        verify(tagRepository).findAllById(List.of(1L));
        verify(transactionRepository).save(any(Transaction.class));
        verify(cache).updateTransaction(1L, transactionDtoResponse);
    }

    @Test
    void createTransaction_success_expense() {
        transactionDtoRequest.setType(false); // Расход
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(billRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(bill));
        when(tagRepository.findAllById(List.of(1L))).thenReturn(List.of(tag));
        when(transactionMapper.toTransactionDto(any())).thenReturn(transactionDtoResponse);

        TransactionDtoResponse result = transactionService.createTransaction(transactionDtoRequest);

        assertNotNull(result);
        assertEquals(transactionDtoResponse, result);
        assertEquals(900.0, bill.getBalance()); // 1000 - 100 (расход)
        verify(userRepository).findById(1L);
        verify(billRepository).findByIdAndUserId(1L, 1L);
        verify(tagRepository).findAllById(List.of(1L));
        verify(transactionRepository).save(any(Transaction.class));
        verify(cache).updateTransaction(1L, transactionDtoResponse);
    }

    @Test
    void createTransaction_userNotFound_throwsNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> transactionService.createTransaction(transactionDtoRequest));

        assertEquals("Пользователь с id 1 не найден", exception.getMessage());
        verify(userRepository).findById(1L);
    }

    @Test
    void createTransaction_billNotFound_throwsNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(billRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> transactionService.createTransaction(transactionDtoRequest));

        assertEquals("Счет с id 1 не найден или не принадлежит пользователю",
                exception.getMessage());
        verify(userRepository).findById(1L);
        verify(billRepository).findByIdAndUserId(1L, 1L);
    }

    @Test
    void createTransaction_insufficientFunds_throwsInvalidInputException() {
        transactionDtoRequest.setType(false); // Расход
        transactionDtoRequest.setAmount(2000.0); // Больше, чем баланс счета (1000)

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(billRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(bill));

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> transactionService.createTransaction(transactionDtoRequest));

        assertEquals("Недостаточно средств на счете для суммы транзакции", exception.getMessage());
        verify(userRepository).findById(1L);
        verify(billRepository).findByIdAndUserId(1L, 1L);
    }

    @Test
    void createTransaction_amountTooLarge_throwsInvalidInputException() {
        transactionDtoRequest.setAmount(1_000_001.0);

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> transactionService.createTransaction(transactionDtoRequest));

        assertEquals("Сумма транзакции не может превышать 1,000,000", exception.getMessage());
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void createTransaction_tagNotFound_throwsInvalidInputException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(billRepository
                .findByIdAndUserId(1L, 1L))
                .thenReturn(Optional.of(bill));
        when(tagRepository.findAllById(List.of(1L)))
                .thenReturn(Collections.emptyList()); // Тег не найден

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> transactionService.createTransaction(transactionDtoRequest));

        assertEquals("Один или несколько тегов по ID не найдены", exception.getMessage());
        verify(userRepository).findById(1L);
        verify(billRepository).findByIdAndUserId(1L, 1L);
        verify(tagRepository).findAllById(List.of(1L));
    }

    @Test
    void createTransaction_tagUserMismatch_throwsInvalidInputException() {
        Tag wrongTag = new Tag();
        wrongTag.setId(1L);
        User wrongUser = new User();
        wrongUser.setId(2L); // Другой пользователь
        wrongTag.setUser(wrongUser);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(billRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(bill));
        when(tagRepository.findAllById(List.of(1L))).thenReturn(List.of(wrongTag));

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> transactionService.createTransaction(transactionDtoRequest));

        assertEquals("Один или несколько тегов не найдены или не принадлежат пользователю",
                exception.getMessage());
        verify(userRepository).findById(1L);
        verify(billRepository).findByIdAndUserId(1L, 1L);
        verify(tagRepository).findAllById(List.of(1L));
    }

    @Test
    void updateTransaction_success() {
        transactionDtoRequest.setAmount(200.0);
        transactionDtoRequest.setType(false); // Расход

        Transaction updatedTransaction = new Transaction();
        updatedTransaction.setId(1L);
        updatedTransaction.setName(transactionDtoRequest.getName());
        updatedTransaction.setDescription(transactionDtoRequest.getDescription());
        updatedTransaction.setType(false);
        updatedTransaction.setAmount(200.0);
        updatedTransaction.setDate(transactionDtoRequest.getDate());
        updatedTransaction.setUser(user);
        updatedTransaction.setBill(bill);
        updatedTransaction.setTags(new ArrayList<>(List.of(tag)));

        TransactionDtoResponse updatedResponse = new TransactionDtoResponse();
        updatedResponse.setId(1L);
        updatedResponse.setName(transactionDtoRequest.getName());
        updatedResponse.setDescription(transactionDtoRequest.getDescription());
        updatedResponse.setType(false);
        updatedResponse.setAmount(200.0);
        updatedResponse.setDate(transactionDtoRequest.getDate());
        updatedResponse.setUserId(1L);
        updatedResponse.setBillId(1L);

        try (MockedStatic<Hibernate> mockHibernate = mockStatic(Hibernate.class)) {
            mockHibernate.when(() -> Hibernate.initialize(any())).thenAnswer(invocation -> null);

            when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(billRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(bill));
            when(tagRepository.findAllById(List.of(1L))).thenReturn(List.of(tag));
            when(transactionRepository.save(any(Transaction.class))).thenReturn(updatedTransaction);
            when(transactionMapper
                    .toTransactionDto(any(Transaction.class)))
                    .thenReturn(updatedResponse);

            TransactionDtoResponse result = transactionService
                    .updateTransaction(1L, transactionDtoRequest);

            assertNotNull(result);
            assertEquals(updatedResponse, result);
            assertEquals(700.0, bill.getBalance());
            verify(transactionRepository).findById(1L);
            verify(userRepository).findById(1L);
            verify(billRepository).findByIdAndUserId(1L, 1L);
            verify(tagRepository).findAllById(List.of(1L));
            verify(userRepository).save(user);
            verify(billRepository).save(bill);
            verify(transactionRepository).save(any(Transaction.class));
            verify(cache).updateTransaction(1L, updatedResponse);
        }
    }

    @Test
    void updateTransaction_success_incomeToIncome() {
        transactionDtoRequest.setAmount(200.0);
        transactionDtoRequest.setType(true); // Новый тип — доход

        Transaction updatedTransaction = new Transaction();
        updatedTransaction.setId(1L);
        updatedTransaction.setName(transactionDtoRequest.getName());
        updatedTransaction.setDescription(transactionDtoRequest.getDescription());
        updatedTransaction.setType(true);
        updatedTransaction.setAmount(200.0);
        updatedTransaction.setDate(transactionDtoRequest.getDate());
        updatedTransaction.setUser(user);
        updatedTransaction.setBill(bill);
        updatedTransaction.setTags(new ArrayList<>(List.of(tag)));

        TransactionDtoResponse updatedResponse = new TransactionDtoResponse();
        updatedResponse.setId(1L);
        updatedResponse.setName(transactionDtoRequest.getName());
        updatedResponse.setDescription(transactionDtoRequest.getDescription());
        updatedResponse.setType(true);
        updatedResponse.setAmount(200.0);
        updatedResponse.setDate(transactionDtoRequest.getDate());
        updatedResponse.setUserId(1L);
        updatedResponse.setBillId(1L);

        try (MockedStatic<Hibernate> mockHibernate = mockStatic(Hibernate.class)) {
            mockHibernate.when(() -> Hibernate.initialize(any())).thenAnswer(invocation -> null);

            when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(billRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(bill));
            when(tagRepository.findAllById(List.of(1L))).thenReturn(List.of(tag));
            when(transactionRepository.save(any(Transaction.class))).thenReturn(updatedTransaction);
            when(transactionMapper
                    .toTransactionDto(any(Transaction.class)))
                    .thenReturn(updatedResponse);

            TransactionDtoResponse result = transactionService
                    .updateTransaction(1L, transactionDtoRequest);

            assertNotNull(result);
            assertEquals(updatedResponse, result);
            assertEquals(1100.0, bill.getBalance()); // Исправлено
            verify(transactionRepository).findById(1L);
            verify(userRepository).findById(1L);
            verify(billRepository).findByIdAndUserId(1L, 1L);
            verify(tagRepository).findAllById(List.of(1L));
            verify(userRepository).save(user);
            verify(billRepository).save(bill);
            verify(transactionRepository).save(any(Transaction.class));
            verify(cache).updateTransaction(1L, updatedResponse);
        }
    }

    @Test
    void updateTransaction_success_expenseToExpense() {
        transaction.setType(false); // Исходная транзакция — расход
        transactionDtoRequest.setAmount(200.0);
        transactionDtoRequest.setType(false); // Новый тип — расход

        Transaction updatedTransaction = new Transaction();
        updatedTransaction.setId(1L);
        updatedTransaction.setName(transactionDtoRequest.getName());
        updatedTransaction.setDescription(transactionDtoRequest.getDescription());
        updatedTransaction.setType(false);
        updatedTransaction.setAmount(200.0);
        updatedTransaction.setDate(transactionDtoRequest.getDate());
        updatedTransaction.setUser(user);
        updatedTransaction.setBill(bill);
        updatedTransaction.setTags(new ArrayList<>(List.of(tag)));

        TransactionDtoResponse updatedResponse = new TransactionDtoResponse();
        updatedResponse.setId(1L);
        updatedResponse.setName(transactionDtoRequest.getName());
        updatedResponse.setDescription(transactionDtoRequest.getDescription());
        updatedResponse.setType(false);
        updatedResponse.setAmount(200.0);
        updatedResponse.setDate(transactionDtoRequest.getDate());
        updatedResponse.setUserId(1L);
        updatedResponse.setBillId(1L);

        try (MockedStatic<Hibernate> mockHibernate = mockStatic(Hibernate.class)) {
            mockHibernate.when(() -> Hibernate.initialize(any())).thenAnswer(invocation -> null);

            when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(billRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(bill));
            when(tagRepository.findAllById(List.of(1L))).thenReturn(List.of(tag));
            when(transactionRepository.save(any(Transaction.class))).thenReturn(updatedTransaction);
            when(transactionMapper
                    .toTransactionDto(any(Transaction.class)))
                    .thenReturn(updatedResponse);

            TransactionDtoResponse result = transactionService
                    .updateTransaction(1L, transactionDtoRequest);

            assertNotNull(result);
            assertEquals(updatedResponse, result);
            assertEquals(900.0, bill.getBalance()); // Исправлено
            verify(transactionRepository).findById(1L);
            verify(userRepository).findById(1L);
            verify(billRepository).findByIdAndUserId(1L, 1L);
            verify(tagRepository).findAllById(List.of(1L));
            verify(userRepository).save(user);
            verify(billRepository).save(bill);
            verify(transactionRepository).save(any(Transaction.class));
            verify(cache).updateTransaction(1L, updatedResponse);
        }
    }

    @Test
    void updateTransaction_success_expenseToIncome() {
        transaction.setType(false); // Исходная транзакция — расход
        transactionDtoRequest.setAmount(200.0);
        transactionDtoRequest.setType(true); // Новая транзакция — доход

        Transaction updatedTransaction = new Transaction();
        updatedTransaction.setId(1L);
        updatedTransaction.setName(transactionDtoRequest.getName());
        updatedTransaction.setDescription(transactionDtoRequest.getDescription());
        updatedTransaction.setType(true);
        updatedTransaction.setAmount(200.0);
        updatedTransaction.setDate(transactionDtoRequest.getDate());
        updatedTransaction.setUser(user);
        updatedTransaction.setBill(bill);
        updatedTransaction.setTags(new ArrayList<>(List.of(tag)));

        TransactionDtoResponse updatedResponse = new TransactionDtoResponse();
        updatedResponse.setId(1L);
        updatedResponse.setName(transactionDtoRequest.getName());
        updatedResponse.setDescription(transactionDtoRequest.getDescription());
        updatedResponse.setType(true);
        updatedResponse.setAmount(200.0);
        updatedResponse.setDate(transactionDtoRequest.getDate());
        updatedResponse.setUserId(1L);
        updatedResponse.setBillId(1L);

        try (MockedStatic<Hibernate> mockHibernate = mockStatic(Hibernate.class)) {
            mockHibernate.when(() -> Hibernate.initialize(any())).thenAnswer(invocation -> null);

            when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(billRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(bill));
            when(tagRepository.findAllById(List.of(1L))).thenReturn(List.of(tag));
            when(transactionRepository.save(any(Transaction.class))).thenReturn(updatedTransaction);
            when(transactionMapper
                    .toTransactionDto(any(Transaction.class)))
                    .thenReturn(updatedResponse);

            TransactionDtoResponse result = transactionService
                    .updateTransaction(1L, transactionDtoRequest);

            assertNotNull(result);
            assertEquals(updatedResponse, result);
            assertEquals(1300.0, bill.getBalance());
            verify(transactionRepository).findById(1L);
            verify(userRepository).findById(1L);
            verify(billRepository).findByIdAndUserId(1L, 1L);
            verify(tagRepository).findAllById(List.of(1L));
            verify(userRepository).save(user);
            verify(billRepository).save(bill);
            verify(transactionRepository).save(any(Transaction.class));
            verify(cache).updateTransaction(1L, updatedResponse);
        }
    }

    @Test
    void updateTransaction_nullTagIds_success() {
        transactionDtoRequest.setAmount(200.0);
        transactionDtoRequest.setType(false); // Расход
        transactionDtoRequest.setTagIds(null);

        Transaction updatedTransaction = new Transaction();
        updatedTransaction.setId(1L);
        updatedTransaction.setName(transactionDtoRequest.getName());
        updatedTransaction.setDescription(transactionDtoRequest.getDescription());
        updatedTransaction.setType(false);
        updatedTransaction.setAmount(200.0);
        updatedTransaction.setDate(transactionDtoRequest.getDate());
        updatedTransaction.setUser(user);
        updatedTransaction.setBill(bill);
        updatedTransaction.setTags(new ArrayList<>());

        TransactionDtoResponse updatedResponse = new TransactionDtoResponse();
        updatedResponse.setId(1L);
        updatedResponse.setName(transactionDtoRequest.getName());
        updatedResponse.setDescription(transactionDtoRequest.getDescription());
        updatedResponse.setType(false);
        updatedResponse.setAmount(200.0);
        updatedResponse.setDate(transactionDtoRequest.getDate());
        updatedResponse.setUserId(1L);
        updatedResponse.setBillId(1L);

        try (MockedStatic<Hibernate> mockHibernate = mockStatic(Hibernate.class)) {
            mockHibernate.when(() -> Hibernate.initialize(any())).thenAnswer(invocation -> null);

            when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(billRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(bill));
            when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
                Transaction savedTransaction = invocation.getArgument(0);
                savedTransaction.setTags(new ArrayList<>());
                return savedTransaction;
            });
            when(transactionMapper
                    .toTransactionDto(any(Transaction.class)))
                    .thenReturn(updatedResponse);

            TransactionDtoResponse result = transactionService
                    .updateTransaction(1L, transactionDtoRequest);

            assertNotNull(result);
            assertEquals(updatedResponse, result);
            assertEquals(700.0, bill.getBalance());
            verify(transactionRepository).findById(1L);
            verify(userRepository).findById(1L);
            verify(billRepository).findByIdAndUserId(1L, 1L);
            verify(tagRepository, never()).findAllById(anyList());
            verify(userRepository).save(user);
            verify(billRepository).save(bill);
            verify(transactionRepository).save(any(Transaction.class));
            verify(cache).updateTransaction(1L, updatedResponse);
        }
    }

    @Test
    void updateTransaction_emptyTagIds_success() {
        transactionDtoRequest.setAmount(200.0);
        transactionDtoRequest.setType(false); // Расход
        transactionDtoRequest.setTagIds(Collections.emptyList());

        Transaction updatedTransaction = new Transaction();
        updatedTransaction.setId(1L);
        updatedTransaction.setName(transactionDtoRequest.getName());
        updatedTransaction.setDescription(transactionDtoRequest.getDescription());
        updatedTransaction.setType(false);
        updatedTransaction.setAmount(200.0);
        updatedTransaction.setDate(transactionDtoRequest.getDate());
        updatedTransaction.setUser(user);
        updatedTransaction.setBill(bill);
        updatedTransaction.setTags(new ArrayList<>());

        TransactionDtoResponse updatedResponse = new TransactionDtoResponse();
        updatedResponse.setId(1L);
        updatedResponse.setName(transactionDtoRequest.getName());
        updatedResponse.setDescription(transactionDtoRequest.getDescription());
        updatedResponse.setType(false);
        updatedResponse.setAmount(200.0);
        updatedResponse.setDate(transactionDtoRequest.getDate());
        updatedResponse.setUserId(1L);
        updatedResponse.setBillId(1L);

        try (MockedStatic<Hibernate> mockHibernate = mockStatic(Hibernate.class)) {
            mockHibernate.when(() -> Hibernate.initialize(any())).thenAnswer(invocation -> null);

            when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(billRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(bill));
            when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
                Transaction savedTransaction = invocation.getArgument(0);
                savedTransaction.setTags(new ArrayList<>());
                return savedTransaction;
            });
            when(transactionMapper
                    .toTransactionDto(any(Transaction.class)))
                    .thenReturn(updatedResponse);

            TransactionDtoResponse result = transactionService
                    .updateTransaction(1L, transactionDtoRequest);

            assertNotNull(result);
            assertEquals(updatedResponse, result);
            assertEquals(700.0, bill.getBalance());
            verify(transactionRepository).findById(1L);
            verify(userRepository).findById(1L);
            verify(billRepository).findByIdAndUserId(1L, 1L);
            verify(tagRepository, never()).findAllById(anyList());
            verify(userRepository).save(user);
            verify(billRepository).save(bill);
            verify(transactionRepository).save(any(Transaction.class));
            verify(cache).updateTransaction(1L, updatedResponse);
        }
    }

    @Test
    void updateTransaction_tagUserMismatch_throwsInvalidInputException() {
        Tag wrongTag = new Tag();
        wrongTag.setId(1L);
        User wrongUser = new User();
        wrongUser.setId(2L); // Другой пользователь
        wrongTag.setUser(wrongUser);

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(billRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(bill));
        when(tagRepository.findAllById(List.of(1L))).thenReturn(List.of(wrongTag));

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> transactionService.updateTransaction(1L, transactionDtoRequest));

        assertEquals("Один или несколько тегов не найдены или не принадлежат пользователю",
                exception.getMessage());
        verify(transactionRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(billRepository).findByIdAndUserId(1L, 1L);
        verify(tagRepository).findAllById(List.of(1L));
    }

    @Test
    void updateTransaction_tagNotFound_throwsInvalidInputException() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(billRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(bill));
        when(tagRepository.findAllById(List.of(1L))).thenReturn(Collections.emptyList());

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> transactionService.updateTransaction(1L, transactionDtoRequest));

        assertEquals("Один или несколько тегов не найдены или не принадлежат пользователю",
                exception.getMessage());
        verify(transactionRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(billRepository).findByIdAndUserId(1L, 1L);
        verify(tagRepository).findAllById(List.of(1L));
    }

    @Test
    void updateTransaction_insufficientFunds_throwsInvalidInputException() {
        transactionDtoRequest.setType(false); // Расход
        transactionDtoRequest.setAmount(2000.0); // Больше, чем баланс (1000)

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(billRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(bill));

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> transactionService.updateTransaction(1L, transactionDtoRequest));

        assertEquals("Недостаточно средств на счете для новой суммы транзакции",
                exception.getMessage());
        verify(transactionRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(billRepository).findByIdAndUserId(1L, 1L);
    }

    @Test
    void updateTransaction_amountTooLarge_throwsInvalidInputException() {
        transactionDtoRequest.setAmount(1_000_001.0);

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> transactionService.updateTransaction(1L, transactionDtoRequest));

        assertEquals("Сумма транзакции не может превышать 1,000,000", exception.getMessage());
        verify(transactionRepository).findById(1L);
    }

    @Test
    void updateTransaction_userNotFound_throwsNotFoundException() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> transactionService.updateTransaction(1L, transactionDtoRequest));

        assertEquals("Пользователь с id 1 не найден", exception.getMessage());
        verify(transactionRepository).findById(1L);
        verify(userRepository).findById(1L);
    }

    @Test
    void updateTransaction_billNotFound_throwsNotFoundException() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(billRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> transactionService.updateTransaction(1L, transactionDtoRequest));

        assertEquals("Счет с данным id 1 не найден или не принадлежит пользователю",
                exception.getMessage());
        verify(transactionRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(billRepository).findByIdAndUserId(1L, 1L);
    }

    @Test
    void updateTransaction_userSaveThrowsException() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(billRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(bill));
        when(tagRepository.findAllById(List.of(1L))).thenReturn(List.of(tag));
        when(userRepository.save(user)).thenThrow(new DataAccessException("Database error") {});

        DataAccessException exception = assertThrows(DataAccessException.class,
                () -> transactionService.updateTransaction(1L, transactionDtoRequest));

        assertEquals("Database error", exception.getMessage());
        verify(transactionRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(billRepository).findByIdAndUserId(1L, 1L);
        verify(tagRepository).findAllById(List.of(1L));
        verify(userRepository).save(user);
    }

    @Test
    void updateTransaction_billSaveThrowsException() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(billRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(bill));
        when(tagRepository.findAllById(List.of(1L))).thenReturn(List.of(tag));
        when(userRepository.save(user)).thenReturn(user);
        when(billRepository.save(bill)).thenThrow(new DataAccessException("Database error") {});

        DataAccessException exception = assertThrows(DataAccessException.class,
                () -> transactionService.updateTransaction(1L, transactionDtoRequest));

        assertEquals("Database error", exception.getMessage());
        verify(transactionRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(billRepository).findByIdAndUserId(1L, 1L);
        verify(tagRepository).findAllById(List.of(1L));
        verify(userRepository).save(user);
        verify(billRepository).save(bill);
    }

    @Test
    void updateTransaction_transactionSaveThrowsException() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(billRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(bill));
        when(tagRepository.findAllById(List.of(1L))).thenReturn(List.of(tag));
        when(userRepository.save(user)).thenReturn(user);
        when(billRepository.save(bill)).thenReturn(bill);
        when(transactionRepository
                .save(transaction))
                .thenThrow(new DataAccessException("Database error") {});

        DataAccessException exception = assertThrows(DataAccessException.class,
                () -> transactionService.updateTransaction(1L, transactionDtoRequest));

        assertEquals("Database error", exception.getMessage());
        verify(transactionRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(billRepository).findByIdAndUserId(1L, 1L);
        verify(tagRepository).findAllById(List.of(1L));
        verify(userRepository).save(user);
        verify(billRepository).save(bill);
        verify(transactionRepository).save(transaction);
    }

    @Test
    void updateTransaction_hibernateInitializeThrowsException() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(billRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(bill));
        when(tagRepository.findAllById(List.of(1L))).thenReturn(List.of(tag));
        when(userRepository.save(user)).thenReturn(user);
        when(billRepository.save(bill)).thenReturn(bill);
        when(transactionRepository.save(transaction)).thenReturn(transaction);

        try (MockedStatic<Hibernate> mockHibernate = mockStatic(Hibernate.class)) {
            mockHibernate.when(() -> Hibernate.initialize(any()))
                    .thenThrow(new RuntimeException("Hibernate initialization failed"));

            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> transactionService.updateTransaction(1L, transactionDtoRequest));

            assertEquals("Hibernate initialization failed", exception.getMessage());
            verify(transactionRepository).findById(1L);
            verify(userRepository).findById(1L);
            verify(billRepository).findByIdAndUserId(1L, 1L);
            verify(tagRepository).findAllById(List.of(1L));
            verify(userRepository).save(user);
            verify(billRepository).save(bill);
            verify(transactionRepository).save(transaction);
        }
    }

    @Test
    void updateTransaction_cacheUpdateThrowsException() {
        Transaction updatedTransaction = new Transaction();
        updatedTransaction.setId(1L);
        updatedTransaction.setName(transactionDtoRequest.getName());
        updatedTransaction.setDescription(transactionDtoRequest.getDescription());
        updatedTransaction.setType(transactionDtoRequest.isType());
        updatedTransaction.setAmount(transactionDtoRequest.getAmount());
        updatedTransaction.setDate(transactionDtoRequest.getDate());
        updatedTransaction.setUser(user);
        updatedTransaction.setBill(bill);
        updatedTransaction.setTags(new ArrayList<>(List.of(tag)));

        TransactionDtoResponse updatedResponse = new TransactionDtoResponse();
        updatedResponse.setId(1L);
        updatedResponse.setName(transactionDtoRequest.getName());
        updatedResponse.setDescription(transactionDtoRequest.getDescription());
        updatedResponse.setType(transactionDtoRequest.isType());
        updatedResponse.setAmount(transactionDtoRequest.getAmount());
        updatedResponse.setDate(transactionDtoRequest.getDate());
        updatedResponse.setUserId(1L);
        updatedResponse.setBillId(1L);

        try (MockedStatic<Hibernate> mockHibernate = mockStatic(Hibernate.class)) {
            mockHibernate.when(() -> Hibernate.initialize(any())).thenAnswer(invocation -> null);

            when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(billRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(bill));
            when(tagRepository.findAllById(List.of(1L))).thenReturn(List.of(tag));
            when(transactionRepository.save(transaction)).thenReturn(updatedTransaction);
            when(transactionMapper
                    .toTransactionDto(updatedTransaction))
                    .thenReturn(updatedResponse);
            doThrow(new RuntimeException("Cache update failed")).when(cache)
                    .updateTransaction(1L, updatedResponse);

            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> transactionService.updateTransaction(1L, transactionDtoRequest));

            assertEquals("Cache update failed", exception.getMessage());
            verify(transactionRepository).findById(1L);
            verify(userRepository).findById(1L);
            verify(billRepository).findByIdAndUserId(1L, 1L);
            verify(tagRepository).findAllById(List.of(1L));
            verify(userRepository).save(user);
            verify(billRepository).save(bill);
            verify(transactionRepository).save(transaction);
            verify(cache).updateTransaction(1L, updatedResponse);
        }
    }

    @Test
    void updateTransaction_transactionNotFound_throwsNotFoundException() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> transactionService.updateTransaction(1L, transactionDtoRequest));

        assertEquals("Транзакция с id 1 не найдена", exception.getMessage());
        verify(transactionRepository).findById(1L);
    }

    @Test
    void updateTransaction_userMismatch_throwsInvalidInputException() {
        transactionDtoRequest.setUserId(2L);

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> transactionService.updateTransaction(1L, transactionDtoRequest));

        assertEquals("Нельзя изменить пользователя транзакции, используй: 1",
                exception.getMessage());
        verify(transactionRepository).findById(1L);
    }

    @Test
    void updateTransaction_billMismatch_throwsInvalidInputException() {
        transactionDtoRequest.setBillId(2L);

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> transactionService.updateTransaction(1L, transactionDtoRequest));

        assertEquals("Нельзя изменить счёт транзакции, используй: 1", exception.getMessage());
        verify(transactionRepository).findById(1L);
    }


    @Test
    void deleteTransaction_expense_success() {
        bill.setBalance(1000.0); // Сбрасываем баланс до исходного значения

        // Создаём новый объект transaction
        Transaction expenseTransaction = new Transaction();
        expenseTransaction.setId(1L);
        expenseTransaction.setName("Test Transaction");
        expenseTransaction.setDescription("Test Description");
        expenseTransaction.setType(false); // Транзакция — расход
        expenseTransaction.setAmount(100.0);
        expenseTransaction.setDate(LocalDate.now());
        expenseTransaction.setUser(user);
        expenseTransaction.setBill(bill);
        expenseTransaction.setTags(new ArrayList<>(List.of(tag)));

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(expenseTransaction));
        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));

        transactionService.deleteTransaction(1L);

        assertEquals(1100.0, bill.getBalance()); // 1000 + 100 (отмена расхода)
        verify(transactionRepository).findById(1L);
        verify(billRepository).findById(1L);
        verify(transactionRepository).delete(expenseTransaction);
        verify(cache).removeTransaction(1L, 1L);
    }

    @Test
    void deleteTransaction_income_success() {
        bill.setBalance(1000.0); // Сбрасываем баланс до исходного значения

        // Создаём новый объект transaction
        Transaction incomeTransaction = new Transaction();
        incomeTransaction.setId(1L);
        incomeTransaction.setName("Test Transaction");
        incomeTransaction.setDescription("Test Description");
        incomeTransaction.setType(true); // Транзакция — доход
        incomeTransaction.setAmount(100.0);
        incomeTransaction.setDate(LocalDate.now());
        incomeTransaction.setUser(user);
        incomeTransaction.setBill(bill);
        incomeTransaction.setTags(new ArrayList<>(List.of(tag)));

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(incomeTransaction));
        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));

        transactionService.deleteTransaction(1L);

        assertEquals(900.0, bill.getBalance()); // 1000 - 100 (доход)
        verify(transactionRepository).findById(1L);
        verify(billRepository).findById(1L);
        verify(transactionRepository).delete(incomeTransaction);
        verify(cache).removeTransaction(1L, 1L);
    }

    @Test
    void deleteTransaction_notFound_throwsNotFoundException() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> transactionService.deleteTransaction(1L));

        assertEquals("Транзакция с id 1 не найдена", exception.getMessage());
        verify(transactionRepository).findById(1L);
        verify(billRepository, never()).findById(anyLong());
    }

    @Test
    void deleteTransaction_billNotFound_throwsNotFoundException() {
        Transaction testTransaction = new Transaction(); // Изменено с transaction
        testTransaction.setId(1L);
        testTransaction.setName("Test Transaction");
        testTransaction.setDescription("Test Description");
        testTransaction.setType(true); // Доход
        testTransaction.setAmount(100.0);
        testTransaction.setDate(LocalDate.now());
        testTransaction.setUser(user);

        Bill testBill = new Bill(); // Изменено с bill
        testBill.setId(1L);
        testTransaction.setBill(testBill);

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));
        when(billRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> transactionService.deleteTransaction(1L));

        assertEquals(String.format("Счет с id %d не найден", 1L), exception.getMessage());
        verify(transactionRepository).findById(1L);
        verify(billRepository).findById(1L);
        verify(transactionRepository, never()).delete(any());
        verify(cache, never()).removeTransaction(anyLong(), anyLong());
    }

    @Test
    void deleteTransaction_cacheRemoveThrowsException() {
        bill.setBalance(1000.0);
        Transaction testTransaction = new Transaction(); // Изменено с transaction
        testTransaction.setId(1L);
        testTransaction.setName("Test Transaction");
        testTransaction.setDescription("Test Description");
        testTransaction.setType(true); // Доход
        testTransaction.setAmount(100.0);
        testTransaction.setDate(LocalDate.now());
        testTransaction.setUser(user);
        testTransaction.setBill(bill);
        testTransaction.setTags(new ArrayList<>(List.of(tag)));

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));
        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));
        doThrow(new RuntimeException("Cache remove failed")).when(cache).removeTransaction(1L, 1L);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> transactionService.deleteTransaction(1L));

        assertEquals("Cache remove failed", exception.getMessage());
        assertEquals(900.0, bill.getBalance()); // Баланс всё равно уменьшился (отмена дохода)
        verify(transactionRepository).findById(1L);
        verify(billRepository).findById(1L);
        verify(transactionRepository).delete(testTransaction);
        verify(cache).removeTransaction(1L, 1L);
    }
}