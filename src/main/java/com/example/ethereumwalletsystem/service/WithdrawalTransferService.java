package com.example.ethereumwalletsystem.service;

import com.example.ethereumwalletsystem.constant.Constants;
import com.example.ethereumwalletsystem.constant.Status;
import com.example.ethereumwalletsystem.entity.Currency;
import com.example.ethereumwalletsystem.entity.Transaction;
import com.example.ethereumwalletsystem.entity.Wallet;
import com.example.ethereumwalletsystem.entity.WithdrawalTransfer;
import com.example.ethereumwalletsystem.payload.ShareRequest;
import com.example.ethereumwalletsystem.payload.TransferCheckMessage;
import com.example.ethereumwalletsystem.payload.WithdrawalRequest;
import com.example.ethereumwalletsystem.payload.WithdrawalResponse;
import com.example.ethereumwalletsystem.repository.WithdrawalTransferRepository;
import com.example.ethereumwalletsystem.util.SecretSharingUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import javax.annotation.PostConstruct;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WithdrawalTransferService {
    private final WithdrawalTransferRepository withdrawalTransferRepository;
    private final WalletService walletService;
    private final CurrencyService currencyService;
    private final TransactionService transactionService;
    private final EthereumNodeService ethereumNodeService;
    private final MessageService messageService;

    @Transactional
    public WithdrawalResponse createNewWithdrawalTransfer(UUID walletUuid, WithdrawalRequest request) {
        Currency currency = currencyService.getCurrencyBySymbol(request.getCurrency());

        String address = request.getAddress();
        BigInteger amount = request.getAmount();
        String share = request.getShare();
        Long gasPrice = request.getGasPrice();
        Long gasLimit = request.getGasLimit();

        BigInteger ethGasPrice;
        if (gasPrice == null) {
            ethGasPrice = ethereumNodeService.getGasPrice();
        } else {
            ethGasPrice = BigInteger.valueOf(gasPrice);
        }

        if (ethGasPrice == null) {
            // TODO error
            return null;
        }

        WithdrawalTransfer withdrawalTransfer = createWithdrawalTransferEntity(walletUuid, currency, address, amount, share, ethGasPrice, gasLimit, 0);

        if (withdrawalTransfer == null) {
            // TODO error
            return null;
        }

        return new WithdrawalResponse(withdrawalTransfer.getUuid(), withdrawalTransfer.getAmount(), withdrawalTransfer.getAddress(), withdrawalTransfer.getGasPrice(), withdrawalTransfer.getGasLimit(), withdrawalTransfer.getTransaction().getHash());
    }

    @Transactional
    public List<WithdrawalResponse> flush(UUID walletUuid, ShareRequest request) {
        Currency currency = currencyService.getDefaultCurrency();

        Wallet masterWallet = walletService.findMasterWalletByUuid(walletUuid);
        List<Wallet> depositWallets = walletService.findDepositWalletByUuid(walletUuid);

        List<WithdrawalResponse> response = new ArrayList<>();

        for (Wallet wallet : depositWallets) {
            BigInteger gasPrice = ethereumNodeService.getGasPrice();

            if (gasPrice == null) {
                // TODO error
                continue;
            }

            BigInteger fee = gasPrice.multiply(BigInteger.valueOf(21000L));
            BigInteger amount = wallet.getSpendableBalance().subtract(fee);

            if (amount.compareTo(BigInteger.ZERO) <= 0) {
                continue;
            }

            String masterAddress = masterWallet.getAddress();
            WithdrawalTransfer withdrawalTransfer = createWithdrawalTransferEntity(walletUuid, currency, masterAddress, amount, request.getShare(), gasPrice, 21000L, wallet.getPath());

            if (withdrawalTransfer == null)
                continue;

            response.add(new WithdrawalResponse(withdrawalTransfer.getUuid(), withdrawalTransfer.getAmount(), withdrawalTransfer.getAddress(), withdrawalTransfer.getGasPrice(), withdrawalTransfer.getGasLimit(), withdrawalTransfer.getTransaction().getHash()));
        }

        return response;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public WithdrawalTransfer createWithdrawalTransferEntity(UUID walletUuid, Currency currency, String address, BigInteger amount, String share, BigInteger gasPrice, Long gasLimit, int path) {
        BigInteger fee = gasPrice.multiply(BigInteger.valueOf(gasLimit));
        Wallet wallet = walletService.findByUuidAndPathForUpdate(walletUuid, path);
        long nonce = wallet.getNonce();
        BigInteger realSpendableBalance = wallet.getSpendableBalance().subtract(fee);

        if (realSpendableBalance.compareTo(amount) < 0) {
            // TODO 잔고 부족하면 에러
            return null;
        }

        String mnemonic = SecretSharingUtils.recover(List.of(share, wallet.getShare()));

        String txId = ethereumNodeService.sendEth(mnemonic, wallet.getPath(), address, amount, gasPrice, gasLimit, nonce);
        wallet.setNonce(wallet.getNonce() + 1);
        wallet.setSpendableBalance(wallet.getSpendableBalance().subtract(amount).subtract(fee));

        Transaction transaction = transactionService.createTransactionEntity(txId);
        WithdrawalTransfer withdrawalTransfer = new WithdrawalTransfer(
            amount,
            gasPrice.longValueExact(),
            gasLimit,
            address,
            wallet,
            transaction,
            currency
        );
        withdrawalTransferRepository.save(withdrawalTransfer);

        TransferCheckMessage transferCheckMessage = new TransferCheckMessage();
        transferCheckMessage.setTransferId(withdrawalTransfer.getId());
        transferCheckMessage.setTransactionId(transaction.getId());
        transferCheckMessage.setTransactionHash(transaction.getHash());
        messageService.queueDelay("ethereum-wallet.check.withdrawal", transferCheckMessage, 30);

        return withdrawalTransfer;
    }

    @JmsListener(
        destination = "ethereum-wallet.check.withdrawal",
        containerFactory = "queueListenerFactory"
    )
    @Transactional
    public void checkWithdrawalStatus(TransferCheckMessage transferCheckMessage) {
        Long transferId = transferCheckMessage.getTransferId();
        Long transactionId = transferCheckMessage.getTransactionId();
        String hash = transferCheckMessage.getTransactionHash();

        TransactionReceipt transactionReceipt = ethereumNodeService.getTransactionReceipt(hash);
        BigInteger latestBlockNumber = ethereumNodeService.getLatestBlockNumber();

        if (transactionReceipt == null) {
            messageService.queueDelay("ethereum-wallet.check.withdrawal", transferCheckMessage, 30);
            log.warn("Withdrawal check unavailable, id : {}, hash : {}", transferId, hash);
            return;
        }

        long confirmations = latestBlockNumber.subtract(transactionReceipt.getBlockNumber()).longValueExact();
        if (confirmations < Constants.MINIMUM_CONFIRMATIONS) {
            Transaction transaction = transactionService.findByIdForUpdate(transactionId);
            transactionService.updateTransaction(transaction, transactionReceipt, latestBlockNumber);
            messageService.queueDelay("ethereum-wallet.check.withdrawal", transferCheckMessage, 30);
            log.info("Withdrawal not confirmed, id : {}, hash : {}", transferId, hash);
            return;
        }

        Optional<WithdrawalTransfer> withdrawalTransferOptional = withdrawalTransferRepository.findByIdWithTransactionAndWalletForUpdate(transferId);
        if (withdrawalTransferOptional.isEmpty()) {
            log.error("Withdrawal Not Found, id : {}", transferId);
            return;
        }
        WithdrawalTransfer withdrawalTransfer = withdrawalTransferOptional.get();

        if (withdrawalTransfer.getStatus().equals(Status.confirmed)) {
            log.info("Withdrawal already confirmed, id : {}, hash : {}", transferId, hash);
            return;
        }

        Transaction transaction = withdrawalTransfer.getTransaction();

        if (!transactionReceipt.isStatusOK()) {
            transactionService.updateTransaction(transaction, transactionReceipt, latestBlockNumber);
            failWithdrawalTransfer(withdrawalTransfer);
            log.info("Withdrawal failed, id : {}, hash : {}", transferId, hash);
            return;
        }

        transactionService.updateTransaction(transaction, transactionReceipt, latestBlockNumber);
        confirmWithdrawalTransfer(withdrawalTransfer);
        log.info("Withdrawal confirmed, id : {}, hash : {}", transferId, hash);
    }

    @Transactional
    public void confirmWithdrawalTransfer(WithdrawalTransfer withdrawalTransfer) {
        withdrawalTransfer.setStatus(Status.confirmed);
        BigInteger amount = withdrawalTransfer.getAmount().add(BigInteger.valueOf(withdrawalTransfer.getGasPrice()).multiply(BigInteger.valueOf(withdrawalTransfer.getGasLimit())));
        Wallet wallet = withdrawalTransfer.getWallet();
        wallet.setConfirmedBalance(wallet.getConfirmedBalance().subtract(amount));
        wallet.setBalance(wallet.getBalance().subtract(amount));
    }

    @Transactional
    public void failWithdrawalTransfer(WithdrawalTransfer withdrawalTransfer) {
        withdrawalTransfer.setStatus(Status.failed);
        BigInteger amount = withdrawalTransfer.getAmount();
        BigInteger fee = BigInteger.valueOf(withdrawalTransfer.getGasPrice()).multiply(BigInteger.valueOf(withdrawalTransfer.getGasLimit()));
        Wallet wallet = withdrawalTransfer.getWallet();
        wallet.setSpendableBalance(wallet.getSpendableBalance().add(amount));
        wallet.setConfirmedBalance(wallet.getConfirmedBalance().subtract(fee));
        wallet.setBalance(wallet.getBalance().subtract(fee));
    }

    @PostConstruct
    void handlePendingWithdrawalTransfers() {
        List<WithdrawalTransfer> pendingWithdrawals = withdrawalTransferRepository.findAllByStatusEquals(Status.pending);

        for (WithdrawalTransfer withdrawalTransfer : pendingWithdrawals) {
            Transaction transaction = withdrawalTransfer.getTransaction();
            log.info("Rescheduling withdrawalTransfer : {}", withdrawalTransfer);
            TransferCheckMessage transferCheckMessage = new TransferCheckMessage();
            transferCheckMessage.setTransferId(withdrawalTransfer.getId());
            transferCheckMessage.setTransactionId(transaction.getId());
            transferCheckMessage.setTransactionHash(transaction.getHash());
            messageService.queue("ethereum-wallet.check.withdrawal", transferCheckMessage);
        }
    }
}
