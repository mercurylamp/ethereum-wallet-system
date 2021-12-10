package com.example.ethereumwalletsystem.payload;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigInteger;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class WithdrawalResponse {
    private final UUID id;
    private final BigInteger amount;
    private final String address;
    private final long gasPrice;
    private final long gasLimit;
    private final String transactionHash;
}
