package com.example.ethereumwalletsystem.repository;

import com.example.ethereumwalletsystem.constant.Status;
import com.example.ethereumwalletsystem.entity.WithdrawalTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface WithdrawalTransferRepository extends JpaRepository<WithdrawalTransfer, Long> {
    @Query("SELECT transfer FROM WithdrawalTransfer transfer JOIN FETCH transfer.transaction WHERE transfer.status = :status")
    List<WithdrawalTransfer> findAllByStatusEquals(Status status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT transfer FROM WithdrawalTransfer transfer JOIN FETCH transfer.transaction JOIN FETCH transfer.wallet WHERE transfer.id = :id")
    Optional<WithdrawalTransfer> findByIdWithTransactionAndWalletForUpdate(Long id);
}
