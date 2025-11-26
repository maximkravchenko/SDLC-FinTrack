package com.example.financery.repository;

import com.example.financery.model.Tag;
import com.example.financery.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Long> {

    @Query(value = "SELECT * FROM tag_table WHERE user_id = ?1", nativeQuery = true)
    List<Tag> findByUser(long userId);

    @Query(value = "SELECT t.* FROM tag_table t "
            + "JOIN transaction_tag tt ON t.id = tt.tag_id "
            + "WHERE tt.transaction_id = ?1", nativeQuery = true)
    List<Tag> findByTransaction(long transactionId);

    @Query("SELECT t FROM Transaction t JOIN t.tags tag WHERE tag.id = :tagId")
    List<Transaction> findTransactionsByTag(@Param("tagId") Long tagId);

    @Modifying
    @Query(value = "DELETE FROM transaction_tag WHERE tag_id = :tagId", nativeQuery = true)
    void removeTagFromTransactions(Long tagId);

}
