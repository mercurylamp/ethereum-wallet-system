package com.example.ethereumwalletsystem.service;

import com.example.ethereumwalletsystem.entity.Transaction;
import com.example.ethereumwalletsystem.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigInteger;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {
    private final TransactionRepository transactionRepository;

    @Transactional
    public Transaction findByIdForUpdate(Long id) {
        return transactionRepository.findByIdForUpdate(id).orElseThrow();
    }

    @Transactional
    public Transaction createTransactionEntity(String hash) {
        Optional<Transaction> transaction = transactionRepository.findByHash(hash);

        if (transaction.isPresent()) {
            return transaction.get();
        }

        Transaction newTransaction = new Transaction(hash);
        return transactionRepository.save(newTransaction);
    }

    @Transactional
    public Transaction updateTransaction(Transaction transaction, String blockHash, Long blockNumber, int confirmation) {
        transaction.setConfirmation(confirmation);
        transaction.setBlockHash(blockHash);
        transaction.setBlockNumber(blockNumber);
        transactionRepository.save(transaction);
        return transaction;
    }

    @Transactional
    public void updateTransaction(Transaction transaction, TransactionReceipt transactionReceipt, BigInteger latestBlockNumber) {
        String blockHash = transactionReceipt.getBlockHash();

        if (!blockHash.equalsIgnoreCase(transaction.getBlockHash())) {
            log.warn("Chain reorg detected, hash : {}", transaction.getHash());
        }

        BigInteger blockNumber = transactionReceipt.getBlockNumber();
        int confirmation = latestBlockNumber.subtract(blockNumber).intValueExact();
        transaction.setConfirmation(confirmation);
        transaction.setBlockHash(blockHash);
        transaction.setBlockNumber(blockNumber.longValueExact());
        transactionRepository.save(transaction);
    }
}
