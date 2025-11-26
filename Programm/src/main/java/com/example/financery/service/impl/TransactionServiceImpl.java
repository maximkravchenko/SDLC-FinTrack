package com.example.financery.service.impl;

import com.example.financery.dto.BillDtoResponse;
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
import com.example.financery.service.TransactionService;
import com.example.financery.utils.InMemoryCache;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    public static final String TRANSACTION_WITH_ID_NOT_FOUND = "Транзакция с id %d не найдена";
    public static final String USER_WITH_ID_NOT_FOUND = "Пользователь с id %d не найден";
    public static final String BILL_WITH_ID_NOT_FOUND = "Счет с id %d не найден";

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final UserRepository userRepository;
    private final BillRepository billRepository;
    private final TagRepository tagRepository;

    private final InMemoryCache cache;

    private static final Logger log = LoggerFactory.getLogger(TransactionServiceImpl.class);

    @Override
    @Transactional
    public List<TransactionDtoResponse> getAllTransactions() {
        List<TransactionDtoResponse> transactionsResponse = new ArrayList<>();
        transactionRepository.findAll().forEach(
                transaction -> {
                    Hibernate.initialize(transaction.getTags());
                    transactionsResponse.add(transactionMapper.toTransactionDto(transaction));
                });
        return transactionsResponse;
    }

    @Override
    @Transactional
    public TransactionDtoResponse getTransactionById(long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new NotFoundException(
                        String.format(TRANSACTION_WITH_ID_NOT_FOUND, transactionId)));
        Hibernate.initialize(transaction.getTags());
        return transactionMapper.toTransactionDto(transaction);
    }

    @Override
    @Transactional
    public List<TransactionDtoResponse> getTransactionsByUserId(long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(String.format(USER_WITH_ID_NOT_FOUND, userId));
        }

        // Проверяем, есть ли в кэше
        List<TransactionDtoResponse> cachedTransactions = cache.get(userId);
        if (cachedTransactions != null) {
            return cachedTransactions;
        }

        List<Transaction> transactions = transactionRepository.findByUserId(userId);
        // Преобразуем в изменяемый список
        List<TransactionDtoResponse> transactionsResponse = new ArrayList<>(
                transactions.stream()
                        .map(transactionMapper::toTransactionDto)
                        .toList()
        );

        log.info("Сопоставлено {} транзакций для пользователя: {}",
                transactionsResponse.size(), userId);
        cache.put(userId, transactionsResponse);
        return transactionsResponse;
    }

    @Override
    @Transactional
    public List<TransactionDtoResponse> getTransactionsByBillId(long billId) {
        billRepository.findById(billId)
                .orElseThrow(() -> new NotFoundException(
                        String.format(BILL_WITH_ID_NOT_FOUND, billId)));

        List<Transaction> transactions = transactionRepository.findByBill(billId);
        List<TransactionDtoResponse> transactionsResponse = new ArrayList<>();

        transactions.forEach(
                transaction -> {
                    Hibernate.initialize(transaction.getTags());
                    transactionsResponse.add(transactionMapper.toTransactionDto(transaction));
                });
        return transactionsResponse;
    }

    @Override
    @Transactional
    public TransactionDtoResponse createTransaction(TransactionDtoRequest transactionDto) {
        // Проверяем сумму транзакции перед обращением к репозиториям
        if (transactionDto.getAmount() > 1_000_000) {
            throw new InvalidInputException("Сумма транзакции не может превышать 1,000,000");
        }

        User user = userRepository.findById(transactionDto.getUserId())
                .orElseThrow(() -> new NotFoundException(
                        String.format(USER_WITH_ID_NOT_FOUND, transactionDto.getUserId())));

        Bill bill = billRepository
                .findByIdAndUserId(
                        transactionDto.getBillId(),
                        transactionDto.getUserId())
                .orElseThrow(() -> new NotFoundException(
                        "Счет с id "
                                + transactionDto.getBillId()
                                + " не найден или не принадлежит пользователю"));

        Transaction transaction = TransactionMapper.toTransaction(transactionDto);

        if (!transaction.isType() && transaction.getAmount() > bill.getBalance()) {
            throw new InvalidInputException("Недостаточно средств на счете для суммы транзакции");
        }

        if (transaction.isType()) {
            bill.addAmount(transaction.getAmount());
        } else {
            bill.subtractAmount(transaction.getAmount());
        }

        transaction.setUser(user);
        transaction.setBill(bill);

        if (transactionDto.getTagIds() != null && !transactionDto.getTagIds().isEmpty()) {
            List<Tag> tags = tagRepository.findAllById(transactionDto.getTagIds());
            if (tags.size() != transactionDto.getTagIds().size()) {
                throw new InvalidInputException(
                        "Один или несколько тегов по ID не найдены");
            }
            if (tags.stream().anyMatch(tag -> tag.getUser().getId() != user.getId())) {
                throw new InvalidInputException(
                        "Один или несколько тегов не найдены или не принадлежат пользователю");
            }
            transaction.setTags(tags);
        }

        transactionRepository.save(transaction);
        cache.updateTransaction(user.getId(), transactionMapper.toTransactionDto(transaction));
        return transactionMapper.toTransactionDto(transaction);
    }

    @Override
    @Transactional
    public TransactionDtoResponse updateTransaction(
            long transactionId, TransactionDtoRequest transactionDto) {
        Transaction existingTransaction = transactionRepository
                .findById(transactionId)
                .orElseThrow(() -> new NotFoundException(
                        String.format(TRANSACTION_WITH_ID_NOT_FOUND, transactionId)));

        if (transactionDto.getAmount() > 1_000_000) {
            throw new InvalidInputException("Сумма транзакции не может превышать 1,000,000");
        }

        if (!existingTransaction.getUser().getId().equals(transactionDto.getUserId())) {
            throw new InvalidInputException(
                    "Нельзя изменить пользователя транзакции, используй: "
                            + existingTransaction.getUser().getId());
        }
        if (!existingTransaction.getBill().getId().equals(transactionDto.getBillId())) {
            throw new InvalidInputException(
                    "Нельзя изменить счёт транзакции, используй: "
                            + existingTransaction.getBill().getId());
        }

        User user = userRepository.findById(transactionDto.getUserId())
                .orElseThrow(() -> new NotFoundException(
                        String.format(USER_WITH_ID_NOT_FOUND, transactionDto.getUserId())));
        Bill bill = billRepository
                .findByIdAndUserId(transactionDto.getBillId(), transactionDto.getUserId())
                .orElseThrow(() -> new NotFoundException(
                        "Счет с данным id " + transactionDto.getBillId()
                                + " не найден или не принадлежит пользователю"));

        double oldAmount = existingTransaction.getAmount();
        double newAmount = transactionDto.getAmount();
        boolean oldType = existingTransaction.isType();
        boolean newType = transactionDto.isType();

        if (!newType && newAmount > bill.getBalance()) {
            throw new InvalidInputException(
                    "Недостаточно средств на счете для новой суммы транзакции");
        }

        existingTransaction.setName(transactionDto.getName());
        existingTransaction.setDescription(transactionDto.getDescription());
        existingTransaction.setType(newType);
        existingTransaction.setAmount(newAmount);
        existingTransaction.setDate(transactionDto.getDate());

        updateTransactionTags(existingTransaction, transactionDto, user); // Новая функция для тегов

        // Обновление баланса одной операцией
        double balanceAdjustment = 0;
        if (!oldType) {
            balanceAdjustment += oldAmount; // Возвращаем старую сумму, если был расход
        } else {
            balanceAdjustment -= oldAmount; // Убираем старую сумму, если был доход
        }
        if (newType) {
            balanceAdjustment += newAmount; // Добавляем новую сумму, если новый тип доход
        } else {
            balanceAdjustment -= newAmount; // Вычитаем новую сумму, если новый тип расход
        }
        bill.addAmount(balanceAdjustment);

        userRepository.save(user);
        billRepository.save(bill);
        transactionRepository.save(existingTransaction);
        Hibernate.initialize(existingTransaction.getTags());
        cache.updateTransaction(user.getId(),
                transactionMapper.toTransactionDto(existingTransaction));
        return transactionMapper.toTransactionDto(existingTransaction);
    }

    private void updateTransactionTags(
            Transaction existingTransaction,
            TransactionDtoRequest transactionDto,
            User user) {
        if (transactionDto.getTagIds() == null) {
            return;
        }
        List<Tag> tags = transactionDto.getTagIds().isEmpty()
                ? new ArrayList<>()
                : tagRepository.findAllById(transactionDto.getTagIds());
        if (!transactionDto.getTagIds()
                .isEmpty()
                && tags.size()
                != transactionDto.getTagIds().size()) {
            throw new InvalidInputException(
                    "Один или несколько тегов не найдены или не принадлежат пользователю");
        }
        if (tags.stream().anyMatch(tag -> tag.getUser().getId() != user.getId())) {
            throw new InvalidInputException(
                    "Один или несколько тегов не найдены или не принадлежат пользователю");
        }
        existingTransaction.setTags(tags);
    }

    @Override
    @Transactional
    public void deleteTransaction(long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new NotFoundException(
                        String.format(TRANSACTION_WITH_ID_NOT_FOUND, transactionId)));

        Bill bill = billRepository.findById(transaction.getBill().getId())
                .orElseThrow(() -> new NotFoundException(
                        String.format(BILL_WITH_ID_NOT_FOUND, transaction.getBill().getId())));

        // Исправляем логику: меняем местами addAmount и subtractAmount
        if (transaction.isType()) { // type == true (доход)
            bill.subtractAmount(transaction.getAmount()); // Уменьшаем баланс (отмена дохода)
        } else { // type == false (расход)
            bill.addAmount(transaction.getAmount()); // Увеличиваем баланс (отмена расхода)
        }

        Long userId = transaction.getUser().getId();
        transactionRepository.delete(transaction);
        cache.removeTransaction(userId, transactionId);
    }
}
