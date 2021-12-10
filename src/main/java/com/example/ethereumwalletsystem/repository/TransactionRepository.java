package com.example.ethereumwalletsystem.repository;

import com.example.ethereumwalletsystem.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByHash(String hash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT transaction FROM Transaction transaction WHERE transaction.id = :id")
    Optional<Transaction> findByIdForUpdate(Long id);
}
