package com.example.financery.repository;

import com.example.financery.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query(""
            + "SELECT t "
            + "FROM Transaction t "
            + "JOIN FETCH t.user u "
            + "JOIN FETCH t.bill b "
            + "LEFT JOIN FETCH t.tags "
            + "WHERE t.user.id "
            + "= :userId")

    List<Transaction> findByUserId(@Param("userId") Long userId);

    @Query(value = "SELECT * FROM transaction_table WHERE user_id = ?1", nativeQuery = true)
    List<Transaction> findByUser(long userId);

    @Query(value = "SELECT * FROM transaction_table WHERE bill_id = ?1", nativeQuery = true)
    List<Transaction> findByBill(long billId);
}
