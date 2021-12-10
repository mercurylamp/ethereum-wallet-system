package com.example.ethereumwalletsystem.payload;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class DepositTransferMessage {
    private long lastBlockNumber;
    private long latestBlockNumber;
    private List<Transfer> transfers;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Transfer {
        private String address;
        private BigInteger amount;
        private String hash;
        private String blockHash;
        private BigInteger blockNumber;
    }
}
