package com.example.financery.repository;

import com.example.financery.model.Bill;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface BillRepository extends JpaRepository<Bill, Long> {

    @Query(value = "SELECT * FROM bill_table WHERE user_id = ?1", nativeQuery = true)
    List<Bill> findByUser(long userId);

    @Query("SELECT b FROM Bill b WHERE b.id = :billId AND b.user.id = :userId")
    Optional<Bill> findByIdAndUserId(@Param("billId") Long billId, @Param("userId") Long userId);
}
