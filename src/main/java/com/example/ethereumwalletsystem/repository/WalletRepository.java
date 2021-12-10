package com.example.ethereumwalletsystem.repository;

import com.example.ethereumwalletsystem.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    @Query("SELECT wallet FROM Wallet wallet JOIN FETCH wallet.currency WHERE wallet.uuid = :uuid AND wallet.path = 0")
    Optional<Wallet> findMasterWalletByUuid(UUID uuid);

    @Query("SELECT wallet FROM Wallet wallet JOIN FETCH wallet.currency WHERE wallet.uuid = :uuid AND wallet.path <> 0")
    List<Wallet> findDepositWalletByUuid(UUID uuid);

    Optional<Wallet> findFirstByUuidOrderByPathDesc(UUID uuid);

    Optional<Wallet> findByUuidAndAddress(UUID uuid, String address);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT wallet FROM Wallet wallet WHERE wallet.address = :address")
    Optional<Wallet> findByAddressForUpdate(String address);

    @Query("SELECT distinct wallet.address FROM Wallet wallet")
    Set<String> getWalletAddresses();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT wallet FROM Wallet wallet WHERE wallet.uuid = :uuid AND wallet.path = :path")
    Optional<Wallet> findByUuidAndPathForUpdate(UUID uuid, int path);
}