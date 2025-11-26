package com.example.financery.utils;

import com.example.financery.dto.TransactionDtoResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class InMemoryCache {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryCache.class);

    private final int maxSize;
    private final Map<Long, List<TransactionDtoResponse>> cache;

    public InMemoryCache() {
        this.maxSize = 3;
        this.cache = new LinkedHashMap<Long, List<TransactionDtoResponse>>(maxSize,
                0.75f,
                true) {
            @Override
            protected boolean removeEldestEntry(
                    Map.Entry<Long,
                            List<TransactionDtoResponse>> eldest) {
                if (size() > maxSize) {
                    logger.info(
                            "Удаление кеша для пользователя используемого давно userId: {}",
                            eldest.getKey());
                    return true;
                }
                return false;
            }
        };
        logger.info("LRU Кеш инициализирован с максимальным размером: {}", maxSize);
    }


    public List<TransactionDtoResponse> get(Long userId) {
        List<TransactionDtoResponse> transactions = cache.get(userId);
        logger.info("Кеш {} для пользователя: {}",
                transactions != null ? "найден" : "не найден", userId);
        return transactions;
    }

    public void put(Long userId, List<TransactionDtoResponse> transactions) {
        cache.put(userId, transactions);
        logger.info("Закешированы транзакции для пользователя: {}, размером: {}",
                userId, transactions.size());
    }

    public void updateTransaction(Long userId, TransactionDtoResponse transaction) {
        List<TransactionDtoResponse> transactions = cache.get(userId);
        if (transactions != null) {
            transactions.removeIf(
                    t -> t.getId() == transaction.getId());
            transactions.add(transaction);
            cache.put(userId, transactions);
            logger.info("Обновление транзакции {} в кеше для пользователя: {}",
                    transaction.getId(), userId);
        }
    }

    public void removeTransaction(Long userId, Long transactionId) {
        List<TransactionDtoResponse> transactions = cache.get(userId);
        if (transactions != null) {
            transactions.removeIf(t -> t.getId() == transactionId);
            cache.put(userId, transactions);
            logger.info("Удаление транзакции {} из кеша пользователя: {}",
                    transactionId, userId);
        }
    }

    public void clearForUser(Long userId) {
        cache.remove(userId);
        logger.info("Очистка кеша для пользователя: {}", userId);
    }

    public void clear() {
        cache.clear();
        logger.info("Очистка всего кеша");
    }
}