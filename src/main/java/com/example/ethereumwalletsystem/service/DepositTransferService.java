package com.example.ethereumwalletsystem.service;

import com.example.ethereumwalletsystem.constant.Constants;
import com.example.ethereumwalletsystem.constant.Status;
import com.example.ethereumwalletsystem.entity.Currency;
import com.example.ethereumwalletsystem.entity.DepositTransfer;
import com.example.ethereumwalletsystem.entity.Transaction;
import com.example.ethereumwalletsystem.entity.Wallet;
import com.example.ethereumwalletsystem.payload.DepositTransferMessage;
import com.example.ethereumwalletsystem.payload.TransferCheckMessage;
import com.example.ethereumwalletsystem.repository.DepositTransferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import javax.annotation.PostConstruct;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class DepositTransferService {
    private final DepositTransferRepository depositTransferRepository;
    private final TransactionService transactionService;
    private final CurrencyService currencyService;
    private final EthereumNodeService ethereumNodeService;
    private final WalletService walletService;
    private final MessageService messageService;

    @Value("${app.blocks}")
    private long blocks;

    @Scheduled(fixedDelay = 10000)
    public void getDepositTransfers() {
        Currency currency = currencyService.getDefaultCurrency();

        long lastBlockNumber = currency.getLastBlockNumber();

        DepositTransferMessage depositTransferMessage = ethereumNodeService.getDepositTransfers(lastBlockNumber, blocks);
        List<DepositTransferMessage.Transfer> transfers = depositTransferMessage.getTransfers();

        Set<String> addresses = walletService.getWalletAddresses();

        long latestBlockNumber = depositTransferMessage.getLatestBlockNumber();

        for (DepositTransferMessage.Transfer transfer : transfers) {
            String address = transfer.getAddress();

            if (address != null && addresses.contains(address)) {
                String hash = transfer.getHash();
                BigInteger amount = transfer.getAmount();
                String blockHash = transfer.getBlockHash();
                long blockNumber = transfer.getBlockNumber().longValueExact();
                int confirmation = (int) (latestBlockNumber - blockNumber);

                createNewDeposit(currency, address, hash, amount, blockHash, blockNumber, confirmation);
            }
        }

        currency.setLastBlockNumber(depositTransferMessage.getLastBlockNumber());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createNewDeposit(Currency currency, String address, String hash, BigInteger amount, String blockHash, long blockNumber, int confirmation) {
        Wallet wallet = walletService.findByAddressForUpdate(address);
        wallet.setBalance(wallet.getBalance().add(amount));
        DepositTransfer depositTransfer = createDepositTransferEntity(wallet, currency, hash, blockHash, blockNumber, confirmation, amount);
        Transaction transaction = depositTransfer.getTransaction();

        TransferCheckMessage transferCheckMessage = new TransferCheckMessage();
        transferCheckMessage.setTransferId(depositTransfer.getId());
        transferCheckMessage.setTransactionId(transaction.getId());
        transferCheckMessage.setTransactionHash(transaction.getHash());
        messageService.queueDelay("ethereum-wallet.check.deposit", transferCheckMessage, 30);
    }

    @JmsListener(
        destination = "ethereum-wallet.check.deposit",
        containerFactory = "queueListenerFactory"
    )
    @Transactional
    public void checkDepositStatus(TransferCheckMessage transferCheckMessage) {
        Long transferId = transferCheckMessage.getTransferId();
        String hash = transferCheckMessage.getTransactionHash();

        TransactionReceipt transactionReceipt = ethereumNodeService.getTransactionReceipt(hash);
        BigInteger latestBlockNumber = ethereumNodeService.getLatestBlockNumber();

        if (transactionReceipt == null) {
            messageService.queueDelay("ethereum-wallet.check.deposit", transferCheckMessage, 30);
            log.warn("Deposit check unavailable, id : {}, hash : {}", transferId, hash);
            return;
        }

        long confirmations = latestBlockNumber.subtract(transactionReceipt.getBlockNumber()).longValueExact();
        if (confirmations < Constants.MINIMUM_CONFIRMATIONS) {
            messageService.queueDelay("ethereum-wallet.check.deposit", transferCheckMessage, 30);
            log.info("Deposit not confirmed, id : {}, hash : {}", transferId, hash);
            return;
        }

        Optional<DepositTransfer> depositTransferOptional = depositTransferRepository.findByIdWithTransactionAndWalletForUpdate(transferId);
        if (depositTransferOptional.isEmpty()) {
            log.error("Deposit Not Found, id : {}", transferId);
            return;
        }
        DepositTransfer depositTransfer = depositTransferOptional.get();

        if (depositTransfer.getStatus().equals(Status.confirmed)) {
            log.info("Deposit already confirmed, id : {}, hash : {}", transferId, hash);
            return;
        }

        Transaction transaction = depositTransfer.getTransaction();

        if (!transactionReceipt.isStatusOK()) {
            transactionService.updateTransaction(transaction, transactionReceipt, latestBlockNumber);
            failDepositTransfer(depositTransfer);
            log.info("Deposit failed, id : {}, hash : {}", transferId, hash);
            return;
        }

        transactionService.updateTransaction(transaction, transactionReceipt, latestBlockNumber);
        confirmDepositTransfer(depositTransfer);
        log.info("Deposit confirmed, id : {}, hash : {}", transferId, hash);
    }

    @Transactional
    public void confirmDepositTransfer(DepositTransfer depositTransfer) {
        depositTransfer.setStatus(Status.confirmed);
        BigInteger amount = depositTransfer.getAmount();
        Wallet wallet = depositTransfer.getWallet();
        wallet.setConfirmedBalance(wallet.getConfirmedBalance().add(amount));
        wallet.setSpendableBalance(wallet.getSpendableBalance().add(amount));
    }

    @Transactional
    public void failDepositTransfer(DepositTransfer depositTransfer) {
        depositTransfer.setStatus(Status.failed);
        BigInteger amount = depositTransfer.getAmount();
        Wallet wallet = depositTransfer.getWallet();
        wallet.setBalance(wallet.getBalance().subtract(amount));
    }

    @Transactional
    public DepositTransfer createDepositTransferEntity(Wallet wallet, Currency currency, String hash, String blockHash, Long blockNumber, int confirmation, BigInteger amount) {
        Transaction transaction = transactionService.createTransactionEntity(hash);
        transaction = transactionService.updateTransaction(transaction, blockHash, blockNumber, confirmation);
        DepositTransfer depositTransfer = new DepositTransfer(amount, wallet, transaction, currency);

        return depositTransferRepository.save(depositTransfer);
    }

    @PostConstruct
    void handlePendingDepositTransfers() {
        List<DepositTransfer> pendingDeposits = depositTransferRepository.findAllByStatusEquals(Status.pending);

        for (DepositTransfer depositTransfer : pendingDeposits) {
            Transaction transaction = depositTransfer.getTransaction();
            log.info("Rescheduling depositTransfer : {}", depositTransfer);
            TransferCheckMessage transferCheckMessage = new TransferCheckMessage();
            transferCheckMessage.setTransferId(depositTransfer.getId());
            transferCheckMessage.setTransactionId(transaction.getId());
            transferCheckMessage.setTransactionHash(transaction.getHash());
            messageService.queue("ethereum-wallet.check.deposit", transferCheckMessage);
        }
    }
}
