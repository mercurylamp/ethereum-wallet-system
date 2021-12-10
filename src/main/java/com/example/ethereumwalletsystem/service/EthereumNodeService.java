package com.example.ethereumwalletsystem.service;

import com.example.ethereumwalletsystem.exception.Web3jException;
import com.example.ethereumwalletsystem.payload.DepositTransferMessage;
import com.example.ethereumwalletsystem.util.SecretSharingUtils;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.Response;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Service
public class EthereumNodeService {
    private final String nodeUrl = "https://ropsten.infura.io/v3/9067328bc4224b11b875f8351fba84fc";
    private Web3j client;

    public Web3j getClient() {
        if (client == null) {
            client = Web3j.build(new HttpService(nodeUrl));
        }
        return client;
    }

    private <T extends Response<?>> T execute(Request<?, T> request) throws Web3jException {
        try {
            T response = request.send();
            if (response.hasError()) {
                throw new Web3jException(response.getError().getMessage());
            }
            return response;
        } catch (IOException exception) {
            // TODO switch node
            return null;
        }
    }

    BigInteger getGasPrice() {
        EthGasPrice ethGasPriceResponse = execute(getClient().ethGasPrice());
        return ethGasPriceResponse == null ? null : ethGasPriceResponse.getGasPrice().multiply(BigInteger.TWO);
    }

    String sendEth(
        String mnemonic, int path, String address, BigInteger amount, BigInteger gasPrice, Long gasLimit, long nonce
    ) {
        Credentials credentials = SecretSharingUtils.getCredentialsFromMnemonic(mnemonic, path);

        RawTransaction rawTransaction = RawTransaction.createEtherTransaction(
            BigInteger.valueOf(nonce),
            gasPrice,
            BigInteger.valueOf(gasLimit),
            address,
            amount
        );

        byte[] signedRawTransaction = TransactionEncoder.signMessage(rawTransaction, credentials);
        String signedRawTransactionHash = Numeric.toHexString(signedRawTransaction);
        EthSendTransaction ethSendTransaction = execute(getClient().ethSendRawTransaction(signedRawTransactionHash));
        return ethSendTransaction == null ? null : ethSendTransaction.getTransactionHash();
    }

    TransactionReceipt getTransactionReceipt(String hash) {
        EthGetTransactionReceipt ethGetTransactionReceipt = execute(getClient().ethGetTransactionReceipt(hash));
        return ethGetTransactionReceipt == null ? null : ethGetTransactionReceipt.getTransactionReceipt().orElse(null);
    }

    Transaction getTransactionByHash(String hash) {
        EthTransaction ethTransaction = execute(getClient().ethGetTransactionByHash(hash));
        return ethTransaction == null ? null : ethTransaction.getTransaction().orElse(null);
    }

    BigInteger getLatestBlockNumber() {
        EthBlockNumber ethBlockNumber = execute(getClient().ethBlockNumber());
        return ethBlockNumber == null ? null : ethBlockNumber.getBlockNumber();
    }

    private EthBlock getBlockByNumber(long i) {
        return execute(getClient().ethGetBlockByNumber(DefaultBlockParameter.valueOf(BigInteger.valueOf(i)), false));
    }

    DepositTransferMessage getDepositTransfers(long lastBlockNumber, long blocks) {
        DepositTransferMessage depositTransferMessage = new DepositTransferMessage();
        List<DepositTransferMessage.Transfer> transfers = new ArrayList<>();

        long latestBlockNumber = getLatestBlockNumber().longValueExact();

        if (lastBlockNumber <= 0)
            lastBlockNumber = latestBlockNumber - blocks;
        long destination = Math.min(latestBlockNumber, lastBlockNumber + blocks);

        for (long i = lastBlockNumber; i < destination; i++) {
            EthBlock ethBlock = getBlockByNumber(i);

            if (ethBlock == null) {
                continue;
            }

            List<EthBlock.TransactionResult> transactionResults = ethBlock.getBlock().getTransactions();

            for (EthBlock.TransactionResult transactionResult : transactionResults) {
                Transaction transactionByHash = getTransactionByHash(transactionResult.get().toString());
                if (transactionByHash == null)
                    continue;

                String toAddress = transactionByHash.getTo();
                if (toAddress == null)
                    continue;

                String transactionHash = transactionByHash.getHash();
                String blockHash = transactionByHash.getBlockHash();
                BigInteger blockNumber = transactionByHash.getBlockNumber();
                BigInteger value = transactionByHash.getValue();

                DepositTransferMessage.Transfer transfer = new DepositTransferMessage.Transfer();
                transfer.setAddress(toAddress);
                transfer.setAmount(value);
                transfer.setHash(transactionHash);
                transfer.setBlockHash(blockHash);
                transfer.setBlockNumber(blockNumber);
                transfers.add(transfer);
            }
        }

        depositTransferMessage.setTransfers(transfers);
        depositTransferMessage.setLastBlockNumber(destination);
        depositTransferMessage.setLatestBlockNumber(latestBlockNumber);
        return depositTransferMessage;
    }
}
